package org.kernelab.bifo.align;

import org.kernelab.basis.Tools;
import org.kernelab.bifo.util.AminoAcid;
import org.kernelab.numeric.matrix.Position;

public class NWAligner extends LCS
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		NWAligner nw = new NWAligner(100);
		// Scanner scanner = new Scanner(System.in);
		// Tools.debug("每行输入一个字符串");
		// lcs.setSequences(scanner.nextLine(), scanner.nextLine());

		String a = "A-";
		String b = "A-";

		Tools.debug(a);
		Tools.debug(b);

		nw.setScoringMatrixName("blosum50");
		nw.setGapOpen(8);
		// nw.setScoringMatrixName("pam250");
		// nw.setGapOpen(5);
		Tools.debug(nw.align(a, b).trace().getScore());
	}

	protected String	scoringMatrixName	= "blosum50";

	public NWAligner(int capacity)
	{
		super(capacity);
		gapOpen = 8;
	}

	public String getScoringMatrixName()
	{
		return scoringMatrixName;
	}

	@Override
	protected void init(AlignMatrix<Double> matrix)
	{
		matrix.set(0.0);

		for (int row = 1; row < matrix.getRows(); row++) {
			matrix.set(-1.0 * gapOpen * row, row, Position.FIRST);
		}

		for (int column = 1; column < matrix.getColumns(); column++) {
			matrix.set(-1.0 * gapOpen * column, Position.FIRST, column);
		}
	}

	@Override
	protected double match(char a, char b)
	{
		double match = 0;

		if (a == Alignment.GAP_CHAR || b == Alignment.GAP_CHAR) {
			if (a == b) {
				match = -1;
			} else {
				match = -gapOpen;
			}
		} else {
			match = AminoAcid.ScoreMatch(a, b, scoringMatrixName);
		}

		return match;
	}

	@Override
	protected double score(AlignMatrix<Double> matrix, int row, int column,
			CharSequence a, CharSequence b, int direction)
	{
		double score = matrix.get(row, column);

		if (direction == DIRECTION_DIAGONAL) {
			score += this.match(a.charAt(row), b.charAt(column));
		} else {
			score -= gapOpen;
		}

		return score;
	}

	public void setScoringMatrixName(String scoringMatrixName)
	{
		this.scoringMatrixName = scoringMatrixName;
	}

}
