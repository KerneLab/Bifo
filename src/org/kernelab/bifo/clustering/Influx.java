package org.kernelab.bifo.clustering;

import java.util.HashSet;
import java.util.Set;

import org.kernelab.basis.Tools;

public class Influx extends Protein implements Cluster
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String[] ss = { "ATTGCCATT", "ATGGCCATT", "ATCCAATTTT", "ATCTTCTT", "ATTGCCGATT" };

		Set<Protein> ps = new HashSet<Protein>();
		for (String s : ss) {
			ps.add(new Protein(s, s));
		}

		MultiAlign(ps);
	}

	public static final String MultiAlign(Set<Protein> samples)
	{
		String align = null;

		for (Protein p : samples) {
			if (align == null) {
				align = p.getSequence();
			} else {
				align = Protein.aligner.align(align, p.getSequence())
						.trace(Protein.alignment).getA();
			}
			Tools.debug(Protein.alignment.toString());
		}

		return align;
	}

	private Set<Protein>	samples;

	public Influx()
	{
		this("", "");
	}

	public Influx(String id, String sequence)
	{
		super(id, sequence);
	}

	@Override
	public void cluster()
	{
		this.setSequence(MultiAlign(samples));
	}

}
