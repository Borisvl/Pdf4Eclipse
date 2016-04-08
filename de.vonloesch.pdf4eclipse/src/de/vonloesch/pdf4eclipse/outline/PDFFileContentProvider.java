/*******************************************************************************
 * Copyright (c) 2011 Boris von Loesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Boris von Loesch - initial API and implementation
 ******************************************************************************/
package de.vonloesch.pdf4eclipse.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.vonloesch.pdf4eclipse.model.IOutlineNode;

public class PDFFileContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		IOutlineNode node = (IOutlineNode) parentElement;
		return node.getChildren();
	}

	@Override
	public Object getParent(Object element) {
		IOutlineNode node = (IOutlineNode) element;
		return node.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		IOutlineNode node = (IOutlineNode) element;
		return node.hasChildren();
	}


}
