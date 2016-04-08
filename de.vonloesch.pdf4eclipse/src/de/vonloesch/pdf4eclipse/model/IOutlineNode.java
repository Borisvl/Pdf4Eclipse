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

public interface IOutlineNode {
	public String getLabel();
	public IOutlineNode getParent();
	public IOutlineNode[] getChildren();
	public boolean hasChildren();
	public IPDFDestination getDestination();
}
