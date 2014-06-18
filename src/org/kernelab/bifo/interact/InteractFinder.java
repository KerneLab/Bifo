package org.kernelab.bifo.interact;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.kernelab.basis.AbstractAccomplishable;
import org.kernelab.basis.Relation;
import org.kernelab.basis.Tools;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.uniprot.UniProt;
import org.kernelab.bifo.uniprot.UniProtItem;
import org.kernelab.bifo.uniprot.UniProtReader;
import org.kernelab.bifo.util.Progressive;

public class InteractFinder extends AbstractAccomplishable<InteractFinder> implements Runnable
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		InteractFinder f = new InteractFinder();
		f.setSpecies("MOUSE");
		f.find();
		Tools.debug(f.result);
	}

	private String							species;

	private String							detail;

	private int								upperRank;

	private int								lowerRank;

	private Map<Integer, List<String>>		result;

	private Set<Relation<String, String>>	relation;

	private Progressive						progress;

	public InteractFinder()
	{
		this.upperRank = 1;

		this.lowerRank = 10;

		this.result = new TreeMap<Integer, List<String>>();

		this.relation = new LinkedHashSet<Relation<String, String>>();
	}

	public Map<Integer, List<String>> find()
	{
		result.clear();

		find(lowerRank, this.find(species));

		List<Integer> remove = Tools.listOfCollection(new ArrayList<Integer>(), result.keySet());

		int filter = upperRank - 1;

		for (int i = Math.max(0, remove.size() - filter); i < remove.size(); i++)
		{
			result.remove(remove.get(i));
		}

		Collection<String> cores = new LinkedList<String>();
		for (Collection<String> id : result.values())
		{
			cores.addAll(id);
		}

		Interact.LoadInteractRelation(cores, relation);

		return result;
	}

	private Map<Integer, List<String>> find(int size, LinkedList<String> receptors)
	{
		SQLKit kit = null;
		if (!InteractReader.Read)
		{
			kit = Interact.DATABASE.getSQLKit();
		}

		int inf = -1;

		if (progress != null)
		{
			progress.resetProgress(receptors.size());
		}
		try
		{

			while (!receptors.isEmpty())
			{

				String receptor = receptors.poll();

				int num = 0;
				if (kit == null)
				{
					num = Interact.QueryInteractLigands(receptor).size();
				}
				else
				{
					num = Interact.QueryInteractLigands(receptor, kit).size();
				}

				if (num >= inf)
				{

					List<String> list = result.get(num);

					if (list == null)
					{

						list = new LinkedList<String>();
						result.put(num, list);

						if (result.size() > size)
						{
							// 剔除最小的
							if (inf == -1)
							{
								for (Integer i : result.keySet())
								{
									inf = i;
									break;
								}
							}

							result.remove(inf);

							for (Integer i : result.keySet())
							{
								inf = i;
								break;
							}
						}
					}

					list.add(receptor);
				}

				if (progress != null)
				{
					progress.nextProgress();
				}
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (kit != null)
			{
				kit.close();
			}
		}

		return result;
	}

	private LinkedList<String> find(String species)
	{
		if (progress != null)
		{
			progress.prepareProgress();
		}

		LinkedList<String> receptors = new LinkedList<String>();

		if (UniProtReader.Read)
		{

			for (UniProtItem item : UniProt.Items.values())
			{

				if (item.getSpecies().equals(species))
				{

					if (detail != null && !detail.equals(""))
					{
						if (item.getDetail().contains(detail))
						{
							receptors.add(item.getId());
						}
					}
					else
					{
						receptors.add(item.getId());
					}
				}
			}

		}
		else
		{

			SQLKit kit = Interact.DATABASE.getSQLKit();
			ResultSet rs = null;

			try
			{
				String sql = "SELECT `id` FROM `" + UniProt.TABLE_NAME + "` WHERE `species`=?";

				if (detail != null && !detail.equals(""))
				{
					sql += " AND `detail` LIKE ?";
					rs = kit.query(sql, species, "%" + detail + "%");
				}
				else
				{
					rs = kit.query(sql, species);
				}

				while (rs.next())
				{
					receptors.add(rs.getString("id"));
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

		return receptors;
	}

	@Override
	protected InteractFinder getAccomplishableSubject()
	{
		return this;
	}

	public String getDetail()
	{
		return detail;
	}

	public int getLowerRank()
	{
		return lowerRank;
	}

	public Set<Relation<String, String>> getRelation()
	{
		return relation;
	}

	public Map<Integer, List<String>> getResult()
	{
		return result;
	}

	public String getSpecies()
	{
		return species;
	}

	public int getUpperRank()
	{
		return upperRank;
	}

	@Override
	public void run()
	{
		this.find();

		this.accomplished();
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	public void setLowerRank(int size)
	{
		this.lowerRank = size;
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

	public void setSpecies(String species)
	{
		this.species = species;
	}

	public void setUpperRank(int upperRank)
	{
		this.upperRank = upperRank;
	}
}
