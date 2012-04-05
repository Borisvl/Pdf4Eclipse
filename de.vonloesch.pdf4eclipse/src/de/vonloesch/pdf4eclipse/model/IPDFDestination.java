package de.vonloesch.pdf4eclipse.model;

import java.awt.geom.Rectangle2D;

public interface IPDFDestination {
	
	public final static int TYPE_URL = 1;
	public final static int TYPE_GOTO = 2;
	
	/**
	 * If the destination is of type <tt>TYPE_GOTO</tt>, returns the page
	 * in the pdf of this destination. Otherwise returns <tt>null</tt>.
	 * @param pdfFile
	 * @return
	 */
	IPDFPage getPage(IPDFFile pdfFile);
	
	/**
	 * If the destination is of type <tt>TYPE_GOTO</tt>, returns the position
	 * on the page of this destination. Otherwise returns <tt>null</tt>.
	 * @return  
	 */
	Rectangle2D getPosition();
	
	/**
	 * Returns the type of the destination.  
	 * @return is either <tt>TYPE_URL</tt> or <tt>TYPE_GOTO</tt>
	 */
	int getType();
	
	/**
	 * Returns the url of the destination if destination is of type <tt>TYPE_URL</tt>.
	 * Otherwise returns <tt>null</tt>.
	 * @return
	 */
	String getURL();
}
