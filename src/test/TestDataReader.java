package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.kernelab.basis.AbstractAccomplishable;

/**
 * This is a framework to read a data file line by line.
 * 
 * @author Dilly King
 * 
 */
public abstract class TestDataReader extends AbstractAccomplishable<TestDataReader> implements Runnable
{

	protected Reader	reader;

	private boolean		reading;

	public TestDataReader()
	{
		super();
		reading = false;
	}

	public Reader getReader()
	{
		return reader;
	}

	public boolean isReading()
	{
		return reading;
	}

	public void read()
	{
		if (this.reader != null)
		{

			BufferedReader reader = new BufferedReader(this.reader);

			String line = null;

			reading = true;

			this.readPrepare();

			try
			{
				while (reading && (line = reader.readLine()) != null)
				{
					this.readLine(line);
				}

				// If the reading process was not terminated by
				// setReading(false), isReading() returns true here.
				this.readFinished();

				reading = false;

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method will be called after all lines have been read.<br />
	 * Attention that If the reading process was not terminated by
	 * setReading(false), here isReading() returns true.
	 */
	protected abstract void readFinished();

	/**
	 * This method will be called while reading each line.
	 * 
	 * @param line
	 *            One line in data file.
	 */
	protected abstract void readLine(String line);

	/**
	 * This method will be called before starting the reading process.
	 */
	protected abstract void readPrepare();

	@Override
	public void run()
	{
		this.resetAccomplishStatus();
		this.read();
		this.accomplished();
	}

	public TestDataReader setDataFile(File file) throws FileNotFoundException
	{
		try
		{
			this.setDataFile(file, Charset.defaultCharset().name());
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public TestDataReader setDataFile(File file, String charSetName) throws UnsupportedEncodingException,
			FileNotFoundException
	{
		return this.setReader(new InputStreamReader(new FileInputStream(file), charSetName));
	}

	/**
	 * To set the Reader.<br />
	 * Attention that if the Reader is reading, this will take no effect.
	 * 
	 * @param reader
	 *            Reader
	 * @return This DataReader object.
	 */
	public TestDataReader setReader(Reader reader)
	{
		if (!reading)
		{
			this.reader = reader;
		}
		return this;
	}

	protected void setReading(boolean reading)
	{
		this.reading = reading;
	}

}
