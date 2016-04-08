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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sun.pdfview.PDFFile;

import de.vonloesch.pdf4eclipse.Messages;
import de.vonloesch.pdf4eclipse.model.IOutlineNode;
import de.vonloesch.pdf4eclipse.model.IPDFFile;
import de.vonloesch.pdf4eclipse.model.IPDFPage;

public class SunPDFFile implements IPDFFile {

	PDFFile pdfFile;
	File input;
	int pageNumbers;
	
	public SunPDFFile(File file) throws IOException{
		input = file;
		reload();
	}
	
	@Override
	public int getNumPages() {
		return pageNumbers;
	}

	@Override
	public IOutlineNode getOutline() throws IOException{
		if (pdfFile.getOutline() == null) return null;
		return new SunOutlineNode(pdfFile.getOutline());
	}
	
	@Override
	public void reload() throws IOException{
		long len = input.length();
		if (len > Integer.MAX_VALUE) {
			throw new IOException(Messages.PDFEditor_ErrorMsg2 + input.getName());
		}
		int contentLength = (int) len;
		RandomAccessFile ff = new RandomAccessFile(input, "r"); //$NON-NLS-1$
		ByteBuffer buf = ByteBuffer.allocateDirect((int) contentLength);
		FileChannel c = ff.getChannel();
		c.read(buf);
		c.close();
		ff.close();
		pdfFile = new PDFFile(buf);	  
		pageNumbers = pdfFile.getNumPages();
	}
	
	@Override
	public IPDFPage getPage(int pageNr) {
		return new SunPDFPage(pdfFile.getPage(pageNr));
	}
	
	
	@Override
	public void close() {
		pdfFile = null;
	}
	
	public PDFFile getInternalPDFFile() {
		return pdfFile;
	}
}
