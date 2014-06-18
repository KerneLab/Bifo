package org.kernelab.bifo.interact;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.kernelab.basis.AbstractAccomplishable;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.util.Progressive;

public class InteractSerializer extends AbstractAccomplishable<InteractSerializer> implements Runnable
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Progressive	progress;

	@Override
	protected InteractSerializer getAccomplishableSubject()
	{
		return this;
	}

	@Override
	public void run()
	{
		this.resetAccomplishStatus();
		this.serialize();
		this.accomplished();
	}

	public void serialize()
	{
		SQLKit kit = Interact.DATABASE.getSQLKit();

		try {
			kit.update("TRUNCATE `" + Interact.TABLE_NAME + "`");

			kit.setAutoCommit(false);

			int index = 0;

			if (progress != null) {
				progress.resetProgress(Interact.Map.size());
			}

			kit.prepareStatement("INSERT INTO `" + Interact.TABLE_NAME
					+ "` VALUES (?,?,?)");

			for (Entry<String, Set<String>> entry : Interact.Map.entrySet()) {

				String receptor = entry.getKey();

				Collection<String> ligands = entry.getValue();

				for (String ligand : ligands) {

					kit.addBatch(SQLKit.NULL, receptor, ligand);

					index++;

					if (index == 1000) {
						kit.executeBatch();
						kit.commit();
						kit.clearBatch();
						index = 0;
					}

				}

				if (progress != null) {
					progress.nextProgress();
				}
			}

			if (index != 0) {
				kit.executeBatch();
				kit.commit();
				kit.clearBatch();
			}

			Interact.Map.clear();
			InteractReader.Read = false;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			kit.close();
			System.gc();
		}
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

}
