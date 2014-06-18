package org.kernelab.bifo.uniprot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.kernelab.basis.Accomplishable.AccomplishListener;
import org.kernelab.basis.Tools;
import org.kernelab.basis.VectorObjects;
import org.kernelab.basis.io.DataReader;
import org.kernelab.basis.sql.DataBase;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.AppBifo;
import org.kernelab.bifo.util.HintTextField;
import org.kernelab.bifo.util.HintTextField.HintListener;
import org.kernelab.graf.table.ListTable;

public class UniProt
{

	public static DataBase								DATABASE			= AppBifo.BIFO_DATABASE.clone();

	public static String								TABLE_NAME			= "uniprotkb";

	public static final Map<String, UniProtItem>		Items				= new Hashtable<String, UniProtItem>();

	public static final Map<String, Collection<String>>	Hints				= new HashMap<String, Collection<String>>();

	public static int									Hint_Cache_Length	= 2;

	public static Collection<String> HintUniProtItemIds(String hint)
	{
		Collection<String> ids = null;

		if (hint != null && hint.length() != 0)
		{

			if (hint.length() > Hint_Cache_Length)
			{
				ids = Hints.get(hint);
			}

			if (ids == null)
			{

				ids = new LinkedList<String>();

				if (UniProtReader.Read)
				{

					for (String id : Items.keySet())
					{
						if (id.startsWith(hint))
						{
							ids.add(id);
						}
					}

				}
				else
				{

					SQLKit kit = DATABASE.getSQLKit();

					try
					{
						ResultSet rs = kit.query("SELECT `id` FROM `" + TABLE_NAME + "` WHERE `id` LIKE ?", hint + "%");

						while (rs.next())
						{
							ids.add(rs.getString("id"));
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
				}

				if (hint.length() > Hint_Cache_Length)
				{
					Hints.put(hint, ids);
				}
			}

		}

		return ids;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	public static UniProtItem QueryUniProtItem(String id) throws SQLException
	{
		UniProtItem item = Items.get(id);

		if (item == null && !UniProtReader.Read)
		{

			SQLKit kit = DATABASE.getSQLKit();

			try
			{
				item = QueryUniProtItem(id, kit);
			}
			finally
			{
				kit.close();
			}

		}

		return item;
	}

	public static UniProtItem QueryUniProtItem(String id, SQLKit kit) throws SQLException
	{
		UniProtItem item = Items.get(id);

		if (item == null)
		{

			if (kit != null)
			{

				String sql = "SELECT * FROM `" + TABLE_NAME + "` WHERE `id`=?";

				ResultSet rs = kit.query(sql, id);

				while (rs.next())
				{
					item = new UniProtItem(rs);
					break;
				}
			}

			if (item != null)
			{
				Items.put(id, item);
			}

		}

		return item;
	}

	private AppBifo						bifo;

	private UniProtReader				reader;

	private UniProtSerializer			serializer;

	// Menus
	private JMenuItem					readUniProtDataMenu;

	private JMenuItem					serializeUniProtDataMenu;

	// Id Query Panel
	private HintTextField				idQueryField;

	private JButton						idQueryButton;

	private JPanel						idQueryPanel;

	// Query Panel
	private JTabbedPane					queryPanel;

	// Query Result Panel
	private ListTable<VectorObjects>	queryResultList;

	private JScrollPane					queryResultPane;

	// UniProt Item Displayer
	private JTextField					uniProtItemIdText;

	private JTextField					uniProtItemSpeciesText;

	private JTextField					uniProtItemEntryNameText;

	private JTextArea					uniProtItemDetailText;

	private JScrollPane					uniProtItemDetailPane;

	private JTextArea					uniProtItemCodeText;

	private JScrollPane					uniProtItemCodePane;

	private JPanel						uniProtItemDisplayer;

	public UniProt(AppBifo bifo)
	{
		this.bifo = bifo;

		this.reader = new UniProtReader();

		this.serializer = new UniProtSerializer();

		// Menues
		this.readUniProtDataMenu = new JMenuItem("打开UniProt数据文件");

		this.serializeUniProtDataMenu = new JMenuItem("将UniProt数据导出至数据库");

		// Id Query Panel
		this.idQueryField = new HintTextField();

		this.idQueryButton = new JButton("查询");

		this.idQueryPanel = new JPanel();

		// Query Panel
		this.queryPanel = new JTabbedPane();

		// Query Result Panel
		this.queryResultList = new ListTable<VectorObjects>("UniProt");

		this.queryResultPane = new JScrollPane(queryResultList.getTable());

		// UniProt Item Displayer
		this.uniProtItemIdText = new JTextField();

		this.uniProtItemSpeciesText = new JTextField();

		this.uniProtItemEntryNameText = new JTextField();

		this.uniProtItemDetailText = new JTextArea();

		this.uniProtItemDetailPane = new JScrollPane(uniProtItemDetailText);

		this.uniProtItemCodeText = new JTextArea();

		this.uniProtItemCodePane = new JScrollPane(uniProtItemCodeText);

		this.uniProtItemDisplayer = new JPanel();

		this.config();

		this.arrange();
	}

	public void actionQueryId()
	{
		String hint = idQueryField.getText().trim();
		if (hint.length() > 0)
		{
			this.displayQueryResult(HintUniProtItemIds(hint));
		}
	}

	public void actionReadUniProtDataFile()
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
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(bifo, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
			}
			new Thread(reader).start();
		}
	}

	public void actionSerializeUniProtData()
	{
		if (JOptionPane.showConfirmDialog(bifo, "确认要将UniProt数据导出至数据库吗？", "确认导出", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		{
			new Thread(serializer).start();
		}
	}

	private void arrange()
	{
		this.arrangeQueryPanel();

		this.arrangeItemDisplayer();
	}

	private void arrangeIdQueryPanel()
	{
		idQueryPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();

		gbc.anchor = GridBagConstraints.NORTHWEST;
		idQueryPanel.add(idQueryField, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		idQueryPanel.add(idQueryButton, gbc);
	}

	private void arrangeItemDisplayer()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();
		gbc.insets = new Insets(1, 5, 1, 0);

		gbc.weightx = 0.0;
		panel.add(new JLabel("ID"), gbc);

		gbc.gridx++;
		gbc.weightx = 0.5;
		panel.add(uniProtItemIdText, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		panel.add(new JLabel("Species"), gbc);

		gbc.gridx++;
		gbc.weightx = 0.6;
		panel.add(uniProtItemSpeciesText, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		panel.add(new JLabel("Entry"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		panel.add(uniProtItemEntryNameText, gbc);

		this.uniProtItemDisplayer.setLayout(new GridBagLayout());
		gbc = Tools.makePreferredGridBagConstraints();
		gbc.insets = new Insets(1, 1, 1, 1);

		gbc.weighty = 0.0;
		this.uniProtItemDisplayer.add(panel, gbc);

		gbc.gridy++;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipady = 50;
		this.uniProtItemDisplayer.add(uniProtItemDetailPane, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		this.uniProtItemDisplayer.add(uniProtItemCodePane, gbc);

	}

	private void arrangeQueryPanel()
	{
		this.arrangeIdQueryPanel();

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(idQueryPanel, gbc);
		queryPanel.add("ID查询", panel);

	}

	public void clearDetails()
	{
		this.uniProtItemIdText.setText("");
		this.uniProtItemSpeciesText.setText("");
		this.uniProtItemEntryNameText.setText("");
		this.uniProtItemDetailText.setText("");
		this.uniProtItemCodeText.setText("");
	}

	private void config()
	{
		this.configReader();

		this.configSerializer();

		this.configMenuItems();

		this.configQueryPanel();

		this.configQueryResultPanel();

		this.configItemDisplayer();
	}

	private void configIdQueryPanel()
	{
		idQueryField.setHintListener(new HintListener() {

			@Override
			public void hint(String hint)
			{
				if (hint.length() > Hint_Cache_Length)
				{
					for (String rec : UniProt.HintUniProtItemIds(hint))
					{
						idQueryField.addHint(rec);
					}
				}
			}

		});

		idQueryField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e)
			{
				if (e.getKeyChar() == KeyEvent.VK_ENTER)
				{
					idQueryField.hideHint();
					actionQueryId();
				}
			}

		});

		idQueryButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionQueryId();
			}

		});
	}

	private void configItemDisplayer()
	{
		this.uniProtItemDetailText.setLineWrap(true);
		this.uniProtItemCodeText.setLineWrap(true);

		this.uniProtItemDetailPane.setBorder(new TitledBorder("Detail"));
		this.uniProtItemCodePane.setBorder(new TitledBorder("Sequence"));
	}

	private void configMenuItems()
	{
		readUniProtDataMenu.setMnemonic('U');
		readUniProtDataMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionReadUniProtDataFile();
			}

		});

		serializeUniProtDataMenu.setMnemonic('P');
		serializeUniProtDataMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionSerializeUniProtData();
			}

		});
	}

	private void configQueryPanel()
	{
		this.configIdQueryPanel();

	}

	private void configQueryResultPanel()
	{
		queryResultList.getModel().getNotEditableColumns().add(0);

		queryResultList.getTable().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (!queryResultList.getSelectedDataIndex().isEmpty())
				{
					displayItem(queryResultList.getSelectedData().get(0).vectorAccess(0).toString());
				}
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
				doneReadUniProtDataFile();
			}
		});
	}

	private void configSerializer()
	{
		serializer.setProgress(bifo);

		serializer.addAccomplishedListener(new AccomplishListener<UniProtSerializer>() {

			@Override
			public void accomplish(UniProtSerializer e)
			{
				doneSerializeUniProtData();
			}
		});
	}

	public void displayItem(String uniProtId)
	{
		try
		{
			displayItem(UniProt.QueryUniProtItem(uniProtId));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void displayItem(UniProtItem item)
	{
		this.clearDetails();
		if (item != null)
		{
			this.uniProtItemIdText.setText(item.getId());
			this.uniProtItemSpeciesText.setText(item.getSpecies());
			this.uniProtItemEntryNameText.setText(item.getEntryName());
			this.uniProtItemDetailText.setText(item.getDetail());
			this.uniProtItemCodeText.setText(item.getSequenceData());
		}
	}

	public void displayQueryResult(Collection<String> result)
	{
		queryResultList.getData().clear();

		for (String id : result)
		{
			queryResultList.getData().add(new VectorObjects(id));
		}

		queryResultList.refreshData();
	}

	public void doneReadUniProtDataFile()
	{
		JOptionPane.showMessageDialog(bifo, "UniProt数据已被成功导入", "导入成功", JOptionPane.INFORMATION_MESSAGE);
	}

	public void doneSerializeUniProtData()
	{
		JOptionPane.showMessageDialog(bifo, "UniProt数据已被成功导出", "导出成功", JOptionPane.INFORMATION_MESSAGE);
	}

	public JButton getIdQueryButton()
	{
		return idQueryButton;
	}

	public HintTextField getIdQueryField()
	{
		return idQueryField;
	}

	public JPanel getIdQueryPanel()
	{
		return idQueryPanel;
	}

	public JTabbedPane getQueryPanel()
	{
		return queryPanel;
	}

	public ListTable<VectorObjects> getQueryResultList()
	{
		return queryResultList;
	}

	public JScrollPane getQueryResultPane()
	{
		return queryResultPane;
	}

	public UniProtReader getReader()
	{
		return reader;
	}

	public JMenuItem getReadUniProtDataMenu()
	{
		return readUniProtDataMenu;
	}

	public UniProtSerializer getSerializer()
	{
		return serializer;
	}

	public JMenuItem getSerializeUniProtDataMenu()
	{
		return serializeUniProtDataMenu;
	}

	public JScrollPane getUniProtItemCodePane()
	{
		return uniProtItemCodePane;
	}

	public JTextArea getUniProtItemCodeText()
	{
		return uniProtItemCodeText;
	}

	public JScrollPane getUniProtItemDetailPane()
	{
		return uniProtItemDetailPane;
	}

	public JTextArea getUniProtItemDetailText()
	{
		return uniProtItemDetailText;
	}

	public JPanel getUniProtItemDisplayer()
	{
		return uniProtItemDisplayer;
	}

	public JTextField getUniProtItemEntryNameText()
	{
		return uniProtItemEntryNameText;
	}

	public JTextField getUniProtItemIdText()
	{
		return uniProtItemIdText;
	}

	public JTextField getUniProtItemSpeciesText()
	{
		return uniProtItemSpeciesText;
	}

}
