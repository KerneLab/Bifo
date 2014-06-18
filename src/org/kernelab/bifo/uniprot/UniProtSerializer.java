package org.kernelab.bifo.uniprot;

import java.sql.SQLException;
import java.util.LinkedList;

import org.kernelab.basis.AbstractAccomplishable;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.util.Progressive;

public class UniProtSerializer extends AbstractAccomplishable<UniProtSerializer> implements Runnable
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Progressive	progress;

	@Override
	public void run()
	{
		this.resetAccomplishStatus();
		this.serialize();
		this.accomplished();
	}

	public void serialize()
	{
		if (progress != null)
		{
			progress.prepareProgress();
		}

		SQLKit kit = UniProt.DATABASE.getSQLKit();

		try
		{

			int index = 0;

			if (progress != null)
			{
				progress.resetProgress(UniProt.Items.size());
			}

			LinkedList<UniProtItem> items = new LinkedList<UniProtItem>(UniProt.Items.values());

			kit.update("TRUNCATE `" + UniProt.TABLE_NAME + "`");

			kit.setAutoCommit(false);

			kit.prepareStatement("INSERT INTO `" + UniProt.TABLE_NAME + "` VALUES (?,?,?,?,?)");

			while (!items.isEmpty())
			{

				UniProtItem item = items.poll();

				kit.addBatch(item.getId(), item.getSpecies(), item.getEntry(), item.getDetail(), item.getSequenceData());

				index++;

				if (index == 1000)
				{
					kit.executeBatch();
					kit.commit();
					kit.clearBatch();
					index = 0;
				}

				if (progress != null)
				{
					progress.nextProgress();
				}
			}

			if (index != 0)
			{
				kit.executeBatch();
				kit.commit();
				kit.clearBatch();
			}

			UniProt.Items.clear();
			UniProtReader.Read = false;

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
			System.gc();
		}
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

	@Override
	protected UniProtSerializer getAccomplishableSubject()
	{
		return this;
	}

}
