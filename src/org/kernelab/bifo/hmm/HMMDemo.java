package org.kernelab.bifo.hmm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.kernelab.basis.Tools;

public class HMMDemo
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// 设置HMM参数
		List<String> states = new LinkedList<String>();
		states.add("雨天");
		states.add("晴天");

		List<String> signals = new LinkedList<String>();
		signals.add("带伞");
		signals.add("不带");

		HiddenMarkovModel<String, String> demo = new HiddenMarkovModel<String, String>(
				states, signals);

		// Double[][] transition = { { 0.7, 0.3 }, { 0.3, 0.7 } };
		// demo.getTransitionMatrix().set(transition);
		// Double[][] emission = { { 0.9, 0.1 }, { 0.2, 0.8 } };
		// demo.getEmissionMatrix().set(emission);

		// 观测序列
		String[][] observation = { { "带伞", "带伞", "不带", "带伞", "带伞" },
				{ "带伞", "带伞", "不带", "不带", "带伞" }, { "带伞", "带伞", "不带", "带伞", "带伞" },
				{ "带伞", "不带", "不带", "带伞", "带伞" } };

		List<List<String>> observations = new LinkedList<List<String>>();
		for (String[] ob : observation) {
			List<String> obs = new ArrayList<String>();
			for (String o : ob) {
				obs.add(o);
			}
			observations.add(obs);
		}

		// // 前向计算 反转观测序列
		// Forward<String, String> forward = new Forward<String, String>();
		// forward.setHmm(demo);
		//
		// RealVector initialProbability = new RealVector(2);
		// initialProbability(new Double[][] { { 0.5, 0.5 } });
		//
		// forward.forward(initialProbability, observedSequence);
		//
		// // 后向计算
		// Backward<String, String> backward = new Backward<String, String>();
		// backward.setHmm(demo);
		//
		// backward.backward(observedSequence);
		//
		// Tools.debug(backward.getStateSequence());

		// 估计状态
		String[] estimated = { "雨天", "雨天", "晴天", "晴天", "雨天" };
		LinkedList<String> estimatedStates = new LinkedList<String>();
		for (String s : estimated) {
			estimatedStates.add(s);
		}

		Collector<String, String> collector = new Collector<String, String>();
		collector.setHmm(demo);
		collector.collect(estimatedStates, observations);

		Tools.debug(demo.getTransitionMatrix());
		Tools.debug(demo.getEmissionMatrix());

		// Baum-Welch
		BaumWelch<String, String> bw = new BaumWelch<String, String>();
		bw.setHmm(demo);

		RealVector initialProbability = new RealVector(2);
		initialProbability.set(new Double[][] { { 0.5, 0.5 } });

		Tools.debug(demo.getTransitionMatrix());
		Tools.debug(demo.getEmissionMatrix());
		Tools.debug("");

		bw.calculate(initialProbability, observations.get(0));
		Tools.debug(demo.getTransitionMatrix());
		Tools.debug(demo.getEmissionMatrix());
		Tools.debug("");

	}

}
