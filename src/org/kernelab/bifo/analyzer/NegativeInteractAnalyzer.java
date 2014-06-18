package org.kernelab.bifo.analyzer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.kernelab.basis.Relation;
import org.kernelab.basis.Tools;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.interact.Interact;

public class NegativeInteractAnalyzer
{

	public static Set<Relation<String, String>> findNegativeInteractByKeyOfPositiveRelation(
			Set<Relation<String, String>> positiveRelations)
	{
		Set<String> entire = new HashSet<String>();

		Map<String, Set<String>> relation = new HashMap<String, Set<String>>();

		for (Relation<String, String> r : positiveRelations) {
			String k = r.getKey();
			String v = r.getValue();
			Set<String> l = relation.get(k);
			if (l == null) {
				l = new HashSet<String>();
				relation.put(k, l);
			}
			l.add(v);
			entire.add(k);
			entire.add(v);
		}

		Set<Relation<String, String>> result = new HashSet<Relation<String, String>>();

		Set<String> removed = new HashSet<String>();

		List<String> list = new LinkedList<String>();

		SQLKit kit = Interact.DATABASE.getSQLKit();

		Random random = new Random();

		try {

			for (Entry<String, Set<String>> entry : relation.entrySet()) {

				String k = entry.getKey();
				Set<String> v = entry.getValue();

				removed.clear();

				Tools.intersection(entire, v, removed);

				entire.removeAll(removed);

				Tools.intersection(entire, Interact.QueryInteractLigands(k, kit), removed);

				entire.removeAll(removed);

				list.clear();
				list.addAll(entire);

				for (int i = 0; i < v.size() && !list.isEmpty(); i++) {
					int j = random.nextInt(list.size());
					String s = list.remove(j);
					result.add(new Relation<String, String>(k, s));
				}

				entire.addAll(removed);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			kit.close();
		}

		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SQLKit kit = Interact.DATABASE.getSQLKit();

		try {
			String sql = "SELECT * FROM `interact` LIMIT 300";

			ResultSet rs = kit.query(sql);

			Set<Relation<String, String>> positiveRelations = new HashSet<Relation<String, String>>();

			while (rs.next()) {
				positiveRelations.add(new Relation<String, String>(rs
						.getString("receptor"), rs.getString("ligand")));
			}

			Tools.debug(positiveRelations);

			Tools.debug(Tools.repeat("=", 50));

			Tools.debug(findNegativeInteractByKeyOfPositiveRelation(positiveRelations));

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			kit.close();
		}
	}
}
