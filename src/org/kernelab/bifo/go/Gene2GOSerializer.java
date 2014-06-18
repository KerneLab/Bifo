package org.kernelab.bifo.go;

import java.sql.SQLException;
import java.util.Collection;

import org.kernelab.basis.AbstractAccomplishable;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.util.Progressive;

public class Gene2GOSerializer extends AbstractAccomplishable<Gene2GOSerializer> implements Runnable
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Progressive	progress;


	@Override
	protected Gene2GOSerializer getAccomplishableSubject()
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
		SQLKit kit = GeneOntology.DATABASE.getSQLKit();

		try {
			kit.update("TRUNCATE `" + GeneOntology.GENE2GO_FULL_TABLE_NAME + "`");

			kit.setAutoCommit(false);

			int index = 0;

			if (progress != null) {
				progress.resetProgress(GeneOntology.Gene2GOFullItems.size());
			}

			kit.prepareStatement("INSERT INTO `" + GeneOntology.GENE2GO_FULL_TABLE_NAME
					+ "` VALUES (?,?,?,?,?,?)");

			for (Collection<Gene2GOFullItem> items : GeneOntology.Gene2GOFullItems
					.values())
			{

				for (Gene2GOFullItem item : items) {

					kit.addBatch(SQLKit.NULL, item.getGoId(), item.getCategory(), item
							.getTerm(), item.getGeneId(), item.getTaxId());

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

			GeneOntology.Gene2GOFullItems.clear();
			Gene2GOReader.Read = false;

			kit.setAutoCommit(true);
			kit.update("TRUNCATE `" + GeneOntology.GENE2GO_TABLE_NAME + "`");
			kit.update("INSERT INTO `" + GeneOntology.GENE2GO_TABLE_NAME
					+ "` (SELECT `go`,`category`,`term` FROM `"
					+ GeneOntology.GENE2GO_FULL_TABLE_NAME + "` GROUP BY `go`)");

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
