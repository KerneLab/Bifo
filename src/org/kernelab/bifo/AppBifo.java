package org.kernelab.bifo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.kernelab.basis.Tools;
import org.kernelab.basis.sql.DataBase;
import org.kernelab.basis.sql.DataBase.MySQL;
import org.kernelab.bifo.go.GeneOntology;
import org.kernelab.bifo.interact.Interact;
import org.kernelab.bifo.uniprot.UniProt;
import org.kernelab.bifo.uniprot.liver.Liver;
import org.kernelab.bifo.util.Progressive;

public class AppBifo extends JFrame implements Progressive
{

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 8267420423660341264L;

	public static final DataBase	BIFO_DATABASE		= new MySQL("bifo", "root",
																"root");

	public static String			Last_File_Location	= ".";

	private static int				Tab_Index			= 0;

	public static final int			UNIPROT_QUERY_TAB	= Tab_Index++;

	public static final int			INTERACT_FIND_TAB	= Tab_Index++;

	static {
		Tools.configLookAndFeel();

		boolean customize = false;

		if (customize) {
			Color background = Color.WHITE;
			Color foreground = Color.BLACK;
			Color focusground = new Color(234, 234, 234);

			UIManager.put("Button.background", background);
			UIManager.put("MenuBar.background", background);
			UIManager.put("Panel.background", background);
			UIManager.put("ScrollPane.background", background);
			UIManager.put("TabbedPane.background", background);
			UIManager.put("TabbedPane.tabAreaBackground", background);
			UIManager.put("Table.selectionBackground", focusground);
			UIManager.put("TextField.inactiveBackground", background);
			UIManager.put("TitledBorder.titleColor", foreground);
			UIManager.put("Viewport.background", background);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		AppBifo app = new AppBifo();
		app.setVisible(true);
	}

	private JMenu			fileMenu;

	private JTabbedPane		tabbedFrames;

	private JProgressBar	progressBar;

	private UniProt			uniProt;

	private Interact		interact;

	private GeneOntology	geneOntology;

	private JSplitPane		uniProtInteractPanel;

	private Liver			liver;

	public AppBifo()
	{
		super("PINE");

		this.tabbedFrames = new JTabbedPane();

		this.progressBar = new JProgressBar();

		this.uniProt = new UniProt(this);

		this.interact = new Interact(this);

		this.geneOntology = new GeneOntology(this);

		this.uniProtInteractPanel = new JSplitPane();

		this.liver = new Liver(this);

		this.config();

		this.arrange();
	}

	private void arrange()
	{
		this.arrangeTabbedFrames();

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();

		this.add(tabbedFrames, gbc);

		gbc.gridy++;
		gbc.weighty = 0.0;
		this.add(progressBar, gbc);

		this.pack();

		this.setBounds(120, 30, 900, 700);

		// this.setMinimumSize(this.getSize());
	}

	private JPanel arrangeQueryResultInteractPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2, 2, 2));
		panel.add(uniProt.getQueryResultPane());
		panel.add(interact.getInteractLigandsPane());

		return panel;
	}

	private void arrangeTabbedFrames()
	{
		this.arrangeUniProtInteractPanel();

		tabbedFrames.add("查询", uniProtInteractPanel);
		tabbedFrames.add("相互作用网络", interact.getInteractFindPanel());
		// tabbedFrames.add("肝脏蛋白相互作用", liver.getInteractPanel());
	}

	private void arrangeUniProtInteractPanel()
	{
		this.uniProtInteractPanel.setContinuousLayout(true);

		this.uniProtInteractPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();

		gbc.weighty = 0.0;
		panel.add(uniProt.getQueryPanel(), gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;
		panel.add(this.arrangeQueryResultInteractPanel(), gbc);

		gbc.gridy++;
		gbc.ipady = 135;
		gbc.weighty = 0.0;
		panel.add(interact.getInteractSingleDisplayer().getCanvas(), gbc);

		panel.setPreferredSize(new Dimension(180, 0));

		this.uniProtInteractPanel.setLeftComponent(panel);

		// Detail Panel
		panel = new JPanel();
		panel.setBorder(new TitledBorder("Gene Ontology"));
		panel.setLayout(new GridBagLayout());

		gbc = Tools.makePreferredGridBagConstraints();
		gbc.insets = new Insets(1, 1, 1, 1);
		gbc.ipadx = 80;
		gbc.weightx = 0.0;
		panel.add(geneOntology.getUniProtGOPane(), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		panel.add(geneOntology.getGene2GOPane(), gbc);

		JPanel detailPanel = new JPanel();
		detailPanel.setLayout(new GridBagLayout());

		gbc = Tools.makePreferredGridBagConstraints();

		detailPanel.add(uniProt.getUniProtItemDisplayer(), gbc);

		gbc.gridy++;
		gbc.weighty = 0.0;
		gbc.ipady = 120;
		detailPanel.add(panel, gbc);

		this.uniProtInteractPanel.setRightComponent(detailPanel);

		this.uniProtInteractPanel.setDividerLocation(180);
	}

	private void config()
	{
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e)
			{
				exit();
			}

		});

		Image appIcon = Toolkit.getDefaultToolkit().getImage(
				Tools.getClassLoader()
						.getResource("org/kernelab/bifo/resources/logo.png"));
		this.setIconImage(appIcon);

		this.configMenu();
	}

	private void configFileMenu()
	{
		fileMenu = new JMenu("文件(F)");
		fileMenu.setMnemonic('F');

		fileMenu.add(uniProt.getReadUniProtDataMenu());
		fileMenu.add(interact.getReadInteractDataMenu());
		fileMenu.add(geneOntology.getReadGene2GOMenu());
		fileMenu.add(geneOntology.getReadUniProtGOMenu());

		fileMenu.addSeparator();

		fileMenu.add(uniProt.getSerializeUniProtDataMenu());
		fileMenu.add(interact.getSerializeInteractDataMenu());
		fileMenu.add(geneOntology.getSerializeGene2GOMenu());
		fileMenu.add(geneOntology.getSerializeUniProtGOMenu());

		// fileMenu.addSeparator();
		// fileMenu.add(liver.getReadLiverDataMenu());

		fileMenu.addSeparator();

		JMenuItem exitMenu = new JMenuItem("退出(X)");
		exitMenu.setMnemonic('X');
		exitMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				exit();
			}

		});
		fileMenu.add(exitMenu);
	}

	private void configMenu()
	{
		this.configFileMenu();

		this.setJMenuBar(new JMenuBar());

		this.getJMenuBar().add(fileMenu);
	}

	private void exit()
	{
		if (JOptionPane.showConfirmDialog(this, "确认要退出程序吗？", "退出",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		{
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			System.exit(0);
		} else {
			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}
	}

	public JMenu getFileMenu()
	{
		return fileMenu;
	}

	public GeneOntology getGeneOntology()
	{
		return geneOntology;
	}

	public Interact getInteract()
	{
		return interact;
	}

	public Liver getLiver()
	{
		return liver;
	}

	public JProgressBar getProgressBar()
	{
		return progressBar;
	}

	public JTabbedPane getTabbedFrames()
	{
		return tabbedFrames;
	}

	public UniProt getUniProt()
	{
		return uniProt;
	}

	public JSplitPane getUniProtInteractPanel()
	{
		return uniProtInteractPanel;
	}

	@Override
	public void nextProgress()
	{
		progressBar.setValue(progressBar.getValue() + 1);
	}

	@Override
	public void prepareProgress()
	{
		progressBar.setIndeterminate(true);
	}

	@Override
	public void resetProgress(int steps)
	{
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		progressBar.setMinimum(0);
		progressBar.setMaximum(steps - 1);
	}

	@Override
	public void setProgress(double ratio)
	{
		progressBar.setValue((int) (ratio * progressBar.getMaximum()));
	}
}
