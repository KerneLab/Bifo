package org.kernelab.bifo.fasta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kernelab.basis.Tools;
import org.kernelab.bifo.uniprot.UniProt;
import org.kernelab.bifo.util.AminoAcid;
import org.kernelab.bifo.wavelet.Fourier;
import org.kernelab.numeric.Complex;
import org.kernelab.numeric.Fraction;

public class SpectumCounter
{

	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException
	{
		Map<Character, Double> map = AminoAcid.ChemicoPhysicalMaps.get("EE");

		String aId = "P63087";

		String bId = "Q9CQV8";

		String a = UniProt.QueryUniProtItem(aId).getSequenceData();

		String b = UniProt.QueryUniProtItem(bId).getSequenceData();

		SpectumCounter counter = new SpectumCounter(1000);
		counter.number = Math.min(a.length(), b.length()) / 2;

		List<Complex> aResult = new ArrayList<Complex>(counter.number);
		counter.count(a, map, aResult);

		List<Complex> bResult = new ArrayList<Complex>(counter.number);
		counter.count(b, map, bResult);

		List<Double> aModulus = new ArrayList<Double>(counter.number);
		List<Double> bModulus = new ArrayList<Double>(counter.number);

		List<Double> p = new ArrayList<Double>(aResult.size());
		for (int i = 0; i < aResult.size(); i++) {
			aModulus.add(aResult.get(i).modulus());
			bModulus.add(bResult.get(i).modulus());
			p.add(aResult.get(i).minus(bResult.get(i)).modulus());
		}

		// Tools.debug(aId + "\t" + bId + "\t" + "PRODUCT");
		for (int i = 0; i < p.size(); i++) {
			Tools.debug(aModulus.get(i) + "\t" + bModulus.get(i) + "\t" + p.get(i));
		}
	}

	public static final void normalize(List<Double> data)
	{
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;

		for (Double d : data) {
			if (d < min) {
				min = d;
			}
			if (d > max) {
				max = d;
			}
		}
		max -= min;

		for (int i = 0; i < data.size(); i++) {
			data.set(i, (data.get(i) - min) / max);
		}
	}

	private int				limit;

	private int				number;

	private Fourier			fourier;

	private List<Complex>	x;

	private List<Complex>	y;

	public SpectumCounter(int limit)
	{
		this.limit = limit;
		fourier = new Fourier(limit);
		x = new ArrayList<Complex>(limit);
		y = new ArrayList<Complex>(limit);
	}

	public List<Complex> count(CharSequence seq, Map<Character, Double> map,
			List<Complex> result)
	{
		x.clear();
		y.clear();

		for (int i = 0; i < seq.length(); i++) {
			x.add(new Complex(map.get(seq.charAt(i))));
		}

		fourier.transform(x, y);

		return this.count(y, result);
	}

	public List<Complex> count(List<Complex> in, List<Complex> out)
	{
		int size = in.size();

		size /= 2;

		Fraction step = new Fraction(size, number);

		Fraction low = new Fraction(0);

		Fraction up = new Fraction(0);

		if (out == null) {
			out = new ArrayList<Complex>(number);
		} else {
			out.clear();
		}

		for (int i = 0; i < number; i++) {

			up.add(step);

			Complex c = new Complex(0.0);
			// double max = 0.0;
			for (int j = (int) Math.ceil(low.doubleValue()); j < up.doubleValue(); j++) {
				// double m = in.get(j).modulus();
				// if (m > max) {
				// max = m;
				// c = in.get(j);
				// }
				Complex k = in.get(j);
				if (k != null) {
					c.add(k);
				}
			}

			out.add(c.normalize());

			low.add(step);
		}

		out.remove(0);

		// normalize(out);

		return out;
	}

	// public List<Double> count(List<Complex> in, List<Double> out)
	// {
	// int size = in.size();
	//
	// size /= 2;
	//
	// Fraction step = new Fraction(size, number);
	//
	// Fraction low = new Fraction(0);
	//
	// Fraction up = new Fraction(0);
	//
	// if (out == null) {
	// out = new ArrayList<Double>(number);
	// } else {
	// out.clear();
	// }
	//
	// for (int i = 0; i < number; i++) {
	//
	// up.add(step);
	//
	// double max = 0.0;
	//
	// for (int j = (int) Math.ceil(low.doubleValue()); j < up.doubleValue();
	// j++) {
	// double m = in.get(j).modulus();
	// if (m > max) {
	// max = m;
	// }
	// }
	//
	// out.add(max);
	//
	// low.add(step);
	// }
	//
	// out.remove(0);
	//
	// normalize(out);
	//
	// return out;
	// }

	public int getLimit()
	{
		return limit;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}

}
