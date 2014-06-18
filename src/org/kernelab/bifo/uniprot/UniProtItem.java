package org.kernelab.bifo.uniprot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.kernelab.basis.Tools;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.fasta.FastA;
import org.kernelab.bifo.fasta.FastAItem;
import org.kernelab.bifo.go.Gene2GOItem;
import org.kernelab.bifo.go.GeneOntology;

public class UniProtItem extends FastAItem implements Comparable<UniProtItem>
{

	/**
	 * 
	 */
	private static final long	serialVersionUID			= 5858249264022771690L;

	public static final char	COLUMN_SPLIT_CHAR			= '|';

	public static final String	COLUMN_SPLIT_MARK			= "|";

	public static final String	COLUMN_SPLIT_REGEX			= REGEX_ESCAPER
																	+ COLUMN_SPLIT_MARK;

	public static final String	UNIPROT_DESCRIPTION_REGEX	= "^\\>sp\\|[A-Z0-9]{6}\\|[\\s\\S]+$";

	public static final int		ID_LENGTH					= 6;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String s = "sp|Q27YE2|Z_IPPYV RING finger protein Z OS=Ippy virus (isolate Rat/Central African Republic/Dak An B 188 d/1970) GN=Z PE=3 SV=1";

		Tools.debug(s.split("\\_", 2));
	}

	private String				id;

	private String				species;

	private String				entry;

	private Set<Gene2GOItem>	goItems;

	private Map<String, String>	information;

	public UniProtItem()
	{

	}

	public UniProtItem(ResultSet rs) throws SQLException
	{
		id = rs.getString("id");
		species = rs.getString("species");
		entry = rs.getString("entry");
		description = rs.getString("detail");
		sequenceData = rs.getString("code");
		this.summaryInformation();
	}

	public UniProtItem(String description)
	{
		this.setDescription(description);
		this.summaryInformation();
	}

	protected UniProtItem(UniProtItem item)
	{
		this.id = item.id;
		this.species = item.species;
		this.entry = item.entry;
		this.description = item.description;
		this.sequenceData = item.sequenceData;
		this.goItems = item.goItems;
		this.summaryInformation();
	}

	@Override
	public UniProtItem clone()
	{
		return new UniProtItem(this);
	}

	@Override
	public int compareTo(UniProtItem o)
	{
		return this.id.compareTo(o.id);
	}

	@Override
	public boolean equals(Object o)
	{
		boolean equal = false;

		if (o instanceof UniProtItem) {
			UniProtItem i = (UniProtItem) o;
			equal = this.id.equals(i.id);
		}

		return equal;
	}

	@Override
	public String getDescription()
	{
		StringBuilder sb = new StringBuilder(FastA.FASTA_ITEM_BEGIN_MARK);

		sb.append("sp");
		sb.append(COLUMN_SPLIT_CHAR);
		sb.append(id);
		sb.append(COLUMN_SPLIT_MARK);
		sb.append(this.getEntryName());
		sb.append(' ');
		sb.append(description);

		return sb.toString();
	}

	public String getDetail()
	{
		return description;
	}

	public String getEntry()
	{
		return entry;
	}

	public String getEntryName()
	{
		return entry + "_" + species;
	}

	public Set<Gene2GOItem> getGOItems()
	{
		if (goItems == null) {
			goItems = GeneOntology.QueryUniProtGOItems(this.id);
		}

		return goItems;
	}

	public Set<Gene2GOItem> getGOItems(SQLKit kit) throws SQLException
	{
		if (goItems == null) {
			goItems = GeneOntology.QueryUniProtGOItems(this.id, kit);
		}

		return goItems;
	}

	public String getId()
	{
		return id;
	}

	public Map<String, String> getInformation()
	{
		return information;
	}

	public String getSpecies()
	{
		return species;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public void setDescription(String description)
	{
		super.setDescription(description);

		FastA.FASTA_ITEM_BEGIN_MATCHER.reset(description);

		if (FastA.FASTA_ITEM_BEGIN_MATCHER.matches()) {

			String[] columns = this.description.split(COLUMN_SPLIT_REGEX, 3);

			id = columns[1];

			columns = columns[2].split("\\s", 2);

			this.description = columns[1];

			columns = columns[0].split("\\_", 2);

			species = columns[1];

			entry = columns[0];
		}
	}

	public void setEntry(String entry)
	{
		this.entry = entry;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setInformation(Map<String, String> information)
	{
		this.information = information;
	}

	public void setSpecies(String species)
	{
		this.species = species;
	}

	protected void summaryInformation()
	{
		information = new LinkedHashMap<String, String>();
		information.put("Id", id);
		information.put("Species", species);
		information.put("Entry", entry);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.getDescription());
		sb.append('\n');
		sb.append(formatSequenceData(60));

		return sb.toString();
	}

}
