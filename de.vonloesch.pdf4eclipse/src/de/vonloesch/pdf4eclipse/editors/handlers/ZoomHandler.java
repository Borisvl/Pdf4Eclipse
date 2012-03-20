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
package de.vonloesch.pdf4eclipse.editors.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.vonloesch.pdf4eclipse.editors.PDFEditor;


/**
 * Handles different zoom request. 
 * 
 * @author Boris von Loesch
 *
 */
public class ZoomHandler extends AbstractHandler {

	private static final String PARAMATER = "PDFViewer.command.parameter.zoom";
	
	private static final String FIT_HORIZONTAL = "fith";
	private static final String FIT_COMPLETE = "fit";

	@Override  
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		if (!PDFEditor.ID.equals(HandlerUtil.getActiveEditorId(event))) return null;

		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor == null) return null;
		
		if (!(editor instanceof PDFEditor)) return null;

		String msg = event.getParameter(PARAMATER);
		if (msg == null) return null;

		float zoomFactor = 1f;	
		if (msg.charAt(0) == '+') {
			zoomFactor = ((PDFEditor) editor).pv.getZoomFactor() + Float.parseFloat(msg.substring(1));
		}
		else if (msg.charAt(0) == '-') {
			zoomFactor = ((PDFEditor) editor).pv.getZoomFactor() - Float.parseFloat(msg.substring(1));
		}
		else if (msg.charAt(0) == '=') {
			zoomFactor = Float.parseFloat(msg.substring(1));
		}
		
		else if (msg.toLowerCase().equals(FIT_HORIZONTAL)) {
			((PDFEditor) editor).fitHorizontal();
			return null;
		}
		else if (msg.toLowerCase().equals(FIT_COMPLETE)) {
			((PDFEditor) editor).fit();
			return null;
		}

		((PDFEditor) editor).pv.setZoomFactor(zoomFactor);
		return null;
	}
}
