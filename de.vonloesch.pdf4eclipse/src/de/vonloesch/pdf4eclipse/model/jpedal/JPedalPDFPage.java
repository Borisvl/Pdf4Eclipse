package de.vonloesch.pdf4eclipse.model.jpedal;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;

import de.vonloesch.pdf4eclipse.model.IPDFPage;

public class JPedalPDFPage implements IPDFPage{

	PdfDecoder decoder;
	int pageNr;
	int width;
	int height;
	
	int imgWidth;
	int imgHeight;
	
	public JPedalPDFPage(PdfDecoder decoder, int pageNr) {
		this.decoder = decoder;
		this.pageNr = pageNr;
		PdfPageData data = decoder.getPdfPageData();
		this.width = data.getCropBoxWidth(pageNr);
		this.height = data.getCropBoxHeight(pageNr);
	}
	
	@Override
	public BufferedImage getImage(int height, int width) {		
		try {
	        double ratio = getHeight() / getWidth();
	        double askratio = (double) height / (double) width;
	        if (askratio > ratio) {
	            // asked for something too high
	            height = (int) (width * ratio + 0.5);
	        } else {
	            // asked for something too wide
	            width = (int) (height / ratio + 0.5);
	        }
	        decoder.scaling = height/getHeight();
			BufferedImage img = decoder.getPageAsImage(pageNr);
			imgWidth = img.getWidth();
			imgHeight = img.getHeight();
			return img;
		} catch (PdfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getPageNumber() {
		return pageNr;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public Rectangle2D image2PdfCoordinates(Rectangle2D r) {
		if (imgWidth == 0 || imgHeight == 0) return null;
    	Rectangle2D tr = new Rectangle();
    	tr.setFrame(r.getX()*getWidth()/imgWidth, 
    			getHeight()-r.getY()*getHeight()/imgHeight, 
    			r.getWidth()*getWidth()/imgWidth, 
    			r.getHeight()*getHeight()/imgHeight);
    	return tr;
	}

	@Override
	public Rectangle2D pdf2ImageCoordinates(Rectangle2D r) {
    	Rectangle2D tr = new Rectangle();
    	tr.setFrame(r.getX()*imgWidth/getWidth(), 
    			imgHeight-r.getY()*imgHeight/getHeight(), 
    			r.getWidth()*imgWidth/getWidth(), 
    			-r.getHeight()*imgHeight/getHeight());
    	return tr;
	}

}
