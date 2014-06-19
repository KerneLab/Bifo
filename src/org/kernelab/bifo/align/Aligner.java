package org.kernelab.bifo.align;

import org.kernelab.basis.Tools;
import org.kernelab.numeric.matrix.Matrix;
import org.kernelab.numeric.matrix.Size;

/**
 * The framework of align algorithm.
 * 
 * @author Dilly King
 * 
 */
public abstract class Aligner
{
	public static final int	DIRECTION_DIAGONAL	= 0;

	public static final int	DIRECTION_UP		= 1;

	public static final int	DIRECTION_LEFT		= 2;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	protected CharSequence			a;

	protected CharSequence			b;

	protected AlignMatrix<Double>	matrix;

	protected double				mark;

	public Aligner(int capacity)
	{
		matrix = new AlignMatrix<Double>(new Size(capacity, capacity));
	}

	/**
	 * The entrance of the align algorithm.
	 */
	public Aligner align(CharSequence a, CharSequence b)
	{
		if (a.length() < b.length())
		{
			this.a = a;
			this.b = b;
		}
		else
		{
			this.a = b;
			this.b = a;
		}

		Matrix.VerifyLimits(matrix, this.a.length() + 1, this.b.length() + 1);

		this.init(matrix);

		double score = 0.0;

		AlignCell<Double> former = null;

		double[] scores = new double[3];

		for (int row = 1; row < matrix.getRows(); row++)
		{
			for (int column = 1; column < matrix.getColumns(); column++)
			{
				for (int direction = 0; direction < scores.length; direction++)
				{
					switch (direction)
					{
						case Aligner.DIRECTION_DIAGONAL:
							scores[direction] = this.score(matrix, row - 1, column - 1, this.a, this.b, direction);
							break;

						case Aligner.DIRECTION_UP:
							scores[direction] = this.score(matrix, row - 1, column, this.a, this.b, direction);
							break;

						case Aligner.DIRECTION_LEFT:
							scores[direction] = this.score(matrix, row, column - 1, this.a, this.b, direction);
							break;
					}
				}

				if (scores[0] >= scores[1] && scores[0] >= scores[2])
				{
					score = scores[0];
					former = matrix.getCell(row - 1, column - 1);
				}
				else if (scores[1] > scores[2])
				{
					score = scores[1];
					former = matrix.getCell(row - 1, column);
				}
				else
				{
					score = scores[2];
					former = matrix.getCell(row, column - 1);
				}

				this.fill(matrix, row, column, score, former);
			}
		}

		mark = this.mark(matrix);

		return this;
	}

	public void debugMatrix()
	{
		for (int r = 0; r < matrix.getRows(); r++)
		{
			for (int c = 0; c < matrix.getColumns(); c++)
			{
				AlignCell<Double> e = matrix.getCell(r, c);
				Tools.mark(e.getElement() + " ");
			}
			Tools.debug("");
		}
	}

	/**
	 * Fill the certain cell in the AlignMatrix with a score.
	 * 
	 * @param matrix
	 *            The AlignMatrix.
	 * @param row
	 *            The row index of the cell to be filled.
	 * @param column
	 *            The column index of the cell to be filled.
	 * @param score
	 *            The score which is the best for this cell.
	 * @param former
	 *            The former cell which indicates the alignment.
	 */
	protected abstract void fill(AlignMatrix<Double> matrix, int row, int column, double score, AlignCell<Double> former);

	// public CharSequence getA()
	// {
	// return a;
	// }
	//
	// public CharSequence getB()
	// {
	// return b;
	// }

	// /**
	// * Get the mark of the alignment.<br />
	// * Attention that this mark has been divided by 3.
	// *
	// * @return The mark of alignment.
	// */
	// public double getMark()
	// {
	// return mark / 3;
	// }

	// public AlignMatrix<Double> getMatrix()
	// {
	// return matrix;
	// }

	public double getMark()
	{
		return mark;
	}

	public double getScore()
	{
		return this.getMark() / 3.0;
	}

	/**
	 * The initialization of the algorithm.
	 * 
	 * @param matrix
	 *            The AlignMatrix&lt;Double&gt;
	 */
	protected abstract void init(AlignMatrix<Double> matrix);

	/**
	 * After all cells in AlignMatrix has been filled, this method will give a
	 * mark of the alignment.<br />
	 * Attention that the mark should NOT have been divided by 3.0
	 * 
	 * @param matrix
	 *            The AlignMatrix
	 * @return The mark of the alignment.
	 */
	protected abstract double mark(AlignMatrix<Double> matrix);

	/**
	 * To give an matching evaluation between 2 character.
	 * 
	 * @param a
	 *            One character.
	 * @param b
	 *            Another character.
	 * @return The matching evaluation.
	 */
	protected abstract double match(char a, char b);

	/**
	 * To give a score in a certain direction.<br />
	 * If the direction is {@code DIRECTION_DIAGONAL} then it means the row-th
	 * character in a will align to the column-th character in b.<br />
	 * If the direction is {@code DIRECTION_UP} then it means the row-th
	 * character in a will align to a blank space.<br />
	 * If the direction is {@code DIRECTION_LEFT} then it means the column-th
	 * character in b will align to a blank space.<br />
	 * 
	 * @param matrix
	 *            The AlignMatrix&lt;Double&gt;
	 * @param row
	 *            The row of former cell and also the index of character in
	 *            sequence a.
	 * @param column
	 *            The column of former cell and also the index of character in
	 *            sequence b.
	 * 
	 * @param a
	 *            The sequence a.
	 * @param b
	 *            The sequence b.
	 * @param direction
	 *            The direction number. They are
	 *            {@link Aligner.DIRECTION_DIAGONAL},
	 *            {@link Aligner.DIRECTION_UP} and
	 *            {@link Aligner.DIRECTION_LEFT}
	 * 
	 * @return The score.
	 */
	protected abstract double score(AlignMatrix<Double> matrix, int row, int column, CharSequence a, CharSequence b,
			int direction);

	/**
	 * Get the Alignment of traced by current AlignMatrix.
	 * 
	 * @return The Alignment.
	 */
	public Alignment trace()
	{
		return this.trace(null);
	}

	/**
	 * Trace back the AlignMatrix to get the alignment.
	 * 
	 * @param matrix
	 *            The AlignMatrix&lt;Double&gt;
	 * @param a
	 *            The sequence a.
	 * @param b
	 *            The sequence b.
	 * @param alignment
	 *            The Alignment which holds the result of trace back.
	 * @return The Alignment.
	 */
	protected abstract Alignment trace(AlignMatrix<Double> matrix, CharSequence a, CharSequence b, Alignment alignment);

	/**
	 * Get the Alignment of traced by current AlignMatrix.
	 * 
	 * @param alignment
	 *            The Alignment which holds the result of trace back.
	 * @return The Alignment.
	 */
	public Alignment trace(Alignment alignment)
	{
		if (alignment == null)
		{
			alignment = new Alignment(mark);
		}
		else
		{
			alignment.reset(mark);
		}
		return this.trace(matrix, a, b, alignment);
	}
}
