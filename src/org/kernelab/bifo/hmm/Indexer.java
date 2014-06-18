package org.kernelab.bifo.hmm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kernelab.basis.Tools;
import org.kernelab.basis.Variable;

public class Indexer<E>
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Indexer<String> i = new Indexer<String>();
		i.addIndexer("Zero");
		i.addIndexer("One");
		i.addIndexer("Two");
		i.addIndexer("Three");
		i.addIndexer("Four");
		Tools.debug(i.getMap());
		Tools.debug("");

		i.removeIndex(2);
		Tools.debug(i.getMap());
	}

	private Map<E, Variable<Integer>>	map;

	private List<E>						list;

	public Indexer()
	{
		this(null, null);
	}

	public Indexer(Map<E, Variable<Integer>> map, List<E> list)
	{
		this.setMap(map);
		this.setList(list);
	}

	public void addIndexer(E indexer)
	{
		if (!map.containsKey(indexer)) {
			map.put(indexer, Variable.newInstance(map.size()));
			list.add(indexer);
		}
	}

	public int getIndex(E indexer)
	{
		Variable<Integer> index = map.get(indexer);
		return index == null ? -1 : index.value;
	}

	public E getIndexer(int index)
	{
		E indexer = null;

		if (index >= 0 && index < list.size()) {
			indexer = list.get(index);
		}

		return indexer;
	}

	protected List<E> getList()
	{
		return list;
	}

	protected Map<E, Variable<Integer>> getMap()
	{
		return map;
	}

	public void removeIndex(int index)
	{
		this.removeIndexer(this.getIndexer(index));
	}

	public void removeIndexer(E indexer)
	{
		if (indexer != null) {
			Variable<Integer> index = map.remove(indexer);
			if (index != null) {
				for (Map.Entry<E, Variable<Integer>> entry : map.entrySet()) {
					if (entry.getValue().value > index.value) {
						entry.getValue().value--;
					}
				}
				list.remove((int) index.value);
			}
		}
	}

	protected void setList(List<E> list)
	{
		if (list == null) {
			list = new ArrayList<E>();
		}
		list.clear();
		this.list = list;
	}

	protected void setMap(Map<E, Variable<Integer>> map)
	{
		if (map == null) {
			map = new LinkedHashMap<E, Variable<Integer>>();
		}
		map.clear();
		this.map = map;
	}

	public int size()
	{
		return list.size();
	}
}
