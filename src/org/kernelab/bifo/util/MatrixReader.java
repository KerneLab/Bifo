package org.kernelab.bifo.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.kernelab.basis.Tools;
import org.kernelab.basis.io.DataReader;
import org.kernelab.numeric.matrix.Matrix;
import org.kernelab.numeric.matrix.Size;

/**
 * To read matrix data.<br />
 * The first line show be {@code rows \t columns}.
 * 
 * <pre>
 * 2	3
 * 5	6	3
 * 2	4	4
 * </pre>
 * 
 * Attention that all the separator is '\t' as default.
 * 
 * @author Dilly King
 * 
 */
public abstract class MatrixReader<E> extends DataReader
{
	public static String						PathBase			= Tools.getPathOfClass(MatrixReader.class)
																			+ "matrix/";

	public static String						FileSuffix			= ".mtrx";

	public static final MatrixReader<Integer>	IntegerMatrixReader	= new MatrixReader<Integer>() {

																		@Override
																		protected Integer convertStringToElement(
																				String elementString)
																		{
																			return Integer.valueOf(elementString);
																		}
																	};

	public static final MatrixReader<Double>	DoubleMatrixReader	= new MatrixReader<Double>() {

																		@Override
																		protected Double convertStringToElement(
																				String elementString)
																		{
																			return Double.valueOf(elementString);
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
		MatrixReader<Integer> reader = new MatrixReader<Integer>() {

			@Override
			protected Integer convertStringToElement(String elementString)
			{
				return Integer.valueOf(elementString);
			}

		};

		try
		{
			reader.setDataFile(new File("./dat/blosum62.mtrx"));
			reader.read();
			Tools.debug(reader.getMatrix());

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

	public static final Matrix<Double> readDoubleMatrix(File file) throws IOException
	{
		DoubleMatrixReader.setDataFile(file);
		DoubleMatrixReader.read();

		return DoubleMatrixReader.getMatrix();
	}

	public static final Matrix<Integer> readIntegerMatrix(File file) throws IOException
	{
		IntegerMatrixReader.setDataFile(file);
		IntegerMatrixReader.read();

		return IntegerMatrixReader.getMatrix();
	}

	private File		matrixFile;

	private Pattern		separatorPattern	= Pattern.compile("\\t");

	private int			row;

	private Matrix<E>	matrix;

	protected abstract E convertStringToElement(String elementString);

	public Matrix<E> getMatrix()
	{
		return matrix;
	}

	public File getMatrixFile()
	{
		return matrixFile;
	}

	public String getSeparatorRegex()
	{
		return separatorPattern.pattern();
	}

	@Override
	protected void readFinished()
	{

	}

	@Override
	protected void readLine(CharSequence line)
	{
		if (row == -1)
		{
			String[] pair = separatorPattern.split(line);
			if (pair.length != 2)
			{
				throw new RuntimeException("The size of matrix has not been defined correctly in the file.\n"
						+ "Check the first line in " + matrixFile.getAbsolutePath());
			}
			else
			{

				int rows = Integer.parseInt(pair[0]);
				int columns = Integer.parseInt(pair[1]);
				if (matrix == null)
				{
					matrix = new Matrix<E>(new Size(rows, columns));
				}
				else
				{
					Matrix.VerifyLimits(matrix, rows, columns);
				}

				row = 0;
			}
		}
		else
		{
			String[] pair = separatorPattern.split(line, matrix.getColumns());
			for (int column = 0; column < matrix.getColumns(); column++)
			{
				matrix.set(convertStringToElement(pair[column]), row, column);
			}
			row++;
		}
	}

	@Override
	protected void readPrepare()
	{
		row = -1;
		matrix = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataReader setDataFile(File file) throws IOException
	{
		super.setDataFile(file);
		this.matrixFile = file;
		return this;
	}

	public void setMatrix(Matrix<E> matrix)
	{
		this.matrix = matrix;
	}

	public void setSeparatorRegex(String separatorRegex)
	{
		separatorPattern = Pattern.compile(separatorRegex);
	}

}
