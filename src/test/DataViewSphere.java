package test;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.TextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.IndexedLineArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PointLight;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class DataViewSphere extends Applet
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 376661908501083539L;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new MainFrame(new DataViewSphere(), 640, 480);
	}

	PointArray			points; // 声明存放点的点数组变量
	IndexedLineArray	lines;
	PickCanvas			pc;	// 声明用于拾取的画板变量
	TextField			text;	// 文本区

	private BranchGroup createSceneGraph(Canvas3D cv)
	{
		BranchGroup root = new BranchGroup();

		TransformGroup spin = new TransformGroup();
		spin.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		spin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		root.addChild(spin);

		// // 创建坐标轴
		// Transform3D tr = new Transform3D();
		// tr.setScale(0.3);
		// TransformGroup tg = new TransformGroup(tr);
		// spin.addChild(tg);
		// Axes axes = new Axes();
		// tg.addChild(axes);

		// 随机生成20个球面数据点
		int n = 200;
		points = new PointArray(n, PointArray.COORDINATES | PointArray.COLOR_4);// 定义点数组
		points.setCapability(PointArray.ALLOW_COORDINATE_READ);
		points.setCapability(PointArray.ALLOW_FORMAT_READ);
		points.setCapability(PointArray.ALLOW_COLOR_READ);
		points.setCapability(PointArray.ALLOW_COLOR_WRITE);
		points.setCapability(PointArray.ALLOW_COUNT_READ);

		Point3f[] coords = new Point3f[n];
		Color4f[] colors = new Color4f[n];

		float radius = 0.6f;

		for (int i = 0; i < n; i++) { // 随机生成数据点

			double theta = 2 * Math.PI * Math.random();
			double phi = 2 * Math.PI * Math.random();

			coords[i] = new Point3f((float) (radius * Math.sin(theta) * Math.cos(phi)),
					(float) (radius * Math.sin(theta) * Math.sin(phi)),
					(float) (radius * Math.cos(theta)));

			// coords[i] = new Point3f((float) (Math.random() - 0.5),
			// (float) (Math.random() - 0.5), (float) (Math.random() - 0.5));

			colors[i] = new Color4f((float) (Math.random()), (float) (Math.random()),
					(float) (Math.random()), 1f);
		}
		int[] indices = { 0, 1, 0, 2, 2, 3 };
		lines = new IndexedLineArray(coords.length, LineArray.COORDINATES, indices.length);
		lines.setCoordinates(0, coords);
		lines.setCoordinateIndices(0, indices);

		points.setCoordinates(0, coords); // 设置geom的点坐标数组
		points.setColors(0, colors); // 设置geom的颜色坐标数组

		BranchGroup bg = new BranchGroup();
		spin.addChild(bg);
		pc = new PickCanvas(cv, bg); // 初始化PickCanvas对象
		pc.setTolerance(5);
		pc.setMode(PickTool.GEOMETRY_INTERSECT_INFO); // 设置拾取模式

		// 设置外观
		Shape3D shape = null;

		Appearance lineAp = new Appearance();
		lineAp.setLineAttributes(new LineAttributes(1.0f, LineAttributes.PATTERN_SOLID,
				true));
		lineAp.setColoringAttributes(new ColoringAttributes(
				new Color3f(0.3f, 0.3f, 0.3f), ColoringAttributes.SHADE_FLAT));
		shape = new Shape3D(lines, lineAp);
		bg.addChild(shape);

		Appearance pointAp = new Appearance();
		pointAp.setPointAttributes(new PointAttributes(8f, true));
		shape = new Shape3D(points, pointAp); // 创建几何形体
		bg.addChild(shape);

		PickTool.setCapabilities(shape, PickTool.INTERSECT_TEST);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);

		BoundingSphere bounds = new BoundingSphere();

		// 设置鼠标旋转行为
		MouseRotate rotator = new MouseRotate(spin);
		rotator.setSchedulingBounds(bounds);
		spin.addChild(rotator);

		// 设置鼠标平移行为
		MouseTranslate translator = new MouseTranslate(spin);
		translator.setSchedulingBounds(bounds);
		spin.addChild(translator);

		// 设置鼠标缩放行为
		MouseZoom zoom = new MouseZoom(spin);
		zoom.setSchedulingBounds(bounds);
		spin.addChild(zoom);

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

		return root;
	}

	@Override
	public void init()
	{
		GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
		Canvas3D cv = new Canvas3D(gc);
		this.setLayout(new BorderLayout());
		this.add(cv, BorderLayout.CENTER);
		cv.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				pick(e);
			}
		});
		text = new TextField();
		this.add(text, BorderLayout.SOUTH);

		BranchGroup bg = createSceneGraph(cv);
		bg.compile();

		SimpleUniverse su = new SimpleUniverse(cv);
		su.getViewingPlatform().setNominalViewingTransform();
		su.addBranchGraph(bg);
	}

	/*
	 * 拾取操作的处理函数
	 */
	private void pick(MouseEvent e)
	{
		Color4f color = new Color4f();

		pc.setShapeLocation(e);

		PickResult[] results = pc.pickAll();

		if (results != null) {

			for (int i = 0; i < results.length; i++) {

				if (results[i].numIntersections() > 0) {

					PickIntersection inter = results[i].getIntersection(0); // 得到交点

					Point3d pt = inter.getClosestVertexCoordinates();
					int[] ind = inter.getPrimitiveCoordinateIndices(); // 得到被选择点的坐标

					text.setText("Vertex " + ind[0] + ": (" + pt.x + ", " + pt.y + ", "
							+ pt.z + ")"); // 文本字段显示索引和点的坐标

					points.getColor(ind[0], color); // 颜色调整

					color.x = 1f - color.x;
					color.y = 1f - color.y;
					color.z = 1f - color.z;
					if (color.w > 0.8) {
						color.w = 0.5f;
					} else {
						color.w = 1f;
					}

					points.setColor(ind[0], color);

					break; // 只获取Pick中的第一个
				}

			}
		}
	}

}
