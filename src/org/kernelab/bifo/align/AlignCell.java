package org.kernelab.bifo.align;

import org.kernelab.numeric.matrix.Cell;

public class AlignCell<E> extends Cell<E>
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -7249562072147923013L;

	private AlignCell<E>		former;

	public AlignCell(E element)
	{
		super(element);
		former = null;
	}

	public AlignCell<E> getFormer()
	{
		return former;
	}

	public void reset()
	{
		former = null;
	}

	public void setFormer(AlignCell<E> former)
	{
		this.former = former;
	}
}
