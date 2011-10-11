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

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

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
		TreeNode node = (TreeNode) parentElement;
		TreeNode[] tn = new TreeNode[node.getChildCount()];
		Enumeration<TreeNode> te = node.children();
		int i = 0;
		while (te.hasMoreElements()) {
			tn[i] = te.nextElement();
			i++;
		}
		return tn;
	}

	@Override
	public Object getParent(Object element) {
		TreeNode node = (TreeNode) element;
		return node.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		TreeNode node = (TreeNode) element;
		return (node.getChildCount() > 0);
	}


}
