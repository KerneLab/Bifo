/*
 * $RCSfile: MouseTranslate.java,v $
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 * $Revision: 1.4 $
 * $Date: 2007/02/09 17:20:13 $
 * $State: Exp $
 */

package org.kernelab.bifo.util;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;

/**
 * MouseTranslate is a Java3D behavior object that lets users control the
 * translation (X, Y) of an object via a mouse drag motion with the third mouse
 * button (alt-click on PC). See MouseRotate for similar usage info.
 */

public class CanvasMouseTranslate extends MouseTranslate
{

	Matrix3d						rotate		= new Matrix3d();

	private Matrix3d				tempMatrix	= new Matrix3d();

	Vector3d						translation	= new Vector3d();

	private MouseBehaviorCallback	callback	= null;

	/**
	 * Creates a default translate behavior.
	 */
	public CanvasMouseTranslate()
	{
		super(0);
	}

	/**
	 * Creates a translate behavior that uses AWT listeners and behavior posts
	 * rather than WakeupOnAWTEvent. The behavior is added to the specified
	 * Component. A null component can be passed to specify the behavior should
	 * use listeners. Components can then be added to the behavior with the
	 * addListener(Component c) method.
	 * 
	 * @param c
	 *            The Component to add the MouseListener and MouseMotionListener
	 *            to.
	 * @since Java 3D 1.2.1
	 */
	public CanvasMouseTranslate(Component c)
	{
		super(c, 0);
	}

	/**
	 * Creates a translate behavior that uses AWT listeners and behavior posts
	 * rather than WakeupOnAWTEvent. The behavior is added to the specified
	 * Component. A null component can be passed to specify the behavior should
	 * use listeners. Components can then be added to the behavior with the
	 * addListener(Component c) method. Note that this behavior still needs a
	 * transform group to work on (use setTransformGroup(tg)) and the transform
	 * group must add this behavior.
	 * 
	 * @param flags
	 *            interesting flags (wakeup conditions).
	 * @since Java 3D 1.2.1
	 */
	public CanvasMouseTranslate(Component c, int flags)
	{
		super(c, flags);
	}

	/**
	 * Creates a translate behavior that uses AWT listeners and behavior posts
	 * rather than WakeupOnAWTEvent. The behaviors is added to the specified
	 * Component and works on the given TransformGroup. A null component can be
	 * passed to specify the behavior should use listeners. Components can then
	 * be added to the behavior with the addListener(Component c) method.
	 * 
	 * @param c
	 *            The Component to add the MouseListener and MouseMotionListener
	 *            to.
	 * @param transformGroup
	 *            The TransformGroup to operate on.
	 * @since Java 3D 1.2.1
	 */
	public CanvasMouseTranslate(Component c, TransformGroup transformGroup)
	{
		super(c, transformGroup);
	}

	/**
	 * Creates a translate behavior. Note that this behavior still needs a
	 * transform group to work on (use setTransformGroup(tg)) and the transform
	 * group must add this behavior.
	 * 
	 * @param flags
	 */
	public CanvasMouseTranslate(int flags)
	{
		super(flags);
	}

	/**
	 * Creates a mouse translate behavior given the transform group.
	 * 
	 * @param transformGroup
	 *            The transformGroup to operate on.
	 */
	public CanvasMouseTranslate(TransformGroup transformGroup)
	{
		super(transformGroup);
	}

	public Matrix3d getRotate()
	{
		return rotate;
	}

	void process(MouseEvent evt)
	{
		int id;
		int dx, dy;

		processMouseEvent(evt);

		if (((buttonPress) && ((flags & MANUAL_WAKEUP) == 0))
				|| ((wakeUp) && ((flags & MANUAL_WAKEUP) != 0)))
		{
			id = evt.getID();
			if ((id == MouseEvent.MOUSE_DRAGGED) && !evt.isAltDown() && evt.isMetaDown())
			{

				x = evt.getX();
				y = evt.getY();

				dx = x - x_last;
				dy = y - y_last;

				if ((!reset) && ((Math.abs(dy) < 50) && (Math.abs(dx) < 50))) {

					transformGroup.getTransform(currXform);

					translation.x = dx * this.getXFactor();
					translation.y = -dy * this.getYFactor();
					translation.z = 0;

					tempMatrix.setZero();
					tempMatrix.setColumn(0, translation);
					tempMatrix.mul(rotate, tempMatrix);
					tempMatrix.getColumn(0, translation);

					transformX.set(translation);

					if (invert) {
						currXform.mul(currXform, transformX);
					} else {
						currXform.mul(transformX, currXform);
					}

					transformGroup.setTransform(currXform);

					transformChanged(currXform);

					if (callback != null)
						callback.transformChanged(MouseBehaviorCallback.TRANSLATE,
								currXform);

				} else {
					reset = false;
				}
				x_last = x;
				y_last = y;
			} else if (id == MouseEvent.MOUSE_PRESSED) {
				x_last = evt.getX();
				y_last = evt.getY();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void processStimulus(Enumeration criteria)
	{
		WakeupCriterion wakeup;
		AWTEvent[] events;
		MouseEvent evt;
		// int id;
		// int dx, dy;

		while (criteria.hasMoreElements()) {
			wakeup = (WakeupCriterion) criteria.nextElement();

			if (wakeup instanceof WakeupOnAWTEvent) {
				events = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
				if (events.length > 0) {
					evt = (MouseEvent) events[events.length - 1];
					this.process(evt);
				}
			}

			else if (wakeup instanceof WakeupOnBehaviorPost) {
				while (true) {
					// access to the queue must be synchronized
					synchronized (mouseq) {
						if (mouseq.isEmpty())
							break;
						evt = (MouseEvent) mouseq.remove(0);
						// consolodate MOUSE_DRAG events
						while ((evt.getID() == MouseEvent.MOUSE_DRAGGED)
								&& !mouseq.isEmpty()
								&& (((MouseEvent) mouseq.get(0)).getID() == MouseEvent.MOUSE_DRAGGED))
						{
							evt = (MouseEvent) mouseq.remove(0);
						}
					}
					this.process(evt);
				}
			}

		}
		wakeupOn(mouseCriterion);
	}

	/**
	 * The transformChanged method in the callback class will be called every
	 * time the transform is updated
	 */
	public void setupCallback(MouseBehaviorCallback callback)
	{
		this.callback = callback;
	}

}
