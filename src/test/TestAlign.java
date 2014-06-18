package test;

import org.kernelab.basis.Tools;
import org.kernelab.bifo.align.Alignment;
import org.kernelab.bifo.align.LCS;

public class TestAlign
{
	static {
		Alignment.MATCHED_CHAR = ' ';
		Alignment.UNMATCH_CHAR = '|';
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		StringBuilder sb = new StringBuilder();

		int interval = 'z' - '0' + 1;

		for (int i = 0; i < 700; i++) {
			char c = (char) ((int) (Math.random() * interval) + '0');
			sb.append(c);
		}

		String a = sb.toString();

		for (int i = 0; i < 23; i++) {
			int p = (int) (Math.random() * sb.length());
			if (Math.random() > 0.7) {
				sb.deleteCharAt(p);
			} else {
				char c = (char) ((int) (Math.random() * interval) + '0');
				sb.setCharAt(p, c);
			}
		}
		String b = sb.toString();

		LCS lcs = new LCS(Math.max(a.length(), b.length()));

		Tools.debug(lcs.align(a, b).trace().toString());
	}
}
