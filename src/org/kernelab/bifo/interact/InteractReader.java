package org.kernelab.bifo.interact;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kernelab.basis.Tools;
import org.kernelab.basis.io.DataReader;
import org.kernelab.bifo.util.Progressive;

/**
 * Parse the interact data file likes:
 * 
 * <pre>
 * uniprotkb:Q9ULJ6|intact:EBI-308362	uniprotkb:Q81TB0|intact:EBI-2810501	uniprotkb:PIAS-like ...
 * </pre>
 * 
 * @author Dilly King
 * 
 */
public class InteractReader extends DataReader
{
	public static boolean	Read				= false;

	/**
	 * To match the lines likes:<br />
	 * 
	 * <pre>
	 * uniprotkb:Q9ULJ6|intact:EBI-308362	uniprotkb:Q81TB0|intact:EBI-2810501	uniprotkb:PIAS-like ...
	 * </pre>
	 */
	public static String	IntAct_Line_Regex	= "^uniprotkb:([A-Z0-9]{6})\\|intact:EBI-\\d+?"
														+ "\\t"
														+ "uniprotkb:([A-Z0-9]{6})\\|intact:EBI-\\d+?[\\d\\D]*$";

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Matcher m = Pattern.compile(IntAct_Line_Regex).matcher("");
		m.reset("uniprotkb:Q9ULJ6|intact:EBI-308362	uniprotkb:Q81TB0|intact:EBI-2810501	uniprotkb:PIAS-like ...");
		Tools.debug(m.matches());
	}

	private Matcher		matcher;

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
		// String[] pair = line.split("\\s");
		//
		// String receptor = pair[0].substring(0, UniProtItem.ID_LENGTH);
		// String ligand = pair[1].substring(0, UniProtItem.ID_LENGTH);

		matcher.reset(line);

		if (matcher.matches()) {

			String receptor = matcher.group(1);
			String ligand = matcher.group(2);

			Set<String> ligands = Interact.Map.get(receptor);
			if (ligands == null) {
				ligands = new HashSet<String>();
				Interact.Map.put(receptor, ligands);
			}
			ligands.add(ligand);

			// 对称
			ligands = Interact.Map.get(ligand);
			if (ligands == null) {
				ligands = new HashSet<String>();
				Interact.Map.put(ligand, ligands);
			}
			ligands.add(receptor);
		}

	}

	@Override
	protected void readPrepare()
	{
		if (progress != null) {
			progress.prepareProgress();
		}

		matcher = Pattern.compile(IntAct_Line_Regex).matcher("");

		Read = false;
		Interact.Map.clear();
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

}
