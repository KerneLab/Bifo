package org.kernelab.bifo.wavelet;

import java.util.ArrayList;
import java.util.List;

import org.kernelab.basis.Tools;
import org.kernelab.numeric.Complex;
import org.kernelab.numeric.matrix.ComplexMatrix;
import org.kernelab.numeric.matrix.Matrix;
import org.kernelab.numeric.matrix.Size;

public class Fourier
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Fourier f = new Fourier(10);

		List<Complex> x = new ArrayList<Complex>();
		x.add(new Complex(1.0));
		x.add(new Complex(2.0));
		x.add(new Complex(3.0));
		x.add(new Complex(4.0));
		x.add(new Complex(5.0));
		x.add(new Complex(6.0));
		x.add(new Complex(7.0));
		x.add(new Complex(8.0));
		x.add(new Complex(9.0));

		List<Complex> y = new ArrayList<Complex>();
		f.transform(x, y);

		List<Double> p1 = new ArrayList<Double>();
		for (Complex c : y) {
			double p = Phrase(c);
			p1.add(p);
			Tools.debug(p);
		}

		Tools.debug();

		x.clear();
		x.add(new Complex(2.0));
		x.add(new Complex(6.0));
		x.add(new Complex(8.0));
		x.add(new Complex(3.0));
		x.add(new Complex(9.0));
		x.add(new Complex(7.0));
		x.add(new Complex(4.0));
		x.add(new Complex(1.0));
		x.add(new Complex(5.0));

		y.clear();
		f.transform(x, y);

		List<Double> p2 = new ArrayList<Double>();
		for (Complex c : y) {
			double p = Phrase(c);
			p2.add(p);
			Tools.debug(p);
		}

		// for (int i = 1; i < p1.size(); i++) {
		// Tools.debug(p1.get(i) - p2.get(i));
		// }

		// Tools.debug(y);
		//
		// f.inverse(y, x);
		//
		// Tools.debug(x);

	}

	public static final Complex Omega(Complex omega, double exponent)
	{
		if (omega == null) {
			omega = new Complex(0);
		}

		omega.real = Math.cos(exponent);
		omega.imagin = Math.sin(exponent);

		return omega;
	}

	public static final double Phrase(Complex c)
	{
		return Math.asin(c.imagin / c.modulus());
	}

	private int				limit;

	private ComplexMatrix	matrix;

	private ComplexMatrix	X;

	private ComplexMatrix	Y;

	public Fourier(int limit)
	{
		this.limit = limit;
		matrix = new ComplexMatrix(new Size(limit, limit));
		X = new ComplexMatrix(new Size(limit, 1));
		Y = new ComplexMatrix(new Size(limit, 1));
	}

	public int getLimit()
	{
		return limit;
	}

	public void inverse(List<Complex> y, List<Complex> x)
	{
		// ComplexMatrix yy = Matrix.ColumnOfCollection(y, new ComplexMatrix());
		Matrix.ColumnOfCollection(y, Y);

		// ComplexMatrix xx = yy.clone(Range.Full(yy));
		Matrix.VerifyLimits(X, Y.getRows(), Y.getColumns());
		X.clone(Y);

		makeInverseMatrix(y.size());

		X.product(matrix, Y);

		// (1/n)*SIGMA(...)
		X.divide(new Complex(y.size()));

		if (x == null) {
			x = new ArrayList<Complex>(y.size());
		} else {
			x.clear();
		}

		Matrix.ListOfColumn(X, 0, x);
	}

	private void makeInverseMatrix(int n)
	{
		limit = Math.max(n, limit);
		Matrix.VerifyLimits(matrix, n, n);

		// -2*PI*[-(j-1)(i-1)]/n = 2*PI*(j-1)(i-1)/n
		double exp = 2 * Math.PI / n;

		for (int r = 0; r < n; r++) {
			for (int c = 0; c < n; c++) {
				matrix.set(Omega(matrix.get(r, c), exp * r * c), r, c);
			}
		}
	}

	private void makeTransformMatrix(int n)
	{
		limit = Math.max(n, limit);
		Matrix.VerifyLimits(matrix, n, n);

		// -2*PI*(j-1)(i-1)/n
		double exp = -2 * Math.PI / n;

		for (int r = 0; r < n; r++) {
			for (int c = 0; c < n; c++) {
				matrix.set(Omega(matrix.get(r, c), exp * r * c), r, c);
			}
		}
	}

	public void transform(List<Complex> x, List<Complex> y)
	{
		// ComplexMatrix xx = Matrix.ColumnOfCollection(x, new ComplexMatrix());
		Matrix.ColumnOfCollection(x, X);

		// ComplexMatrix yy = X.clone(Range.Full(X));
		Matrix.VerifyLimits(Y, X.getRows(), X.getColumns());
		Y.clone(X);

		makeTransformMatrix(x.size());

		Y.product(matrix, X);

		if (y == null) {
			y = new ArrayList<Complex>(x.size());
		} else {
			y.clear();
		}

		Matrix.ListOfColumn(Y, 0, y);
	}
}
