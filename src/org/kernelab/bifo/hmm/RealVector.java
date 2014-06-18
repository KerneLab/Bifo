package org.kernelab.bifo.hmm;

import org.kernelab.numeric.Real;
import org.kernelab.numeric.matrix.Position;
import org.kernelab.numeric.matrix.RealMatrix;

public class RealVector extends RealMatrix
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1489294055232599354L;

	public RealVector(int length)
	{
		super(1, length);
	}

	@Override
	public Real get(int index)
	{
		Real real = null;
		if (this.isRow()) {
			real = this.get(Position.FIRST, index);
		} else {
			real = this.get(index, Position.FIRST);
		}
		return real;
	}

	public void normalize()
	{
		double total = 0.0;

		for (Real real : this) {
			total += real.value;
		}

		if (total != 0.0) {
			for (Real real : this) {
				real.value /= total;
			}
		}
	}

	public void set(double value, int index)
	{
		Real real = this.get(index);
		if (real == null) {
			real = new Real(0);
			this.set(real, index);
		}
		real.value = value;
	}

	@Override
	public void set(Real real, int index)
	{
		if (this.isRow()) {
			this.set(real, Position.FIRST, index);
		} else {
			this.set(real, index, Position.FIRST);
		}
	}

}
