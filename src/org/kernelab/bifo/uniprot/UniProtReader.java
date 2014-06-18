package org.kernelab.bifo.uniprot;

import org.kernelab.bifo.fasta.FastAReader;
import org.kernelab.bifo.util.Progressive;

public class UniProtReader extends FastAReader
{

	public static boolean	Read	= false;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Progressive	progress;

	@Override
	protected UniProtItem newItem(String description)
	{
		UniProtItem item = new UniProtItem(description);
		UniProt.Items.put(item.getId(), item);
		return item;
	}

	@Override
	protected void readFinished()
	{
		super.readFinished();

		UniProtReader.Read = true;

		if (progress != null) {
			progress.resetProgress(0);
		}
	}

	@Override
	protected void readPrepare()
	{
		if (progress != null) {
			progress.prepareProgress();
		}

		UniProtReader.Read = false;
		UniProt.Items.clear();

		super.readPrepare();
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

}
