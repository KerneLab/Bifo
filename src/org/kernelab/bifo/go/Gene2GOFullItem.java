package org.kernelab.bifo.go;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kernelab.basis.Tools;
import org.kernelab.basis.VectorAccessible;

public class Gene2GOFullItem extends Gene2GOItem implements VectorAccessible
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String s = "3702	814629	GO:0008270	IEA	-	zinc ion binding	-	Function";
		Tools.debug(new Gene2GOFullItem(s));
	}

	private int		geneId;

	private int		taxId;

	private String	string;

	protected Gene2GOFullItem(Gene2GOFullItem item)
	{
		super(item);
		this.geneId = item.geneId;
		this.taxId = item.taxId;
		this.string = item.string;
	}

	public Gene2GOFullItem(ResultSet rs) throws SQLException
	{
		super(rs);
		this.setGeneId(rs.getInt("gene"));
		this.setTaxId(rs.getInt("tax"));

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
	public Gene2GOFullItem(String line)
	{
		super(line);
		String[] columns = line.split("\\\t");
		this.setGoId(columns[GO_ID_INDEX]);
		this.setCategory(columns[Category_INDEX]);
		this.setTerm(columns[GO_term_INDEX]);
		this.setGeneId(columns[GeneID_INDEX]);
		this.setTaxId(columns[tax_id_INDEX]);

		this.makeString();
	}

	@Override
	public Gene2GOFullItem clone()
	{
		return new Gene2GOFullItem(this);
	}

	@Override
	public int compareTo(Gene2GOItem o)
	{
		int c = super.compareTo(o);

		if (c == 0) {

			if (o instanceof Gene2GOFullItem) {

				Gene2GOFullItem i = (Gene2GOFullItem) o;

				if (c == 0) {

					c = this.geneId - i.geneId;

					if (c == 0) {

						c = this.taxId - i.taxId;

					}
				}
			}

		}

		return c;
	}

	@Override
	public boolean equals(Object o)
	{
		boolean is = super.equals(o);

		if (is) {
			if (o instanceof Gene2GOFullItem) {
				Gene2GOFullItem i = (Gene2GOFullItem) o;
				is = this.geneId == i.geneId && this.taxId == i.taxId;
			}
		}

		return is;
	}

	public int getGeneId()
	{
		return geneId;
	}

	public int getTaxId()
	{
		return taxId;
	}

	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}

	private void makeString()
	{
		string = this.getGo() + "\t" + this.getCategory() + "\t" + this.getTerm() + "\t"
				+ this.getGeneId() + "\t" + this.getTaxId();
	}

	public void setGeneId(int geneId)
	{
		this.geneId = geneId;
	}

	public void setGeneId(String geneId)
	{
		this.setGeneId(Integer.parseInt(geneId));
	}

	public void setTaxId(int taxId)
	{
		this.taxId = taxId;
	}

	public void setTaxId(String taxId)
	{
		this.setTaxId(Integer.parseInt(taxId));
	}

	@Override
	public String toString()
	{
		return string;
	}

	@Override
	public int vectorAccess()
	{
		return 5;
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

			case 3:
				object = this.getGeneId();
				break;

			case 4:
				object = this.getTaxId();
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

			case 3:
				this.setGeneId(object.toString());
				break;

			case 4:
				this.setTaxId(object.toString());
				break;
		}
	}

}
