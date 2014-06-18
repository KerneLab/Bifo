package org.kernelab.bifo.fasta;

import java.io.Serializable;

import org.kernelab.basis.Copieable;

public class FastAItem implements FastA, Copieable<FastAItem>, Serializable
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7360668019500182345L;

	public static final String	REGEX_ESCAPER		= "\\";

	protected String			description;

	protected String			sequenceData;

	protected FastAItem()
	{

	}

	protected FastAItem(FastAItem item)
	{
		this.description = item.description;
		this.sequenceData = item.sequenceData;
	}

	public FastAItem(String description)
	{
		this.setDescription(description);
	}

	@Override
	public FastAItem clone()
	{
		return new FastAItem(this);
	}

	public StringBuilder formatSequenceData()
	{
		return formatSequenceData(RECOMMENDED_LINE_LENGTH);
	}

	public StringBuilder formatSequenceData(int length)
	{
		StringBuilder sb = new StringBuilder();

		int lines = sequenceData.length() / length;
		if (sequenceData.length() % length != 0) {
			lines++;
		}

		for (int line = 0; line < lines; line++) {
			sb.append(sequenceData.substring(line * length,
					Math.min(sequenceData.length(), (line + 1) * length)));
			sb.append('\n');
		}

		return sb;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getSequenceData()
	{
		return sequenceData;
	}

	public void setDescription(String description)
	{
		this.description = description.replaceFirst(FASTA_ITEM_BEGIN_MARK, "");
	}

	public void setSequenceData(String sequenceData)
	{
		this.sequenceData = sequenceData;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(FASTA_ITEM_BEGIN_MARK);

		sb.append(description);
		sb.append('\n');

		sb.append(formatSequenceData());

		return sb.toString();
	}

}
