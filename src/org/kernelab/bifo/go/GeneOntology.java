package org.kernelab.bifo.go;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.TableRowSorter;

import org.kernelab.basis.Accomplishable.AccomplishListener;
import org.kernelab.basis.Tools;
import org.kernelab.basis.VectorObjects;
import org.kernelab.basis.io.DataReader;
import org.kernelab.basis.sql.DataBase;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.AppBifo;
import org.kernelab.graf.table.ListTable;
import org.kernelab.graf.table.ListTableModel;

public class GeneOntology
{

	public static DataBase											DATABASE						= AppBifo.BIFO_DATABASE
																											.clone();

	public static final String										UNIPROT_GO_TABLE_NAME			= "uniprotgo";

	public static final String										GENE2GO_TABLE_NAME				= "gene2go";

	public static final String										GENE2GO_FULL_TABLE_NAME			= "gene2go_full";

	public static final String										INTERACT_GO_TABLE_NAME			= "interactgo";

	public static final String										INTERACT_GO_SOLELY_TABLE_NAME	= "interactgo_solely";

	public static final Map<String, Gene2GOItem>					Gene2GOItems					= new Hashtable<String, Gene2GOItem>();

	public static final Map<String, Collection<Gene2GOFullItem>>	Gene2GOFullItems				= new Hashtable<String, Collection<Gene2GOFullItem>>();

	public static final Map<String, Set<String>>					UniProtGOAnnotations			= new Hashtable<String, Set<String>>();

	public static final Map<String, Set<Gene2GOItem>>				UniProtGOItems					= new Hashtable<String, Set<Gene2GOItem>>();

	public static final Map<String, Set<String>>					GOUniProts						= new Hashtable<String, Set<String>>();

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Tools.debug(QueryUniProtsByGO("0003973"));
	}

	public static Collection<Gene2GOFullItem> QueryGene2GOFullItems(String goId)
	{
		Collection<Gene2GOFullItem> items = Gene2GOFullItems.get(goId);

		if (items == null)
		{

			if (Gene2GOReader.Read)
			{

				items = new HashSet<Gene2GOFullItem>();
				Gene2GOFullItems.put(goId, items);

			}
			else
			{

				SQLKit kit = DATABASE.getSQLKit();

				try
				{
					items = QueryGene2GOFullItems(goId, kit);
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

		return items;
	}

	public static Collection<Gene2GOFullItem> QueryGene2GOFullItems(String goId, SQLKit kit) throws SQLException
	{
		Collection<Gene2GOFullItem> items = Gene2GOFullItems.get(goId);

		if (items == null)
		{

			items = new HashSet<Gene2GOFullItem>();

			if (kit != null)
			{

				String sql = "SELECT * FROM `" + GENE2GO_FULL_TABLE_NAME + "` WHERE `go`=?";

				ResultSet rs = kit.query(sql, goId);

				while (rs.next())
				{
					items.add(new Gene2GOFullItem(rs));
				}
			}

			Gene2GOFullItems.put(goId, items);
		}

		return items;
	}

	public static Gene2GOItem QueryGene2GOItem(String goId)
	{
		Gene2GOItem item = Gene2GOItems.get(goId);

		if (item == null && !Gene2GOReader.Read)
		{

			SQLKit kit = DATABASE.getSQLKit();

			try
			{
				item = QueryGene2GOItem(goId, kit);
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

		return item;
	}

	public static Gene2GOItem QueryGene2GOItem(String goId, SQLKit kit) throws SQLException
	{
		Gene2GOItem item = Gene2GOItems.get(goId);

		if (item == null && kit != null)
		{

			String sql = "SELECT `go`,`category`,`term` FROM `" + GENE2GO_TABLE_NAME + "` WHERE `go`=?";

			ResultSet rs = kit.query(sql, goId);

			while (rs.next())
			{
				item = new Gene2GOItem(rs);
				break;
			}
			if (item != null)
			{
				Gene2GOItems.put(goId, item);
			}
		}

		return item;
	}

	public static Set<String> QueryUniProtGOAnnotations(String uniProtId)
	{
		Set<String> annotations = UniProtGOAnnotations.get(uniProtId);

		if (annotations == null)
		{

			if (UniProtGOAnnotationsReader.Read)
			{

				annotations = new HashSet<String>();
				UniProtGOAnnotations.put(uniProtId, annotations);

			}
			else
			{

				SQLKit kit = DATABASE.getSQLKit();

				try
				{
					annotations = QueryUniProtGOAnnotations(uniProtId, kit);
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

		return annotations;
	}

	public static Set<String> QueryUniProtGOAnnotations(String uniProtId, SQLKit kit) throws SQLException
	{
		Set<String> annotations = UniProtGOAnnotations.get(uniProtId);

		if (annotations == null)
		{

			annotations = new HashSet<String>();

			if (kit != null)
			{

				String sql = "SELECT DISTINCT `go` FROM `" + UNIPROT_GO_TABLE_NAME + "` WHERE `uniprot`=?";

				ResultSet rs = kit.query(sql, uniProtId);

				while (rs.next())
				{
					annotations.add(rs.getString("go"));
				}
			}

			UniProtGOAnnotations.put(uniProtId, annotations);
		}

		return annotations;
	}

	public static Set<Gene2GOItem> QueryUniProtGOItems(String uniProtId)
	{
		Set<Gene2GOItem> items = UniProtGOItems.get(uniProtId);

		if (items == null)
		{

			if (UniProtGOAnnotationsReader.Read)
			{

				items = new HashSet<Gene2GOItem>();
				UniProtGOItems.put(uniProtId, items);

			}
			else
			{

				SQLKit kit = DATABASE.getSQLKit();

				try
				{
					items = QueryUniProtGOItems(uniProtId, kit);
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

		return items;
	}

	public static Set<Gene2GOItem> QueryUniProtGOItems(String uniProtId, SQLKit kit) throws SQLException
	{
		Set<Gene2GOItem> items = UniProtGOItems.get(uniProtId);

		if (items == null)
		{

			items = new HashSet<Gene2GOItem>();

			if (kit != null)
			{

				String sql = "SELECT DISTINCT * FROM `" + GeneOntology.GENE2GO_TABLE_NAME + "` AS `g`, `"
						+ GeneOntology.UNIPROT_GO_TABLE_NAME + "` AS `u` WHERE `u`.`uniprot`=? AND `g`.`go`=`u`.`go`";

				ResultSet rs = kit.query(sql, uniProtId);

				while (rs.next())
				{
					items.add(new Gene2GOItem(rs));
				}
			}

			UniProtGOItems.put(uniProtId, items);
		}

		return items;
	}

	public static Set<String> QueryUniProtsByGO(String goId)
	{
		Set<String> uniprots = GOUniProts.get(goId);

		if (uniprots == null)
		{

			if (UniProtGOAnnotationsReader.Read)
			{

				uniprots = new HashSet<String>();
				GOUniProts.put(goId, uniprots);

			}
			else
			{

				SQLKit kit = DATABASE.getSQLKit();

				try
				{
					uniprots = QueryUniProtsByGO(goId, kit);
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

		return uniprots;
	}

	public static Set<String> QueryUniProtsByGO(String goId, SQLKit kit) throws SQLException
	{
		Set<String> uniprots = GOUniProts.get(goId);

		if (uniprots == null)
		{

			uniprots = new HashSet<String>();

			if (kit != null)
			{

				String sql = "SELECT DISTINCT `uniprot` FROM `" + UNIPROT_GO_TABLE_NAME + "` WHERE `go`=?";

				ResultSet rs = kit.query(sql, goId);

				while (rs.next())
				{
					uniprots.add(rs.getString("uniprot"));
				}
			}

			GOUniProts.put(goId, uniprots);
		}

		return uniprots;
	}

	private AppBifo							bifo;

	private Gene2GOReader					gene2GOReader;

	private UniProtGOAnnotationsReader		uniProtGOReader;

	private Gene2GOSerializer				gene2GOSerializer;

	private UniProtGOAnnotationsSerializer	uniProtGOSerializer;

	private JMenuItem						readGene2GOMenu;

	private JMenuItem						readUniProtGOMenu;

	private JMenuItem						serializeGene2GOMenu;

	private JMenuItem						serializeUniProtGOMenu;

	private ListTable<VectorObjects>		uniProtGOList;

	private JScrollPane						uniProtGOPane;

	private ListTable<Gene2GOFullItem>		gene2GOList;

	private JScrollPane						gene2GOPane;

	public GeneOntology(AppBifo bifo)
	{
		this.bifo = bifo;

		this.gene2GOReader = new Gene2GOReader();

		this.uniProtGOReader = new UniProtGOAnnotationsReader();

		this.gene2GOSerializer = new Gene2GOSerializer();

		this.uniProtGOSerializer = new UniProtGOAnnotationsSerializer();

		this.readGene2GOMenu = new JMenuItem("打开Gene2GO文件");

		this.readUniProtGOMenu = new JMenuItem("打开GO Annotations文件");

		this.serializeGene2GOMenu = new JMenuItem("将Gene2GO数据导出至数据库");

		this.serializeUniProtGOMenu = new JMenuItem("将GO Annotations数据导出至数据库");

		this.uniProtGOList = new ListTable<VectorObjects>("GO");

		this.uniProtGOPane = new JScrollPane(uniProtGOList.getTable());

		this.gene2GOList = new ListTable<Gene2GOFullItem>("GO", "Category", "Term", "Gene", "Tax");

		this.gene2GOPane = new JScrollPane(gene2GOList.getTable());

		this.config();
	}

	public void actionReadGene2GOData()
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
				gene2GOReader.setDataFile(selectedFile);
			}
			catch (FileNotFoundException e)
			{
				JOptionPane.showMessageDialog(bifo, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			new Thread(gene2GOReader).start();
		}
	}

	public void actionReadUniProtGOAnnotationsData()
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
				uniProtGOReader.setDataFile(selectedFile);
			}
			catch (FileNotFoundException e)
			{
				JOptionPane.showMessageDialog(bifo, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			new Thread(uniProtGOReader).start();
		}
	}

	public void actionSerializeGene2GOData()
	{
		if (JOptionPane.showConfirmDialog(bifo, "确认要将Gene2GO数据导出至数据库吗？", "确认导出", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		{
			new Thread(gene2GOSerializer).start();
		}
	}

	public void actionSerializeUniProtGOAnnotationsData()
	{
		if (JOptionPane.showConfirmDialog(bifo, "确认要将GO Annotations数据导出至数据库吗？", "确认导出", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		{
			new Thread(uniProtGOSerializer).start();
		}
	}

	private void config()
	{
		this.configReaders();

		this.configSerializers();

		this.configGOList();

		this.configMenuItems();
	}

	private void configGOList()
	{
		bifo.getUniProt().getQueryResultList().getTable().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				List<VectorObjects> list = bifo.getUniProt().getQueryResultList().getSelectedData();
				if (list != null && !list.isEmpty())
				{
					showGeneOntologyOfUniProt(list.get(0).vectorAccess(0).toString());
				}
			}

		});

		bifo.getInteract().getInteractLigandsList().getTable().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				List<VectorObjects> list = bifo.getInteract().getInteractLigandsList().getSelectedData();
				if (list != null && !list.isEmpty())
				{
					showGeneOntologyOfUniProt(list.get(0).vectorAccess(0).toString());
				}
			}

		});

		uniProtGOList.getTable().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				List<VectorObjects> list = uniProtGOList.getSelectedData();
				if (list != null && !list.isEmpty())
				{
					showGene2GO(list.get(0).vectorAccess(0).toString().replaceFirst("^GO\\:", ""));
				}
			}

		});

		gene2GOList.getTable().getColumn("GO").setMaxWidth(70);
		gene2GOList.getTable().getColumn("GO").setMinWidth(70);
		gene2GOList.getTable().getColumn("Category").setMaxWidth(65);
		gene2GOList.getTable().getColumn("Category").setMinWidth(65);
		// gene2GOList.getTable().getColumn("Term").setMinWidth(120);
		gene2GOList.getTable().getColumn("Gene").setMaxWidth(65);
		gene2GOList.getTable().getColumn("Gene").setMinWidth(65);
		gene2GOList.getTable().getColumn("Tax").setMaxWidth(50);
		gene2GOList.getTable().getColumn("Tax").setMinWidth(50);

		TableRowSorter<ListTableModel<Gene2GOFullItem>> gene2GOListSorter = new TableRowSorter<ListTableModel<Gene2GOFullItem>>(
				gene2GOList.getModel());
		gene2GOList.getTable().setRowSorter(gene2GOListSorter);

		TableRowSorter<ListTableModel<VectorObjects>> uniProtGOListSorter = new TableRowSorter<ListTableModel<VectorObjects>>(
				uniProtGOList.getModel());
		uniProtGOList.getTable().setRowSorter(uniProtGOListSorter);
	}

	private void configMenuItems()
	{
		readGene2GOMenu.setMnemonic('G');
		readGene2GOMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionReadGene2GOData();
			}

		});

		serializeGene2GOMenu.setMnemonic('O');
		serializeGene2GOMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionSerializeGene2GOData();
			}

		});

		readUniProtGOMenu.setMnemonic('A');
		readUniProtGOMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionReadUniProtGOAnnotationsData();
			}

		});

		serializeUniProtGOMenu.setMnemonic('N');
		serializeUniProtGOMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionSerializeUniProtGOAnnotationsData();
			}

		});
	}

	private void configReaders()
	{
		gene2GOReader.setProgress(bifo);
		gene2GOReader.addAccomplishedListener(new AccomplishListener<DataReader>() {

			@Override
			public void accomplish(DataReader e)
			{
				doneReadGene2GOData();
			}
		});

		uniProtGOReader.setProgress(bifo);
		uniProtGOReader.addAccomplishedListener(new AccomplishListener<DataReader>() {

			@Override
			public void accomplish(DataReader e)
			{
				doneReadUniProtGOAnnotationsData();
			}
		});
	}

	private void configSerializers()
	{
		gene2GOSerializer.setProgress(bifo);
		gene2GOSerializer.addAccomplishedListener(new AccomplishListener<Gene2GOSerializer>() {

			@Override
			public void accomplish(Gene2GOSerializer e)
			{
				doneSerializeGene2GOData();
			}
		});

		uniProtGOSerializer.setProgress(bifo);
		uniProtGOSerializer.addAccomplishedListener(new AccomplishListener<UniProtGOAnnotationsSerializer>() {

			@Override
			public void accomplish(UniProtGOAnnotationsSerializer e)
			{
				doneSerializeUniProtGOAnnotationsData();
			}
		});
	}

	public void doneReadGene2GOData()
	{
		JOptionPane.showMessageDialog(bifo, "Gene2GO数据已被成功导入", "导入成功", JOptionPane.INFORMATION_MESSAGE);
	}

	public void doneReadUniProtGOAnnotationsData()
	{
		JOptionPane.showMessageDialog(bifo, "GO Annotations数据已被成功导入", "导入成功", JOptionPane.INFORMATION_MESSAGE);
	}

	public void doneSerializeGene2GOData()
	{
		JOptionPane.showMessageDialog(bifo, "已将Gene2GO数据导出至数据库", "导出成功", JOptionPane.INFORMATION_MESSAGE);
	}

	public void doneSerializeUniProtGOAnnotationsData()
	{
		JOptionPane.showMessageDialog(bifo, "已将GO Annotations数据导出至数据库", "导出成功", JOptionPane.INFORMATION_MESSAGE);
	}

	public ListTable<Gene2GOFullItem> getGene2GOList()
	{
		return gene2GOList;
	}

	public JScrollPane getGene2GOPane()
	{
		return gene2GOPane;
	}

	public Gene2GOReader getGene2GOReader()
	{
		return gene2GOReader;
	}

	public Gene2GOSerializer getGene2GOSerializer()
	{
		return gene2GOSerializer;
	}

	public JMenuItem getReadGene2GOMenu()
	{
		return readGene2GOMenu;
	}

	public JMenuItem getReadUniProtGOMenu()
	{
		return readUniProtGOMenu;
	}

	public JMenuItem getSerializeGene2GOMenu()
	{
		return serializeGene2GOMenu;
	}

	public JMenuItem getSerializeUniProtGOMenu()
	{
		return serializeUniProtGOMenu;
	}

	public ListTable<VectorObjects> getUniProtGOList()
	{
		return uniProtGOList;
	}

	public JScrollPane getUniProtGOPane()
	{
		return uniProtGOPane;
	}

	public UniProtGOAnnotationsReader getUniProtGOReader()
	{
		return uniProtGOReader;
	}

	public UniProtGOAnnotationsSerializer getUniProtGOSerializer()
	{
		return uniProtGOSerializer;
	}

	public void showGene2GO(String goId)
	{
		gene2GOList.getData().clear();

		gene2GOList.getData().addAll(QueryGene2GOFullItems(goId));

		gene2GOList.refreshData();
	}

	public void showGeneOntologyOfUniProt(String uniProtId)
	{
		Collection<String> annotations = QueryUniProtGOAnnotations(uniProtId);

		gene2GOList.getData().clear();
		gene2GOList.refreshData();

		uniProtGOList.getData().clear();

		for (String annotation : annotations)
		{
			uniProtGOList.getData().add(new VectorObjects("GO:" + annotation));
		}

		uniProtGOList.refreshData();
	}

}
