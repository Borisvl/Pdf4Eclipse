/*******************************************************************************
 * Copyright (c) 2012 Boris von Loesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Boris von Loesch - initial API and implementation
 ******************************************************************************/

package de.vonloesch.pdf4eclipse.model;

import java.awt.geom.Rectangle2D;


public interface IPDFLinkAnnotation {
	
	/**
	 * The coordinates of the annotation on the page.
	 * @return
	 */
	public Rectangle2D getPosition();
	
	/**
	 * The destination where this annotation links to.
	 * @return
	 */
	public IPDFDestination getDestination();
}
