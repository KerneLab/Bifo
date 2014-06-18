package org.kernelab.bifo.align;

import org.kernelab.basis.Tools;
import org.kernelab.bifo.util.AminoAcid;
import org.kernelab.numeric.matrix.Position;

public class SWAligner extends LCS
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SWAligner sw = new SWAligner(100);
		// Scanner scanner = new Scanner(System.in);
		// Tools.debug("每行输入一个字符串");
		// lcs.setSequences(scanner.nextLine(), scanner.nextLine());

		String a = null;
		String b = null;
		a = "VSPAGMASGYD";
		b = "IPGKASYD";
		a = "GCCCTAGCG";
		b = "GCGCAATG";
		a = "HEAGAWGHEE";
		b = "PA-WHEAE";

		Tools.debug(a);
		Tools.debug(b);

		// sw.setSequences(a, b);

		sw.setScoringMatrixName("blosum50");
		sw.setGapOpen(8);
		// sw.setScoringMatrixName("pam250");
		// sw.setGapOpen(5);

		Tools.debug(sw.align(a, b).trace());
	}

	protected String	scoringMatrixName	= "blosum50";

	public SWAligner(int capacity)
	{
		super(capacity);
		gapOpen = 8;
	}

	@Override
	protected void fill(AlignMatrix<Double> matrix, int row, int column, double score,
			AlignCell<Double> former)
	{
		matrix.set(Math.max(score, 0), row, column);
		if (score >= 0) {
			matrix.getCell(row, column).setFormer(former);
		}
	}

	public String getScoringMatrixName()
	{
		return scoringMatrixName;
	}

	@Override
	protected double mark(AlignMatrix<Double> matrix)
	{
		double mark = -Double.MAX_VALUE;

		for (Double score : matrix) {
			if (score > mark) {
				mark = score;
			}
		}

		return mark;
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

	@Override
	protected Alignment trace(AlignMatrix<Double> matrix, CharSequence a, CharSequence b,
			Alignment alignment)
	{
		AlignCell<Double> cell = null;

		for (int row = 1; cell == null && row < matrix.getRows(); row++) {
			for (int column = 1; cell == null && column < matrix.getColumns(); column++) {
				cell = matrix.getCell(row, column);
				if (cell.getElement() != mark) {
					cell = null;
				}
			}
		}

		Position p = cell.getPosition(matrix);
		int row = p.getRow();
		int column = p.getColumn();

		Position former = Position.New(row, column);

		while (row >= 0 && column >= 0) {

			cell = cell.getFormer();

			if (cell == null) {
				break;
			} else {
				p = cell.getPosition(matrix);
				former.setPosition(p);
			}

			char s = Alignment.GAP_CHAR;
			if (former.row != row) {
				s = a.charAt(former.row);
			}

			char t = Alignment.GAP_CHAR;
			if (former.column != column) {
				t = b.charAt(former.column);
			}

			double match = -gapOpen;
			if (s != Alignment.GAP_CHAR && t != Alignment.GAP_CHAR) {
				match = this.match(s, t);
			}

			alignment.add(s, t, match);

			row = former.row;
			column = former.column;

			if (row == 0 && column == 0) {
				break;
			}
		}

		return alignment;
	}
}
