package org.kernelab.bifo.interact;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kernelab.basis.AbstractAccomplishable;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.go.GeneOntology;
import org.kernelab.bifo.util.Progressive;

public class InteractPredictor extends AbstractAccomplishable<InteractPredictor> implements Runnable
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Map<String, Set<String>>	interact;

	private Set<String>					targets;

	private Map<String, Set<String>>	predicts;

	private Progressive					progress;

	@Override
	protected InteractPredictor getAccomplishableSubject()
	{
		return this;
	}

	public Map<String, Set<String>> getInteract()
	{
		return interact;
	}

	public Map<String, Set<String>> getPredicts()
	{
		return predicts;
	}

	public void predict()
	{
		predicts = this.predictPositiveInteractByInteractGOSolely(interact, targets,
				new HashMap<String, Set<String>>());

		this.accomplished();
	}

	/**
	 * 根据interactgo_solely预测正数据中可能存在的相互作用
	 * 
	 * @param interact
	 *            相互作用的蛋白质编号映射，应当满足对称性
	 * @return 预测出的相互作用关系，满足对称性
	 */
	public Map<String, Set<String>> predictPositiveInteractByInteractGOSolely(
			Map<String, Set<String>> interact, Set<String> targets,
			Map<String, Set<String>> predicts)
	{
		String sql = "SELECT COUNT(*) FROM `" + GeneOntology.UNIPROT_GO_TABLE_NAME
				+ "` AS `rgo`, `" + GeneOntology.UNIPROT_GO_TABLE_NAME
				+ "` AS `lgo` WHERE `rgo`.`uniprot`=? AND `lgo`.`uniprot`=?"
				+ " AND EXISTS (SELECT * FROM `"
				+ GeneOntology.INTERACT_GO_SOLELY_TABLE_NAME
				+ "` AS `s` WHERE `s`.`rgo`=`rgo`.`go` AND `s`.`lgo`=`lgo`.`go`)";

		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		Set<String> receptors = targets == null ? interact.keySet() : targets;

		try {

			for (String rid : receptors) {

				if (progress != null) {
					progress.resetProgress(interact.size());
				}

				for (String lid : interact.keySet()) {

					if (!interact.get(rid).contains(lid)) {

						ResultSet rs = kit.query(sql, rid, lid);
						rs.next();

						if (rs.getInt(1) > 0) {

							Set<String> ligands = predicts.get(rid);
							if (ligands == null) {
								ligands = new HashSet<String>();
								predicts.put(rid, ligands);
							}
							ligands.add(lid);

							ligands = predicts.get(lid);
							if (ligands == null) {
								ligands = new HashSet<String>();
								predicts.put(lid, ligands);
							}
							ligands.add(rid);
						}
					}

					if (progress != null) {
						progress.nextProgress();
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			kit.close();
		}

		return predicts;
	}

	@Override
	public void run()
	{
		this.predict();
	}

	public void setInteract(Map<String, Set<String>> interact)
	{
		this.interact = interact;
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

	public void setTargets(Set<String> targets)
	{
		this.targets = targets;
	}

}
