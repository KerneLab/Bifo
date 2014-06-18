package org.kernelab.bifo.hmm;

import java.util.Collection;
import java.util.List;

import org.kernelab.numeric.matrix.Position;
import org.kernelab.numeric.matrix.Range;

public class Collector<S, O>
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private HiddenMarkovModel<S, O>	hmm;

	public void collect(List<S> estimatedStateSequence, Collection<List<O>> observations)
	{
		this.initialize();

		int lastStateIndex = -1;
		for (S state : estimatedStateSequence) {
			int stateIndex = hmm.getIndexOfState(state);
			if (lastStateIndex != -1) {
				hmm.getTransitionMatrix().get(lastStateIndex, stateIndex).value++;
			}
			lastStateIndex = stateIndex;
		}

		RealVector rowOfTransition = new RealVector(hmm.getStatesNumber());
		Range range = Range.Row(hmm.getTransitionMatrix(), Position.FIRST);
		for (int row = Position.FIRST; row < hmm.getStatesNumber(); row++) {
			rowOfTransition.quote(hmm.getTransitionMatrix(), range);
			rowOfTransition.normalize();
			range.getBegin().row++;
			range.getEnd().row++;
		}

		for (List<O> observation : observations) {
			int index = 0;
			for (O signal : observation) {
				S state = estimatedStateSequence.get(index);
				hmm.getEmissionMatrix().get(hmm.getIndexOfState(state),
						hmm.getIndexOfSignal(signal)).value++;
				index++;
			}
		}

		RealVector rowOfEmission = new RealVector(hmm.getStatesNumber());
		range = Range.Row(hmm.getEmissionMatrix(), Position.FIRST);
		for (int row = Position.FIRST; row < hmm.getStatesNumber(); row++) {
			rowOfEmission.quote(hmm.getEmissionMatrix(), range);
			rowOfEmission.normalize();
			range.getBegin().row++;
			range.getEnd().row++;
		}
	}

	private void initialize()
	{
		for (int row = Position.FIRST; row < hmm.getStatesNumber(); row++) {
			for (int column = Position.FIRST; column < hmm.getStatesNumber(); column++) {
				hmm.getTransitionMatrix().set(0.0, row, column);
			}
		}
		for (int row = Position.FIRST; row < hmm.getStatesNumber(); row++) {
			for (int column = Position.FIRST; column < hmm.getSignalsNumber(); column++) {
				hmm.getEmissionMatrix().set(0.0, row, column);
			}
		}
	}

	public void setHmm(HiddenMarkovModel<S, O> hmm)
	{
		this.hmm = hmm;
	}

}
