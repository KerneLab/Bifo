package org.kernelab.bifo.go;

import java.util.Collection;
import java.util.HashSet;

import org.kernelab.basis.io.DataReader;
import org.kernelab.bifo.util.Progressive;

public class Gene2GOReader extends DataReader
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
	protected void readFinished()
	{
		Read = true;

		if (progress != null) {
			progress.resetProgress(0);
		}
	}

	@Override
	protected void readLine(CharSequence line)
	{
		if (line.charAt(0) != '#') {

			Gene2GOFullItem item = new Gene2GOFullItem(line.toString());

			Collection<Gene2GOFullItem> items = GeneOntology.Gene2GOFullItems.get(item
					.getGoId());

			if (items == null) {
				items = new HashSet<Gene2GOFullItem>();
				GeneOntology.Gene2GOFullItems.put(item.getGoId(), items);
			}

			items.add(item);
		}
	}

	@Override
	protected void readPrepare()
	{
		if (progress != null) {
			progress.prepareProgress();
		}

		Read = false;
		GeneOntology.Gene2GOFullItems.clear();
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

}
