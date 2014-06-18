package org.kernelab.bifo.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.kernelab.basis.Tools;

public class SequenceAligner
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SequenceAligner s = new SequenceAligner();
		// s.setAB("IPGKASYD", "VSPAGMASGYD");
		s.setAB("PAWHEAE", "HEAGAWGHEE");
		// s
		// .setAB(
		// "MEEKLKKAKIIFVVGGPGSGKGTQCEKIVQKYGYTHLSTGDLLRAEVSSGSERGKKLSAIMEKGELVPLDTVLDMLRDAMLAKVDSSNGFLIDGYPREVKQGEEFEQKIGQPTLLLYVDAGAETMTQRLLKRGETSGRVDDNEETIKKRLETYYNATEPVISFYDKRGIVRKVNAEGTVDTVFSEVCTYLDSLK",
		// "MVQKRTAELQGFHRSFKGQNPFELAFSLDLAQHRDSDFSPQCEARPDMPSSQPIDIPDAKKRGRKKKRCRATDSFSGRFEDVYQLQEDVLGEGAHARVQTCVNLITNQEYAVKIIEKQLGHIRSRVFREVEMLYQCQGHRNVLELIEFFEEEDRFYLVFEKMRGGSILSHIHRRRHFNELEASVVVQDVASALDFLHNKGIAHRDLKPENILCEHPNQVSPVKICDFDLGSGIKLNGDCSPISTPELLTPCGSAEYMAPEVVEAFSEEASIYDKRCDLWSLGVILYILLSGYPPFVGHCGSDCGWDRGEACPACQNMLFESIQEGKYEFPDKDWSHISFAAKDLISKLLVRDAKQRLSAAQVLQHPWVQGCAPENTLPTPLVLQRNSCAKDLTSFAAEAIAMNRQLAQCEEDAGQDQPVVIRATSRCLQLSPPSQSKLAQRRQRASLSATPVVLVGDRA");
		s.align();
		// Tools.debug(s.getSchemes());
		// Tools.debug(s.toString(s.getSchemes().get(1)));
		Tools.debug(s.toString());
	}

	// Shorter
	private StringBuilder				a;

	// Longer
	private StringBuilder				b;

	private List<Map<Integer, Integer>>	schemes;

	public SequenceAligner()
	{
		this.schemes = new LinkedList<Map<Integer, Integer>>();
	}

	public void align()
	{
		this.initialize();

		this.align(0, 0, new TreeMap<Integer, Integer>());
	}

	/**
	 * 
	 * @param i
	 *            a序列索引
	 * @param j
	 *            b序列索引
	 * @param map
	 *            a,b序列匹配映射
	 */
	private void align(int i, int j, Map<Integer, Integer> map)
	{
		if (i >= a.length()) {
			schemes.add(map);
		} else {

			String chA = String.valueOf(a.charAt(i));

			int k = b.indexOf(chA, j);

			if (k != -1) {
				map.put(i, k);
			} else {
				j--;
			}

			j = Math.max(j, k);

			// 广度搜索 i <--> j+*
			k = b.indexOf(chA, j + 1);
			if (k != -1) {
				this.align(i, k, new TreeMap<Integer, Integer>(map));
			}

			// 深度搜索 i+1 <--> j+*
			this.align(i + 1, j + 1, map);
		}
	}

	public StringBuilder getA()
	{
		return a;
	}

	public StringBuilder getB()
	{
		return b;
	}

	private Map<Integer, Integer> getBestScheme()
	{
		List<Map<Integer, Integer>> bests = new LinkedList<Map<Integer, Integer>>();

		int maxLength = -1;

		for (Map<Integer, Integer> map : this.schemes) {

			int size = map.size();
			if (size > maxLength) {
				bests.clear();
				bests.add(map);
				maxLength = size;
			} else if (size == maxLength) {
				bests.add(map);
			}
		}

		Map<Integer, Integer> best = null;
		double bestScore = -Double.MAX_VALUE;
		for (Map<Integer, Integer> map : bests) {
			double score = score(map);
			if (score > bestScore) {
				best = map;
				bestScore = score;
			}
		}

		return best;
	}

	public List<Map<Integer, Integer>> getSchemes()
	{
		return schemes;
	}

	private void initialize()
	{
		this.schemes.clear();
	}

	public double score(Map<Integer, Integer> map)
	{
		double score = 0.0;

		int lastKey = -1;
		int lastVal = -1;

		for (Entry<Integer, Integer> entry : map.entrySet()) {
			score += 1.0; // 匹配加分

			if (lastKey != -1) {
				score -= (entry.getKey() - lastKey - 1) * 0.25; // 插空扣分
			}

			lastKey = entry.getKey();

			if (lastVal != -1) {
				score -= (entry.getValue() - lastVal - 1) * 0.25;
			}
		}

		return score;
	}

	private void setA(CharSequence a)
	{
		this.a = new StringBuilder(a);
	}

	public void setAB(CharSequence a, CharSequence b)
	{
		if (a.length() <= b.length()) {
			this.setA(a);
			this.setB(b);
		} else {
			this.setA(b);
			this.setB(a);
		}
	}

	private void setB(CharSequence b)
	{
		this.b = new StringBuilder(b);
	}

	@Override
	public String toString()
	{
		return toString(this.getBestScheme());
	}

	public String toString(Map<Integer, Integer> map)
	{
		StringBuilder a = new StringBuilder();

		StringBuilder b = new StringBuilder(this.b);

		StringBuilder c = new StringBuilder();

		int last = -1;

		for (int i = 0; i < this.a.length(); i++) {

			if (map.containsKey(i)) {
				if (last != -1) {
					for (int j = 0; j < (map.get(i) - map.get(last)) - (i - last); j++) {
						a.append('-');
						c.append(' ');
					}

					for (int j = 0; j < (i - last) - (map.get(i) - map.get(last)); j++) {
						b.insert((int) map.get(i), '-');
					}
				}

				last = i;
				c.append('|');
			} else {
				c.append(' ');
			}

			a.append(this.a.charAt(i));
		}

		for (Entry<Integer, Integer> entry : map.entrySet()) {
			for (int i = 0; i < entry.getValue() - entry.getKey(); i++) {
				a.insert(0, '-');
				c.insert(0, ' ');
			}
			break;
		}

		String r = a.toString() + "\n" + c.toString() + "\n" + b.toString();

		return r;
	}
}
