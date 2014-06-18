package org.kernelab.bifo.uniprot.liver;

import java.io.File;
import java.io.IOException;
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
import org.kernelab.bifo.interact.Interact;
import org.kernelab.bifo.interact.InteractReader;
import org.kernelab.bifo.util.Progressive;

public class LiverInteractFinder extends AbstractAccomplishable<LiverInteractFinder> implements Runnable
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		LiverReader r = new LiverReader();
		r.setDataFile(new File("./dat/HLPP_French_Liver.FASTA"));
		r.read();
		LiverInteractFinder f = new LiverInteractFinder();
		f.find();
		Tools.debug(f.result);
	}

	private String							detail;

	private int								upperRank;

	private int								lowerRank;

	private Map<Integer, List<String>>		result;

	private Set<Relation<String, String>>	relation;

	private Progressive						progress;

	public LiverInteractFinder()
	{
		this.upperRank = 1;

		this.lowerRank = 10;

		this.result = new TreeMap<Integer, List<String>>();

		this.relation = new LinkedHashSet<Relation<String, String>>();
	}

	public Map<Integer, List<String>> find()
	{
		result.clear();

		find(lowerRank);

		List<Integer> remove = Tools.listOfCollection(new ArrayList<Integer>(),
				result.keySet());

		int filter = upperRank - 1;

		for (int i = Math.max(0, remove.size() - filter); i < remove.size(); i++) {
			result.remove(remove.get(i));
		}

		Collection<String> cores = new LinkedList<String>();
		for (Collection<String> id : result.values()) {
			cores.addAll(id);
		}

		Liver.LoadInteractRelation(cores, relation);

		return result;
	}

	private Map<Integer, List<String>> find(int size)
	{
		SQLKit kit = null;
		if (!InteractReader.Read) {
			kit = Interact.DATABASE.getSQLKit();
		}

		int inf = -1;

		if (progress != null) {
			progress.resetProgress(Liver.SwissProtItems.size());
		}

		try {
			for (String receptor : Liver.SwissProtItems.keySet()) {

				int num = 0;

				for (String ligand : Interact.QueryInteractLigands(receptor, kit)) {
					if (Liver.SwissProtItems.containsKey(ligand)) {
						num++;
					}
				}

				if (num >= inf) {

					List<String> list = result.get(num);

					if (list == null) {

						list = new LinkedList<String>();
						result.put(num, list);

						if (result.size() > size) {
							// 剔除最小的
							if (inf == -1) {
								for (Integer i : result.keySet()) {
									inf = i;
									break;
								}
							}

							result.remove(inf);

							for (Integer i : result.keySet()) {
								inf = i;
								break;
							}
						}
					}

					list.add(receptor);
				}

				if (progress != null) {
					progress.nextProgress();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (kit != null) {
				kit.close();
			}
		}

		return result;
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

	public void setUpperRank(int upperRank)
	{
		this.upperRank = upperRank;
	}

	@Override
	protected LiverInteractFinder getAccomplishableSubject()
	{
		return this;
	}
}
