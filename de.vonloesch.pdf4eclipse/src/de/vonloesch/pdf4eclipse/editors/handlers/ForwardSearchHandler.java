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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import de.vonloesch.pdf4eclipse.editors.PDFEditor;


/**
 * Triggers a forward search in all open pdf editors. Opens the first
 * editor for which the search was successful.
 * 
 * @author Boris von Loesch
 *
 */
public class ForwardSearchHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart e = HandlerUtil.getActiveEditor(event);
		
		if (!(e instanceof ITextEditor)) {
			return null;
		}
		
		//Get file and selection in current editor
		if (e.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) e.getEditorInput();
			String fileName = input.getFile().getRawLocation().toOSString();
			int lineNr = 0;

			ISelection selection = ((ITextEditor) e).getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection)selection;
				lineNr = textSelection.getStartLine() + 1;
			}

			IWorkbenchPage page =  e.getSite().getPage();
			IEditorReference[] ref = page.getEditorReferences();
			for (IEditorReference iEditorReference : ref) {
				if (PDFEditor.ID.equals(iEditorReference.getId())) {
					IEditorPart p = iEditorReference.getEditor(true);
					if (p == null) continue;				
					if (p instanceof PDFEditor) {
						PDFEditor pdfeditor = (PDFEditor) p;
						int returnCode = pdfeditor.forwardSearch(fileName, lineNr);
						if (returnCode == PDFEditor.FORWARD_SEARCH_OK) return null;
					}
				}
			}
			MessageDialog.openInformation(e.getEditorSite().getShell(), 
					"Forward search failed", 
					"The position could not be found in any currently open pdf files.");
		}
		return null;
	}
}
