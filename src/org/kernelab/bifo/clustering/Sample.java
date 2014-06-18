package org.kernelab.bifo.clustering;

public interface Sample extends Clusterable
{
	public double distance(Sample sample);
}
