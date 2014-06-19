package org.kernelab.bifo.align;

public class Alignment
{
	public static char		GAP_CHAR		= '-';

	public static char		RELATION_CHAR	= ':';

	public static char		MATCHED_CHAR	= '|';

	public static char		UNMATCH_CHAR	= ' ';

	private double			mark;

	private StringBuilder	a				= new StringBuilder();

	private StringBuilder	r				= new StringBuilder();

	private StringBuilder	b				= new StringBuilder();

	public Alignment(double mark)
	{
		this.mark = mark;
	}

	public void add(char a, char b, double match)
	{
		this.a.insert(0, a == LCS.GAP_HOLDER ? GAP_CHAR : a);
		this.b.insert(0, b == LCS.GAP_HOLDER ? GAP_CHAR : b);

		char r = RELATION_CHAR;
		if (match > 0 && a == b)
		{
			r = MATCHED_CHAR;
		}
		else if (match < 0)
		{
			r = UNMATCH_CHAR;
		}
		this.r.insert(0, r);
	}

	public String getA()
	{
		return a.toString();
	}

	public String getB()
	{
		return b.toString();
	}

	public double getEvaluation()
	{
		return this.getScore() / this.size();
	}

	public double getMark()
	{
		return mark;
	}

	public String getR()
	{
		return r.toString();
	}

	public double getScore()
	{
		return mark / 3.0;
	}

	public void reset(double mark)
	{
		this.setMark(mark);
		a.delete(0, a.length());
		r.delete(0, r.length());
		b.delete(0, b.length());
	}

	protected void setA(StringBuilder a)
	{
		this.a = a;
	}

	protected void setB(StringBuilder b)
	{
		this.b = b;
	}

	protected void setMark(double mark)
	{
		this.mark = mark;
	}

	public int size()
	{
		return r.length();
	}

	@Override
	public String toString()
	{
		return a.toString() + '\n' + r.toString() + '\n' + b.toString();
	}
}
