package org.kernelab.bifo.interact;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;

import org.kernelab.basis.Accomplishable.AccomplishListener;
import org.kernelab.basis.Relation;
import org.kernelab.basis.Tools;
import org.kernelab.basis.VectorObjects;
import org.kernelab.basis.io.DataReader;
import org.kernelab.basis.io.DataWriter;
import org.kernelab.basis.io.FilesFilter;
import org.kernelab.basis.io.FilesFilter.SingleFilter;
import org.kernelab.basis.sql.DataBase;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.AppBifo;
import org.kernelab.bifo.fasta.FrequencyCounter;
import org.kernelab.bifo.fasta.FrequencyCounter.Result;
import org.kernelab.bifo.uniprot.UniProt;
import org.kernelab.bifo.uniprot.UniProtItem;
import org.kernelab.graf.table.ListTable;
import org.kernelab.graf.table.ListTableModel;

public class Interact
{

	public static DataBase							DATABASE				= AppBifo.BIFO_DATABASE.clone();

	public static final String						TABLE_NAME				= "interact";

	public static final String						ALL_INTACT_UNIPROT_SQL	= "SELECT DISTINCT `receptor` AS `aiu` FROM `"
																					+ TABLE_NAME + "`";

	public static final Map<String, Set<String>>	Map						= new Hashtable<String, Set<String>>();

	/**
	 * Count the frequency in code by three peptide method.
	 * 
	 * @param flag
	 *            To fig out whether the data in interact file is positive(1) or
	 *            negative(-1). Zero for test data.
	 * @param interactFile
	 *            The interact file which holds some data forms as:
	 * 
	 *            <pre>
	 * UniProtID1	UniProtID2
	 * </pre>
	 * 
	 * @param resultFile
	 *            The result file into which the result will be output.
	 */
	public static void CountCodeFrequencyOfInteract(final int flag, File interactFile, File resultFile,
			Map<Character, Integer> classifier)
	{
		final SQLKit kit = UniProt.DATABASE.getSQLKit();

		FrequencyCounter counter = new FrequencyCounter();

		counter.setClassifier(classifier);

		final Result r = counter.newResult();
		final Result l = counter.newResult();

		final DataWriter writer = new DataWriter();

		DataReader reader = new DataReader() {

			String	prefix	= "";

			{
				if (flag != 0)
				{
					prefix = flag + " ";
				}
			}

			@Override
			protected void readFinished()
			{

			}

			@Override
			protected void readLine(CharSequence line)
			{
				String[] pair = Tools.splitCharSequence(line, "\t");

				if (pair.length == 2)
				{
					try
					{
						UniProtItem ri = UniProt.QueryUniProtItem(pair[0], kit);
						UniProtItem li = UniProt.QueryUniProtItem(pair[1], kit);
						if (ri != null && li != null)
						{
							r.count(ri.getSequenceData());
							l.count(li.getSequenceData());
							writer.write(prefix + r.normalize().joint(l.normalize()));
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void readPrepare()
			{

			}

		};

		try
		{
			writer.setAutoFlush(false);

			writer.setDataFile(resultFile);

			reader.setDataFile(interactFile);

			reader.read();

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
			writer.close();
		}

	}

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

		SQLKit kit = Interact.DATABASE.getSQLKit();

		try
		{
			for (String c : cores)
			{
				for (String p : Interact.QueryInteractLigands(c, kit))
				{
					relation.add(new Relation<String, String>(c, p));
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

	public static Set<Relation<String, String>> parseRelationFile(File file, Set<Relation<String, String>> relation)
	{
		InteractRelationParser parser = new InteractRelationParser();

		try
		{
			parser.setDataFile(file);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		parser.setRelation(relation);

		parser.read();

		return parser.getRelation();
	}

	public static boolean QueryInteractExists(String receptor, String ligand)
	{
		boolean exists = false;

		SQLKit kit = DATABASE.getSQLKit();

		try
		{
			exists = QueryInteractExists(receptor, ligand, kit);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			kit.close();
		}

		return exists;
	}

	public static boolean QueryInteractExists(String receptor, String ligand, SQLKit kit) throws SQLException
	{
		boolean exists = false;

		String sql = "SELECT COUNT(`receptor`) FROM `" + TABLE_NAME + "` WHERE `receptor`=? AND `ligand`=?";

		ResultSet rs = kit.query(sql, receptor, ligand);

		while (rs.next())
		{
			exists = rs.getInt(1) > 0;
			break;
		}

		return exists;
	}

	public static Set<String> QueryInteractLigands(String receptor)
	{
		Set<String> ligands = Map.get(receptor);

		if (ligands == null)
		{

			if (InteractReader.Read)
			{

				ligands = new HashSet<String>();
				Map.put(receptor, ligands);

			}
			else
			{

				SQLKit kit = DATABASE.getSQLKit();

				try
				{
					ligands = QueryInteractLigands(receptor, kit);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				finally
				{
					kit.close();
				}
			}
		}

		return ligands;
	}

	public static Set<String> QueryInteractLigands(String receptor, SQLKit kit) throws SQLException
	{
		Set<String> ligands = Map.get(receptor);

		if (ligands == null)
		{

			ligands = new HashSet<String>();

			if (kit != null)
			{

				String sql = "SELECT DISTINCT `ligand` FROM `" + TABLE_NAME + "` WHERE `receptor`=?";

				ResultSet rs = kit.query(sql, receptor);

				while (rs.next())
				{
					ligands.add(rs.getString("ligand"));
				}
			}

			Map.put(receptor, ligands);
		}

		return ligands;
	}

	private AppBifo						bifo;

	private InteractReader				reader;

	private InteractSerializer			serializer;

	private JMenuItem					readInteractDataMenu;

	private JMenuItem					serializeInteractDataMenu;

	private ListTable<VectorObjects>	interactLigandsList;

	private JScrollPane					interactLigandsPane;

	private InteractDisplayer			interactSingleDisplayer;

	private InteractFinder				finder;

	private JTextField					findSpeciesText;

	private JTextField					findDetailText;

	private JTextField					findUpperRankText;

	private JTextField					findLowerRankText;

	private JButton						findButton;

	private JButton						interactDetailReDisplayButton;

	private JButton						exportRelationButton;

	private InteractPredictor			predictor;

	private JButton						predictButton;

	private JPanel						spanNetworkPanel;

	private JTextField					coreUniProtIdTextField;

	private JButton						spanNetworkButton;

	private JPanel						constructPanel;

	private JTextField					coresNumberTextField;

	private JButton						constructNetworkButton;

	private JButton						interact3DisplayButton;

	private JButton						stereoModeToggleButton;

	private JButton						saveGraphButton;

	private JTextField					importExportRelationFileField;

	private JButton						importRelationButton;

	private JPanel						importExportRelationPanel;

	private InteractDisplayer			interactDetailDisplayer;

	private Interact3Displayer			interact3Displayer;

	private JTabbedPane					displayerPanel;

	private ListTable<VectorObjects>	relationList;

	private JScrollPane					relationPanel;

	private InteractDisplayer			interactLocalDisplayer;

	private JPanel						operationPanel;

	private JPanel						relationListPanel;

	private JPanel						interactFindPanel;

	private SQLKit						kit;

	public Interact(AppBifo bifo)
	{
		this.bifo = bifo;

		this.reader = new InteractReader();

		this.serializer = new InteractSerializer();

		this.readInteractDataMenu = new JMenuItem("打开相互作用数据文件(I)");

		this.serializeInteractDataMenu = new JMenuItem("将相互作用数据导出至数据库(T)");

		this.interactLigandsList = new ListTable<VectorObjects>("Interact");

		this.interactLigandsPane = new JScrollPane(interactLigandsList.getTable());

		this.interactSingleDisplayer = new InteractDisplayer();

		this.finder = new InteractFinder();

		this.findSpeciesText = new JTextField("MOUSE");

		this.findDetailText = new JTextField();

		this.findUpperRankText = new JTextField("1");

		this.findLowerRankText = new JTextField("10");

		this.findButton = new JButton("构建");

		this.interactDetailReDisplayButton = new JButton("重构");

		this.exportRelationButton = new JButton("导出");

		this.predictor = new InteractPredictor();

		this.predictButton = new JButton("预测");

		this.spanNetworkPanel = new JPanel();

		this.coreUniProtIdTextField = new JTextField();

		this.spanNetworkButton = new JButton("扩张");

		this.constructPanel = new JPanel();

		this.coresNumberTextField = new JTextField("6");

		this.constructNetworkButton = new JButton("构建网络");

		this.interact3DisplayButton = new JButton("三维视图");

		this.stereoModeToggleButton = new JButton("双图模式");

		this.saveGraphButton = new JButton("保存图像");

		this.importExportRelationFileField = new JTextField();

		this.importRelationButton = new JButton("导入");

		this.importExportRelationPanel = new JPanel();

		this.interactDetailDisplayer = new InteractDisplayer();

		this.interact3Displayer = new Interact3Displayer();

		this.displayerPanel = new JTabbedPane();

		this.operationPanel = new JPanel();

		this.relationList = new ListTable<VectorObjects>("蛋白质A", "蛋白质B");

		this.relationPanel = new JScrollPane(this.relationList.getTable());

		this.interactLocalDisplayer = new InteractDisplayer();

		this.interactFindPanel = new JPanel();

		this.config();

		this.arrange();
	}

	public void actionExportRelation()
	{
		JFileChooser fc = new JFileChooser(AppBifo.Last_File_Location);
		fc.setDragEnabled(true);
		int result = fc.showSaveDialog(bifo);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fc.getSelectedFile();
			AppBifo.Last_File_Location = selectedFile.getParent();

			Set<Relation<String, String>> relation = interactDetailDisplayer.getRelation();

			DataWriter writer = new DataWriter();

			try
			{
				importExportRelationFileField.setText(selectedFile.getCanonicalPath());
				writer.setDataFile(selectedFile);

				// Set<ItemEdge> centers = new HashSet<ItemEdge>();
				// Set<ItemEdge> bridges = new HashSet<ItemEdge>();
				//
				// for (ItemEdge e :
				// interactDetailDisplayer.getEdges().values()) {
				// if (e.getA().getPeriphery().size() == 1
				// || e.getB().getPeriphery().size() == 1)
				// {
				// centers.add(e);
				// } else {
				// bridges.add(e);
				// }
				// }
				//
				// writer.write("Centers");
				// for (ItemEdge e : centers) {
				// writer.write(e.getA().getItem().getId() + "\t"
				// + e.getB().getItem().getId());
				// }
				// writer.write();
				// writer.write("Bridges");
				// for (ItemEdge e : bridges) {
				// writer.write(e.getA().getItem().getId() + "\t"
				// + e.getB().getItem().getId());
				// }

				for (Relation<String, String> r : relation)
				{
					writer.write(r.getKey() + "\t" + r.getValue());
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
			finder.setSpecies(findSpeciesText.getText());
			finder.setDetail(findDetailText.getText());
			finder.setUpperRank(Integer.parseInt(findUpperRankText.getText()));
			finder.setLowerRank(Integer.parseInt(findLowerRankText.getText()));
			finder.resetAccomplishStatus();
			this.toggleOperationButtons(false);
			new Thread(finder).start();

		}
		catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(bifo, "输入的数字格式不正确", "参数格式", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void actionImportRelation()
	{
		JFileChooser fc = new JFileChooser(AppBifo.Last_File_Location);
		fc.setDragEnabled(true);
		int result = fc.showOpenDialog(bifo);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fc.getSelectedFile();
			AppBifo.Last_File_Location = selectedFile.getParent();

			try
			{
				importExportRelationFileField.setText(selectedFile.getCanonicalPath());

				Set<Relation<String, String>> relation = this.getRelation();
				relation.clear();
				parseRelationFile(selectedFile, relation);
				this.showRelationList(relation);
				coreUniProtIdTextField.setText("");
				JOptionPane.showMessageDialog(bifo, "相互作用关系已经导入", "导入成功", JOptionPane.INFORMATION_MESSAGE);

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void actionInteract3Display()
	{
		interact3DisplayButton.setEnabled(false);
		this.showInteract3DNetwork();
	}

	private void actionPredictInteract()
	{
		predictor.setTargets(interactDetailDisplayer.getBridgeNodes());
		predictor.setInteract(interactDetailDisplayer.getInteractMap());
		predictor.resetAccomplishStatus();
		new Thread(predictor).start();
	}

	public void actionReadInteractDataFile()
	{
		JFileChooser fc = new JFileChooser(AppBifo.Last_File_Location);
		fc.setDragEnabled(true);
		int result = fc.showOpenDialog(bifo);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fc.getSelectedFile();
			AppBifo.Last_File_Location = selectedFile.getParent();
			try
			{
				reader.setDataFile(selectedFile);
			}
			catch (FileNotFoundException e)
			{
				JOptionPane.showMessageDialog(bifo, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
			}
			catch (IOException e)
			{
				e.printStackTrace();
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
			interactDetailDisplayer.exportGraphicsToFile(selectedFile);
		}
	}

	public void actionSerializeInteractData()
	{
		if (JOptionPane.showConfirmDialog(bifo, "确认要将相互作用数据导出至数据库吗？", "确认导出", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		{
			new Thread(serializer).start();
		}
	}

	private void arrange()
	{
		this.arrangeOperationPanel();

		this.arrangeRelationListPanel();

		interactFindPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridwidth = 2;
		gbc.weighty = 0.0;
		interactFindPanel.add(operationPanel, gbc);

		displayerPanel.add("二维网络", this.getInteractDetailDisplayer().getCanvas());
		displayerPanel.add("三维网络", this.getInteract3Displayer().getCanvas());

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		interactFindPanel.add(displayerPanel, gbc);

		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.ipadx = 150;
		interactFindPanel.add(relationListPanel, gbc);

	}

	private void arrangeConstructPanel()
	{
		constructPanel.setBorder(new TitledBorder("操作"));
		constructPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();
		gbc.insets = new Insets(1, 2, 1, 2);

		/*gbc.weightx = 0.0;
		operationPanel.add(new JLabel("Species"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.ipadx = 50;
		operationPanel.add(findSpeciesText, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.ipadx = 0;
		operationPanel.add(new JLabel("Detail"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.ipadx = 70;
		operationPanel.add(findDetailText, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.ipadx = 0;
		operationPanel.add(new JLabel("Rank"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.ipadx = 35;
		operationPanel.add(findUpperRankText, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.ipadx = 0;
		operationPanel.add(new JLabel(":"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.ipadx = 35;
		operationPanel.add(findLowerRankText, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.ipadx = 0;
		operationPanel.add(findButton, gbc);

		gbc.gridx++;
		operationPanel.add(interactDetailReDisplayButton, gbc);

		gbc.gridx++;
		operationPanel.add(predictButton, gbc);
		
		*/

		gbc.weightx = 0.0;
		gbc.ipadx = 0;
		gbc.gridx++;
		constructPanel.add(new JLabel("核心数"), gbc);

		gbc.ipadx = 60;
		gbc.gridx++;
		constructPanel.add(coresNumberTextField, gbc);

		gbc.gridx++;
		gbc.ipadx = 0;
		constructPanel.add(constructNetworkButton, gbc);

		gbc.gridx++;
		constructPanel.add(interact3DisplayButton, gbc);

		gbc.gridx++;
		constructPanel.add(stereoModeToggleButton, gbc);

		// gbc.gridx++;
		// operationPanel.add(saveGraphButton, gbc);
	}

	private void arrangeImportExportRelationPanel()
	{
		importExportRelationPanel.setBorder(new TitledBorder("导入/导出"));
		importExportRelationPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();
		gbc.insets = new Insets(1, 2, 1, 2);

		gbc.ipadx = 180;
		importExportRelationPanel.add(importExportRelationFileField, gbc);

		gbc.ipadx = 0;
		gbc.gridx++;
		importExportRelationPanel.add(importRelationButton, gbc);

		gbc.gridx++;
		importExportRelationPanel.add(exportRelationButton, gbc);
	}

	private void arrangeOperationPanel()
	{
		this.arrangeSpanNetworkPanel();
		this.arrangeImportExportRelationPanel();
		this.arrangeConstructPanel();

		operationPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();

		operationPanel.add(spanNetworkPanel, gbc);

		gbc.gridx++;
		operationPanel.add(importExportRelationPanel, gbc);

		gbc.gridx++;
		operationPanel.add(constructPanel, gbc);

	}

	private void arrangeRelationListPanel()
	{
		relationListPanel = new JPanel();

		relationListPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();

		relationListPanel.add(relationPanel, gbc);

		gbc.gridy++;
		gbc.weighty = 0.0;
		gbc.ipady = 150;
		relationListPanel.add(interactLocalDisplayer.getCanvas(), gbc);
	}

	private void arrangeSpanNetworkPanel()
	{
		spanNetworkPanel.setBorder(new TitledBorder("种子"));
		spanNetworkPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();
		gbc.insets = new Insets(1, 2, 1, 2);

		gbc.weightx = 0.0;
		spanNetworkPanel.add(new JLabel("ID"), gbc);

		gbc.ipadx = 90;
		gbc.gridx++;
		spanNetworkPanel.add(coreUniProtIdTextField, gbc);

		gbc.ipadx = 0;
		gbc.gridx++;
		spanNetworkPanel.add(spanNetworkButton, gbc);
	}

	private void config()
	{
		this.configReader();

		this.configSerializer();

		this.configQueryLigands();

		this.configFinder();

		this.configImportExportRelationPanel();

		this.configDisplayer();

		this.configPredictor();

		this.configMenuItems();
	}

	private void configDisplayer()
	{
		interactSingleDisplayer.setProgress(bifo);
		interactDetailDisplayer.setProgress(bifo);
		interact3Displayer.setProgress(bifo);

		interactSingleDisplayer.setAbsoluteDegrees(true);
		interactDetailDisplayer.setAbsoluteDegrees(false);
		interactLocalDisplayer.setAbsoluteDegrees(true);

		interactSingleDisplayer.setCores(1);
		interactDetailDisplayer.setInteract(this);
		interactDetailDisplayer.setCores(4);

		interactSingleDisplayer.setMargin(new Insets(5, 5, 5, 5));
		interactDetailDisplayer.setMargin(new Insets(5, 5, 5, 5));
		interactLocalDisplayer.setMargin(new Insets(5, 5, 5, 5));

		interactDetailDisplayer.addAccomplishedListener(new AccomplishListener<InteractDisplayer>() {

			@Override
			public void accomplish(InteractDisplayer e)
			{
				doneDisplayDetailInteraction();
			}
		});

		interactDetailReDisplayButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				doneFindInteraction();
			}

		});

		interact3DisplayButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionInteract3Display();
			}

		});

		interact3Displayer.addAccomplishedListener(new AccomplishListener<Interact3Displayer>() {

			@Override
			public void accomplish(Interact3Displayer e)
			{
				doneInteract3Display();
			}
		});

		stereoModeToggleButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (stereoModeToggleButton.getText().equals("双图模式"))
				{
					interact3Displayer.toggleStereoMode();
					stereoModeToggleButton.setText("裸眼双图");
				}
				else if (stereoModeToggleButton.getText().equals("裸眼双图"))
				{
					interact3Displayer.switchStereoMode();
					stereoModeToggleButton.setText("单图模式");
				}
				else if (stereoModeToggleButton.getText().equals("单图模式"))
				{
					interact3Displayer.switchStereoMode();
					interact3Displayer.toggleStereoMode();
					stereoModeToggleButton.setText("双图模式");
				}
			}

		});

	}

	private void configFinder()
	{
		finder.setProgress(bifo);

		findButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionFindInteraction();
			}

		});

		finder.addAccomplishedListener(new AccomplishListener<InteractFinder>() {

			@Override
			public void accomplish(InteractFinder e)
			{
				showRelationList(finder.getRelation());
				setRelation(finder.getRelation());
				doneFindInteraction();
			}
		});

		saveGraphButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionSaveGraph();
			}

		});

		spanNetworkButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				spanCoreUniProtNetwork(coreUniProtIdTextField.getText());
			}

		});

		constructNetworkButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleOperationButtons(false);
				showInteractNetwork();
			}

		});

	}

	private void configImportExportRelationPanel()
	{
		importExportRelationFileField.setEditable(false);

		importRelationButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionImportRelation();
			}
		});

		exportRelationButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionExportRelation();
			}

		});
	}

	private void configMenuItems()
	{
		readInteractDataMenu.setMnemonic('I');
		readInteractDataMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionReadInteractDataFile();
			}

		});

		serializeInteractDataMenu.setMnemonic('T');
		serializeInteractDataMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionSerializeInteractData();
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

	private void configQueryLigands()
	{
		interactLigandsList.getModel().getNotEditableColumns().add(0);

		TableRowSorter<ListTableModel<VectorObjects>> sorter = new TableRowSorter<ListTableModel<VectorObjects>>(
				interactLigandsList.getModel());
		interactLigandsList.getTable().setRowSorter(sorter);

		interactLigandsList.getTable().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (!interactLigandsList.getSelectedDataIndex().isEmpty())
				{
					String uniProtId = interactLigandsList.getSelectedData().get(0).vectorAccess(0).toString();
					drawLigandsOfReceptor(uniProtId);
					bifo.getUniProt().displayItem(uniProtId);
				}
			}

		});

		bifo.getUniProt().getQueryResultList().getTable().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				showLigandsOfReceptor(bifo.getUniProt().getQueryResultList().getSelectedData().get(0).vectorAccess(0)
						.toString());
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
				doneReadInteractDataFile();
			}
		});
	}

	private void configSerializer()
	{
		serializer.setProgress(bifo);

		serializer.addAccomplishedListener(new AccomplishListener<InteractSerializer>() {

			@Override
			public void accomplish(InteractSerializer e)
			{
				doneSerializeInteractData();
			}
		});
	}

	public void displayInteractLigands(Collection<String> ligands)
	{
		interactLigandsList.getData().clear();

		for (String ligand : ligands)
		{
			interactLigandsList.getData().add(new VectorObjects(ligand));
		}

		interactLigandsList.refreshData();
	}

	private void doneDisplayDetailInteraction()
	{
		this.toggleOperationButtons(true);
		displayerPanel.setSelectedIndex(0);
	}

	private void doneFindInteraction()
	{
		this.toggleOperationButtons(false);

		// Collection<String> cores = new HashSet<String>();
		// for (Collection<String> result : finder.getResult().values()) {
		// cores.addAll(result);
		// }
		// interactDetailDisplayer.setData(cores);
		// interactDetailDisplayer.setRelation(LoadInteractRelation(cores,
		// null));
		showInteractNetwork();
	}

	private void doneInteract3Display()
	{
		interact3DisplayButton.setEnabled(true);
		displayerPanel.setSelectedIndex(1);
	}

	private void donePredictInteract()
	{
		// TreeSet<Integer> sort = new TreeSet<Integer>();
		// for (Set<String> ligands : predictor.getPredicts().values()) {
		// sort.add(ligands.size());
		// }
		//
		// int number = Integer.valueOf(findLowerRankText.getText())
		// - Integer.valueOf(findUpperRankText.getText()) + 1;
		//
		// Set<Integer> acceptSizes = new HashSet<Integer>();
		// int index = 0;
		// for (Integer i : sort.descendingSet()) {
		// acceptSizes.add(i);
		// index++;
		// if (index >= number) {
		// break;
		// }
		// }
		//
		// Map<String, Set<String>> accept = new HashMap<String, Set<String>>();
		// for (Entry<String, Set<String>> entry :
		// predictor.getPredicts().entrySet()) {
		// if (acceptSizes.contains(entry.getValue().size())) {
		// accept.put(entry.getKey(), entry.getValue());
		// }
		// }
		// predictor.getPredicts().clear();
		//
		// interactDetailDisplayer.setPredicts(accept);

		interactDetailDisplayer.setPredicts(predictor.getPredicts());
	}

	public void doneReadInteractDataFile()
	{
		JOptionPane.showMessageDialog(bifo, "相互数据已被成功导入", "导入成功", JOptionPane.INFORMATION_MESSAGE);
	}

	public void doneSerializeInteractData()
	{
		JOptionPane.showMessageDialog(bifo, "相互数据已被成功导出", "导出成功", JOptionPane.INFORMATION_MESSAGE);
	}

	public void drawLigandsOfReceptor(String receptor)
	{
		Collection<String> id = new HashSet<String>();
		id.add(receptor);
		// interactSingleDisplayer.setData(id);
		interactSingleDisplayer.setRelation(LoadInteractRelation(id, null));
		new Thread(interactSingleDisplayer).start();
	}

	public JButton getConstructNetworkButton()
	{
		return constructNetworkButton;
	}

	public JPanel getConstructPanel()
	{
		return constructPanel;
	}

	public JTextField getCoresNumberTextField()
	{
		return coresNumberTextField;
	}

	public JTextField getCoreUniProtIdTextField()
	{
		return coreUniProtIdTextField;
	}

	public JTabbedPane getDisplayerPanel()
	{
		return displayerPanel;
	}

	public JButton getExportRelationButton()
	{
		return exportRelationButton;
	}

	public JButton getFindButton()
	{
		return findButton;
	}

	public JTextField getFindDetailText()
	{
		return findDetailText;
	}

	public InteractFinder getFinder()
	{
		return finder;
	}

	public JTextField getFindLowerRankText()
	{
		return findLowerRankText;
	}

	public JTextField getFindSpeciesText()
	{
		return findSpeciesText;
	}

	public JTextField getFindUpperRankText()
	{
		return findUpperRankText;
	}

	public JTextField getImportExportRelationFileField()
	{
		return importExportRelationFileField;
	}

	public JPanel getImportExportRelationPanel()
	{
		return importExportRelationPanel;
	}

	public JButton getImportRelationButton()
	{
		return importRelationButton;
	}

	public Interact3Displayer getInteract3Displayer()
	{
		return interact3Displayer;
	}

	public InteractDisplayer getInteractDetailDisplayer()
	{
		return interactDetailDisplayer;
	}

	public JButton getInteractDetailReDisplayButton()
	{
		return interactDetailReDisplayButton;
	}

	public JPanel getInteractFindPanel()
	{
		return interactFindPanel;
	}

	public ListTable<VectorObjects> getInteractLigandsList()
	{
		return interactLigandsList;
	}

	public JScrollPane getInteractLigandsPane()
	{
		return interactLigandsPane;
	}

	public InteractDisplayer getInteractLocalDisplayer()
	{
		return interactLocalDisplayer;
	}

	public InteractDisplayer getInteractSingleDisplayer()
	{
		return interactSingleDisplayer;
	}

	public JPanel getOperationPanel()
	{
		return operationPanel;
	}

	public JButton getPredictButton()
	{
		return predictButton;
	}

	public InteractPredictor getPredictor()
	{
		return predictor;
	}

	public JMenuItem getReadInteractDataMenu()
	{
		return readInteractDataMenu;
	}

	public Set<Relation<String, String>> getRelation()
	{
		return interactDetailDisplayer.getRelation();
	}

	public ListTable<VectorObjects> getRelationList()
	{
		return relationList;
	}

	public JScrollPane getRelationPanel()
	{
		return relationPanel;
	}

	public JButton getSaveGraphButton()
	{
		return saveGraphButton;
	}

	public JMenuItem getSerializeInteractDataMenu()
	{
		return serializeInteractDataMenu;
	}

	public JButton getSpanNetworkButton()
	{
		return spanNetworkButton;
	}

	public JPanel getSpanNetworkPanel()
	{
		return spanNetworkPanel;
	}

	protected SQLKit getSQLKit()
	{
		if (!InteractReader.Read && (kit == null || kit.isClosed()))
		{
			kit = DATABASE.getSQLKit();
		}
		return kit;
	}

	public JButton getStereoModeToggleButton()
	{
		return stereoModeToggleButton;
	}

	public void setRelation(Set<Relation<String, String>> relation)
	{
		interactDetailDisplayer.setRelation(relation);
	}

	public void showInteract3DNetwork()
	{
		interact3Displayer.setRelation(interactDetailDisplayer.getRelation());
		interact3Displayer.setCores(Integer.parseInt(coresNumberTextField.getText()));
		new Thread(interact3Displayer).start();
	}

	public void showInteractNetwork()
	{
		interactDetailDisplayer.setCores(Integer.parseInt(coresNumberTextField.getText()));
		new Thread(interactDetailDisplayer).start();
	}

	public void showLigandsOfReceptor(String receptor)
	{
		Collection<String> ligands = QueryInteractLigands(receptor);

		this.displayInteractLigands(ligands);

		if (!ligands.isEmpty())
		{
			coreUniProtIdTextField.setText(receptor);
		}

		this.drawLigandsOfReceptor(receptor);
	}

	public void showLocalInteractOfUniProt(String id)
	{
		Set<Relation<String, String>> local = new LinkedHashSet<Relation<String, String>>();
		try
		{
			for (String p : Interact.QueryInteractLigands(id, this.getSQLKit()))
			{
				local.add(new Relation<String, String>(id, p));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		interactLocalDisplayer.setRelation(local);
		interactLocalDisplayer.display();
	}

	private void showRelationList(Set<Relation<String, String>> relation)
	{
		relationList.getData().clear();
		for (Relation<String, String> r : relation)
		{
			relationList.getData().add(new VectorObjects(r.getKey(), r.getValue()));
		}
		relationList.refreshData();
	}

	private void spanCoreUniProtNetwork(String coreUniProt)
	{
		coreUniProt = coreUniProt.toUpperCase();

		Set<Relation<String, String>> relation = this.getRelation();
		relation.clear();

		try
		{

			for (String periphery : Interact.QueryInteractLigands(coreUniProt, this.getSQLKit()))
			{
				relation.add(new Relation<String, String>(coreUniProt, periphery));

				for (String span : Interact.QueryInteractLigands(periphery, this.getSQLKit()))
				{
					relation.add(new Relation<String, String>(periphery, span));
				}
			}

			showRelationList(relation);

			importExportRelationFileField.setText("");

			JOptionPane.showMessageDialog(bifo, "已根据种子" + coreUniProt + "扩张成局部网络", "扩张完成",
					JOptionPane.INFORMATION_MESSAGE);

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void toggleOperationButtons(boolean enabled)
	{
		findButton.setEnabled(enabled);
		interactDetailReDisplayButton.setEnabled(enabled);
		spanNetworkButton.setEnabled(enabled);
		importRelationButton.setEnabled(enabled);
		exportRelationButton.setEnabled(enabled);
		predictButton.setEnabled(enabled);
		constructNetworkButton.setEnabled(enabled);
		interact3DisplayButton.setEnabled(enabled);
		stereoModeToggleButton.setEnabled(enabled);
		saveGraphButton.setEnabled(enabled);
	}

}
