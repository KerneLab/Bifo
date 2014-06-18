package org.kernelab.bifo.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.kernelab.basis.Tools;
import org.kernelab.basis.Variable;
import org.kernelab.basis.io.DataReader;
import org.kernelab.basis.io.DataWriter;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.go.GeneOntology;
import org.kernelab.bifo.uniprot.UniProt;

public class InteractGOAnalyzer extends DataReader
{

	public static final String	GO_SPLIT	= "_";

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		int flag = 1;
		InteractGOAnalyzer analyzer = new InteractGOAnalyzer(flag, "Function");

		// String name = flag == 1 ? "MOUSE_1_10.txt" : "negative.txt";
		// analyzer.setDataFile(new File("./dat/interact/" + name));

		// analyzer.read();
		analyzer.readDatabase();

		DataWriter writer = new DataWriter();
		writer.setDataFile(new File("./dat/interact/GA_positive.txt"));

		Tools.getOuts().add(new PrintStream(writer.getOutputStream()));
		Tools.debug(analyzer.getRecords());
		Tools.debug(analyzer.getCount());
		writer.close();

		Tools.resetOuts();
		Tools.debug("Done");
	}

	private SQLKit							kit		= UniProt.DATABASE.getSQLKit();

	private String							category;

	private String							sql		= "SELECT `u`.`go` AS `gos` FROM `"
															+ GeneOntology.UNIPROT_GO_TABLE_NAME
															+ "` AS `u`,`"
															+ GeneOntology.GENE2GO_TABLE_NAME
															+ "` AS `g` WHERE `u`.`uniprot`=?"
															+ " AND `u`.`go`=`g`.`go` AND `g`.`category`=?";

	private Set<String>						left	= new HashSet<String>();

	private Set<String>						right	= new HashSet<String>();

	private Map<String, Variable<Integer>>	records	= new TreeMap<String, Variable<Integer>>();

	private int								count;

	public InteractGOAnalyzer(int flag, String category)
	{
		this.category = category;
	}

	private void addRecord(String leftGO, String rightGO)
	{
		String key = null;

		if (leftGO.compareTo(rightGO) < 0) {
			key = leftGO + GO_SPLIT + rightGO;
		} else {
			key = rightGO + GO_SPLIT + leftGO;
		}

		Variable<Integer> value = records.get(key);
		if (value == null) {
			value = new Variable<Integer>(0);
			records.put(key, value);
		}
		value.value++;
	}

	public String getCategory()
	{
		return category;
	}

	public int getCount()
	{
		return count;
	}

	public Map<String, Variable<Integer>> getRecords()
	{
		return records;
	}

	public void readDatabase()
	{
		try {
			ResultSet rs = kit.query("SELECT `receptor`,`ligand` FROM `interact`");

			while (rs.next()) {
				record(rs.getString("receptor"), rs.getString("ligand"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void readFinished()
	{
		Tools.debug(records);
	}

	@Override
	protected void readLine(CharSequence line)
	{
		String[] pair = Tools.splitCharSequence(line, "\t", 2);
		if (pair.length == 2) {
			this.record(pair[0], pair[1]);
		}
	}

	@Override
	protected void readPrepare()
	{
		count = 0;
	}

	private void record(String leftId, String rightId)
	{
		left.clear();
		right.clear();

		try {
			ResultSet rs = kit.query(sql, leftId, category);
			while (rs.next()) {
				left.add(rs.getString("gos"));
			}

			rs = kit.query(sql, rightId, category);
			while (rs.next()) {
				right.add(rs.getString("gos"));
			}

			if (!left.isEmpty() && !right.isEmpty()) {
				for (String l : left) {
					for (String r : right) {
						this.addRecord(l, r);
					}
				}
				count++;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

}
