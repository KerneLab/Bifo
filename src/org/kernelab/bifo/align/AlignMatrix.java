package org.kernelab.bifo.align;

import org.kernelab.numeric.matrix.Matrix;
import org.kernelab.numeric.matrix.Size;

public class AlignMatrix<E> extends Matrix<E>
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5809147749318294129L;

	public AlignMatrix(Matrix<E> matrix)
	{
		super(matrix, null);
	}

	public AlignMatrix(Size size)
	{
		super(size);
	}

	@Override
	protected AlignCell<E> createCell(E element)
	{
		return new AlignCell<E>(element);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AlignCell<E>[][] createCellsArray(int... dimensions)
	{
		return (AlignCell<E>[][]) java.lang.reflect.Array.newInstance(AlignCell.class,
				dimensions);
	}

	@Override
	public AlignCell<E> getCell(int row, int column)
	{
		return (AlignCell<E>) super.getCell(row, column);
	}

	public void clear()
	{
		for (int row = 0; row < this.getRows(); row++) {
			for (int column = 0; column < this.getColumns(); column++) {
				this.getCell(row, column).reset();
			}
		}
	}
}
