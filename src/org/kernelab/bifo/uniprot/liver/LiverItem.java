package org.kernelab.bifo.uniprot.liver;

import org.kernelab.basis.Tools;
import org.kernelab.bifo.uniprot.UniProtItem;

/**
 * NumericMatrix class for liver item.<br />
 * 
 * <pre>
 * &gt;gnl|HLPP_French_Liver|IPI00026272.1  |SWISS-PROT:P04908 Tax_Id=9606 Histone H2A type 1-B
 * SGRGKQGGKARAKAKTRSSRAGLQFPVGRVHRLLRKAHYSERVGAGAPVYLAAVLEYLTA
 * EILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGRVTIAQGGVLPNIQAVLLPKKT
 * ESHHKAKGK
 * </pre>
 * 
 * Here, "Histone H2A type 1-B" would be the outline information. And "P04908"
 * is the id.
 * 
 * @author Dilly King
 * 
 */
public abstract class LiverItem extends UniProtItem
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6881141451835902027L;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private String	outline;

	public LiverItem(String description)
	{
		super(description);
		this.setDescription(description);
	}

	/**
	 * Fetch the id from a description string.
	 * 
	 * @param description
	 *            The description string.
	 * @return The id.
	 */
	protected abstract String getId(String description);

	public String getOutline()
	{
		return outline;
	}

	/**
	 * Fetch the outline information from a description string.
	 * 
	 * @param description
	 *            The description string.
	 * @return The outline information.
	 */
	protected String getOutline(String description)
	{
		int lastIndex = Tools.seekLastIndex(description, '=');
		int index = Tools.seekIndex(description, " ", lastIndex + 1);

		return description.substring(index + 1);
	}

	@Override
	public void setDescription(String description)
	{
		super.setDescription(description);

		this.setId(this.getId(description));

		this.setOutline(this.getOutline(description));
	}

	protected void summaryInformation()
	{
		super.summaryInformation();
		this.getInformation().clear();
		this.getInformation().put("Id", this.getId());
		this.getInformation().put("Outline", this.getOutline());
	}

	public void setOutline(String outline)
	{
		this.outline = outline;
	}
}
