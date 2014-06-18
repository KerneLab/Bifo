package org.kernelab.bifo.hmm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.kernelab.numeric.Real;
import org.kernelab.numeric.matrix.Position;

public class Forward<S, O>
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private HiddenMarkovModel<S, O>	hmm;

	private LinkedList<O>			reverse			= new LinkedList<O>();

	private int						current;

	private List<RealVector>		stateSequence	= new ArrayList<RealVector>();

	public RealVector forward(RealVector initialProbability, List<O> observedSequence)
	{
		reverse.clear();

		int lacuna = observedSequence.size() - stateSequence.size() + 1;
		for (int l = 0; l < lacuna; l++) {
			stateSequence.add(new RealVector(hmm.getStatesNumber()));
		}
		while (lacuna < 0) {
			stateSequence.remove(stateSequence.size() - 1);
			lacuna++;
		}

		RealVector initial = stateSequence.get(0);
		for (int column = Position.FIRST; column < initial.size(); column++) {
			initial.set(initialProbability.get(column), column);
		}

		current = 0;
		RealVector statesProbability = initialProbability;
		for (O observation : observedSequence) {
			reverse.addFirst(observation);
			current++;
			statesProbability = forward(statesProbability, observation);
		}

		observedSequence.clear();
		observedSequence.addAll(reverse);

		return statesProbability;
	}

	public RealVector forward(RealVector statesProbability, O observation)
	{
		RealVector current = stateSequence.get(this.current);

		current.product(statesProbability, hmm.getTransitionMatrix());

		int signal = hmm.getIndexOfSignal(observation);

		int states = hmm.getStatesNumber();
		for (int state = Position.FIRST; state < states; state++) {
			current.get(state).value *= hmm.getEmissionMatrix().get(state, signal).value;
		}
		// current.normalize();

		return current;
	}

	public double getProbability()
	{
		double sum = 0.0;
		RealVector last = stateSequence.get(stateSequence.size() - 1);
		for (Real real : last) {
			sum += real.value;
		}
		return sum;
	}

	public List<RealVector> getStateSequence()
	{
		return stateSequence;
	}

	public void setHmm(HiddenMarkovModel<S, O> hmm)
	{
		this.hmm = hmm;
	}
}
