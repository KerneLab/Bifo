package org.kernelab.bifo.fasta;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.kernelab.basis.io.DataReader;

/**
 * The class to read FastA formatted file.<br />
 * 
 * <b>FastA format</b>
 * 
 * <pre>
 * &gt;sp|Q8VW57|RL21_BRUAB 50S ribosomal protein L21 OS=Brucella abortus GN=rplU PE=3 SV=1
 * MFAVIKTGGKQYRVAANDLIKVEKVAGEAGDIVEFAEVLMVGSTIGAPTVAGSLVTAEVV
 * EQGRGRKVIAFKKRRRQNSKRTRGHRQELTTIRISEILTDGAKPSKKAAEKKAPKADAAE
 * GEAAKPKKAAPKKAATKAESAE
 * </pre>
 * 
 * One single line description start with '>' and followed by several lines of
 * sequence data.
 * 
 * @author Dilly King
 * 
 */

public class FastAReader extends DataReader
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	protected StringBuilder			buffer;

	protected FastAItem				item;

	private LinkedList<FastAItem>	items;

	public FastAReader()
	{
		super();
		buffer = new StringBuilder(1024);
		items = new LinkedList<FastAItem>();
	}

	/**
	 * To buffer a line of sequence data. This method will be called when read
	 * each line of the sequence data in a FastAItem between the nearest two
	 * lines started with {@code FastA.FASTA_ITEM_BEGIN_MARK}.
	 * 
	 * @param line
	 *            One line in sequence data.
	 */
	protected void bufferLine(String line)
	{
		buffer.append(line);
	}

	protected void clearBuffer()
	{
		buffer.delete(0, buffer.length());
	}

	protected StringBuilder getBuffer()
	{
		return buffer;
	}

	public LinkedList<FastAItem> getItems()
	{
		return items;
	}

	/**
	 * Make a new FastAItem with the description line. This method will be
	 * called when this reader receive a new line started with
	 * {@code FastA.FASTA_ITEM_BEGIN_MARK}.
	 * 
	 * @param description
	 *            The description of the FastAItem object which started with
	 *            {@code FastA.FASTA_ITEM_BEGIN_MARK}.
	 * @return a FastAItem object. Any sub class of FastAReader may override
	 *         this method to return an object which extends FastAItem for some
	 *         certain usage.
	 */
	protected FastAItem newItem(String description)
	{
		FastAItem item = new FastAItem(description);
		items.add(item);
		return item;
	}

	@Override
	protected void readFinished()
	{
		if (item != null)
		{
			item.setSequenceData(buffer.toString());
		}
	}

	@Override
	protected void readLine(CharSequence line)
	{
		if (Pattern.matches(FastA.FASTA_ITEM_BEGIN_REGEX, line))
		{

			if (item != null)
			{
				item.setSequenceData(buffer.toString());
			}

			item = newItem(line.toString());

			clearBuffer();
		}
		else
		{
			if (item != null)
			{
				bufferLine(line.toString());
			}
		}
	}

	@Override
	protected void readPrepare()
	{
		item = null;
		items.clear();
		System.gc();
	}
}
