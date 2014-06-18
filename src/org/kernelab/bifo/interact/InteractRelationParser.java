package org.kernelab.bifo.interact;

import java.util.LinkedHashSet;
import java.util.Set;

import org.kernelab.basis.Relation;
import org.kernelab.basis.Tools;
import org.kernelab.basis.io.DataReader;

public class InteractRelationParser extends DataReader
{

	public static String	SPLIT	= "\t";

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Set<Relation<String, String>>	relation;

	public Set<Relation<String, String>> getRelation()
	{
		return relation;
	}

	@Override
	protected void readFinished()
	{

	}

	@Override
	protected void readLine(CharSequence line)
	{
		String[] pair = Tools.splitCharSequence(line, SPLIT, 2);
		if (pair.length == 2) {
			relation.add(new Relation<String, String>(pair[0], pair[1]));
		}
	}

	@Override
	protected void readPrepare()
	{
		if (relation == null) {
			relation = new LinkedHashSet<Relation<String, String>>();
		} else {
			relation.clear();
		}
	}

	public void setRelation(Set<Relation<String, String>> relation)
	{
		this.relation = relation;
	}

}
