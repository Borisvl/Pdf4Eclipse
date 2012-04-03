package de.vonloesch.pdf4eclipse.model;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public interface IPDFPage {
	/**
	 * Returns the current page as image. The parameters <tt>height</tt> and
	 * <tt>width</tt> are hints for the size of the image. 
	 * The image returned will have at least one of the 
	 * width and height values identical to those requested. 
	 * The other dimension may be smaller, so as to keep the 
	 * aspect ratio the same as in the original page.
	 * @param height
	 * @param width
	 * @return
	 */
	public BufferedImage getImage(int height, int width);
	
	public int getPageNumber();
	
	public float getWidth();
	public float getHeight();
	
	public Rectangle2D image2PdfCoordinates(Rectangle2D r);
	
	public Rectangle2D pdf2ImageCoordinates(Rectangle2D r);
	
	public IPDFAnnotation[] getAnnotations();
}
