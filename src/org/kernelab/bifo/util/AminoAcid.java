package org.kernelab.bifo.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.kernelab.numeric.matrix.Matrix;

final public class AminoAcid
{

	public static final String								AminoAcidMapName		= "AminoAcid";

	public static final Map<Character, Integer>				AminoAcidIndexMap		= new HashMap<Character, Integer>();

	public static final String[]							ScoringMatrixesList		= { "blosum50", "blosum62",
			"pam40", "pam250"														};

	public static final Map<String, Matrix<Integer>>		ScoringMatrixes			= new HashMap<String, Matrix<Integer>>();

	public static final String[]							ChemicoPhysicalMapsList	= { "EE", "EIIP", "IC" };

	public static final Map<String, Map<Character, Double>>	ChemicoPhysicalMaps		= new HashMap<String, Map<Character, Double>>();

	static
	{

		try
		{
			MapReader.readCharacterIntegerMap(MapReader.getMapFile(AminoAcidMapName), AminoAcidIndexMap);

			for (String name : ChemicoPhysicalMapsList)
			{
				ChemicoPhysicalMaps.put(name, MapReader.readCharacterDoubleMap(MapReader.getMapFile(name), null));
			}

			for (String name : ScoringMatrixesList)
			{
				ScoringMatrixes.put(name, MatrixReader.readIntegerMatrix(MatrixReader.getMapFile(name)));
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Tools.debug(ChemicoPhysicalMaps);
	}

	public static int ScoreMatch(char a, char b, String scoringMatrixName)
	{
		Matrix<Integer> matrix = ScoringMatrixes.get(scoringMatrixName);

		if (matrix == null)
		{
			try
			{
				matrix = MatrixReader.readIntegerMatrix(MatrixReader.getMapFile(scoringMatrixName));
				ScoringMatrixes.put(scoringMatrixName, matrix);
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

		return matrix.get(AminoAcidIndexMap.get(a) - 1, AminoAcidIndexMap.get(b) - 1);
	}

}
