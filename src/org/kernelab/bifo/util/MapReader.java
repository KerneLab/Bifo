package org.kernelab.bifo.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.kernelab.basis.Tools;
import org.kernelab.basis.io.DataReader;

/**
 * NumericMatrix framework which is to read a two-column file separated by "\t"
 * as default, and put the relation into a Map<K,V>. The first column in file
 * are keys, the second are values.
 * 
 * @author Dilly King
 * 
 * @param <K>
 *            The generic type of map key.
 * @param <V>
 *            The generic type of map value.
 */
public abstract class MapReader<K, V> extends DataReader
{

	public static String								PathBase				= Tools.getPathOfClass(MatrixReader.class)
																						+ "map/";

	public static String								FileSuffix				= ".map";

	public static String								SEPARATOR				= "\t";

	public static final MapReader<Character, Double>	CharacterDoubleReader	= new MapReader<Character, Double>() {

																					@Override
																					protected Character makeKey(
																							String string)
																					{
																						return string.charAt(0);
																					}

																					@Override
																					protected Double makeValue(
																							String string)
																					{
																						return Double.valueOf(string);
																					}

																					@Override
																					protected Map<Character, Double> newMap()
																					{
																						return new HashMap<Character, Double>();
																					}
																				};

	public static final MapReader<Character, Integer>	CharacterIntegerReader	= new MapReader<Character, Integer>() {

																					@Override
																					protected Character makeKey(
																							String string)
																					{
																						return string.charAt(0);
																					}

																					@Override
																					protected Integer makeValue(
																							String string)
																					{
																						return Integer.valueOf(string);
																					}

																					@Override
																					protected Map<Character, Integer> newMap()
																					{
																						return new HashMap<Character, Integer>();
																					}
																				};

	public static final File getMapFile(String name)
	{
		File file = null;

		try
		{
			file = new File(Tools.getClassLoader().getResource(getMapFilePath(name)).toURI());
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}

		return file;
	}

	public static final String getMapFilePath(String name)
	{
		return PathBase + name + FileSuffix;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			File file = new File(Tools.getClassLoader().getResource("org/kernelab/bifo/util/map/AminoAcid.ndx").toURI());
			MapReader<Character, Integer> reader = new MapReader<Character, Integer>() {

				@Override
				protected Character makeKey(String string)
				{
					return string.charAt(0);
				}

				@Override
				protected Integer makeValue(String string)
				{
					return Integer.valueOf(string);
				}

				@Override
				protected Map<Character, Integer> newMap()
				{
					return new HashMap<Character, Integer>();
				}

			};

			reader.setDataFile(file);

			reader.read();

			Tools.debug(reader.getMap());

		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static final Map<Character, Double> readCharacterDoubleMap(File file, Map<Character, Double> map)
			throws IOException
	{
		CharacterDoubleReader.setDataFile(file);
		CharacterDoubleReader.setMap(map);
		CharacterDoubleReader.read();

		return CharacterDoubleReader.getMap();
	}

	public static final Map<Character, Integer> readCharacterIntegerMap(File file, Map<Character, Integer> map)
			throws IOException
	{
		CharacterIntegerReader.setDataFile(file);
		CharacterIntegerReader.setMap(map);
		CharacterIntegerReader.read();

		return CharacterIntegerReader.getMap();
	}

	private Map<K, V>	map;

	public Map<K, V> getMap()
	{
		return map;
	}

	protected abstract K makeKey(String string);

	protected abstract V makeValue(String string);

	/**
	 * Create a new Map<K,V>.<br />
	 * This method would be called if {@link MapReader#getMap()}==null.
	 * 
	 * @return a new Map<K,V>
	 */
	protected abstract Map<K, V> newMap();

	@Override
	protected void readFinished()
	{

	}

	@Override
	protected void readLine(CharSequence line)
	{
		String[] pair = Tools.splitCharSequence(line, SEPARATOR, 2);
		if (pair.length == 2)
		{
			this.map.put(makeKey(pair[0]), makeValue(pair[1]));
		}
	}

	@Override
	protected void readPrepare()
	{
		if (this.map == null)
		{
			this.map = newMap();
		}
		else
		{
			this.map.clear();
		}
	}

	public void setMap(Map<K, V> map)
	{
		this.map = map;
	}

}
