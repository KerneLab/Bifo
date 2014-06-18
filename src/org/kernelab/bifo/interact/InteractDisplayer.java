package org.kernelab.bifo.interact;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.kernelab.basis.AbstractAccomplishable;
import org.kernelab.basis.ColorIndexer;
import org.kernelab.basis.Relation;
import org.kernelab.basis.Tools;
import org.kernelab.basis.io.FilesFilter;
import org.kernelab.basis.sql.DataBase;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.AppBifo;
import org.kernelab.bifo.uniprot.UniProt;
import org.kernelab.bifo.uniprot.UniProtItem;
import org.kernelab.bifo.util.Progressive;

public class InteractDisplayer extends AbstractAccomplishable<InteractDisplayer> implements Runnable
{

	public class ItemEdge
	{
		private ItemNode	a;

		private ItemNode	b;

		private boolean		focused;

		private boolean		exists;

		public ItemEdge(ItemNode a, ItemNode b)
		{
			super();

			this.setAB(a, b);

			this.focused = false;
		}

		public void draw(Graphics2D g)
		{
			Point a = this.a.center;
			Point b = this.b.center;
			if (!this.exists()) {
				g.setColor(Color.GREEN);
			} else {
				g.setColor(focused ? EDGE_FOCUS_COLOR : Color.DARK_GRAY);
			}
			g.drawLine(a.x, a.y, b.x, b.y);
		}

		@Override
		public boolean equals(Object o)
		{
			boolean equal = false;

			if (o instanceof ItemEdge) {
				ItemEdge e = (ItemEdge) o;
				equal = this.toString().equals(e.toString());
			}

			return equal;
		}

		public boolean exists()
		{
			return exists;
		}

		public ItemNode getA()
		{
			return a;
		}

		public ItemNode getB()
		{
			return b;
		}

		@Override
		public int hashCode()
		{
			return toString().hashCode();
		}

		public boolean isFocused()
		{
			return focused;
		}

		private void setAB(ItemNode a, ItemNode b)
		{
			if (a.item.getId().compareTo(b.item.getId()) <= 0) {
				this.a = a;
				this.b = b;
			} else {
				this.a = b;
				this.b = a;
			}

			try {
				exists = Interact.QueryInteractExists(a.item.getId(), b.item.getId(),
						getSQLKit());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public void setFocused(boolean focused)
		{
			this.focused = focused;
		}

		@Override
		public String toString()
		{
			return a.item.getId() + '\t' + b.item.getId();
		}

	}

	public class ItemNode extends JLabel
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1876840426589328153L;

		private UniProtItem			item;

		private Set<String>			periphery;

		private int					type;

		private boolean				located;

		private Point				center;

		private Point				dragFrom;

		private Point				dragScreen;

		private int					lastTranslateX;

		private int					lastTranslateY;

		private Color				color;

		private Color				focus;

		private boolean				focused;

		private boolean				locked;

		private JLabel				nameplate;

		public ItemNode(UniProtItem item)
		{
			this.item = item;

			this.periphery = new TreeSet<String>();

			this.type = 0;

			this.located = false;

			this.center = new Point();

			this.dragFrom = new Point();

			this.dragScreen = new Point();

			this.focused = false;

			this.locked = false;

			this.setOpaque(false);

			this.setPreferredSize(Node_Size);

			this.setCursor(NODE_HOVER_CURSOR);

			this.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e)
				{
					if (e.getButton() == MouseEvent.BUTTON1) {

						if (e.isShiftDown()) {
							if (interact != null) {
								interact.showLocalInteractOfUniProt(ItemNode.this.item
										.getId());
							}
						}

					} else if (e.getButton() == MouseEvent.BUTTON3) {
						if (e.isAltDown()) { // 移除结点
							removeNode(ItemNode.this);
						} else if (e.isControlDown()) { // 固定结点
							ItemNode.this.locked = !ItemNode.this.locked;
						} else { // 显示结点铭牌
							showNameplate();
						}
					}
				}

				@Override
				public void mouseEntered(MouseEvent e)
				{
					focusNode(ItemNode.this, true);
					showHint(ItemNode.this);
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					focusNode(ItemNode.this, false);
					hint.setVisible(false);
				}

				@Override
				public void mousePressed(MouseEvent e)
				{
					if (e.getButton() == MouseEvent.BUTTON1) {
						getLocation(dragFrom);
						dragScreen.x = e.getXOnScreen();
						dragScreen.y = e.getYOnScreen();
						lastTranslateX = 0;
						lastTranslateY = 0;
					}
				}

				@Override
				public void mouseReleased(MouseEvent e)
				{
					if (e.getButton() == MouseEvent.BUTTON1) {
						movedNode(ItemNode.this);
					}
				}

			});

			this.addMouseMotionListener(new MouseMotionAdapter() {

				@Override
				public void mouseDragged(MouseEvent e)
				{
					if (dragFrom.x != 0 && dragFrom.y != 0) {
						int x = e.getXOnScreen() - dragScreen.x;
						int y = e.getYOnScreen() - dragScreen.y;
						// setLocation(dragFrom.x + x, dragFrom.y + y);

						shift(x - lastTranslateX, y - lastTranslateY);
						lastTranslateX = x;
						lastTranslateY = y;
					}
				}

			});

			this.addComponentListener(new ComponentAdapter() {

				@Override
				public void componentMoved(ComponentEvent e)
				{
					e.getComponent().getLocation(center);
					center.x += Node_Radius;
					center.y += Node_Radius;
				}

			});

		}

		@Override
		public boolean equals(Object o)
		{
			boolean is = false;

			if (o instanceof ItemNode) {
				ItemNode n = (ItemNode) o;
				is = this.item.equals(n.item);
			}

			return is;
		}

		public Point getCenter()
		{
			return center;
		}

		public Point getGravityCenterByPeriphery()
		{
			return getGravityCenterByPeriphery(null);
		}

		public Point getGravityCenterByPeriphery(Point point)
		{
			return getGravityCenterByPeriphery(point, ALL_NODE_TYPE);
		}

		public Point getGravityCenterByPeriphery(Point point, int types)
		{
			if (point == null) {
				point = new Point();
			} else {
				point.x = 0;
				point.y = 0;
			}

			int count = 0;

			for (String p : periphery) {
				ItemNode n = getNode(p);
				if (n.isLocated() && (n.getType() & types) != 0) {
					point.x += n.center.x;
					point.y += n.center.y;
					count++;
				}
			}

			count = count == 0 ? 1 : count;

			point.x /= count;
			point.y /= count;

			return point;
		}

		public Point getGravityCenterByPeriphery(Point point, Set<ItemNode> set)
		{
			if (point == null) {
				point = new Point();
			} else {
				point.x = 0;
				point.y = 0;
			}

			int count = 0;

			for (String p : periphery) {
				ItemNode n = getNode(p);
				if (n.isLocated() && set.contains(n)) {
					point.x += n.center.x;
					point.y += n.center.y;
					count++;
				}
			}

			count = count == 0 ? 1 : count;

			point.x /= count;
			point.y /= count;

			return point;
		}

		public UniProtItem getItem()
		{
			return item;
		}

		public int getLocatedPeripheryNumber(int types)
		{
			int count = 0;

			for (String p : periphery) {
				ItemNode node = getNode(p);
				if ((node.getType() & types) != 0 && node.isLocated()) {
					count++;
				}
			}

			return count;
		}

		public Collection<String> getPeriphery()
		{
			return periphery;
		}

		public int getPeripheryNumber(int types)
		{
			int count = 0;

			for (String p : periphery) {
				if ((getNode(p).getType() & types) != 0) {
					count++;
				}
			}

			return count;
		}

		public int getType()
		{
			return type;
		}

		@Override
		public int hashCode()
		{
			return item.hashCode();
		}

		public boolean isFocused()
		{
			return focused;
		}

		public boolean isLocated()
		{
			return located;
		}

		public boolean isLocked()
		{
			return locked;
		}

		private void moveNameplate(int x, int y)
		{
			if (nameplate != null) {
				nameplate.setLocation(nameplate.getX() + x, nameplate.getY() + y);
			}
		}

		private void movePeriphery(int x, int y)
		{
			if (periphery.size() < edges.size()) {

				for (String id : periphery) {
					ItemNode node = getNode(id);
					if (node.periphery != null) {
						if (node.periphery.size() == 1) {
							node.translate(x, y);
						}
					}
				}

			} else {

				for (ItemEdge edge : edges.values()) {

					if (edge.getA() == this) {
						if (edge.getB().periphery.size() == 1) {
							edge.getB().translate(x, y);
						}
					} else if (edge.getB() == this) {
						if (edge.getA().periphery.size() == 1) {
							edge.getA().translate(x, y);
						}
					}
				}
			}
		}

		public void moveTo(int x, int y)
		{
			this.shift(x - center.x, y - center.y);
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			Tools.graphicsAntiAliasing(g);

			if (color == null) {
				int index = 0;
				if (periphery != null) {
					index = Math.max(degrees.indexOf(periphery.size()), 0);
				}
				color = ColorIndexer.getColorRedBlueBounded(0.02 + 0.96 * index
						/ degrees.size());
				focus = color.brighter();
			}

			g.setColor(focused ? focus : color);

			g.fillOval(1, 1, Node_Diameter - 2, Node_Diameter - 2);
		}

		public void setFocused(boolean focused)
		{
			this.focused = focused;
		}

		public void setLocated(boolean located)
		{
			this.located = located;
		}

		public void setPeriphery(Set<String> periphery)
		{
			if (this.periphery == null) {
				this.periphery = periphery;
			} else if (periphery != null) {
				this.periphery.clear();
				this.periphery.addAll(periphery);
			}
		}

		public void setType(int type)
		{
			this.type = type;
		}

		public void shift(int x, int y)
		{
			this.translate(x, y);
			this.movePeriphery(x, y);
		}

		private void showNameplate()
		{
			if (nameplate == null) {
				nameplate = new JLabel(item.getId());
				nameplate.setBackground(canvas.getBackground());
				nameplate.setOpaque(true);
				FontMetrics fm = nameplate.getFontMetrics(nameplate.getFont());
				int width = fm.stringWidth(nameplate.getText()) + 2;
				int height = fm.getHeight();
				nameplate.setSize(width, height);
				nameplate.setHorizontalAlignment(SwingConstants.CENTER);
				nameplate.setVisible(true);
				canvas.add(nameplate);
				nameplate.setBounds(center.x - nameplate.getWidth() / 2,
						(int) (center.y + Node_Radius * 1.5), nameplate.getWidth(),
						nameplate.getHeight());
				canvas.setComponentZOrder(nameplate, 0);
			} else {
				canvas.remove(nameplate);
				nameplate = null;
			}
			canvas.updateUI();
		}

		private void translate(int x, int y)
		{
			if (!locked) {
				center.x += x;
				center.y += y;
				this.setLocation(center.x - Node_Radius, center.y - Node_Radius);
				this.moveNameplate(x, y);
			}
		}
	}

	// 核心结点
	public static final int			CORE_NODE_TYPE			= 1 << 0;

	// 簇状桥接结点
	public static final int			CORE_BRIDGE_NODE_TYPE	= 1 << 1;

	// 桥接结点
	public static final int			BRIDGE_NODE_TYPE		= 1 << 2;

	// 围绕结点
	public static final int			ROUND_NODE_TYPE			= 1 << 3;

	public static final int			ALL_NODE_TYPE			= ~0;

	private static DataBase			DATABASE				= AppBifo.BIFO_DATABASE
																	.clone();

	public static final Cursor		NODE_HOVER_CURSOR		= new Cursor(
																	Cursor.HAND_CURSOR);

	public static int				Node_Radius				= 4;

	public static final int			Node_Diameter			= 2 * Node_Radius + 1;

	public static final Dimension	Node_Size				= new Dimension(
																	Node_Diameter,
																	Node_Diameter);

	public static final Color		EDGE_FOCUS_COLOR		= Color.LIGHT_GRAY;

	public static BasicStroke		INTERACT_STROKE			= new BasicStroke(1.0f);

	public static float[]			DASH_ARRAY				= { 10, 5 };

	public static BasicStroke		PREDICTS_STROKE			= new BasicStroke(
																	1.0f,
																	BasicStroke.CAP_BUTT,
																	BasicStroke.JOIN_BEVEL,
																	0, DASH_ARRAY, 0);

	public static final String EdgeString(String a, String b)
	{
		String string = null;
		if (a.compareTo(b) <= 0) {
			string = a + '\t' + b;
		} else {
			string = b + '\t' + a;
		}
		return string;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	private Interact						interact;

	private JPanel							canvas;

	private Dimension						size;

	private double							interval;

	private Insets							margin;

	// private Collection<String> data;
	// private Map<String, Collection<String>> relation;
	private Set<Relation<String, String>>	relation;

	private int								cores;

	private Set<ItemNode>					coreSet;

	private Map<String, ItemNode>			nodes;

	private Map<String, ItemEdge>			edges;

	private TreeSet<ItemNode>				nodeSorter;

	private Map<String, ItemEdge>			predicts;

	private boolean							absoluteDegrees;

	private List<Integer>					degrees;

	private JPopupMenu						hint;

	private SQLKit							kit;

	private Progressive						progress;

	public InteractDisplayer()
	{
		this.canvas = new JPanel() {

			/**
			 * 
			 */
			private static final long	serialVersionUID	= -7249442539664834172L;

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);

				Graphics2D g2d = Tools.graphicsAntiAliasing(g);

				g2d.setStroke(INTERACT_STROKE);
				for (ItemEdge e : edges.values()) {
					e.draw(g2d);
				}

				g2d.setStroke(PREDICTS_STROKE);
				for (ItemEdge e : predicts.values()) {
					e.draw(g2d);
				}
			}
		};

		this.size = new Dimension(400, 300);

		this.margin = new Insets(0, 0, 0, 0);

		this.canvas.setSize(size);

		this.relation = new LinkedHashSet<Relation<String, String>>();

		this.cores = 1;

		this.coreSet = new HashSet<ItemNode>();

		this.nodes = new HashMap<String, ItemNode>();

		this.edges = new HashMap<String, ItemEdge>();

		this.nodeSorter = new TreeSet<ItemNode>(new Comparator<ItemNode>() {

			@Override
			public int compare(ItemNode o1, ItemNode o2)
			{
				int c = o1.getType() - o2.getType();
				if (c == 0) {
					c = o2.periphery.size() - o1.periphery.size();
					if (c == 0) {
						c = o1.item.compareTo(o2.item);
					}
				}
				return c;
			}

		});

		this.predicts = new HashMap<String, ItemEdge>();

		this.absoluteDegrees = true;

		this.degrees = new ArrayList<Integer>();

		this.hint = new JPopupMenu();

		this.config();

		this.arrange();
	}

	private void arrange()
	{
		this.hint.setLayout(new GridBagLayout());

	}

	private void config()
	{
		canvas.setLayout(null);

		canvas.setBackground(Color.WHITE);

		canvas.setOpaque(true);

		canvas.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e)
			{
				e.getComponent().getSize(size);
			}

		});

	}

	public void display()
	{
		this.makeNodesAndEdges();

		this.generateCores();

		this.layoutNetwork();
	}

	public void exportGraphicsToFile(File file)
	{
		String type = FilesFilter.getFileExtension(file);
		Dimension size = canvas.getSize();
		BufferedImage image = new BufferedImage(size.width, size.height,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		canvas.paint(g);
		g.dispose();

		try {
			ImageIO.write(image, type, file);
			JOptionPane.showMessageDialog(null, "相互作用网络图已经保存", "保存成功",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void focusNode(ItemNode node, boolean focused)
	{
		node.setFocused(focused);
		node.repaint();

		if (node.periphery != null) {
			for (String id : node.periphery) {
				ItemNode n = nodes.get(id);
				if (n != null) {
					n.setFocused(focused);
					n.repaint();
					ItemEdge e = edges.get(InteractDisplayer.EdgeString(
							node.item.getId(), n.item.getId()));
					if (e != null) {
						e.setFocused(focused);
					}
				}
			}
			canvas.repaint();
		}
	}

	// public void display()
	// {
	// if (progress != null) {
	// progress.prepareProgress();
	// }
	// nodes.clear();
	// edges.clear();
	// predicts.clear();
	// canvas.removeAll();
	//
	// if (!UniProtReader.Read) {
	// kit = DATABASE.getSQLKit();
	// }
	//
	// if (progress != null) {
	// progress.resetProgress(data.size());
	// }
	//
	// for (String id : data) {
	// ItemNode node = this.getNode(id, true);
	// this.makePeriphery(node);
	//
	// if (progress != null) {
	// progress.nextProgress();
	// }
	// }
	//
	// if (kit != null) {
	// kit.close();
	// }
	//
	// List<Integer> coreDegrees = degrees;
	// if (!absoluteDegrees) {
	// TreeSet<Integer> sortedCoreDegrees = new TreeSet<Integer>();
	// if (progress != null) {
	// progress.resetProgress(data.size());
	// }
	// for (String id : data) {
	// ItemNode node = this.getNode(id);
	// sortedCoreDegrees.add(node.periphery.size());
	//
	// if (progress != null) {
	// progress.nextProgress();
	// }
	// }
	// coreDegrees = new ArrayList<Integer>(sortedCoreDegrees);
	// }
	//
	// TreeSet<Integer> sortedDegrees = new TreeSet<Integer>();
	// if (progress != null) {
	// progress.resetProgress(nodes.size());
	// }
	// for (ItemNode node : nodes.values()) {
	//
	// sortedDegrees.add(node.periphery.size());
	//
	// if (progress != null) {
	// progress.nextProgress();
	// }
	// }
	// degrees.clear();
	// degrees.addAll(sortedDegrees);
	//
	// double width = size.width - margin.left - margin.right - Node_Diameter;
	// double height = size.height - margin.top - margin.bottom - Node_Diameter;
	// interval = Math.min(width, height) / coreDegrees.size();
	//
	// TreeSet<ItemNode> nodes = new TreeSet<ItemNode>(new
	// Comparator<ItemNode>() {
	//
	// @Override
	// public int compare(ItemNode o1, ItemNode o2)
	// {
	// int c = o2.periphery.size() - o1.periphery.size();
	// if (c == 0) {
	// c = o1.item.compareTo(o2.item);
	// }
	// return c;
	// }
	//
	// });
	// nodes.addAll(this.nodes.values());
	// if (progress != null) {
	// progress.resetProgress(nodes.size());
	// }
	//
	// Point gravityCenter = new Point();
	// int coreIndex = 0;
	// for (ItemNode node : nodes) {
	//
	// int degree = node.periphery.size();
	//
	// double x = 0, y = 0;
	//
	// if (!absoluteDegrees && degree < coreDegrees.get(0)) {
	//
	// // 摆放周边结点
	//
	// double r = Math.min(40, interval) * degree;
	//
	// if (degree == 1) {
	//
	// // 围绕核心结点周围的点
	//
	// double arg = 2 * Math.PI * Math.random();
	// x = r * Math.cos(arg);
	// y = r * Math.sin(arg);
	//
	// if (Math.random() < 0.5) {
	// x -= Math.random() * 0.1 * r;
	// } else {
	// x += Math.random() * 0.1 * r;
	// }
	//
	// if (Math.random() < 0.5) {
	// y -= Math.random() * 0.1 * r;
	// } else {
	// y += Math.random() * 0.1 * r;
	// }
	//
	// } else {
	//
	// // 桥接结点，在相邻点的重心附近，按圆形分布
	// double arg = 2 * Math.PI * Math.random();
	// r = r * degree * Math.exp(-degree) * Math.random();
	// x = r * Math.cos(arg);
	// y = r * Math.sin(arg);
	// }
	//
	// gravityCenter = node.getGravityCenterByPeriphery(gravityCenter);
	//
	// x += gravityCenter.x;
	// y += gravityCenter.y;
	//
	// } else {
	//
	// // 摆放核心结点
	//
	// if (data.size() == 1) {
	//
	// // 只有一个核心，沿椭圆轨迹分层摆放
	//
	// int levels = degrees.size() - 1;
	//
	// double scale = 1.0 * (levels - degrees.indexOf(degree))
	// / Math.max(levels, 1);
	//
	// scale -= Math.random() * 0.05;
	//
	// scale = Math.max(scale, 0.01);
	//
	// double a = scale * width / 2;
	// double b = scale * height / 2;
	//
	// // x = Math.pow(Math.random(), 0.7) * a;
	// // if (Math.random() < 0.5) {
	// // x *= -1;
	// // }
	// //
	// // y = Math.sqrt((1 - (x * x) / (a * a)) * b * b);
	// // if (Math.random() < 0.5) {
	// // y *= -1;
	// // }
	//
	// double arg = 2 * Math.PI * Math.random();
	//
	// double cos = Math.cos(arg);
	//
	// double r = b / Math.sqrt(1 + ((b * b) / (a * a) - 1) * cos * cos);
	//
	// x = r * Math.cos(arg);
	// y = r * Math.sin(arg);
	//
	// } else {
	//
	// // 沿椭圆轨迹分布多个核心
	//
	// double a = width / 2 - interval;
	// double b = height / 2 - interval;
	//
	// double arg = 2 * Math.PI * coreIndex / data.size();
	//
	// double cos = Math.cos(arg);
	//
	// double r = b / Math.sqrt(1 + ((b * b) / (a * a) - 1) * cos * cos);
	//
	// x = r * Math.cos(arg);
	// y = r * Math.sin(arg);
	//
	// coreIndex++;
	// }
	//
	// x += width / 2 + Node_Radius + margin.left;
	// y += height / 2 + Node_Radius + margin.top;
	// }
	//
	// canvas.add(node);
	//
	// node.center.x = (int) x;
	// node.center.y = (int) y;
	//
	// x -= Node_Radius;
	// y -= Node_Radius;
	// node.setBounds((int) x, (int) y, Node_Diameter, Node_Diameter);
	//
	// if (progress != null) {
	// progress.nextProgress();
	// }
	// }
	//
	// canvas.updateUI();
	// canvas.repaint();
	//
	// }

	protected void generateCores()
	{
		TreeSet<Integer> sortDegrees = new TreeSet<Integer>();
		if (progress != null) {
			progress.resetProgress(nodes.values().size());
		}
		for (ItemNode node : nodes.values()) {
			sortDegrees.add(node.periphery.size());
			if (progress != null) {
				progress.nextProgress();
			}
		}

		degrees.clear();
		degrees.addAll(sortDegrees);

		nodeSorter.clear();
		nodeSorter.addAll(nodes.values());

		if (!absoluteDegrees) {

			coreSet.clear();
			if (progress != null) {
				progress.resetProgress(cores);
			}
			for (ItemNode node : nodeSorter) {
				coreSet.add(node);
				if (progress != null) {
					progress.nextProgress();
				}
				if (coreSet.size() == cores) {
					break;
				}
			}

			if (progress != null) {
				progress.resetProgress(nodes.size());
			}
			for (ItemNode node : nodes.values()) {
				int nodeType = 0;
				if (coreSet.contains(node)) {
					// 核心结点
					nodeType = CORE_NODE_TYPE;
				} else {
					// 非核心结点
					if (node.periphery.size() == 1) {
						// 周边结点
						nodeType = ROUND_NODE_TYPE;
					} else {
						// 桥节结点
						nodeType = BRIDGE_NODE_TYPE;
						for (String p : node.periphery) {
							if (getNode(p).periphery.size() == 1) {
								nodeType = CORE_BRIDGE_NODE_TYPE;
								break;
							}
						}
					}
				}
				node.setType(nodeType);
				if (progress != null) {
					progress.nextProgress();
				}
			}
		}
	}

	@Override
	protected InteractDisplayer getAccomplishableSubject()
	{
		return this;
	}

	public Set<String> getBridgeNodes()
	{
		Set<String> nodes = new HashSet<String>();

		for (Entry<String, ItemNode> entry : this.nodes.entrySet()) {
			if (!coreSet.contains(entry.getKey())
					&& entry.getValue().getPeriphery().size() > 1)
			{
				nodes.add(entry.getKey());
			}
		}

		return nodes;
	}

	// protected Collection<String> getData()
	// {
	// return data;
	// }

	public JPanel getCanvas()
	{
		return canvas;
	}

	public int getCores()
	{
		return cores;
	}

	public Map<String, ItemEdge> getEdges()
	{
		return edges;
	}

	// protected ItemNode getNode(String id, boolean loadPeriphery)
	// {
	// ItemNode node = nodes.get(id);
	//
	// if (node == null) {
	//
	// try {
	// kit = this.getSQLKit();
	//
	// UniProtItem item = UniProt.QueryUniProtItem(id, kit);
	//
	// if (item == null) {
	// item = new UniProtItem();
	// item.setId(id);
	// item.setSpecies("");
	// item.setEntry("");
	// }
	//
	// node = new ItemNode(item);
	// nodes.put(id, node);
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// if (loadPeriphery) {
	// this.loadPeriphery(node);
	// }
	//
	// return node;
	// }

	public JPopupMenu getHint()
	{
		return hint;
	}

	public Interact getInteract()
	{
		return interact;
	}

	public Map<String, Set<String>> getInteractMap()
	{
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		for (ItemEdge e : this.getEdges().values()) {
			String a = e.getA().getItem().getId();
			String b = e.getB().getItem().getId();

			Set<String> ligands = map.get(a);
			if (ligands == null) {
				ligands = new HashSet<String>();
				map.put(a, ligands);
			}
			ligands.add(b);

			ligands = map.get(b);
			if (ligands == null) {
				ligands = new HashSet<String>();
				map.put(b, ligands);
			}
			ligands.add(a);
		}

		return map;
	}

	public Insets getMargin()
	{
		return margin;
	}

	protected ItemNode getNode(String id)
	{
		ItemNode node = nodes.get(id);

		if (node == null) {
			try {
				UniProtItem item = UniProt.QueryUniProtItem(id, this.getSQLKit());
				if (item == null) {
					item = new UniProtItem();
					item.setId(id);
					item.setSpecies("");
					item.setEntry("");
				}

				node = new ItemNode(item);
				nodes.put(id, node);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return node;
	}

	// protected Map<String, Collection<String>> getRelation()
	// {
	// return relation;
	// }

	protected Map<String, ItemNode> getNodes()
	{
		return nodes;
	}

	// protected void loadPeriphery(ItemNode node)
	// {
	// node.setPeriphery(Interact.QueryInteractLigands(node.item.getId()));
	// }

	protected Progressive getProgress()
	{
		return progress;
	}

	// protected void makePeriphery(ItemNode node)
	// {
	// for (String id : node.getPeriphery()) {
	// ItemNode n = this.getNode(id, absoluteDegrees);
	// if (n.periphery == null) {
	// n.periphery = new HashSet<String>();
	// }
	// n.periphery.add(node.item.getId());
	// ItemEdge e = new ItemEdge(node, n);
	// edges.put(e.toString(), e);
	// }
	// }

	protected Set<Relation<String, String>> getRelation()
	{
		return relation;
	}

	protected Dimension getSize()
	{
		return size;
	}

	protected SQLKit getSQLKit()
	{
		if (!InteractReader.Read && (kit == null || kit.isClosed())) {
			kit = DATABASE.getSQLKit();
		}
		return kit;
	}

	public boolean isAbsoluteDegrees()
	{
		return absoluteDegrees;
	}

	public void layoutNetwork()
	{
		if (progress != null) {
			progress.prepareProgress();
		}

		// if (progress != null) {
		// progress.resetProgress(data.size());
		// }
		//
		// for (String id : data) {
		// ItemNode node = this.getNode(id, true);
		// this.makePeriphery(node);
		//
		// if (progress != null) {
		// progress.nextProgress();
		// }
		// }

		// if (kit != null) {
		// kit.close();
		// }

		predicts.clear();
		canvas.removeAll();

		for (ItemNode node : this.nodes.values()) {
			node.setLocated(false);
		}

		List<Integer> coreDegrees = degrees;
		if (!absoluteDegrees) {
			TreeSet<Integer> sortedCoreDegrees = new TreeSet<Integer>();
			if (progress != null) {
				progress.resetProgress(relation.size());
			}
			for (ItemNode node : coreSet) {
				sortedCoreDegrees.add(node.periphery.size());

				if (progress != null) {
					progress.nextProgress();
				}
			}
			coreDegrees = new ArrayList<Integer>(sortedCoreDegrees);
		}

		double width = size.width - margin.left - margin.right - Node_Diameter;
		double height = size.height - margin.top - margin.bottom - Node_Diameter;
		interval = Math.min(width, height) / coreDegrees.size();

		Point gravityCenter = new Point();
		int degree, coreIndex = 0;
		double x, y;

		TreeSet<ItemNode> nodeSorter = new TreeSet<ItemNode>(new Comparator<ItemNode>() {

			@Override
			public int compare(ItemNode o1, ItemNode o2)
			{
				int c = 0;
				if (o1.getType() != o2.getType()) {
					if (o1.getType() == CORE_NODE_TYPE) {
						c = -1;
					} else if (o2.getType() == CORE_NODE_TYPE) {
						c = 1;
					}
				}
				if (c == 0) {
					c = o2.getLocatedPeripheryNumber(~ROUND_NODE_TYPE)
							- o1.getLocatedPeripheryNumber(~ROUND_NODE_TYPE);
					if (c == 0) {
						c = o1.getType() - o2.getType();
						if (c == 0) {
							c = o2.periphery.size() - o1.periphery.size();
							if (c == 0) {
								c = o1.item.compareTo(o2.item);
							}
						}
					}
				}
				return c;
			}

		});
		nodeSorter.addAll(this.nodeSorter);
		Set<ItemNode> tempSet = new HashSet<ItemNode>();

		if (progress != null) {
			progress.resetProgress(nodeSorter.size());
		}

		while (!nodeSorter.isEmpty()) {

			ItemNode node = nodeSorter.pollFirst();

			node.setLocated(true);

			tempSet.clear();
			tempSet.addAll(nodeSorter);

			nodeSorter.clear();
			nodeSorter.addAll(tempSet);

			degree = node.periphery.size();
			x = 0;
			y = 0;

			if (!absoluteDegrees && node.getType() != CORE_NODE_TYPE) {

				// 摆放周边结点

				double r = Math.min(40, interval) * degree;

				if (degree == 1) {

					// 围绕核心结点周围的点

					double arg = 2 * Math.PI * Math.random();
					x = r * Math.cos(arg);
					y = r * Math.sin(arg);

					if (Math.random() < 0.5) {
						x -= Math.random() * 0.1 * r;
					} else {
						x += Math.random() * 0.1 * r;
					}

					if (Math.random() < 0.5) {
						y -= Math.random() * 0.1 * r;
					} else {
						y += Math.random() * 0.1 * r;
					}

					gravityCenter = node.getGravityCenterByPeriphery(gravityCenter);

				} else {

					gravityCenter = node.getGravityCenterByPeriphery(gravityCenter,
							~ROUND_NODE_TYPE);
					degree = node.getPeripheryNumber(~ROUND_NODE_TYPE);

					// 桥接结点，在相邻点的重心附近，按圆形分布
					r = r * degree * Math.exp(-degree) * Math.random();

					double arg = 2 * Math.PI * Math.random();
					x = r * Math.cos(arg);
					y = r * Math.sin(arg);

				}

				x += gravityCenter.x;
				y += gravityCenter.y;

			} else {

				// 摆放核心结点

				if (absoluteDegrees || coreSet.size() == 1) {

					// 只有一个核心，周边结点沿椭圆轨迹分层摆放

					int levels = degrees.size() - 1;

					double scale = 1.0 * (levels - degrees.indexOf(degree))
							/ Math.max(levels, 1);

					scale -= Math.random() * 0.05;

					scale = Math.max(scale, 0.01);

					double a = scale * width / 2;
					double b = scale * height / 2;

					// x = Math.pow(Math.random(), 0.7) * a;
					// if (Math.random() < 0.5) {
					// x *= -1;
					// }
					//
					// y = Math.sqrt((1 - (x * x) / (a * a)) * b * b);
					// if (Math.random() < 0.5) {
					// y *= -1;
					// }

					double arg = 2 * Math.PI * Math.random();

					double cos = Math.cos(arg);

					double r = b / Math.sqrt(1 + ((b * b) / (a * a) - 1) * cos * cos);

					x = r * Math.cos(arg);
					y = r * Math.sin(arg);

				} else {

					// 沿椭圆轨迹分布多个核心

					double a = width / 2 - interval;
					double b = height / 2 - interval;

					double arg = 2 * Math.PI * coreIndex / coreSet.size();

					double cos = Math.cos(arg);

					double r = b / Math.sqrt(1 + ((b * b) / (a * a) - 1) * cos * cos);

					x = r * Math.cos(arg);
					y = r * Math.sin(arg);

					coreIndex++;
				}

				x += width / 2 + Node_Radius + margin.left;
				y += height / 2 + Node_Radius + margin.top;
			}

			canvas.add(node);

			node.center.x = (int) x;
			node.center.y = (int) y;

			x -= Node_Radius;
			y -= Node_Radius;
			node.setBounds((int) x, (int) y, Node_Diameter, Node_Diameter);

			if (progress != null) {
				progress.nextProgress();
			}
		}

		canvas.updateUI();
		canvas.repaint();
	}

	protected void makeNodesAndEdges()
	{
		nodes.clear();
		edges.clear();

		if (progress != null) {
			progress.resetProgress(relation.size());
		}

		// // 加载核心结点
		// for (String id : relation.keySet()) {
		// getNode(id);
		// }

		// 加载与核心结点相连的周边结点
		// for (Entry<String, Collection<String>> entry : relation.entrySet()) {

		for (Relation<String, String> r : relation) {

			// String c = entry.getKey();
			String aId = r.getKey();
			String bId = r.getValue();
			ItemNode a = getNode(aId);
			ItemNode b = getNode(bId);

			if (absoluteDegrees) {

				try {

					a.getPeriphery().addAll(
							Interact.QueryInteractLigands(aId, this.getSQLKit()));

					b.getPeriphery().addAll(
							Interact.QueryInteractLigands(bId, this.getSQLKit()));

					String string = EdgeString(aId, bId);
					if (!edges.containsKey(string)) {
						ItemEdge edge = new ItemEdge(a, b);
						edges.put(string, edge);
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}

			} else {

				a.getPeriphery().add(bId);
				b.getPeriphery().add(aId);

				String string = EdgeString(aId, bId);
				if (!edges.containsKey(string)) {
					ItemEdge edge = new ItemEdge(a, b);
					edges.put(string, edge);
				}
			}

			if (progress != null) {
				progress.nextProgress();
			}
		}

	}

	// public void setData(Collection<String> data)
	// {
	// this.data = data;
	// }

	protected void movedNode(ItemNode node)
	{
		// 移动了核心结点，调整相连的桥接结点
		if (node.getType() == CORE_NODE_TYPE || node.getType() == CORE_BRIDGE_NODE_TYPE) {

			Point gravityCenter = new Point();

			for (String p : node.getPeriphery()) {

				ItemNode np = this.getNode(p);

				// 不调整其他核心
				if (!coreSet.contains(np)) {

					// np为桥接结点
					if (np.getType() == BRIDGE_NODE_TYPE) {

						int degree = np.getPeriphery().size();

						double r = Math.min(40, interval) * degree;
						r = r * degree * Math.exp(-degree) * Math.random();
						double arg = 2 * Math.PI * Math.random();
						double x = r * Math.cos(arg);
						double y = r * Math.sin(arg);

						gravityCenter = np.getGravityCenterByPeriphery(gravityCenter);
						x += gravityCenter.x;
						y += gravityCenter.y;

						np.moveTo((int) x, (int) y);
					}
				}
			}
		}

		canvas.repaint();
	}

	protected void removeNode(ItemNode node)
	{
		nodes.remove(node.getItem().getId());
		canvas.remove(node);
		for (String p : node.periphery) {
			ItemNode np = nodes.get(p);
			if (np != null) {
				ItemEdge e = new ItemEdge(node, np);
				edges.remove(e.toString());
				np.periphery.remove(node.getItem().getId());
				if (np.periphery.isEmpty()) {
					nodes.remove(p);
					canvas.remove(np);
				}
			}
		}
		canvas.updateUI();
		canvas.repaint();
	}

	@Override
	public void run()
	{
		this.resetAccomplishStatus();
		this.display();
		this.accomplished();
	}

	public void setAbsoluteDegrees(boolean absoluteDegrees)
	{
		this.absoluteDegrees = absoluteDegrees;
	}

	public void setCores(int cores)
	{
		this.cores = cores;
	}

	public void setInteract(Interact interact)
	{
		this.interact = interact;
	}

	public void setMargin(Insets margin)
	{
		this.margin = margin;
	}

	public void setPredicts(Map<String, Set<String>> predicts)
	{
		this.predicts.clear();

		for (Entry<String, Set<String>> entry : predicts.entrySet()) {
			String rid = entry.getKey();
			for (String lid : entry.getValue()) {
				ItemEdge edge = new ItemEdge(this.getNode(rid), this.getNode(lid));
				this.predicts.put(edge.toString(), edge);
			}
		}

		canvas.repaint();
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

	public void setRelation(Set<Relation<String, String>> relation)
	{
		this.relation.clear();
		this.relation.addAll(relation);
	}

	public void showHint(ItemNode node)
	{
		UniProtItem item = node.item;

		hint.removeAll();

		GridBagConstraints gbc = Tools.makePreferredGridBagConstraints();

		gbc.insets = new Insets(3, 5, 0, 5);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		if (item.getInformation() != null) {
			for (Entry<String, String> entry : item.getInformation().entrySet()) {

				if (gbc.gridy > 0) {
					gbc.insets.top = 0;
				}
				if (gbc.gridy == item.getInformation().size() - 1) {
					gbc.insets.bottom = 3;
				}

				gbc.gridx = 0;
				gbc.weightx = 0.0;
				this.hint.add(new JLabel(entry.getKey()), gbc);

				gbc.gridx++;
				gbc.weightx = 1.0;
				this.hint.add(new JLabel(entry.getValue()), gbc);

				gbc.gridy++;
			}
		}

		Point center = node.getCenter();
		int x, y;
		if (center.x < size.width / 2) {
			x = -hint.getWidth();
		} else {
			x = Node_Diameter;
		}
		if (center.y < size.height / 2) {
			y = -hint.getHeight();
		} else {
			y = Node_Diameter;
		}
		hint.show(node, x, y);

		center = node.getCenter();
		if (center.x < size.width / 2) {
			x = -hint.getWidth();
		} else {
			x = Node_Diameter;
		}
		if (center.y < size.height / 2) {
			y = -hint.getHeight();
		} else {
			y = Node_Diameter;
		}
		hint.show(node, x, y);
	}
}
