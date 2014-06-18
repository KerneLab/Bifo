package test;

import java.util.Random;

import org.kernelab.basis.Tools;

public class TestExpectation
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Random random = new Random();
		double sum = 0;
		for (int i = 1; i <= 100000; i++) {
			sum += random.nextDouble() * random.nextDouble();
			Tools.debug(sum / i);
		}
	}

}
