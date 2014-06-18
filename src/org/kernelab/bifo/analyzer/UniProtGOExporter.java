package org.kernelab.bifo.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.kernelab.basis.Tools;
import org.kernelab.basis.io.DataReader;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.go.GeneOntology;
import org.kernelab.bifo.uniprot.UniProt;
import org.kernelab.bifo.uniprot.UniProtItem;

/**
 * 根据特定需求导出UniprotGo的信息
 * 
 * @author Dilly King
 * 
 */
public class UniProtGOExporter
{

	public static final Map<String, String>	Escaper	= new HashMap<String, String>();

	static
	{
		Escaper.put(">", "+");
		Escaper.put("<", "-");
		Escaper.put(">=", "+=");
		Escaper.put("<=", "-=");
		Escaper.put("!=", "~=");
		Escaper.put("<>", "~=");
	}

	/**
	 * 将特定的符号转义成可写入文件名的符号。
	 * 
	 * @param relation
	 *            关系符号
	 * @return 可写入文件名的关系符号
	 */
	public static final String EscapeRelation(String relation)
	{
		String escape = Escaper.get(relation);
		if (escape == null)
		{
			escape = "=";
		}
		return escape;
	}

	/**
	 * 根据蛋白质上所含GO的数量导出蛋白质信息。
	 * 
	 * @param number
	 *            蛋白质上GO的数量
	 */
	public static final void ExportUniProtByGONumber(int number)
	{
		ExportUniProtByGONumber(number, "=");
	}

	/**
	 * 根据蛋白质上所含GO的数量导出蛋白质信息。
	 * 
	 * @param number
	 *            蛋白质上GO的数量
	 * @param relation
	 *            GO的数量关系，可取"="、">"、">="、"<"、"<="、"!="，表示GO的数量等于、大于、大于等于、小于、小于等于
	 *            、 不等于
	 */
	public static final void ExportUniProtByGONumber(int number, String relation)
	{
		String dir = "./dat/go" + EscapeRelation(relation) + number + "/";

		File directory = new File(dir);

		if (directory.exists())
		{
			Tools.clearDirectory(directory);
		}
		else
		{
			directory.mkdir();
		}

		Collection<String> index = new LinkedList<String>();

		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		String sql = "SELECT `u`.`uniprot`,`u`.`go` FROM `" + GeneOntology.UNIPROT_GO_TABLE_NAME
				+ "` AS `u`, (SELECT `uniprot`,`go` FROM `" + GeneOntology.UNIPROT_GO_TABLE_NAME
				+ "` GROUP BY `uniprot` HAVING COUNT(DISTINCT `go`)" + relation
				+ "?) AS `g` WHERE `u`.`uniprot`=`g`.`uniprot` ORDER BY `u`.`go`";

		try
		{
			ResultSet rs = kit.query(sql, number);

			String go = "";

			Collection<UniProtItem> items = new HashSet<UniProtItem>();

			while (rs.next())
			{

				String g = rs.getString("go");

				if (!go.equals(g))
				{
					if (!go.equals(""))
					{
						Collection<String> lines = new LinkedList<String>();
						for (UniProtItem item : items)
						{
							lines.add(item.getId() + "/" + go);
							lines.add(item.getSequenceData());
						}
						if (lines.size() > 0)
						{
							String fileName = "GO-" + go + ".txt";
							index.add(fileName);
							File out = new File(dir + fileName);
							Tools.outputStringsToFile(out, lines);
						}
					}
					go = g;
					items.clear();
				}

				UniProtItem item = UniProt.QueryUniProtItem(rs.getString("uniprot"), kit);
				if (item != null)
				{
					items.add(item);
				}
			}

			{
				Collection<String> lines = new LinkedList<String>();
				for (UniProtItem item : items)
				{
					lines.add(item.getId() + "/" + go);
					lines.add(item.getSequenceData());
				}
				if (lines.size() > 0)
				{
					String fileName = "GO-" + go + ".txt";
					index.add(fileName);
					File out = new File(dir + fileName);
					Tools.outputStringsToFile(out, lines);
				}
			}

			Tools.outputStringsToFile(new File(dir + "index.txt"), index);

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
		}
	}

	/**
	 * 将含有某个GO的蛋白质导出到文件中
	 * 
	 * @param file
	 */
	public static final void ExportUniProtsByGO(File file)
	{
		final SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		final String dir = "./dat/go/";

		final int goLimit = 100;

		final int uniprotLimit = 50;

		DataReader reader = new DataReader() {

			int					goes		= 0;

			Collection<String>	fileNames	= new LinkedList<String>();

			@Override
			protected void readFinished()
			{
				Tools.outputStringsToFile(new File(dir + "index.txt"), fileNames);
			}

			@Override
			protected void readLine(CharSequence line)
			{
				try
				{

					Collection<String> uniprotIds = GeneOntology.QueryUniProtsByGO(line.toString(), kit);

					if (uniprotIds.size() > 0)
					{

						LinkedList<String> lines = new LinkedList<String>();

						int number = 0;
						for (String uniprot : uniprotIds)
						{

							UniProtItem item = UniProt.QueryUniProtItem(uniprot, kit);

							if (item != null)
							{

								lines.add(uniprot + '/' + line);

								lines.add(item.getSequenceData());

								number++;
								if (number == uniprotLimit)
								{
									break;
								}
							}
						}

						if (number > 0)
						{

							String fileName = "GO-" + line + ".txt";
							fileNames.add(fileName);
							File out = new File(dir + fileName);
							Tools.outputStringsToFile(out, lines);

							goes++;
							if (goes == goLimit)
							{
								this.setReading(false);
							}
						}
					}

				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

			@Override
			protected void readPrepare()
			{

			}
		};

		try
		{
			reader.setDataFile(file);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		reader.read();

		kit.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// ExportUniProtsByGO(new File("./dat/Go.txt"));
		ExportUniProtByGONumber(1, "=");
	}
}
