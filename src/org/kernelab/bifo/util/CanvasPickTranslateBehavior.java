package org.kernelab.bifo.util;

import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import com.sun.j3d.utils.behaviors.mouse.MouseBehavior;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.behaviors.PickTranslateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickingCallback;

/**
 * A mouse behavior that allows user to pick and translate scene graph objects.
 * Common usage: 1. Create your scene graph. 2. Create this behavior with the
 * root and canvas. See PickRotateBehavior for more details.
 */

public class CanvasPickTranslateBehavior extends PickTranslateBehavior
{
	CanvasMouseTranslate	translate;
	private PickingCallback	callback	= null;
	private TransformGroup	currentTG;

	/**
	 * Creates a pick/translate behavior that waits for user mouse events for
	 * the scene graph.
	 * 
	 * @param root
	 *            Root of your scene graph.
	 * @param canvas
	 *            Java 3D drawing canvas.
	 * @param bounds
	 *            Bounds of your scene.
	 * @param pickMode
	 *            specifys PickTool.BOUNDS, PickTool.GEOMETRY or
	 *            PickTool.GEOMETRY_INTERSECT_INFO.
	 * @see PickTool#setMode
	 **/
	public CanvasPickTranslateBehavior(BranchGroup root, Canvas3D canvas, Bounds bounds,
			int pickMode)
	{
		super(root, canvas, bounds, pickMode);
		translate = new CanvasMouseTranslate(MouseBehavior.MANUAL_WAKEUP);
		translate.setTransformGroup(currGrp);
		translate.rotate.setIdentity();
		currGrp.addChild(translate);
		translate.setSchedulingBounds(bounds);
		this.setSchedulingBounds(bounds);
		this.setMode(pickMode);
	}

	public CanvasMouseTranslate getTranslate()
	{
		return translate;
	}

	public void resetRotate()
	{
		translate.rotate.setIdentity();
	}

	/**
	 * Register the class @param callback to be called each time the picked
	 * object moves
	 */
	public void setupCallback(PickingCallback callback)
	{
		this.callback = callback;
		if (callback == null) {
			translate.setupCallback(null);
		} else {
			translate.setupCallback(this);
		}
	}

	/**
	 * Callback method from MouseTranslate This is used when the Picking
	 * callback is enabled
	 */
	public void transformChanged(int type, Transform3D transform)
	{
		callback.transformChanged(PickingCallback.TRANSLATE, currentTG);
	}

	/**
	 * Update the scene to manipulate any nodes. This is not meant to be called
	 * by users. Behavior automatically calls this. You can call this only if
	 * you know what you are doing.
	 * 
	 * @param xpos
	 *            Current mouse X pos.
	 * @param ypos
	 *            Current mouse Y pos.
	 **/
	public void updateScene(int xpos, int ypos)
	{
		TransformGroup tg = null;

		if (!mevent.isAltDown() && mevent.isMetaDown()) {

			pickCanvas.setShapeLocation(xpos, ypos);
			PickResult pr = pickCanvas.pickClosest();
			if ((pr != null)
					&& ((tg = (TransformGroup) pr.getNode(PickResult.TRANSFORM_GROUP)) != null)
					&& (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_READ))
					&& (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE)))
			{
				translate.setTransformGroup(tg);
				translate.wakeup();
				currentTG = tg;
			} else if (callback != null)
				callback.transformChanged(PickingCallback.NO_PICK, null);
		}

	}

}
