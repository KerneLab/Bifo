package org.kernelab.bifo.uniprot.liver;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.kernelab.basis.Accomplishable.AccomplishListener;
import org.kernelab.basis.Relation;
import org.kernelab.basis.Tools;
import org.kernelab.basis.io.DataReader;
import org.kernelab.basis.io.DataWriter;
import org.kernelab.basis.io.FilesFilter;
import org.kernelab.basis.io.FilesFilter.SingleFilter;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.AppBifo;
import org.kernelab.bifo.interact.Interact;
import org.kernelab.bifo.interact.InteractDisplayer;
import org.kernelab.bifo.interact.InteractDisplayer.ItemEdge;
import org.kernelab.bifo.interact.InteractPredictor;

public class Liver
{

	public static final Map<String, LiverSwissProtItem>	SwissProtItems	= new Hashtable<String, LiverSwissProtItem>();

	public static final Set<Relation<String, String>> LoadInteractRelation(Collection<String> cores,
			Set<Relation<String, String>> relation)
	{
		if (relation == null)
		{
			relation = new LinkedHashSet<Relation<String, String>>();
		}
		else
		{
			relation.clear();
		}

		SQLKit kit = null;

		try
		{
			kit = Interact.DATABASE.getSQLKit();
			for (String id : cores)
			{

				Set<String> interact = Interact.QueryInteractLigands(id, kit);

				for (String l : interact)
				{
					if (Liver.SwissProtItems.containsKey(l))
					{
						relation.add(new Relation<String, String>(id, l));
					}
				}

			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
		}

		return relation;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	public static LiverSwissProtItem QueryLiverSwissProtItem(String id)
	{
		return SwissProtItems.get(id);
	}

	private AppBifo					bifo;

	private LiverReader				reader;

	// Menus
	private JMenuItem				readLiverDataMenu;
	// Setting
	private JTextField				interactUpperRankText;
	private JTextField				interactLowerRankText;
	private JButton					interactFindButton;
	private JButton					interactReDisplayButton;
	private JButton					exportInteractButton;
	private InteractPredictor		predictor;
	private JButton					predictButton;
	private JButton					saveGraphButton;

	private JPanel					interactSettingPanel;

	private LiverInteractFinder		finder;

	// Displayer
	private LiverInteractDisplayer	interactDisplayer;

	private JPanel					interactPanel;

	public Liver(AppBifo bifo)
	{
		this.bifo = bifo;

		this.reader = new LiverReader();

		this.readLiverDataMenu = new JMenuItem("打开肝脏蛋白质数据(L)");

		this.interactUpperRankText = new JTextField("1");

		this.interactLowerRankText = new JTextField("10");

		this.interactFindButton = new JButton("构建");

		this.interactReDisplayButton = new JButton("重构");

		this.exportInteractButton = new JButton("导出");

		this.predictor = new InteractPredictor();

		this.predictButton = new JButton("预测");

		this.saveGraphButton = new JButton("保存图像");

		this.interactSettingPanel = new JPanel();

		this.finder = new LiverInteractFinder();

		this.interactDisplayer = new LiverInteractDisplayer();

		this.interactPanel = new JPanel();

		this.config();

		this.arrange();
	}

	public void actionExportInteract()
	{
		JFileChooser fc = new JFileChooser(AppBifo.Last_File_Location);
		fc.setDragEnabled(true);
		int result = fc.showSaveDialog(bifo);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			AppBifo.Last_File_Location = fc.getSelectedFile().getParent();
			DataWriter writer = new DataWriter();
			try
			{

				Set<ItemEdge> centers = new HashSet<ItemEdge>();
				Set<ItemEdge> bridges = new HashSet<ItemEdge>();

				for (ItemEdge e : interactDisplayer.getEdges().values())
				{
					if (e.getA().getPeriphery().size() == 1 || e.getB().getPeriphery().size() == 1)
					{
						centers.add(e);
					}
					else
					{
						bridges.add(e);
					}
				}
				writer.setDataFile(fc.getSelectedFile());

				writer.write("Centers");
				for (ItemEdge e : centers)
				{
					writer.write(e.getA().getItem().getId() + "\t" + e.getB().getItem().getId());
				}
				writer.write();
				writer.write("Bridges");
				for (ItemEdge e : bridges)
				{
					writer.write(e.getA().getItem().getId() + "\t" + e.getB().getItem().getId());
				}

				JOptionPane.showMessageDialog(bifo, "图中的相互作用数据已导出", "完成", JOptionPane.INFORMATION_MESSAGE);

			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(bifo, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
			}
			finally
			{
				writer.close();
			}
		}
	}

	public void actionFindInteraction()
	{
		try
		{
			finder.setUpperRank(Integer.parseInt(interactUpperRankText.getText()));
			finder.setLowerRank(Integer.parseInt(interactLowerRankText.getText()));
			finder.resetAccomplishStatus();
			new Thread(finder).start();
			interactFindButton.setEnabled(false);
			interactReDisplayButton.setEnabled(false);
			exportInteractButton.setEnabled(false);
			predictButton.setEnabled(false);
			saveGraphButton.setEnabled(false);

		}
		catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(bifo, "输入的数字格式不正确", "参数格式", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void actionPredictInteract()
	{
		predictor.setTargets(interactDisplayer.getBridgeNodes());
		predictor.setInteract(interactDisplayer.getInteractMap());
		predictor.resetAccomplishStatus();
		new Thread(predictor).start();
	}

	public void actionReadLiverDataFile()
	{
		JFileChooser fc = new JFileChooser(AppBifo.Last_File_Location);
		fc.setDragEnabled(true);
		int result = fc.showOpenDialog(bifo);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			AppBifo.Last_File_Location = fc.getSelectedFile().getParent();
			try
			{
				reader.setDataFile(fc.getSelectedFile());
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(bifo, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
			}
			new Thread(reader).start();
		}
	}

	public void actionSaveGraph()
	{
		JFileChooser fc = new JFileChooser(AppBifo.Last_File_Location);

		FilesFilter ff = new FilesFilter();
		ff.addExtension("png", "PNG");
		// ff.addExtension("jpg", "JPEG");
		ff.addExtension("gif", "GIF");

		ff.attachToFileChooser(fc);

		fc.setDragEnabled(true);
		int result = fc.showSaveDialog(bifo);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fc.getSelectedFile();
			AppBifo.Last_File_Location = selectedFile.getParent();
			FileFilter f = fc.getFileFilter();
			if (!f.accept(selectedFile))
			{
				if (f instanceof SingleFilter)
				{
					SingleFilter sf = (SingleFilter) f;
					selectedFile = new File(selectedFile.getAbsoluteFile() + "." + sf.extension);
				}
			}
			interactDisplayer.exportGraphicsToFile(selectedFile);
		}
	}

	private void arrange()
	{
		this.arrangeSettingPanel();

		interactPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 0.0;
		interactPanel.add(interactSettingPanel, gbc);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		interactPanel.add(interactDisplayer.getCanvas(), gbc);
	}

	private void arrangeSettingPanel()
	{
		interactSettingPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();

		gbc.insets = new Insets(1, 2, 1, 2);

		gbc.gridx++;
		gbc.weightx = 0.0;
		interactSettingPanel.add(new JLabel("Rank"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.ipadx = 40;
		interactSettingPanel.add(interactUpperRankText, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.ipadx = 0;
		interactSettingPanel.add(new JLabel(":"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.ipadx = 40;
		interactSettingPanel.add(interactLowerRankText, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.ipadx = 0;
		interactSettingPanel.add(interactFindButton, gbc);

		gbc.gridx++;
		interactSettingPanel.add(interactReDisplayButton, gbc);

		gbc.gridx++;
		interactSettingPanel.add(exportInteractButton, gbc);

		gbc.gridx++;
		interactSettingPanel.add(predictButton, gbc);

		gbc.gridx++;
		interactSettingPanel.add(saveGraphButton, gbc);
	}

	private void config()
	{
		this.configReader();
		this.configMenu();
		this.configInteractFinder();
		this.configDisplayer();
		this.configPredictor();
	}

	private void configDisplayer()
	{
		interactDisplayer.setProgress(bifo);

		interactDisplayer.addAccomplishedListener(new AccomplishListener<InteractDisplayer>() {

			@Override
			public void accomplish(InteractDisplayer e)
			{
				doneDisplayerInteraction();
			}
		});
	}

	private void configInteractFinder()
	{
		interactSettingPanel.setBorder(new TitledBorder("Setting"));

		finder.setProgress(bifo);

		finder.addAccomplishedListener(new AccomplishListener<LiverInteractFinder>() {

			@Override
			public void accomplish(LiverInteractFinder e)
			{
				doneFindInteraction();
			}
		});

		interactFindButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionFindInteraction();
			}

		});

		interactReDisplayButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				doneFindInteraction();
			}

		});

		exportInteractButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionExportInteract();
			}

		});

		saveGraphButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionSaveGraph();
			}

		});

	}

	private void configMenu()
	{
		this.readLiverDataMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionReadLiverDataFile();
			}

		});
	}

	private void configPredictor()
	{
		this.predictor.setProgress(bifo);
		this.predictor.addAccomplishedListener(new AccomplishListener<InteractPredictor>() {

			@Override
			public void accomplish(InteractPredictor e)
			{
				donePredictInteract();
			}
		});

		this.predictButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionPredictInteract();
			}

		});
	}

	private void configReader()
	{
		reader.setProgress(bifo);

		reader.addAccomplishedListener(new AccomplishListener<DataReader>() {

			@Override
			public void accomplish(DataReader e)
			{
				doneReadLiverDataFile();
			}
		});
	}

	private void doneDisplayerInteraction()
	{
		interactFindButton.setEnabled(true);
		interactReDisplayButton.setEnabled(true);
		exportInteractButton.setEnabled(true);
		predictButton.setEnabled(true);
		saveGraphButton.setEnabled(true);
	}

	public void doneFindInteraction()
	{
		// Collection<String> cores = new HashSet<String>();
		// for (Collection<String> result : finder.getResult().values()) {
		// cores.addAll(result);
		// }
		// interactDisplayer.setData(cores);
		// interactDisplayer.setRelation(LoadInteractRelation(cores, null));

		interactDisplayer.setRelation(finder.getRelation());
		new Thread(interactDisplayer).start();
	}

	private void donePredictInteract()
	{

		interactDisplayer.setPredicts(predictor.getPredicts());
	}

	public void doneReadLiverDataFile()
	{
		JOptionPane.showMessageDialog(bifo, "肝脏蛋白质数据已被成功导入", "导入成功", JOptionPane.INFORMATION_MESSAGE);
	}

	public JButton getExportInteractButton()
	{
		return exportInteractButton;
	}

	public LiverInteractFinder getFinder()
	{
		return finder;
	}

	public LiverInteractDisplayer getInteractDisplayer()
	{
		return interactDisplayer;
	}

	public JButton getInteractFindButton()
	{
		return interactFindButton;
	}

	public JTextField getInteractLowerRankText()
	{
		return interactLowerRankText;
	}

	public JPanel getInteractPanel()
	{
		return interactPanel;
	}

	public JButton getInteractReDisplayButton()
	{
		return interactReDisplayButton;
	}

	public JPanel getInteractSettingPanel()
	{
		return interactSettingPanel;
	}

	public JTextField getInteractUpperRankText()
	{
		return interactUpperRankText;
	}

	public JButton getPredictButton()
	{
		return predictButton;
	}

	public InteractPredictor getPredictor()
	{
		return predictor;
	}

	public LiverReader getReader()
	{
		return reader;
	}

	public JMenuItem getReadLiverDataMenu()
	{
		return readLiverDataMenu;
	}

	public JButton getSaveGraphButton()
	{
		return saveGraphButton;
	}

}
