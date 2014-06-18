package org.kernelab.bifo.align;

import java.util.Scanner;

import org.kernelab.basis.Tools;
import org.kernelab.numeric.matrix.Position;

/**
 * The algorithm to solve the Longest Common Subsequence problem.
 * 
 * @author Dilly King
 * 
 */
public class LCS extends Aligner
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		LCS lcs = new LCS(10);

		Scanner s = new Scanner(System.in);

		String a = s.nextLine();

		String b = s.nextLine();

		// lcs.setSequences(a, b);

		lcs.align(a, b);

		// lcs.debugMatrix();

		Tools.debug(lcs.trace().toString());
	}

	protected int	gapOpen	= 1;

	public LCS(int capacity)
	{
		super(capacity);
	}

	@Override
	protected void fill(AlignMatrix<Double> matrix, int row, int column, double score, AlignCell<Double> former)
	{
		matrix.set(score, row, column);
		matrix.getCell(row, column).setFormer(former);
	}

	public int getGapOpen()
	{
		return gapOpen;
	}

	@Override
	protected void init(AlignMatrix<Double> matrix)
	{
		matrix.set(0.0);

		for (int row = 1; row < matrix.getRows(); row++)
		{
			matrix.set(0.0, row, Position.FIRST);
		}

		for (int column = 1; column < matrix.getColumns(); column++)
		{
			matrix.set(0.0, Position.FIRST, column);
		}
	}

	@Override
	protected double mark(AlignMatrix<Double> matrix)
	{
		return matrix.get(matrix.getRows() - 1, matrix.getColumns() - 1);
	}

	@Override
	protected double match(char a, char b)
	{
		return a == b ? 1 : 0;
	}

	@Override
	protected double score(AlignMatrix<Double> matrix, int row, int column, CharSequence a, CharSequence b,
			int direction)
	{
		double score = matrix.get(row, column);

		if (direction == DIRECTION_DIAGONAL)
		{
			score += this.match(a.charAt(row), b.charAt(column));
		}

		return score;
	}

	public void setGapOpen(int openGap)
	{
		this.gapOpen = openGap;
	}

	@Override
	protected Alignment trace(AlignMatrix<Double> matrix, CharSequence a, CharSequence b, Alignment alignment)
	{
		int row = matrix.getRows() - 1;
		int column = matrix.getColumns() - 1;

		Position former = Position.New(row, column);

		AlignCell<Double> cell = matrix.getCell(row, column);

		while (row >= 0 && column >= 0)
		{
			if (cell != null)
			{
				cell = cell.getFormer();
			}

			if (cell != null)
			{
				Position p = cell.getPosition(matrix);

				former.setPosition(p);
			}
			else
			{
				int r = Math.max(row - 1, 0);
				int c = Math.max(column - 1, 0);

				double[] values = { matrix.get(r, c), matrix.get(r, column), matrix.get(row, c) };

				if (values[0] >= values[1] && values[0] >= values[2])
				{
					former = new Position(r, c);
				}
				else if (values[1] >= values[2])
				{
					former = new Position(r, column);
				}
				else
				{
					former = new Position(row, c);
				}
			}

			char s = Alignment.GAP_CHAR;
			if (former.row != row)
			{
				s = a.charAt(former.row);
			}

			char t = Alignment.GAP_CHAR;
			if (former.column != column)
			{
				t = b.charAt(former.column);
			}

			double match = -gapOpen;
			if (s != Alignment.GAP_CHAR && t != Alignment.GAP_CHAR)
			{
				match = this.match(s, t);
			}

			alignment.add(s, t, match);

			row = former.row;
			column = former.column;

			if (row == 0 && column == 0)
			{
				break;
			}
		}

		return alignment;
	}
}
