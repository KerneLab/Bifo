package org.kernelab.bifo.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFrame;

import org.kernelab.basis.Tools;
import org.kernelab.basis.Variable;
import org.kernelab.basis.io.DataReader;
import org.kernelab.basis.io.DataWriter;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.fasta.FrequencyCounter;
import org.kernelab.bifo.go.Gene2GOItem;
import org.kernelab.bifo.go.GeneOntology;
import org.kernelab.bifo.interact.Interact;
import org.kernelab.bifo.uniprot.UniProt;
import org.kernelab.bifo.util.GridChart;
import org.kernelab.bifo.util.MapReader;
import org.kernelab.numeric.matrix.Matrix;

public class UniProtGOInteractAnalyzer
{

	public static final Map<String, Integer>	Relation	= new HashMap<String, Integer>();

	static
	{
		Relation.put("=", 0);
		Relation.put("==", 0);
		Relation.put(">", 1);
		Relation.put("<", 2);
		Relation.put(">=", 3);
		Relation.put("<=", 4);
		Relation.put("!=", 5);
		Relation.put("<>", 5);
	}

	/**
	 * 分析两个相互作用的GO所属的分类
	 */
	public static final void AnalyzeGOCategoryByInteract()
	{
		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		try
		{
			ResultSet rs = kit.query("SELECT `rgo`,`lgo` FROM `" + GeneOntology.INTERACT_GO_SOLELY_TABLE_NAME + "`");

			while (rs.next())
			{
				String rgo = rs.getString("rgo");
				String lgo = rs.getString("lgo");

				Gene2GOItem rgoItem = GeneOntology.QueryGene2GOItem(rgo, kit);
				Gene2GOItem lgoItem = GeneOntology.QueryGene2GOItem(lgo, kit);

				if (rgoItem != null && lgoItem != null)
				{
					Tools.debug(rgoItem.getCategory() + '\t' + lgoItem.getCategory());
				}
			}

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
	 * 分析与指定蛋白质相互作用的配体上GO的数据信息。
	 * 
	 * @param uniProtId
	 *            给定蛋白质的UniProt ID
	 */
	public static final void AnalyzeGODataOnUniProtInteract(String uniProtId)
	{
		SQLKit kit = UniProt.DATABASE.getSQLKit();

		Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();

		try
		{

			for (String ligand : Interact.QueryInteractLigands(uniProtId, kit))
			{

				for (String goId : GeneOntology.QueryUniProtGOAnnotations(ligand, kit))
				{

					Collection<String> l = map.get(goId);

					if (l == null)
					{
						l = new HashSet<String>();
						map.put(goId, l);
					}

					l.add(ligand);
				}
			}

			for (Entry<String, Collection<String>> entry : map.entrySet())
			{
				Tools.debug(entry.getKey() + "\t" + entry.getValue().size() + "\t" + entry.getValue());
			}

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
	 * 分析两个GO之间的相互作用
	 * 
	 * @param rgo
	 * @param lgo
	 */
	public static final void AnalyzeInteractByGO()
	{
		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		DataWriter writer = new DataWriter();

		try
		{

			File file = new File("./dat/interactByGO.txt");
			writer.setDataFile(file);

			long timeStamp = Tools.getTimeStamp();

			// String sql = "SELECT `receptor`,`ligand` FROM `" +
			// Interact.TABLE_NAME
			// + "` WHERE `receptor` IN (SELECT DISTINCT `uniprot` FROM `"
			// + GeneOntology.UNIPROTGO_TABLE_NAME
			// + "` WHERE `go`=? AND `uniprot` IN ("
			// + Interact.ALL_INTACT_UNIPROT_SQL
			// + ")) AND `ligand` IN (SELECT DISTINCT `uniprot` FROM `"
			// + GeneOntology.UNIPROTGO_TABLE_NAME
			// + "` WHERE `go`=? AND `uniprot` IN ("
			// + Interact.ALL_INTACT_UNIPROT_SQL + "))";
			// ResultSet rs = kit.query(sql, rgo, lgo);

			// String sql =
			// "SELECT DISTINCT `receptor`,`ligand`,`rgo`,`lgo` FROM `"
			// + Interact.TABLE_NAME
			// +
			// "`,`"+GeneOntology.INTERACT_GO_SOLELY_TABLE_NAME+"` WHERE `receptor` IN (SELECT DISTINCT `uniprot` FROM `"
			// + GeneOntology.UNIPROTGO_TABLE_NAME
			// + "` WHERE `go`=`rgo` AND `uniprot` IN ("
			// + Interact.ALL_INTACT_UNIPROT_SQL
			// + ")) AND `ligand` IN (SELECT DISTINCT `uniprot` FROM `"
			// + GeneOntology.UNIPROTGO_TABLE_NAME
			// + "` WHERE `go`=`lgo` AND `uniprot` IN ("
			// + Interact.ALL_INTACT_UNIPROT_SQL + "))";

			String sql = "SELECT DISTINCT `receptor`,`ligand` FROM `" + Interact.TABLE_NAME + "`,`"
					+ GeneOntology.INTERACT_GO_SOLELY_TABLE_NAME
					+ "` WHERE `receptor` IN (SELECT DISTINCT `uniprot` FROM `" + GeneOntology.UNIPROT_GO_TABLE_NAME
					+ "` WHERE `go`=`rgo` AND `uniprot` IN (" + Interact.ALL_INTACT_UNIPROT_SQL
					+ ")) AND `ligand` IN (SELECT DISTINCT `uniprot` FROM `" + GeneOntology.UNIPROT_GO_TABLE_NAME
					+ "` WHERE `go`=`lgo` AND `uniprot` IN (" + Interact.ALL_INTACT_UNIPROT_SQL + "))";

			ResultSet rs = kit.query(sql);

			timeStamp = Tools.getTimeStamp() - timeStamp;

			writer.write("Query Time: " + (timeStamp / 1000 / 60.0) + " min");

			while (rs.next())
			{
				// writer
				// .write(rs.getString("receptor") + "(" + rs.getString("rgo") +
				// ")"
				// + "\t" + rs.getString("ligand") + "("
				// + rs.getString("lgo") + ")");
				writer.write(rs.getString("receptor") + "\t" + rs.getString("ligand"));
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
			writer.close();
		}
	}

	public static final void AnalyzeInteractCombination()
	{
		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		DataWriter writer = new DataWriter();

		try
		{

			File file = new File("./dat/interactCombinationByGO.txt");
			writer.setDataFile(file);

			long timeStamp = Tools.getTimeStamp();

			// String sql =
			// "SELECT `r`.`uniprot`, `r`.`go`, `l`.`uniprot`, `l`.`go` FROM `"
			// + GeneOntology.UNIPROTGO_TABLE_NAME + "` AS `r`, `"
			// + GeneOntology.UNIPROTGO_TABLE_NAME
			// +
			// "` AS `l`, `"+GeneOntology.INTERACT_GO_SOLELY_TABLE_NAME+"` WHERE `r`.`go`=`rgo` AND `l`.`go`=`lgo`";

			String sql = "SELECT DISTINCT `uniprot` FROM `" + GeneOntology.UNIPROT_GO_TABLE_NAME
					+ "` WHERE `uniprot` IN (" + Interact.ALL_INTACT_UNIPROT_SQL
					+ ") AND `go` IN (SELECT DISTINCT `rgo` FROM `" + GeneOntology.INTERACT_GO_SOLELY_TABLE_NAME + "`)";

			ResultSet rs = kit.query(sql);

			timeStamp = Tools.getTimeStamp() - timeStamp;

			writer.write("Query Time: " + (timeStamp / 1000 / 60.0) + " min");

			while (rs.next())
			{
				writer.write(rs.getString("uniprot"));
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
			writer.close();
		}

	}

	/**
	 * 分析相互作用度数与GO数量的关系
	 */
	public static final void AnalyzeInteractDegreeWithGONumber()
	{
		Map<Integer, Map<Integer, Variable<Integer>>> result = new TreeMap<Integer, Map<Integer, Variable<Integer>>>();

		SQLKit kit = Interact.DATABASE.getSQLKit();

		Set<Integer> itSet = new TreeSet<Integer>();
		Set<Integer> goSet = new TreeSet<Integer>();

		try
		{

			String sql = "SELECT `receptor`, COUNT(`ligand`) AS `count` FROM `" + Interact.TABLE_NAME
					+ "` GROUP BY `receptor`";

			ResultSet rs = kit.query(sql);

			while (rs.next())
			{
				String receptor = rs.getString(1);
				Integer itCount = rs.getInt(2);

				if (itCount > 0)
				{
					itSet.add(itCount);
				}

				Map<Integer, Variable<Integer>> goes = result.get(itCount);
				if (goes == null)
				{
					goes = new TreeMap<Integer, Variable<Integer>>();
					result.put(itCount, goes);
				}

				Integer goCount = GeneOntology.QueryUniProtGOAnnotations(receptor, kit).size();

				if (goCount > 0)
				{
					goSet.add(goCount);

					Variable<Integer> count = goes.get(goCount);
					if (count == null)
					{
						count = Variable.newInstance(0);
						goes.put(goCount, count);
					}
					count.value++;
				}
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
		}

		List<Integer> itIndex = new ArrayList<Integer>(itSet);
		List<Integer> goIndex = new ArrayList<Integer>(goSet);

		Matrix<Variable<Integer>> data = new Matrix<Variable<Integer>>(goIndex.size(), itIndex.size());

		for (int row = 0; row < goIndex.size(); row++)
		{
			Integer go = goIndex.get(row);
			for (int column = 0; column < itIndex.size(); column++)
			{
				Integer it = itIndex.get(column);
				Variable<Integer> count = result.get(it).get(go);
				if (count != null)
				{
					data.set(count, row, column);
				}
			}
		}

		GridChart<Variable<Integer>> gc = new GridChart<Variable<Integer>>(data);
		gc.setGridSize(5, 5);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(gc.getCanvas());
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * 根据相互作用度数，导出蛋白质信息
	 */
	public static final void AnalyzeUniProtByInteractNumber(int number, String relation)
	{
		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		String sql = "SELECT `receptor`, COUNT(DISTINCT `ligand`) AS `cl` FROM `" + Interact.TABLE_NAME
				+ "` GROUP BY `receptor` HAVING `cl`" + relation + "?";

		try
		{
			ResultSet rs = kit.query(sql, number);

			while (rs.next())
			{
				String uniProt = rs.getString("receptor");
				int ligands = rs.getInt("cl");
				Set<String> gos = GeneOntology.QueryUniProtGOAnnotations(uniProt, kit);
				StringBuilder sb = new StringBuilder(uniProt);
				sb.append('\t');
				sb.append(ligands);
				sb.append('\t');
				sb.append(gos.size());
				sb.append('\t');
				for (String go : gos)
				{
					sb.append(go);
					sb.append('\t');
				}
				Tools.debug(sb.toString());
			}

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
	 * 寻找含有特定数量GO的蛋白质之间的相互作用
	 * 
	 * @param number
	 *            GO的数量
	 * @param relation
	 *            GO的数量关系，可取"="、">"、">="、"<"、"<="、"!="，表示GO的数量等于、大于、大于等于、小于、小于等于
	 *            、 不等于
	 */
	public static final void AnalyzeUniProtInteractByGONumber(int number, String relation)
	{
		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		String sql = "SELECT `uniprot`,`go` FROM `" + GeneOntology.UNIPROT_GO_TABLE_NAME
				+ "` WHERE `uniprot` IN (SELECT `receptor` FROM `" + Interact.TABLE_NAME
				+ "`) GROUP BY `uniprot` HAVING COUNT(DISTINCT `go`)" + relation + "?";

		Set<String> uniProtIds = new HashSet<String>();

		DataWriter writer = new DataWriter();

		try
		{
			ResultSet rs = kit.query(sql, number);

			while (rs.next())
			{
				String uniProt = rs.getString("uniprot");
				uniProtIds.add(uniProt);
			}

			File result = new File("./dat/interact@go" + UniProtGOExporter.EscapeRelation(relation) + number + ".txt");

			writer.setDataFile(result);

			for (String id : uniProtIds)
			{

				StringBuilder sb = new StringBuilder(id);
				Collection<String> receptorGoes = GeneOntology.QueryUniProtGOAnnotations(id, kit);
				if (ConformRelation(receptorGoes.size(), relation, number))
				{

					for (String go : receptorGoes)
					{
						sb.append('\t');
						sb.append("GO:" + go);
					}
					String receptor = sb.toString();

					ResultSet rsi = kit.query("SELECT DISTINCT `ligand` FROM `" + Interact.TABLE_NAME
							+ "` WHERE `receptor`=?", id);

					while (rsi.next())
					{

						String l = rsi.getString("ligand");
						StringBuilder ligand = new StringBuilder(l);

						Collection<String> ligandGoes = GeneOntology.QueryUniProtGOAnnotations(l, kit);

						if (ConformRelation(ligandGoes.size(), relation, number))
						{
							for (String go : ligandGoes)
							{
								ligand.append('\t');
								ligand.append("GO:" + go);
							}
							writer.write(receptor);
							writer.write(ligand);
							writer.write();
						}
					}
				}
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
			writer.close();
		}
	}

	public static final <T> boolean ConformRelation(Comparable<T> a, String relation, T b)
	{
		boolean conform = false;

		switch (Relation.get(relation))
		{
			case 0:
				if (a.compareTo(b) == 0)
				{
					conform = true;
				}
				break;

			case 1:
				if (a.compareTo(b) > 0)
				{
					conform = true;
				}
				break;

			case 2:
				if (a.compareTo(b) < 0)
				{
					conform = true;
				}
				break;

			case 3:
				if (a.compareTo(b) >= 0)
				{
					conform = true;
				}
				break;

			case 4:
				if (a.compareTo(b) <= 0)
				{
					conform = true;
				}
				break;

			case 5:
				if (a.compareTo(b) != 0)
				{
					conform = true;
				}
				break;
		}

		return conform;
	}

	public static void ExportCodeFrequencyOfInteract(int flag, File interactFile)
	{
		Interact.CountCodeFrequencyOfInteract(flag, interactFile, new File(interactFile.getParent() + "/CF_"
				+ interactFile.getName()), FrequencyCounter.DEFAULT_CLASSIFIER);
	}

	public static void ExportCodeFrequencyOfInteract(int flag, File interactFile, String classifierName)
			throws IOException
	{
		Map<Character, Integer> classifier = new HashMap<Character, Integer>();
		MapReader.readCharacterIntegerMap(MapReader.getMapFile(classifierName), classifier);
		Interact.CountCodeFrequencyOfInteract(flag, interactFile, new File(interactFile.getParent() + "/CF_"
				+ interactFile.getName()), classifier);
	}

	/**
	 * 找出同物种相互作用的蛋白质上，具有相同指定分类的GO
	 * 
	 * @param category
	 */
	public static final void ExportGOInteractOfSameSpeciesAndCategory(String category)
	{
		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		String sql = "SELECT DISTINCT `r`.`species`, `rgg`.`go`, `lgg`.`go` FROM `" + GeneOntology.GENE2GO_TABLE_NAME
				+ "` AS `rgg`, `" + GeneOntology.GENE2GO_TABLE_NAME + "` AS `lgg`, `"
				+ GeneOntology.UNIPROT_GO_TABLE_NAME + "` AS `rg`, `" + GeneOntology.UNIPROT_GO_TABLE_NAME
				+ "` AS `lg`, `" + UniProt.TABLE_NAME + "` AS `r`, `" + UniProt.TABLE_NAME + "` AS `l`, `"
				+ Interact.TABLE_NAME + "` AS `i` WHERE `rgg`.`category`=? AND `rgg`.`category`=`lgg`.`category`"
				+ " AND `rgg`.`go`=`rg`.`go` AND `lgg`.`go`=`lg`.`go`"
				+ " AND `rg`.`uniprot`=`r`.`id` AND `lg`.`uniprot`=`l`.`id`"
				+ " AND `i`.`receptor`=`r`.`id` AND `i`.`ligand`=`l`.`id`" + " AND `r`.`species`=`l`.`species`";

		DataWriter writer = new DataWriter();

		try
		{
			writer.setDataFile(new File("./dat/InteractNetwork/SameSpecies" + category + ".txt"));
			writer.write(Tools.getTimeStampString());

			ResultSet rs = kit.query(sql, category);

			writer.write(Tools.getTimeStampString());

			while (rs.next())
			{
				writer.write(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3));
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
			writer.close();
		}

	}

	/**
	 * 在所有相互作用的蛋白质中，导出双方都只含有一个GO情况。
	 */
	public static final void ExportSolelyGOByUniProtInteract()
	{
		int number = 1;

		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		String sql = "SELECT `uniprot`,`go` FROM `" + GeneOntology.UNIPROT_GO_TABLE_NAME
				+ "` WHERE `uniprot` IN (SELECT `receptor` FROM `" + Interact.TABLE_NAME
				+ "`) GROUP BY `uniprot` HAVING COUNT(DISTINCT `go`)=?";

		try
		{
			ResultSet rs = kit.query(sql, number);

			Set<String> uniProtIds = new HashSet<String>();
			while (rs.next())
			{
				String uniProt = rs.getString("uniprot");
				uniProtIds.add(uniProt);
			}

			Map<String, Set<String>> interact = new HashMap<String, Set<String>>();

			for (String receptor : uniProtIds)
			{

				List<String> receptorGOs = new LinkedList<String>(GeneOntology.QueryUniProtGOAnnotations(receptor, kit));

				if (receptorGOs.size() == number)
				{

					String receptorGO = receptorGOs.get(0);

					Set<String> ligandsGOs = interact.get(receptorGO);
					if (ligandsGOs == null)
					{
						ligandsGOs = new HashSet<String>();
						interact.put(receptorGO, ligandsGOs);
					}

					ResultSet rsi = kit.query("SELECT DISTINCT `ligand` FROM `interact` WHERE `receptor`=?", receptor);

					while (rsi.next())
					{

						String ligand = rsi.getString("ligand");

						List<String> ligandGOs = new LinkedList<String>(GeneOntology.QueryUniProtGOAnnotations(ligand,
								kit));

						if (ligandGOs.size() == number)
						{
							ligandsGOs.add(ligandGOs.get(0));
						}
					}

				}
			}

			kit.update("TRUNCATE `" + GeneOntology.INTERACT_GO_SOLELY_TABLE_NAME + "`");
			kit.setAutoCommit(false);
			kit.prepareStatement("INSERT INTO `" + GeneOntology.INTERACT_GO_SOLELY_TABLE_NAME + "` VALUES (?,?,?)");
			for (Entry<String, Set<String>> entry : interact.entrySet())
			{
				String rgo = entry.getKey();
				for (String lgo : entry.getValue())
				{
					kit.addBatch(SQLKit.NULL, rgo, lgo);
				}
			}
			kit.commitBatch();
			kit.setAutoCommit(true);

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
	 * 寻找指定物种，不相互作用的蛋白质对。
	 * <ul>
	 * <li>两者所含分类为Component的GO，至少有一种组合不存在与interactgo中</li>
	 * <li>在相互作用对中不存在的</li>
	 * <li>两者至少有一方在相互作用中存在</li>
	 * </ul>
	 * 
	 * @param species
	 * @return 结果数据文件
	 */
	public static File FindNegativeInteractOfSpecies(String species)
	{
		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		String sql = "SELECT DISTINCT `r`.`id`, `l`.`id` FROM `" + UniProt.TABLE_NAME + "` AS `r`, `"
				+ UniProt.TABLE_NAME + "` AS `l`, `" + GeneOntology.UNIPROT_GO_TABLE_NAME + "` AS `rg`, `"
				+ GeneOntology.UNIPROT_GO_TABLE_NAME
				+ "` AS `lg` WHERE `r`.`species`=? AND `r`.`species`=`l`.`species`"
				+ " AND `r`.`id`=`rg`.`uniprot` AND `l`.`id`=`lg`.`uniprot`" + " AND NOT EXISTS (SELECT * FROM `"
				+ Interact.TABLE_NAME + "` AS `i` WHERE `i`.`receptor`=`r`.`id` AND `i`.`ligand`=`l`.`id`)"
				+ " AND (`r`.`id` IN (SELECT DISTINCT `receptor` FROM `" + Interact.TABLE_NAME
				+ "`) OR `l`.`id` IN (SELECT DISTINCT `receptor` FROM `" + Interact.TABLE_NAME + "`))"
				+ " AND NOT EXISTS (SELECT * FROM `" + GeneOntology.INTERACT_GO_TABLE_NAME
				+ "` AS `ig` WHERE `ig`.`species`=`r`.`species`"
				+ " AND `ig`.`rgo`=`rg`.`go` AND `ig`.`lgo`=`lg`.`go`) LIMIT 1000";

		DataWriter writer = new DataWriter();

		File resultFile = new File("./dat/InteractNetwork/Negative_" + species + ".txt");

		try
		{
			writer.setAutoFlush(false);
			writer.setDataFile(resultFile);

			writer.write(Tools.getTimeStampString());
			ResultSet rs = kit.query(sql, species);
			writer.write(Tools.getTimeStampString());

			while (rs.next())
			{
				writer.write(rs.getString(1) + "\t" + rs.getString(2));
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
			writer.close();
		}

		return resultFile;
	}

	/**
	 * 
	 *            
	 */

	public static final void ImportGOInteractOfSameSpecies(File file)
	{
		final SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		DataReader reader = new DataReader() {

			@Override
			protected void readFinished()
			{
				try
				{
					kit.commitBatch();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

			@Override
			protected void readLine(CharSequence line)
			{
				String[] columns = Tools.splitCharSequence(line, "\t", 3);
				if (columns.length == 3)
				{
					try
					{
						kit.addBatch(SQLKit.NULL, columns[0], columns[1], columns[2]);
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void readPrepare()
			{
				try
				{
					kit.update("TRUNCATE `" + GeneOntology.INTERACT_GO_TABLE_NAME + "`");
					kit.setAutoCommit(false);
					kit.prepareStatement("INSERT INTO `" + GeneOntology.INTERACT_GO_TABLE_NAME + "` VALUES (?,?,?,?)");

				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

		};

		try
		{
			reader.setDataFile(file);
			reader.read();

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// AnalyzeGODataOnUniProtInteract("Q9CQV8");

		// AnalyzeUniProtInteractByGONumber(1, "=");

		// ExportSolelyGOByUniProtInteract();

		// AnalyzeGOCategoryByInteract();

		// AnalyzeInteractByGO();

		// AnalyzeInteractCombination();

		// AnalyzeUniProtByInteractNumber(10, ">=");

		// ExportGOInteractOfSameSpeciesAndCategory("Component");

		// ImportGOInteractOfSameSpecies(new File(
		// "./dat/InteractNetwork/SameSpeciesComponent.txt"));

		// ExportCodeFrequencyOfInteract(1, new
		// File("./dat/InteractNetwork/MOUSE_1_10.txt"));

		// try {
		// ExportCodeFrequencyOfInteract(1, new File(
		// "./dat/InteractNetwork/MOUSE_1_10.txt"), "AminoAcidClassifier6");
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }

		// ExportCodeFrequencyOfInteract(-1,
		// FindNegativeInteractOfSpecies("MOUSE"));

		// try {
		// ExportCodeFrequencyOfInteract(-1, new File(
		// "./dat/InteractNetwork/Negative_MOUSE.txt"), "AminoAcidClassifier6");
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }

		// PredictInteractInPositiveDataByInteractGOSolely(new File(
		// "./dat/InteractNetwork/MOUSE_1_10.txt"));

		// ExportCodeFrequencyOfInteract(true, new File(
		// "./dat/InteractNetwork/P_MOUSE_1_10.txt"));

		AnalyzeInteractDegreeWithGONumber();
	}

	/**
	 * 根据interactgo_solely预测正数据中可能存在的相互作用
	 * 
	 * @param file
	 */
	public static final File PredictInteractInPositiveDataByInteractGOSolely(File file)
	{
		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		final Map<String, Set<String>> interact = new HashMap<String, Set<String>>();

		DataReader reader = new DataReader() {

			@Override
			protected void readFinished()
			{

			}

			@Override
			protected void readLine(CharSequence line)
			{
				String[] pair = Tools.splitCharSequence(line, "\t", 2);

				if (pair.length == 2)
				{

					Set<String> ligands = interact.get(pair[0]);
					if (ligands == null)
					{
						ligands = new HashSet<String>();
						interact.put(pair[0], ligands);
					}
					ligands.add(pair[1]);

					ligands = interact.get(pair[1]);
					if (ligands == null)
					{
						ligands = new HashSet<String>();
						interact.put(pair[1], ligands);
					}
					ligands.add(pair[0]);
				}
			}

			@Override
			protected void readPrepare()
			{

			}
		};

		File resultFile = new File(file.getParent() + "/P_" + file.getName());
		DataWriter writer = new DataWriter();

		try
		{
			reader.setDataFile(file);
			reader.read();

			writer.setAutoFlush(false);
			writer.setDataFile(resultFile);

			String sql = "SELECT COUNT(*) FROM `" + GeneOntology.UNIPROT_GO_TABLE_NAME + "` AS `rgo`, `"
					+ GeneOntology.UNIPROT_GO_TABLE_NAME + "` AS `lgo` WHERE `rgo`.`uniprot`=? AND `lgo`.`uniprot`=?"
					+ " AND EXISTS (SELECT * FROM `" + GeneOntology.INTERACT_GO_SOLELY_TABLE_NAME
					+ "` AS `s` WHERE `s`.`rgo`=`rgo`.`go` AND `s`.`lgo`=`lgo`.`go`)";

			for (String rid : interact.keySet())
			{
				for (String lid : interact.keySet())
				{
					if (!interact.get(rid).contains(lid))
					{
						ResultSet rs = kit.query(sql, rid, lid);
						rs.next();
						int num = rs.getInt(1);
						if (num > 0)
						{
							writer.write(rid + "\t" + lid);
						}
					}
				}
			}

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
			writer.close();
		}

		return resultFile;
	}

}
