package org.kernelab.bifo.fasta;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.kernelab.basis.Tools;
import org.kernelab.basis.Variable;
import org.kernelab.bifo.util.MapReader;

public class FrequencyCounter
{

	public class Result extends TreeMap<Integer, Variable<Double>>
	{

		/**
		 * 
		 */
		private static final long	serialVersionUID	= 6724443991886468856L;

		public Result()
		{
			super();
			clear();
		}

		@Override
		public void clear()
		{
			super.clear();

			int radix = category.size();

			int total = 1;

			for (int i = 0; i < view; i++)
			{
				total *= radix;
			}

			for (int i = 1; i <= total; i++)
			{
				this.put(i, Variable.newInstance(0.0));
			}
		}

		public void count(CharSequence code)
		{
			FrequencyCounter.this.count(code, this);
		}

		public String joint(Result result)
		{
			return JointResult(this, result);
		}

		public Result normalize()
		{
			double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;

			for (Variable<Double> count : this.values())
			{
				if (count.value < min)
				{
					min = count.value;
				}
				if (count.value > max)
				{
					max = count.value;
				}
			}

			max -= min;

			for (Variable<Double> count : this.values())
			{
				count.value = (count.value - min) / max;
			}

			return this;
		}

		public void reset()
		{
			for (Variable<Double> count : this.values())
			{
				count.value = 0.0;
			}
		}

		@Override
		public String toString()
		{
			return MakeResult(this, buffer).toString();
		}
	}

	public static final String					DEFAULT_CLASSIFIER_NAME	= "AminoAcidClassifier";

	public static final Map<Character, Integer>	DEFAULT_CLASSIFIER		= new HashMap<Character, Integer>();

	static
	{
		try
		{
			MapReader.readCharacterIntegerMap(MapReader.getMapFile(DEFAULT_CLASSIFIER_NAME), DEFAULT_CLASSIFIER);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static final String JointResult(Result a, Result b)
	{
		return JointResult(a, b, new StringBuilder());
	}

	public static final String JointResult(Result a, Result b, StringBuilder buffer)
	{
		MakeResult(a, buffer);
		MakeResult(b, buffer, a.size());

		return buffer.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		FrequencyCounter counter = new FrequencyCounter();
		counter.setClassifier(DEFAULT_CLASSIFIER);

		Result ra = counter.newResult();
		Result rb = counter.newResult();

		counter.count(
				"MIVTTSPNIEGKQIIEYKKIVFGEVITGVNFMKDIGAGLRNFFGGRSQGYEDELINAREEAIREMESRAKDIGANAVIGVDIDYEVLGADNGMLMVTASGTAVVIEVQDY",
				ra);
		counter.count("MAKANEHFFYVLKCNDNSYYGGYTTDVTRREAEHNAGIRCKYTKTRRPVKVIHFEKFETRSEATKAEAAFKKLSRKNKDSYLIEREEDSE", rb);

		Tools.debug(ra.normalize().toString());
		Tools.debug(rb.normalize().toString());

		Tools.debug(ra.joint(rb));
	}

	public static final StringBuilder MakeResult(Result result)
	{
		return MakeResult(result, new StringBuilder());
	}

	public static final StringBuilder MakeResult(Result result, StringBuilder buffer)
	{
		return MakeResult(result, buffer, 0);
	}

	public static final StringBuilder MakeResult(Result result, StringBuilder buffer, int offset)
	{
		if (offset == 0)
		{
			buffer.delete(0, buffer.length());
		}

		for (Entry<Integer, Variable<Double>> entry : result.entrySet())
		{
			buffer.append(entry.getKey() + offset);
			buffer.append(':');
			buffer.append(entry.getValue());
			buffer.append(' ');
		}

		return buffer;
	}

	private Set<Integer>			category	= new HashSet<Integer>();

	private Map<Character, Integer>	classifier	= new HashMap<Character, Integer>();

	private int						view		= 3;

	private Result					cache		= new Result();

	private StringBuilder			buffer		= new StringBuilder();

	public Result count(CharSequence code)
	{
		return count(code, cache);
	}

	public Result count(CharSequence code, Result result)
	{
		int radix = category.size();

		int total = code.length() - view + 1;

		result.reset();

		char[] views = new char[view];

		for (int index = 0; index < total; index++)
		{

			boolean legal = true;
			for (int v = 0; v < view; v++)
			{
				views[v] = code.charAt(index + v);
				if (!classifier.containsKey(views[v]))
				{
					legal = false;
				}
			}

			if (legal)
			{
				buffer.delete(0, buffer.length());

				for (int v = 0; v < view; v++)
				{
					buffer.append(classifier.get(views[v]) - 1);
				}

				int id = Integer.parseInt(buffer.toString(), radix) + 1;

				Variable<Double> count = result.get(id);
				if (count == null)
				{
					count = Variable.newInstance(0.0);
					result.put(id, count);
				}
				count.value++;
			}
		}

		return result;
	}

	public Map<Character, Integer> getClassifier()
	{
		return classifier;
	}

	public int getView()
	{
		return view;
	}

	public Result newResult()
	{
		return new Result();
	}

	public void setClassifier(Map<Character, Integer> classifier)
	{
		this.classifier.clear();
		this.classifier.putAll(classifier);
		category.clear();
		category.addAll(classifier.values());
		cache.clear();
	}

	public void setView(int view)
	{
		this.view = view;
		cache.clear();
	}
}
