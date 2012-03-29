package de.vonloesch.pdf4eclipse.model;

import java.io.IOException;

public interface IPDFFile {
	/**
	 * Return the number of pages in this PDFFile. 
	 * The pages will be numbered from 1 to getNumPages(), inclusive.
	 * @return
	 */
	int getNumPages();
	
	Object getOutline()  throws IOException;
	
	void reload() throws IOException;
	
	IPDFPage getPage(int pageNr);
}
