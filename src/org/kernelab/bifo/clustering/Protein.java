package org.kernelab.bifo.clustering;

import org.kernelab.bifo.align.Alignment;
import org.kernelab.bifo.align.NWAligner;
import org.kernelab.bifo.uniprot.UniProtItem;

public class Protein implements Sample
{

	public static final int			PROTEIN_SEQUENCE_LIMIT	= 250;

	public static final NWAligner	aligner					= new NWAligner(PROTEIN_SEQUENCE_LIMIT);

	public static final Alignment	alignment				= new Alignment(0);

	static
	{
		aligner.setGapOpen(8);
		aligner.setScoringMatrixName("blosum50");
	}

	public static final double Distance(CharSequence a, CharSequence b)
	{
		// return aligner.align(a, b).trace(alignment).getScore();
		return aligner.align(a, b).trace(alignment).getEvaluation();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private String	id;

	private String	sequence;

	public Protein(String id, String sequence)
	{
		this.id = id;
		this.sequence = sequence;
	}

	public Protein(UniProtItem item)
	{
		this.id = item.getId();
		this.sequence = item.getSequenceData();
	}

	@Override
	public double distance(Sample sample)
	{
		Protein protein = null;

		if (sample instanceof Protein)
		{
			protein = (Protein) sample;
		}

		return Distance(this.sequence, protein.sequence);
	}

	@Override
	public boolean equals(Object o)
	{
		boolean is = false;

		if (o instanceof Protein)
		{
			Protein p = (Protein) o;
			if (this.id.equals(p.id))
			{
				is = true;
			}
		}

		return is;
	}

	public String getId()
	{
		return id;
	}

	public String getSequence()
	{
		return sequence;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	protected void setId(String id)
	{
		this.id = id;
	}

	protected void setSequence(String sequence)
	{
		this.sequence = sequence;
	}

}
