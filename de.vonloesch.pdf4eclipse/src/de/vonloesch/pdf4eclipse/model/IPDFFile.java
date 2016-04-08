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

import java.io.IOException;

public interface IPDFFile {
	
	/**
	 * Return the number of pages in this PDFFile. 
	 * The pages will be numbered from 1 to getNumPages(), inclusive.
	 * @return
	 */
	int getNumPages();
	
	/**
	 * Returns the root node of the pdf bookmarks
	 * @return <tt>null</tt>, if the renderer does not support bookmarks
	 * @throws IOException
	 */
	IOutlineNode getOutline()  throws IOException;
	
	void reload() throws IOException;
	
	IPDFPage getPage(int pageNr);
	
	void close();
}
