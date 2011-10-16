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
package de.vonloesch.pdf4eclipse.editors;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.NavigationLocation;

public class PDFNavigationLocation extends NavigationLocation {

	int page;
	Point origin;
		
	public PDFNavigationLocation(IEditorPart editor) {
		super(editor);
		update();
	}
	
	@Override
	public void update() {
		if (getEditorPart() instanceof PDFEditor) {
			PDFEditor editor = (PDFEditor) getEditorPart();
			page = editor.currentPage;
			origin = editor.getOrigin();			
		}
	}
	
	@Override
	public void saveState(IMemento memento) {
		memento.putInteger("Page", page); //$NON-NLS-1$
	}
	
	@Override
	public void restoreState(IMemento memento) {
		page = memento.getInteger("Page"); //$NON-NLS-1$
		restoreLocation();
	}
	
	@Override
	public void restoreLocation() {
		if (getEditorPart() instanceof PDFEditor) {
			PDFEditor editor = (PDFEditor) getEditorPart();
			editor = (PDFEditor) getEditorPart();
			editor.showPage(page);
			editor.setOrigin(origin);
		}
	}
	
	@Override
	public boolean mergeInto(INavigationLocation currentLocation) {
		if (currentLocation instanceof PDFNavigationLocation) {
			PDFNavigationLocation loc = (PDFNavigationLocation) currentLocation;
			if (loc.page == page && origin.equals(loc.origin)) return true;
		}
		return false;
	}
}
