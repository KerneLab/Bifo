package org.kernelab.bifo.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

import org.kernelab.basis.ColorIndexer;
import org.kernelab.basis.Reducer;
import org.kernelab.basis.Tools;
import org.kernelab.numeric.matrix.Matrix;

public class GridChart<T extends Number>
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Matrix<T>	data;

	private JPanel		canvas;

	private Point		gridSize	= new Point();

	public GridChart(Matrix<T> data)
	{
		this.data = data;
		this.canvas = new JPanel() {

			/**
			 * 
			 */
			private static final long	serialVersionUID	= -1475601928038538543L;

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);

				drawData(g);
			}
		};
		this.setGridSize(1, 1);
	}

	private void drawData(Graphics g)
	{
		double[] result = Tools.reduce(data, new Reducer<T, double[]>() {

			public double[] reduce(double[] r, Number e)
			{
				if (e != null) {
					int v = e.intValue();
					if (v > r[0]) {
						r[0] = v;
					}
					if (0 < v && v < r[1]) {
						r[1] = v;
					}
				}
				return r;
			}

		}, new double[] { -Double.MAX_VALUE, Double.MAX_VALUE });

		double max = result[0];
		double min = result[1];

		max -= min;

		for (int row = 0; row < data.getRows(); row++) {
			for (int column = 0; column < data.getColumns(); column++) {
				Number number = data.get(row, column);
				if (number == null) {
					g.setColor(Color.WHITE);
				} else {
					double ratio = 1.0 * (number.intValue() - min) / max;
					g.setColor(ratio == 0.0 ? Color.WHITE : ColorIndexer
							.getColorRedBlueBounded(ratio));
				}
				g.fillRect(column * gridSize.x, row * gridSize.y, gridSize.x, gridSize.y);
			}
		}
	}

	public JPanel getCanvas()
	{
		return canvas;
	}

	public void setGridSize(int x, int y)
	{
		gridSize.x = x;
		gridSize.y = y;
		canvas.setPreferredSize(new Dimension(gridSize.x * data.getColumns(), gridSize.y
				* data.getRows()));
	}

}
