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

package de.vonloesch.pdf4eclipse.model.sun;

import java.util.Enumeration;

import com.sun.pdfview.OutlineNode;
import com.sun.pdfview.PDFDestination;
import com.sun.pdfview.action.GoToAction;
import com.sun.pdfview.action.PDFAction;

import de.vonloesch.pdf4eclipse.model.IOutlineNode;
import de.vonloesch.pdf4eclipse.model.IPDFDestination;

public class SunOutlineNode implements IOutlineNode{
	
	private OutlineNode node;

	public SunOutlineNode(OutlineNode node) {
		assert node != null;
		this.node = node;
	}
	
	@Override
	public IOutlineNode[] getChildren() {
		IOutlineNode[] tn = new SunOutlineNode[node.getChildCount()];
		Enumeration<OutlineNode> te = node.children();
		int i = 0;
		while (te.hasMoreElements()) {
			tn[i] = new SunOutlineNode(te.nextElement());
			i++;
		}
		return tn;
	}
	
	@Override
	public IPDFDestination getDestination() {
		PDFAction action = node.getAction();
		if (action instanceof GoToAction) {
			PDFDestination dest = ((GoToAction) action).getDestination();
			return new SunPDFDestination(dest);
		}
		return null;
	}
	
	@Override
	public String getLabel() {
		return node.toString();
	}
	
	@Override
	public IOutlineNode getParent() {
		OutlineNode n = (OutlineNode)node.getParent();
		if (n == null) return null;
		return new SunOutlineNode(n);
	}

	@Override
	public boolean hasChildren() {
		return (node.getChildCount() > 0);
	};
}
