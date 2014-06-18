package org.kernelab.bifo.interact;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PointLight;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import org.kernelab.basis.AbstractAccomplishable;
import org.kernelab.basis.ColorIndexer;
import org.kernelab.basis.Relation;
import org.kernelab.basis.Tools;
import org.kernelab.basis.sql.SQLKit;
import org.kernelab.bifo.interact.InteractDisplayer.ItemEdge;
import org.kernelab.bifo.uniprot.UniProt;
import org.kernelab.bifo.uniprot.UniProtItem;
import org.kernelab.bifo.util.CanvasPickTranslateBehavior;
import org.kernelab.bifo.util.Progressive;

import com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.behaviors.PickingCallback;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class Interact3Displayer extends AbstractAccomplishable<Interact3Displayer> implements Runnable
{

	public class ItemEdge3D
	{
		private ItemNode3D	a;
		private ItemNode3D	b;

		private LineArray	line;

		private float[]		coordinates;

		public ItemEdge3D(ItemNode3D a, ItemNode3D b)
		{
			coordinates = new float[6];
			this.setAB(a, b);
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

		public ItemNode3D getA()
		{
			return a;
		}

		public ItemNode3D getB()
		{
			return b;
		}

		public LineArray getLine()
		{
			if (line == null) {
				makeLine();
			}
			return line;
		}

		@Override
		public int hashCode()
		{
			return toString().hashCode();
		}

		protected LineArray makeLine()
		{
			line = new LineArray(2, LineArray.COORDINATES | LineArray.BY_REFERENCE);
			// line.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
			// line.setCoordinate(0, a.getCenter());
			// line.setCoordinate(1, b.getCenter());
			line.setCapability(LineArray.ALLOW_REF_DATA_READ);
			line.setCapability(LineArray.ALLOW_REF_DATA_WRITE);
			this.refreshEndpoint();
			line.setCoordRefFloat(coordinates);

			return line;
		}

		public void refreshEndpoint()
		{
			coordinates[0] = a.getCenter().x;
			coordinates[1] = a.getCenter().y;
			coordinates[2] = a.getCenter().z;
			coordinates[3] = b.getCenter().x;
			coordinates[4] = b.getCenter().y;
			coordinates[5] = b.getCenter().z;
		}

		private void setAB(ItemNode3D a, ItemNode3D b)
		{
			if (a.item.getId().compareTo(b.item.getId()) <= 0) {
				this.a = a;
				this.b = b;
			} else {
				this.a = b;
				this.b = a;
			}
		}

		@Override
		public String toString()
		{
			return a.item.getId() + '\t' + b.item.getId();
		}

	}

	public class ItemNode3D
	{
		private UniProtItem	item;

		private Set<String>	periphery;

		private String		peripheryString;

		private int			type;

		private boolean		located;

		private Point3f		initial;

		private float[]		coordinate;

		private Point3f		center;

		private float[]		color;

		private PointArray	point;

		public ItemNode3D(UniProtItem item)
		{
			this.item = item;

			this.periphery = new TreeSet<String>();

			this.type = 0;

			this.located = false;

			this.initial = new Point3f();

			this.coordinate = new float[3];

			this.center = new Point3f();

			// this.color = new Color4f();
			this.color = new float[4];
		}

		@Override
		public boolean equals(Object o)
		{
			boolean is = false;

			if (o instanceof ItemNode3D) {
				ItemNode3D n = (ItemNode3D) o;
				is = this.item.equals(n.item);
			}

			return is;
		}

		public Point3d getAngleGravityByPeriphery(Point3d angle)
		{
			if (angle == null) {
				angle = new Point3d();
			} else {
				angle.y = 0;
				angle.z = 0;
			}

			Point3d temp = new Point3d();
			for (String p : periphery) {
				ItemNode3D n = getNode(p);
				OrthogonalToPolar(n.getCenter(), temp, ORIGIN);
				angle.y += temp.y;
				angle.z += temp.z;
			}

			angle.y %= 2 * Math.PI;
			angle.z %= 2 * Math.PI;
			angle.y /= periphery.size();
			angle.z /= periphery.size();

			return angle;
		}

		public Point3f getCenter()
		{
			// if (point != null) {
			// point.getCoordinate(0, center);
			// }
			return center;
		}

		public float[] getColor()
		{
			return color;
		}

		public Point3f getGravityCenterByPeriphery(Point3f point)
		{
			return getGravityCenterByPeriphery(point, ALL_NODE_TYPE);
		}

		public Point3f getGravityCenterByPeriphery(Point3f point, int types)
		{
			if (point == null) {
				point = new Point3f();
			} else {
				point.x = 0;
				point.y = 0;
				point.z = 0;
			}

			int count = 0;

			for (String p : periphery) {
				ItemNode3D n = getNode(p);
				if (n.isLocated() && (n.getType() & types) != 0) {
					point.x += n.center.x;
					point.y += n.center.y;
					point.z += n.center.z;
					count++;
				}
			}

			count = count == 0 ? 1 : count;

			point.x /= count;
			point.y /= count;
			point.z /= count;

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
				ItemNode3D node = getNode(p);
				if ((node.getType() & types) != 0 && node.isLocated()) {
					count++;
				}
			}

			return count;
		}

		public Set<String> getPeriphery()
		{
			return periphery;
		}

		public String getPeripheryString()
		{
			if (peripheryString == null) {
				if (this.type == CORE_NODE_TYPE || this.type == CORE_BRIDGE_NODE_TYPE) {
					peripheryString = item.getId();
				} else {
					StringBuilder sb = new StringBuilder();
					for (String id : periphery) {
						if (getNode(id).periphery.size() == 1) {
							sb.delete(0, sb.length());
							sb.append(item.getId());
							break;
						}
						if (sb.length() != 0) {
							sb.append(' ');
						}
						sb.append(id);
					}
					peripheryString = sb.toString();
				}
			}
			return peripheryString;
		}

		public PointArray getPoint()
		{
			if (point == null) {
				makePoint();
			}
			return point;
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

		protected void initializeCoordinate(float x, float y, float z)
		{
			initial.set(x, y, z);
			this.setCoordinate(initial.x, initial.y, initial.z);
		}

		public boolean isLocated()
		{
			return located;
		}

		protected PointArray makePoint()
		{
			point = new PointArray(1, PointArray.COORDINATES | PointArray.COLOR_4
					| PointArray.BY_REFERENCE);
			point.setCapability(PointArray.ALLOW_COORDINATE_READ);
			point.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
			point.setCapability(PointArray.ALLOW_FORMAT_READ);
			// point.setCapability(PointArray.ALLOW_COLOR_READ);
			// point.setCapability(PointArray.ALLOW_COLOR_WRITE);
			point.setCapability(PointArray.ALLOW_COUNT_READ);

			// point.setCoordinate(0, center);
			// point.setColor(0, color);

			point.setCapability(PointArray.ALLOW_REF_DATA_READ);
			point.setCapability(PointArray.ALLOW_REF_DATA_WRITE);
			point.setCoordRefFloat(coordinate);
			point.setColorRefFloat(color);

			return point;
		}

		public void setColor(float r, float g, float b, float a)
		{
			color[0] = r;
			color[1] = g;
			color[2] = b;
			color[3] = a;
		}

		public void setCoordinate(float x, float y, float z)
		{
			coordinate[0] = x;
			coordinate[1] = y;
			coordinate[2] = z;
			center.set(coordinate);
		}

		public void setCoordinate(Point3f coord)
		{
			this.setCoordinate(coord.x, coord.y, coord.z);
		}

		public void setLocated(boolean located)
		{
			this.located = located;
		}

		public void setPeripheryString(String peripheryString)
		{
			this.peripheryString = peripheryString;
		}

		public void setType(int type)
		{
			this.type = type;
		}

		public Point3f transformCenter(Transform3D tr)
		{
			center.set(initial);
			tr.transform(center);
			// this.setCoordinate(center);
			return center;
		}
	}

	// 核心结点
	public static final int		CORE_NODE_TYPE			= 1 << 0;

	// 簇状桥接结点
	public static final int		CORE_BRIDGE_NODE_TYPE	= 1 << 1;

	// 桥接结点
	public static final int		BRIDGE_NODE_TYPE		= 1 << 2;

	// 围绕结点
	public static final int		ROUND_NODE_TYPE			= 1 << 3;

	public static final int		ALL_NODE_TYPE			= ~0;

	public static final Point3f	ORIGIN					= new Point3f(0f, 0f, 0f);

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

	// public static final Map<String, Collection<String>>
	// loadInteractRelation(String[] ids)
	// {
	// Map<String, Collection<String>> map = new HashMap<String,
	// Collection<String>>();
	//
	// SQLKit kit = Interact.DATABASE.getSQLKit();
	//
	// try {
	// for (String id : ids) {
	// map.put(id, Interact.QueryInteractLigands(id, kit));
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// } finally {
	// kit.close();
	// }
	//
	// return map;
	// }

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		JFrame frame = new JFrame("BiFo - 3Displayer Demo - KerneLab.org");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Image appIcon = Toolkit.getDefaultToolkit().getImage(
				Tools.getClassLoader()
						.getResource("org/kernelab/bifo/resources/logo.png"));
		frame.setIconImage(appIcon);

		Interact3Displayer displayer = new Interact3Displayer();

		frame.setLayout(new BorderLayout());
		frame.add(displayer.getCanvas(), BorderLayout.CENTER);

		frame.setVisible(true);
		frame.setBounds(200, 100, 640, 500);

		String[] ids = { "P62158", "P49407", "P32121", "P01106", "P23508", "P30480",
				"P40337" };
		// String[] ids = { "P62158", "P49407", "P32121", "P01106" };

		Collection<String> cores = new ArrayList<String>();
		for (String id : ids) {
			cores.add(id);
		}

		Set<Relation<String, String>> relation = Interact.LoadInteractRelation(cores,
				null);

		displayer.setRelation(relation);
		displayer.display();
	}

	/*
	 * 把以orgin为原点的直角坐标系下的点坐标，转换为相应的极坐标。
	 * x=polar.x*sin(polar.y)*cos(polar.z)
	 * y=polar.x*sin(polar.y)*sin(polar.z)
	 * z=polar.x*cos(polar.y)
	 * x,y,z是以origin为原点的坐标系下的orth坐标
	 */
	public static final void OrthogonalToPolar(Point3f orth, Point3d polar, Point3f origin)
	{
		double x = orth.x - origin.x;
		double y = orth.y - origin.y;
		double z = orth.z - origin.z;

		polar.x = Math.sqrt(x * x + y * y + z * z);
		polar.y = Math.acos(z / (polar.x + Double.MIN_VALUE));
		polar.z = Math.asin(y
				/ ((polar.x + Double.MIN_VALUE) * Math.sin(polar.y) + Double.MIN_VALUE));
	}

	private Set<Relation<String, String>>	relation;

	private boolean							makeNodesAndEdges;

	private int								cores;

	private Set<ItemNode3D>					coreSet;

	private Map<String, ItemNode3D>			nodes;

	private TreeSet<ItemNode3D>				nodeSorter;

	private List<Integer>					degrees;

	private Map<String, ItemEdge3D>			edges;

	private SQLKit							kit;

	private Progressive						progress;

	private JPanel							canvas;

	private Canvas3D						leftSightCanvas;

	private Canvas3D						rightSightCanvas;

	// private PickCanvas pickCanvas;
	//
	// private PointArray points;

	// private IndexedLineArray lines;

	private Map<String, TransformGroup>		coreGroups;

	private BranchGroup						network;

	private float							spacing;

	// private PickTranslateBehavior picker;
	private CanvasPickTranslateBehavior		picker;

	private Transform3D						pickingTransform;

	private TransformGroup					spin;

	private BranchGroup						root;

	public Interact3Displayer()
	{
		makeNodesAndEdges = false;
		cores = 1;
		coreSet = new HashSet<ItemNode3D>();
		nodes = new HashMap<String, ItemNode3D>();
		nodeSorter = new TreeSet<ItemNode3D>(new Comparator<ItemNode3D>() {

			@Override
			public int compare(ItemNode3D o1, ItemNode3D o2)
			{
				int c = o2.periphery.size() - o1.periphery.size();
				if (c == 0) {
					c = o1.item.compareTo(o2.item);
				}
				return c;
			}

		});
		degrees = new ArrayList<Integer>();
		edges = new HashMap<String, ItemEdge3D>();

		coreGroups = new HashMap<String, TransformGroup>();
		pickingTransform = new Transform3D();
		spin = new TransformGroup();
		root = new BranchGroup();

		config();
	}

	private void config()
	{
		BoundingSphere bounds = new BoundingSphere();

		spin.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		spin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		spin.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		spin.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);

		// 设置鼠标旋转行为
		MouseRotate rotator = new MouseRotate(spin);
		rotator.setSchedulingBounds(bounds);
		rotator.setupCallback(new MouseBehaviorCallback() {

			@Override
			public void transformChanged(int type, Transform3D transform)
			{
				transform.get(picker.getTranslate().getRotate());
				picker.getTranslate().getRotate().invert();
			}

		});
		spin.addChild(rotator);

		// // 设置鼠标平移行为
		// MouseTranslate translator = new MouseTranslate(spin);
		// translator.setSchedulingBounds(bounds);
		// spin.addChild(translator);

		// 设置鼠标缩放行为
		MouseZoom zoom = new MouseZoom(spin);
		zoom.setSchedulingBounds(bounds);
		spin.addChild(zoom);

		root.addChild(spin);

		// 设置背景和光照
		Background background = new Background(1.0f, 1.0f, 1.0f);
		background.setApplicationBounds(bounds);
		root.addChild(background);
		AmbientLight light = new AmbientLight(true, new Color3f(Color.RED));
		light.setInfluencingBounds(bounds);
		root.addChild(light);

		PointLight ptLight = new PointLight(new Color3f(Color.GREEN), new Point3f(3f, 3f,
				3f), new Point3f(1f, 0f, 0f));
		ptLight.setInfluencingBounds(bounds);
		root.addChild(ptLight);

		PointLight ptLight2 = new PointLight(new Color3f(Color.ORANGE), new Point3f(-2f,
				2f, 2f), new Point3f(1f, 0f, 0f));
		ptLight2.setInfluencingBounds(bounds);
		root.addChild(ptLight2);

		// canvas.addMouseListener(new MouseAdapter() {
		//
		// @Override
		// public void mouseClicked(MouseEvent e)
		// {
		// pick(e);
		// }
		//
		// });

		double pd = 0.03; // 瞳距
		double vd = 2.4; // 视距

		GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();

		SimpleUniverse su = new SimpleUniverse(new Canvas3D(gc));
		su.getViewingPlatform().setNominalViewingTransform();

		leftSightCanvas = new Canvas3D(gc);
		BranchGroup leftSight = this.createSight(leftSightCanvas,
				new Point3d(-pd, 0, vd), new Point3d(0, 0, 0), new Vector3d(0, 1, 0));
		su.addBranchGraph(leftSight);

		rightSightCanvas = new Canvas3D(gc);
		BranchGroup rightSight = this.createSight(rightSightCanvas,
				new Point3d(pd, 0, vd), new Point3d(0, 0, 0), new Vector3d(0, 1, 0));
		su.addBranchGraph(rightSight);

		canvas = new JPanel();
		canvas.setLayout(new GridLayout(1, 2));

		canvas.add(leftSightCanvas);
		// canvas.add(rightSightCanvas);
		rightSightCanvas.setVisible(false);

		root.compile();
		su.addBranchGraph(root);
	}

	private BranchGroup createSight(Canvas3D cv, Point3d eye, Point3d center, Vector3d vup)
	{
		View view = new View();
		view.addCanvas3D(cv);
		ViewPlatform vp = new ViewPlatform();
		view.attachViewPlatform(vp);
		view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
		// back/front <= 1000，否则会使得depth buffer溢出而无法正常工作
		view.setFrontClipDistance(0.004);
		view.setBackClipDistance(4.0);
		view.setPhysicalBody(new PhysicalBody());
		view.setPhysicalEnvironment(new PhysicalEnvironment());
		Transform3D trans = new Transform3D();
		trans.lookAt(eye, center, vup);
		trans.invert();
		TransformGroup tg = new TransformGroup(trans);
		tg.addChild(vp);
		BranchGroup sight = new BranchGroup();
		sight.addChild(tg);
		return sight;
	}

	// public void layoutNetwork()
	// {
	// layoutNodes();
	//
	// int size = nodeIndex.size();
	//
	// points = new PointArray(size, PointArray.COORDINATES |
	// PointArray.COLOR_4);
	// points.setCapability(PointArray.ALLOW_COORDINATE_READ);
	// points.setCapability(PointArray.ALLOW_FORMAT_READ);
	// points.setCapability(PointArray.ALLOW_COLOR_READ);
	// points.setCapability(PointArray.ALLOW_COLOR_WRITE);
	// points.setCapability(PointArray.ALLOW_COUNT_READ);
	//
	// Point3f[] coords = new Point3f[size];
	// Color4f[] colors = new Color4f[size];
	//
	// for (int i = 0; i < size; i++) {
	// coords[i] = nodeIndex.get(i).center;
	// colors[i] = nodeIndex.get(i).color;
	// }
	// points.setCoordinates(0, coords);
	// points.setColors(0, colors);
	//
	// int[] indices = new int[2 * edges.size()];
	// int index = 0;
	// for (Edge edge : edges.values()) {
	// indices[index] = edge.a.getIndex();
	// index++;
	// indices[index] = edge.b.getIndex();
	// index++;
	// }
	// lines = new IndexedLineArray(coords.length, LineArray.COORDINATES,
	// indices.length);
	// lines.setCoordinates(0, coords);
	// lines.setCoordinateIndices(0, indices);
	//
	// if (network != null) {
	// spin.removeChild(network);
	// }
	//
	// network = new BranchGroup();
	// network.setCapability(BranchGroup.ALLOW_DETACH);
	// pickCanvas = new PickCanvas(canvas, network);
	// pickCanvas.setTolerance(5);
	// pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO); // 设置拾取模式
	//
	// // 设置外观
	// Shape3D shape = null;
	//
	// Appearance lineAp = new Appearance();
	// lineAp.setLineAttributes(new LineAttributes(1.0f,
	// LineAttributes.PATTERN_SOLID,
	// true));
	// lineAp.setColoringAttributes(new ColoringAttributes(
	// new Color3f(0.3f, 0.3f, 0.3f), ColoringAttributes.SHADE_FLAT));
	// shape = new Shape3D(lines, lineAp);
	// network.addChild(shape);
	//
	// Appearance pointAp = new Appearance();
	// pointAp.setPointAttributes(new PointAttributes(8f, true));
	// shape = new Shape3D(points, pointAp); // 创建几何形体
	// shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
	// PickTool.setCapabilities(shape, PickTool.INTERSECT_TEST); // 允许拾取
	// network.addChild(shape);
	//
	// spin.addChild(network);
	// }

	public void display()
	{
		makeNodesAndEdges();
		generateCores();
		layoutNetwork();
	}

	protected void generateCores()
	{
		TreeSet<Integer> sortDegrees = new TreeSet<Integer>();

		if (progress != null) {
			progress.resetProgress(nodes.values().size());
		}
		for (ItemNode3D node : nodes.values()) {
			sortDegrees.add(node.periphery.size());
			if (progress != null) {
				progress.nextProgress();
			}
		}

		degrees.clear();
		degrees.addAll(sortDegrees);

		nodeSorter.clear();
		nodeSorter.addAll(nodes.values());

		coreSet.clear();
		if (progress != null) {
			progress.resetProgress(cores);
		}
		for (ItemNode3D node : nodeSorter) {
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
		for (ItemNode3D node : nodes.values()) {
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

	@Override
	protected Interact3Displayer getAccomplishableSubject()
	{
		return this;
	}

	public JPanel getCanvas()
	{
		return canvas;
	}

	public int getCores()
	{
		return cores;
	}

	protected Map<String, ItemEdge3D> getEdges()
	{
		return edges;
	}

	public Canvas3D getLeftSightCanvas()
	{
		return leftSightCanvas;
	}

	private ItemNode3D getNode(String id)
	{
		ItemNode3D node = nodes.get(id);

		if (node == null) {
			try {
				UniProtItem item = UniProt.QueryUniProtItem(id, this.getSQLKit());
				if (item == null) {
					item = new UniProtItem();
					item.setId(id);
					item.setSpecies("");
					item.setEntry("");
				}

				node = new ItemNode3D(item);
				nodes.put(id, node);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return node;
	}

	public Canvas3D getRightSightCanvas()
	{
		return rightSightCanvas;
	}

	// private void pick(MouseEvent me)
	// {
	// pickCanvas.setShapeLocation(me);
	//
	// PickResult[] results = pickCanvas.pickAll();
	//
	// if (results != null) {
	//
	// for (int i = 0; i < results.length; i++) {
	//
	// if (results[i].numIntersections() > 0) {
	//
	// PickIntersection inter = results[i].getIntersection(0); // 得到交点
	//
	// int[] ind = inter.getPrimitiveCoordinateIndices(); // 得到被选择点的坐标
	//
	// Tools.debug(ind[0]);
	//
	// break; // 只获取Pick中的第一个
	// }
	// }
	// }
	// }

	protected SQLKit getSQLKit()
	{
		if (!InteractReader.Read && (kit == null || kit.isClosed())) {
			kit = UniProt.DATABASE.getSQLKit();
		}
		return kit;
	}

	public void layoutNetwork()
	{
		layoutNodes();

		if (progress != null) {
			progress.prepareProgress();
		}

		if (network != null) {
			spin.removeChild(network);
		}

		network = new BranchGroup();
		network.setCapability(BranchGroup.ALLOW_DETACH);
		network.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		network.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		network.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		// pickCanvas = new PickCanvas(canvas, network);
		// pickCanvas.setTolerance(5);
		// pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO); // 设置拾取模式

		coreGroups.clear();

		// 设置拾取平移行为
		// PickTranslateBehavior picker = new PickTranslateBehavior(network,
		// canvas,
		// new BoundingSphere(), PickTool.GEOMETRY);
		picker = new CanvasPickTranslateBehavior(network, leftSightCanvas,
				new BoundingSphere(), PickTool.GEOMETRY);
		picker.setupCallback(new PickingCallback() {

			@Override
			public void transformChanged(int type, TransformGroup tg)
			{
				if (tg != null) {
					pickTranslated(tg);
				}
			}

		});
		network.addChild(picker);

		// points = new PointArray(size, PointArray.COORDINATES |
		// PointArray.COLOR_4);
		// points.setCapability(PointArray.ALLOW_COORDINATE_READ);
		// points.setCapability(PointArray.ALLOW_FORMAT_READ);
		// points.setCapability(PointArray.ALLOW_COLOR_READ);
		// points.setCapability(PointArray.ALLOW_COLOR_WRITE);
		// points.setCapability(PointArray.ALLOW_COUNT_READ);

		// Point3f[] coords = new Point3f[size];
		// Color4f[] colors = new Color4f[size];

		RenderingAttributes ra = new RenderingAttributes();
		ra.setDepthBufferEnable(true);
		ra.setDepthBufferWriteEnable(true);
		ra.setDepthTestFunction(RenderingAttributes.LESS);

		Appearance pointAp = new Appearance();
		pointAp.setPointAttributes(new PointAttributes(8f, true));
		pointAp.setRenderingAttributes(ra);

		TreeSet<ItemNode3D> nodes = new TreeSet<ItemNode3D>(new Comparator<ItemNode3D>() {

			@Override
			public int compare(ItemNode3D o1, ItemNode3D o2)
			{
				int c = o2.periphery.size() - o1.periphery.size();
				if (c == 0) {
					c = o1.item.compareTo(o2.item);
				}
				return c;
			}

		});
		nodes.addAll(this.nodes.values());

		if (progress != null) {
			progress.resetProgress(nodes.size());
		}

		for (ItemNode3D node : nodes) {

			// coords[i] = node.center;
			// colors[i] = node.color;

			PointArray point = node.getPoint();

			Shape3D shape = new Shape3D(point, pointAp);

			TransformGroup group = null;

			switch (node.getType())
			{
				case CORE_NODE_TYPE: // 核心结点的组
					group = coreGroups.get(node.getItem().getId());
					if (group == null) {
						group = new TransformGroup();
						group.setUserData(node);
						group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
						group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
						group.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
						PickTool.setCapabilities(shape, PickTool.INTERSECT_FULL); // 允许拾取
						coreGroups.put(node.getItem().getId(), group);
						network.addChild(group);
					}
					break;

				case CORE_BRIDGE_NODE_TYPE:
					String starBridges = node.getPeripheryString();
					group = coreGroups.get(starBridges);
					if (group == null) {
						group = new TransformGroup();
						group.setUserData(node);
						group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
						group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
						group.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
						PickTool.setCapabilities(shape, PickTool.INTERSECT_FULL); // 允许拾取
						coreGroups.put(starBridges, group);
						network.addChild(group);
					}
					break;

				case BRIDGE_NODE_TYPE:
					String bridges = node.getPeripheryString();
					group = coreGroups.get(bridges);
					if (group == null) {
						group = new TransformGroup();
						group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
						group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
						group.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
						PickTool.setCapabilities(shape, PickTool.INTERSECT_FULL); // 允许拾取
						coreGroups.put(bridges, group);
						network.addChild(group);
					}
					break;

				// 非桥接周边结点使用核心结点的组
				case ROUND_NODE_TYPE:
					for (String id : node.getPeriphery()) {
						group = coreGroups.get(id);
					}
					break;

			}

			group.addChild(shape);

			if (progress != null) {
				progress.nextProgress();
			}
		}
		// points.setCoordinates(0, coords);
		// points.setColors(0, colors);

		Appearance lineAp = new Appearance();
		lineAp.setLineAttributes(new LineAttributes(1.0f, LineAttributes.PATTERN_SOLID,
				true));
		lineAp.setColoringAttributes(new ColoringAttributes(
				new Color3f(0.3f, 0.3f, 0.3f), ColoringAttributes.SHADE_FLAT));
		lineAp.setRenderingAttributes(ra);

		if (progress != null) {
			progress.resetProgress(edges.size());
		}

		// int[] indices = new int[2 * edges.size()];
		// int index = 0;
		for (ItemEdge3D edge : edges.values()) {

			Group group = null;

			if (edge.a.type == ROUND_NODE_TYPE || edge.b.type == ROUND_NODE_TYPE) {

				if (edge.a.type == CORE_NODE_TYPE) {
					group = coreGroups.get(edge.a.item.getId());
				} else if (edge.b.type == CORE_NODE_TYPE) {
					group = coreGroups.get(edge.b.item.getId());
				} else if (edge.a.type == CORE_BRIDGE_NODE_TYPE) {
					group = coreGroups.get(edge.a.item.getId());
				} else if (edge.b.type == CORE_BRIDGE_NODE_TYPE) {
					group = coreGroups.get(edge.b.item.getId());
				}
				// }
				// else if (edge.a.type == BRIDGE_NODE_TYPE || edge.b.type ==
				// BRIDGE_NODE_TYPE)
				// {
				// String bridges = null;
				// if (edge.a.type == BRIDGE_NODE_TYPE) {
				// bridges = edge.a.getPeripheryString();
				// } else {
				// bridges = edge.b.getPeripheryString();
				// }
				// group = coreGroups.get(bridges);
			} else {
				// group = new TransformGroup();
				// network.addChild(group);
				group = network;
			}

			// indices[index] = edge.a.getIndex();
			// index++;
			// indices[index] = edge.b.getIndex();
			// index++;
			group.addChild(new Shape3D(edge.getLine(), lineAp));

			if (progress != null) {
				progress.nextProgress();
			}
		}

		// lines = new IndexedLineArray(coords.length, LineArray.COORDINATES,
		// indices.length);
		// lines.setCoordinates(0, coords);
		// lines.setCoordinateIndices(0, indices);
		//
		// Shape3D shape = new Shape3D(lines, lineAp);
		// network.addChild(shape);

		spin.addChild(network);
		spin.setTransform(new Transform3D());
		picker.resetRotate();
	}

	private void layoutNodes()
	{
		if (progress != null) {
			progress.prepareProgress();
		}

		TreeSet<Integer> coreDegrees = new TreeSet<Integer>();

		// for (String id : relation.keySet()) {
		// coreDegrees.add(getNode(id).getPeriphery().size());
		// }

		for (ItemNode3D node : coreSet) {
			coreDegrees.add(node.periphery.size());
		}

		List<Integer> coreSort = new ArrayList<Integer>(coreDegrees.descendingSet());

		float levels = Math.max(coreSort.size() - 1, 1f);

		float region = 0.6f;

		spacing = Math.min(region / levels, 0.1f);

		// Point3d angle = new Point3d();

		// int cores = relation.keySet().size();
		// int corePhiParts = (cores + 1) / 2;
		// int coreThetaParts = Math.max(corePhiParts - 1, 2);
		// double corePhiInterval = 2 * Math.PI / corePhiParts;
		// double coreThetaInterval = Math.PI / coreThetaParts;

		for (ItemNode3D node : nodes.values()) {
			node.setLocated(false);
		}

		TreeSet<ItemNode3D> nodeSorter = new TreeSet<ItemNode3D>(
				new Comparator<ItemNode3D>() {

					@Override
					public int compare(ItemNode3D o1, ItemNode3D o2)
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
		Set<ItemNode3D> tempSet = new HashSet<ItemNode3D>();

		if (progress != null) {
			progress.resetProgress(nodeSorter.size());
		}

		while (!nodeSorter.isEmpty()) {

			ItemNode3D node = nodeSorter.pollFirst();

			node.setLocated(true);

			tempSet.clear();
			tempSet.addAll(nodeSorter);

			nodeSorter.clear();
			nodeSorter.addAll(tempSet);

			int nodeType = node.getType();

			float radius = region;
			double theta = Math.PI * Math.random();
			double phi = 2 * Math.PI * Math.random();

			// 计算中心点和极径
			switch (nodeType)
			{
				case CORE_NODE_TYPE: // 核心结点的中心点为原点
					node.initial.set(0f, 0f, 0f);

					// 1. 以度的排名分层
					// radius *=
					// coreSort.indexOf(node.getPeriphery().size()) /
					// levels;

					// 2. 在不同角区域中分布核心结点
					// phi = (coreCount - 1) * corePhiInterval +
					// Math.random()
					// * corePhiInterval;
					// phi %= 2 * Math.PI;
					//
					// theta = (coreCount / corePhiParts) *
					// coreThetaInterval
					// + Math.random()
					// * Math.min(coreThetaInterval, corePhiInterval);
					// theta %= Math.PI;

					// 3. 球面随机分布

					break;

				case CORE_BRIDGE_NODE_TYPE:
					node.getGravityCenterByPeriphery(node.initial, ~ROUND_NODE_TYPE);
					radius = spacing;
					break;

				case BRIDGE_NODE_TYPE:
					node.getGravityCenterByPeriphery(node.initial, ~ROUND_NODE_TYPE);
					radius = spacing / 2;
					break;

				// 非核心节点的中心为其所连接点构成的重心
				case ROUND_NODE_TYPE:
					node.getGravityCenterByPeriphery(node.initial);
					radius = spacing;
					break;

				// case PERIPHERY_NODE_TYPE:
				// node.getCenterGravityByPeriphery(node.getCenter());
				// radius = spacing;
				// break;
				//
				// case BRIDGE_NODE_TYPE:
				// node.getAngleGravityByPeriphery(angle);
				// node.getCenter().x = (float) (radius *
				// Math.sin(angle.y)
				// *
				// Math
				// .cos(angle.z));
				// node.getCenter().y = (float) (radius *
				// Math.sin(angle.y)
				// *
				// Math
				// .sin(angle.z));
				// node.getCenter().z = (float) (radius *
				// Math.cos(angle.y));
				// radius = spacing / 2;
				// break;
			}

			node.initial.x += radius * Math.sin(theta) * Math.cos(phi);
			node.initial.y += radius * Math.sin(theta) * Math.sin(phi);
			node.initial.z += radius * Math.cos(theta);

			node.initializeCoordinate(node.initial.x, node.initial.y, node.initial.z);

			int degree = Math.max(degrees.indexOf(node.getPeriphery().size()), 0);
			Color color = ColorIndexer.getColorRedBlueBounded(0.02 + 0.96 * degree
					/ Math.max(degrees.size() - 1, 1));

			node.setColor(color.getRed() / 255f, color.getGreen() / 255f,
					color.getBlue() / 255f, color.getAlpha() / 255f);

			if (progress != null) {
				progress.nextProgress();
			}
		}

	}

	protected void makeNodesAndEdges()
	{
		if (makeNodesAndEdges) {
			makeNodesAndEdges = false;

			nodes.clear();
			edges.clear();

			if (progress != null) {
				progress.resetProgress(relation.size());
			}

			// 加载核心结点
			// for (String id : relation.keySet()) {
			// getNode(id);
			// }

			// 加载与核心结点相连的周边结点
			// for (Entry<String, Collection<String>> entry :
			// relation.entrySet()) {
			for (Relation<String, String> r : relation) {

				String aId = r.getKey();
				String bId = r.getValue();
				ItemNode3D a = getNode(aId);
				ItemNode3D b = getNode(bId);

				a.getPeriphery().add(bId);
				b.getPeriphery().add(aId);

				String string = EdgeString(aId, bId);
				if (!edges.containsKey(string)) {
					ItemEdge3D edge = new ItemEdge3D(a, b);
					edges.put(string, edge);
				}

				if (progress != null) {
					progress.nextProgress();
				}
			}

		}
	}

	private void pickTranslated(TransformGroup tg)
	{
		if (tg.getUserData() instanceof ItemNode3D) {

			tg.getTransform(pickingTransform);
			ItemNode3D node = (ItemNode3D) tg.getUserData();
			node.transformCenter(pickingTransform);

			Point3f gravity = new Point3f();

			for (String id : node.getPeriphery()) {

				ItemNode3D n = getNode(id);

				if (n.getType() == BRIDGE_NODE_TYPE) {
					// 更新桥接结点坐标
					n.getGravityCenterByPeriphery(gravity);

					double theta = 2 * Math.PI * Math.random();
					double phi = 2 * Math.PI * Math.random();
					gravity.x += spacing / 2 * Math.sin(theta) * Math.cos(phi);
					gravity.y += spacing / 2 * Math.sin(theta) * Math.sin(phi);
					gravity.z += spacing / 2 * Math.cos(theta);
					n.setCoordinate(gravity);

					// 更新桥接边的端点
					for (String p : n.getPeriphery()) {
						ItemEdge3D edge = edges.get(EdgeString(id, p));
						if (edge != null) {
							edge.refreshEndpoint();
						}
					}
				}

				if (n.getType() != ROUND_NODE_TYPE) {
					ItemEdge3D edge = edges.get(EdgeString(id, node.getItem().getId()));
					if (edge != null) {
						edge.refreshEndpoint();
					}
				}
			}
		}
	}

	@Override
	public void run()
	{
		resetAccomplishStatus();

		display();

		accomplished();
	}

	public void setCores(int cores)
	{
		this.cores = cores;
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

	public void setRelation(Set<Relation<String, String>> relation)
	{
		this.relation = relation;
		makeNodesAndEdges = true;
	}

	public void switchStereoMode()
	{
		Canvas3D a, b;
		if (canvas.getComponent(0) == leftSightCanvas) {
			a = rightSightCanvas;
			b = leftSightCanvas;
		} else {
			a = leftSightCanvas;
			b = rightSightCanvas;
		}
		canvas.removeAll();
		canvas.add(a);
		canvas.add(b);
	}

	public void toggleStereoMode()
	{
		if (rightSightCanvas.isVisible()) {
			canvas.remove(rightSightCanvas);
		} else {
			canvas.add(rightSightCanvas);
		}
		rightSightCanvas.setVisible(!rightSightCanvas.isVisible());
		canvas.updateUI();
	}
}
