package de.vonloesch.pdf4eclipse.model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sun.pdfview.PDFFile;

import de.vonloesch.pdf4eclipse.Messages;

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
	public Object getOutline() throws IOException{
		return pdfFile.getOutline();
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
}
