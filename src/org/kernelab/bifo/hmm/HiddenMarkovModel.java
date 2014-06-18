package org.kernelab.bifo.hmm;

import java.util.Collection;

import org.kernelab.numeric.matrix.RealMatrix;
import org.kernelab.numeric.matrix.Size;

public class HiddenMarkovModel<S, O>
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Indexer<S>	statesIndexer;

	private RealMatrix	transitionMatrix;

	private Indexer<O>	signalsIndexer;

	private RealMatrix	emissionMatrix;

	public HiddenMarkovModel(Collection<S> states, Collection<O> signals)
	{
		this(states.size(), signals.size());

		for (S s : states) {
			statesIndexer.addIndexer(s);
		}

		for (O o : signals) {
			signalsIndexer.addIndexer(o);
		}
	}

	public HiddenMarkovModel(int statesNumber, int signalsNumber)
	{
		statesIndexer = new Indexer<S>();
		transitionMatrix = new RealMatrix(new Size(statesNumber, statesNumber));
		signalsIndexer = new Indexer<O>();
		emissionMatrix = new RealMatrix(new Size(statesNumber, signalsNumber));
	}

	public RealMatrix getEmissionMatrix()
	{
		return emissionMatrix;
	}

	public int getIndexOfSignal(O signal)
	{
		return signalsIndexer.getIndex(signal);
	}

	public int getIndexOfState(S state)
	{
		return statesIndexer.getIndex(state);
	}

	public O getSignalOfIndex(int index)
	{
		return signalsIndexer.getIndexer(index);
	}

	public Indexer<O> getSignalsIndexer()
	{
		return signalsIndexer;
	}

	public int getSignalsNumber()
	{
		return signalsIndexer.size();
	}

	public S getStateOfIndex(int index)
	{
		return statesIndexer.getIndexer(index);
	}

	public Indexer<S> getStatesIndexer()
	{
		return statesIndexer;
	}

	public int getStatesNumber()
	{
		return statesIndexer.size();
	}

	public RealMatrix getTransitionMatrix()
	{
		return transitionMatrix;
	}

	protected void setEmissionMatrix(RealMatrix emissionMatrix)
	{
		this.emissionMatrix = emissionMatrix;
	}

	protected void setSignalsIndexer(Indexer<O> signalsIndexer)
	{
		this.signalsIndexer = signalsIndexer;
	}

	protected void setStatesIndexer(Indexer<S> statesIndexer)
	{
		this.statesIndexer = statesIndexer;
	}

	protected void setTransitionMatrix(RealMatrix transitionMatrix)
	{
		this.transitionMatrix = transitionMatrix;
	}
}
