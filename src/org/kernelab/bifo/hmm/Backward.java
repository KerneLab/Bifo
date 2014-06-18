package org.kernelab.bifo.hmm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.kernelab.numeric.Real;
import org.kernelab.numeric.matrix.Position;

public class Backward<S, O>
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private HiddenMarkovModel<S, O>	hmm;

	private LinkedList<O>			reverse			= new LinkedList<O>();

	private RealVector				tempVector;

	private int						current;

	private List<RealVector>		stateSequence	= new ArrayList<RealVector>();

	public RealVector backward(List<O> observedSequence)
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

		current = observedSequence.size();
		RealVector statesProbability = stateSequence.get(current);
		for (int column = Position.FIRST; column < statesProbability.getColumns(); column++)
		{
			statesProbability.set(1.0, column);
		}
		for (O observation : observedSequence) {
			reverse.addFirst(observation);
			current--;
			statesProbability = backward(statesProbability, observation);
		}

		observedSequence.clear();
		observedSequence.addAll(reverse);

		return statesProbability;
	}

	public RealVector backward(RealVector statesProbability, O observation)
	{
		RealVector current = stateSequence.get(this.current);

		int signal = hmm.getIndexOfSignal(observation);

		int states = hmm.getStatesNumber();
		for (int state = Position.FIRST; state < states; state++) {
			current.set(
					statesProbability.get(state).value
							* hmm.getEmissionMatrix().get(state, signal).value, state);
		}

		double sum = 0.0;
		for (int row = Position.FIRST; row < states; row++) {
			sum = 0.0;
			for (int column = Position.FIRST; column < states; column++) {
				sum += hmm.getTransitionMatrix().get(row, column).value
						* current.get(column).value;
			}
			tempVector.set(sum, row);
		}

		current.clone(tempVector);
		// current.normalize();

		return current;
	}

	public double getProbability()
	{
		double sum = 0.0;
		RealVector last = stateSequence.get(0);
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
		tempVector = new RealVector(hmm.getStatesNumber());
		for (int column = Position.FIRST; column < tempVector.getColumns(); column++) {
			tempVector.set(new Real(0.0), column);
		}
	}
}
