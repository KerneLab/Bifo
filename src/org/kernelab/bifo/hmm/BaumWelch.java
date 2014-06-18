package org.kernelab.bifo.hmm;

import java.util.ArrayList;
import java.util.List;

import org.kernelab.numeric.Real;
import org.kernelab.numeric.matrix.Position;
import org.kernelab.numeric.matrix.RealMatrix;

public class BaumWelch<S, O>
{

	public static double	ERROR	= 0.001;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private HiddenMarkovModel<S, O>	hmm;

	private Forward<S, O>			forward			= new Forward<S, O>();

	private Backward<S, O>			backward		= new Backward<S, O>();

	private List<RealVector>		stateSequence	= new ArrayList<RealVector>();

	private double					probability;

	private RealMatrix				transitionMatrix;

	private RealVector				stateSummary;

	public void calculate(RealVector initialProbability, List<O> observedSequence)
	{
		double last = 0;
		do {
			last = this.probability;

			this.recur(initialProbability, observedSequence);

		} while (this.probability - last > ERROR);
	}

	public double getProbability()
	{
		return probability;
	}

	public List<RealVector> getStateSequence()
	{
		return stateSequence;
	}

	public void recur(RealVector initialProbability, List<O> observedSequence)
	{
		if (initialProbability == null) {
			initialProbability = stateSequence.get(1);
		}

		forward.forward(initialProbability, observedSequence);
		backward.backward(observedSequence);

		int states = hmm.getStatesNumber();
		int lacuna = observedSequence.size() - stateSequence.size() + 1;
		for (int l = 0; l < lacuna; l++) {
			stateSequence.add(new RealVector(states));
		}
		while (lacuna < 0) {
			stateSequence.remove(stateSequence.size() - 1);
			lacuna++;
		}

		this.probability = 0.0;
		for (int time = Position.FIRST; time <= observedSequence.size(); time++) {
			RealVector current = stateSequence.get(time);
			RealVector forward = this.forward.getStateSequence().get(time);
			RealVector backward = this.backward.getStateSequence().get(time);

			for (int column = Position.FIRST; column < states; column++) {
				Real probability = current.get(column);
				if (probability == null) {
					probability = new Real(0.0);
					current.set(probability, column);
				}
				probability.value = forward.get(column).value
						* backward.get(column).value;
				if (time == observedSequence.size()) {
					this.probability += probability.value;
				}
			}
			current.normalize();
		}

		// Make temporary TransitionMatrix Sigma t:1~T-1
		for (int time = 1; time < observedSequence.size(); time++) {

			O signal = observedSequence.get(time);
			RealVector forward = this.forward.getStateSequence().get(time);
			RealVector backward = this.backward.getStateSequence().get(time);

			double probability = 0.0;
			for (int state = 0; state < states; state++) {
				probability += forward.get(state).value * backward.get(state).value;
			}

			backward = this.backward.getStateSequence().get(time + 1);

			for (int row = Position.FIRST; row < transitionMatrix.getRows(); row++) {
				for (int column = Position.FIRST; column < transitionMatrix.getColumns(); column++)
				{
					double value = forward.get(row).value
							* hmm.getTransitionMatrix().get(row, column).value
							* hmm.getEmissionMatrix().get(column,
									hmm.getIndexOfSignal(signal)).value
							* backward.get(column).value;
					value /= probability;
					if (time == 1) {
						transitionMatrix.set(value, row, column);
					} else {
						transitionMatrix.get(row, column).value += value;
					}
				}
				if (time == 1) {
					stateSummary.set(stateSequence.get(time).get(row).value, row);
				} else {
					stateSummary.get(row).value += stateSequence.get(time).get(row).value;
				}
			}
		}

		// Refresh TransitionMatrix in HMM.
		for (int row = Position.FIRST; row < hmm.getTransitionMatrix().getRows(); row++) {
			for (int column = Position.FIRST; column < hmm.getTransitionMatrix()
					.getColumns(); column++)
			{
				hmm.getTransitionMatrix().set(
						transitionMatrix.get(row, column).value
								/ stateSummary.get(row).value, row, column);
			}
		}

		for (int state = Position.FIRST; state < states; state++) {
			stateSummary.get(state).value += stateSequence.get(observedSequence.size())
					.get(state).value;
		}

		// Refresh EmissionMatrix in HMM.
		for (int row = Position.FIRST; row < hmm.getStatesNumber(); row++) {
			for (int column = Position.FIRST; column < hmm.getSignalsNumber(); column++) {
				O signal = hmm.getSignalOfIndex(column);
				double sum = 0.0;
				for (int time = 0; time < observedSequence.size(); time++) {
					O ob = observedSequence.get(time);
					if (ob.equals(signal)) {
						sum += stateSequence.get(time + 1).get(row).value;
					}
				}
				hmm.getEmissionMatrix().set(sum / stateSummary.get(row).value, row,
						column);
			}
		}
	}

	public void setHmm(HiddenMarkovModel<S, O> hmm)
	{
		this.hmm = hmm;
		forward.setHmm(hmm);
		backward.setHmm(hmm);
		transitionMatrix = new RealMatrix(hmm.getStatesNumber(), hmm.getStatesNumber());
		stateSummary = new RealVector(hmm.getStatesNumber());
	}

}
