package org.kernelab.bifo.go;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.kernelab.basis.AbstractAccomplishable;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.util.Progressive;

public class UniProtGOAnnotationsSerializer extends AbstractAccomplishable<UniProtGOAnnotationsSerializer> implements
		Runnable
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Progressive	progress;

	@Override
	protected UniProtGOAnnotationsSerializer getAccomplishableSubject()
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
			kit.update("TRUNCATE `" + GeneOntology.UNIPROT_GO_TABLE_NAME + "`");

			kit.setAutoCommit(false);

			int index = 0;

			if (progress != null) {
				progress.resetProgress(GeneOntology.UniProtGOAnnotations.size());
			}

			kit.prepareStatement("INSERT INTO `" + GeneOntology.UNIPROT_GO_TABLE_NAME
					+ "` VALUES (?,?,?)");

			for (Entry<String, Set<String>> entry : GeneOntology.UniProtGOAnnotations
					.entrySet())
			{
				String uniProtId = entry.getKey();

				Collection<String> annotations = entry.getValue();

				for (String annotation : annotations) {

					kit.addBatch(SQLKit.NULL, uniProtId, annotation);

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

			GeneOntology.UniProtGOAnnotations.clear();
			UniProtGOAnnotationsReader.Read = false;

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
