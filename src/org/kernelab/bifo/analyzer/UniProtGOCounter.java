package org.kernelab.bifo.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kernelab.basis.Tools;
import org.kernelab.basis.Variable;
import org.kernelab.basis.io.DataReader;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.go.GeneOntology;

public class UniProtGOCounter
{

	public static void CountUniProtGO()
	{
		Map<Integer, Variable<Integer>> count = new HashMap<Integer, Variable<Integer>>();

		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		ResultSet rs = null;

		String sql = "SELECT COUNT(DISTINCT `go`) AS `c` FROM `uniprotgo` GROUP BY `uniprot`";

		try
		{
			rs = kit.query(sql);

			while (rs.next())
			{
				int countGO = rs.getInt("c");
				Variable<Integer> countVariable = count.get(countGO);
				if (countVariable == null)
				{
					countVariable = new Variable<Integer>(0);
					count.put(countGO, countVariable);
				}
				countVariable.value++;
			}

			for (Entry<Integer, Variable<Integer>> entry : count.entrySet())
			{
				Tools.debug(entry.getKey() + "\t" + entry.getValue());
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

	public static final void CountUniProtGO(File file, final String split)
	{
		final Map<String, Variable<Double>> result = new HashMap<String, Variable<Double>>();

		final SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		DataReader reader = new DataReader() {

			Set<String>	uniProts	= new HashSet<String>();

			@Override
			protected void readFinished()
			{
				for (String id : uniProts)
				{
					try
					{
						for (String go : GeneOntology.QueryUniProtGOAnnotations(id, kit))
						{
							Variable<Double> count = result.get(go);
							if (count == null)
							{
								count = Variable.newInstance(0.0);
								result.put(go, count);
							}
							count.value++;
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}

				double sum = 0.0;

				for (Variable<Double> count : result.values())
				{
					sum += count.value;
				}

				for (Variable<Double> count : result.values())
				{
					count.value /= sum;
				}
			}

			@Override
			protected void readLine(CharSequence line)
			{
				String[] pairs = Tools.splitCharSequence(line, split);

				for (String id : pairs)
				{
					uniProts.add(id);
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
			reader.read();
			kit.close();
			Tools.debug(result);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		CountUniProtGO(new File("./dat/InteractNetwork/YEAST_4_10.txt"), "\t");
	}

}
