package org.kernelab.bifo.uniprot.liver;

import java.util.regex.Pattern;

import org.kernelab.bifo.fasta.FastA;
import org.kernelab.bifo.fasta.FastAReader;
import org.kernelab.bifo.util.Progressive;

public class LiverReader extends FastAReader
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
	protected LiverSwissProtItem newItem(String description)
	{
		LiverSwissProtItem item = new LiverSwissProtItem(description);
		Liver.SwissProtItems.put(item.getId(), item);
		return item;
	}

	@Override
	protected void readFinished()
	{
		super.readFinished();

		LiverReader.Read = true;

		if (progress != null) {
			progress.resetProgress(0);
		}
	}

	@Override
	protected void readLine(CharSequence line)
	{
		if (Pattern.matches(FastA.FASTA_ITEM_BEGIN_MARK, line)) {

			if (item != null) {
				item.setSequenceData(buffer.toString());
			}

			if (Pattern.matches(LiverSwissProtItem.LiverSwissProtItemRegex, line)) {
				item = newItem(line.toString());
				clearBuffer();
			} else {
				item = null;
			}

		} else {
			if (item != null) {
				bufferLine(line.toString());
			}
		}
	}

	@Override
	protected void readPrepare()
	{
		if (progress != null) {
			progress.prepareProgress();
		}

		LiverReader.Read = false;
		Liver.SwissProtItems.clear();

		super.readPrepare();
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}
}
