package org.kernelab.bifo.go;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kernelab.basis.Copieable;
import org.kernelab.basis.Tools;
import org.kernelab.basis.VectorAccessible;

public class Gene2GOItem implements VectorAccessible, Copieable<Gene2GOItem>,
		Comparable<Gene2GOItem>
{
	private static int		INDEX			= 0;

	public static final int	tax_id_INDEX	= INDEX++;

	public static final int	GeneID_INDEX	= INDEX++;

	public static final int	GO_ID_INDEX		= INDEX++;

	public static final int	Evidence_INDEX	= INDEX++;

	public static final int	Qualifier_INDEX	= INDEX++;

	public static final int	GO_term_INDEX	= INDEX++;

	public static final int	PubMed_INDEX	= INDEX++;

	public static final int	Category_INDEX	= INDEX++;

	public static final int	GO_ID_LENGTH	= 7;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String s = "3702	814629	GO:0008270	IEA	-	zinc ion binding	-	Function";
		Tools.debug(new Gene2GOItem(s));
	}

	private String	goId;

	private String	category;

	private String	term;

	private String	string;

	protected Gene2GOItem(Gene2GOItem item)
	{
		this.goId = item.goId;
		this.category = item.category;
		this.term = item.term;
		this.string = item.string;
	}

	public Gene2GOItem(ResultSet rs) throws SQLException
	{
		this.setGoId(rs.getString("go"));
		this.setCategory(rs.getString("category"));
		this.setTerm(rs.getString("term"));

		this.makeString();
	}

	/**
	 * Create a Gene2GOItem by a line of data likes:<br />
	 * 3702 814629 GO:0008270 IEA - zinc ion binding - Function<br />
	 * The columns are separated by '\t'.
	 * 
	 * @param line
	 *            a line of data
	 */
	public Gene2GOItem(String line)
	{
		String[] columns = line.split("\\\t");
		this.setGoId(columns[GO_ID_INDEX]);
		this.setCategory(columns[Category_INDEX]);
		this.setTerm(columns[GO_term_INDEX]);

		this.makeString();
	}

	@Override
	public Gene2GOItem clone()
	{
		return new Gene2GOItem(this);
	}

	@Override
	public int compareTo(Gene2GOItem o)
	{
		int c = this.goId.compareTo(o.goId);

		if (c == 0) {

			c = this.category.compareTo(o.category);

			if (c == 0) {

				c = this.term.compareTo(o.term);
			}
		}

		return c;
	}

	@Override
	public boolean equals(Object o)
	{
		boolean is = false;

		if (o instanceof Gene2GOItem) {
			Gene2GOItem i = (Gene2GOItem) o;
			is = this.goId.equals(i.goId) && this.category.equals(i.category)
					&& this.term.equals(i.term);
		}

		return is;
	}

	public String getCategory()
	{
		return category;
	}

	public String getGo()
	{
		return "GO:" + goId;
	}

	public String getGoId()
	{
		return goId;
	}

	public String getTerm()
	{
		return term;
	}

	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}

	private void makeString()
	{
		string = this.getGo() + "\t" + this.getCategory() + "\t" + this.getTerm();
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public void setGoId(String goId)
	{
		this.goId = goId.replaceFirst("^GO\\:", "");
	}

	public void setTerm(String term)
	{
		this.term = term;
	}

	@Override
	public String toString()
	{
		return string;
	}

	@Override
	public int vectorAccess()
	{
		return 3;
	}

	@Override
	public Object vectorAccess(int index)
	{
		Object object = null;

		switch (index)
		{
			case 0:
				object = this.getGo();
				break;

			case 1:
				object = this.getCategory();
				break;

			case 2:
				object = this.getTerm();
				break;
		}

		return object;
	}

	@Override
	public void vectorAccess(int index, Object object)
	{
		switch (index)
		{
			case 0:
				this.setGoId(object.toString());
				break;

			case 1:
				this.setCategory(object.toString());
				break;

			case 2:
				this.setTerm(object.toString());
				break;
		}
	}

}
