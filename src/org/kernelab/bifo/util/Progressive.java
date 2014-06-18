package org.kernelab.bifo.util;

public interface Progressive
{
	public void nextProgress();

	public void prepareProgress();

	public void resetProgress(int steps);

	public void setProgress(double ratio);
}
