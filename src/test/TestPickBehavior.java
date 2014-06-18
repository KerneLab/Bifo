package test;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.TextArea;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PointLight;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.behaviors.PickRotateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickTranslateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickZoomBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class TestPickBehavior extends Applet
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8595497807628748058L;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new MainFrame(new TestPickBehavior(), 480, 480);
	}

	private Node createObject()
	{
		Transform3D trans = new Transform3D();
		trans.setTranslation(new Vector3d(Math.random() - 0.5, Math.random() - 0.5, Math
				.random() - 0.5));
		trans.setScale(0.1);

		TransformGroup spin = new TransformGroup(trans);
		spin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		spin.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		spin.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

		Appearance ap = new Appearance();
		ap.setMaterial(new Material());
		Shape3D shape = new ColorCube();
		shape.setAppearance(ap);

		PickTool.setCapabilities(shape, PickTool.INTERSECT_FULL);
		spin.addChild(shape);

		return spin;
	}

	private BranchGroup createSceneGraph(Canvas3D cv)
	{
		BranchGroup root = new BranchGroup();

		for (int i = 0; i < 8; i++) {
			root.addChild(createObject());
		}

		BoundingSphere bounds = new BoundingSphere();

		// 设置拾取旋转行为
		PickRotateBehavior rotator = new PickRotateBehavior(root, cv, bounds,
				PickTool.GEOMETRY);
		root.addChild(rotator);

		// 设置拾取平移行为
		PickTranslateBehavior translator = new PickTranslateBehavior(root, cv, bounds,
				PickTool.GEOMETRY);
		root.addChild(translator);

		// 设置拾取缩放行为
		PickZoomBehavior zoom = new PickZoomBehavior(root, cv, bounds, PickTool.GEOMETRY);
		root.addChild(zoom);

		// 设置光照
		AmbientLight light = new AmbientLight(true, new Color3f(Color.BLUE));
		light.setInfluencingBounds(bounds);
		root.addChild(light);

		PointLight ptLight = new PointLight(new Color3f(Color.WHITE), new Point3f(0f, 0f,
				2f), new Point3f(1f, 0.3f, 0f));
		ptLight.setInfluencingBounds(bounds);
		root.addChild(ptLight);

		// 设置背景
		Background background = new Background(1.0f, 1.0f, 1.0f);
		background.setApplicationBounds(bounds);
		root.addChild(background);

		return root;
	}

	@Override
	public void init()
	{
		GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
		Canvas3D cv = new Canvas3D(gc);
		this.setLayout(new BorderLayout());
		this.add(cv, BorderLayout.CENTER);

		TextArea ta = new TextArea("", 3, 30, TextArea.SCROLLBARS_NONE);
		ta.setText("Rotation: Drag with left button\n");
		ta.append("Translation: Drag with right button\n");
		ta.append("Zoom: Hold Alt key and drag with left button");
		ta.setEditable(false);
		this.add(ta, BorderLayout.SOUTH);

		BranchGroup bg = createSceneGraph(cv);
		bg.compile();
		SimpleUniverse su = new SimpleUniverse(cv);
		su.getViewingPlatform().setNominalViewingTransform();
		su.addBranchGraph(bg);
	}

}
