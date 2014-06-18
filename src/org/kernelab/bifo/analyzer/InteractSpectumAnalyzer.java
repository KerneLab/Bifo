package org.kernelab.bifo.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kernelab.basis.Tools;
import org.kernelab.basis.io.DataReader;
import org.kernelab.basis.io.DataWriter;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.fasta.SpectumCounter;
import org.kernelab.bifo.uniprot.UniProt;
import org.kernelab.bifo.util.AminoAcid;
import org.kernelab.numeric.Complex;

public class InteractSpectumAnalyzer extends DataReader
{

	/**
	 * @param args
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws SQLException, IOException
	{
		InteractSpectumAnalyzer analyzer = new InteractSpectumAnalyzer();

		int flag = 1;
		String name = flag == 1 ? "positive.txt" : "negative.txt";
		analyzer.setFlag(flag);
		analyzer.setDataFile(new File("./dat/interact/" + name));

		DataWriter writer = new DataWriter();
		writer.setDataFile(new File("./dat/interact/SA_" + name));
		Tools.getOuts().add(new PrintStream(writer.getOutputStream()));
		Tools.debug("Min\tAvrg\tStdv\tA\tB");

		analyzer.read();
		writer.close();
		Tools.resetOuts();
		Tools.debug("Done");
	}

	public static final double		EXPECTATION	= 0.5;

	public static final char		SPLIT		= '\t';

	private Map<Character, Double>	map			= AminoAcid.ChemicoPhysicalMaps.get("EE");

	private SQLKit					kit			= UniProt.DATABASE.getSQLKit();

	private int						flag;

	private int						number;

	private SpectumCounter			counter		= new SpectumCounter(1300);

	private List<Complex>			aResult		= new ArrayList<Complex>();

	private List<Complex>			bResult		= new ArrayList<Complex>();

	// private List<Double> pResult = new LinkedList<Double>();

	public void analyze(String aId, String bId) throws SQLException
	{
		if (!aId.equals(bId))
		{
			try
			{
				CharSequence a = UniProt.QueryUniProtItem(aId, kit).getSequenceData();
				CharSequence b = UniProt.QueryUniProtItem(bId, kit).getSequenceData();

				if (a.length() <= counter.getLimit() && b.length() <= counter.getLimit())
				{
					this.analyze(a, b);

					// Tools.mark(aId);
					// Tools.mark(SPLIT);
					// Tools.mark(bId);
					Tools.mark('\n');
				}

			}
			catch (NullPointerException e)
			{
				// e.printStackTrace();
			}
		}
	}

	public void analyze(CharSequence a, CharSequence b)
	{
		counter.setNumber(Math.min(a.length(), b.length()) / 2);
		counter.setNumber(101);

		counter.count(a, map, aResult);
		counter.count(b, map, bResult);

		int length = aResult.size();

		// pResult.clear();
		// double sum = 0;
		// int over = 0;
		// double min = Double.MAX_VALUE;
		Tools.mark(flag);
		Tools.mark(' ');
		for (int i = 0; i < length; i++)
		{
			Double p = Math.abs(aResult.get(i).minus(bResult.get(i)).modulus());
			// sum += p;
			// // if (p > EXPECTATION) {
			// // over++;
			// // }
			// if (p < min) {
			// min = p;
			// }
			Tools.mark(i + 1);
			Tools.mark(':');
			Tools.mark(p);
			Tools.mark(' ');
			// pResult.add(p);
		}

		// SpectumCounter.normalize(pResult);
		// for (Double d : pResult) {
		// sum += d;
		// }

		// double per = 1.0 * over / counter.getNumber();
		// Tools.mark(per);
		// Tools.mark(SPLIT);

		// Tools.mark(min);
		// Tools.mark(SPLIT);
		//
		// double avg = sum / counter.getNumber();
		// Tools.mark(avg);
		// Tools.mark(SPLIT);

		// double sn = min / avg;
		// Tools.mark(sn);
		// Tools.mark(SPLIT);

		// sum = 0;
		// double temp = 0;
		// for (Double d : pResult) {
		// temp = d - avg;
		// sum += temp * temp;
		// }
		// sum /= counter.getNumber() - 1;
		// double std = Math.sqrt(sum);
		// Tools.mark(std);
		// Tools.mark(SPLIT);

		number++;
	}

	@Override
	protected void readFinished()
	{

	}

	@Override
	protected void readLine(CharSequence line)
	{
		String[] pair = Tools.splitCharSequence(line, "\t", 2);
		if (pair.length == 2)
		{
			try
			{
				this.analyze(pair[0], pair[1]);
				if (number >= 400)
				{
					this.setReading(false);
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}

		}
	}

	@Override
	protected void readPrepare()
	{
		number = 0;
	}

	public int getFlag()
	{
		return flag;
	}

	public void setFlag(int flag)
	{
		this.flag = flag;
	}
}
