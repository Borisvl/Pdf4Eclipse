package de.vonloesch.pdf4eclipse.model.sun;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.List;

import com.sun.pdfview.ImageInfo;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;
import com.sun.pdfview.RefImage;
import com.sun.pdfview.Watchable;
import com.sun.pdfview.annotation.PDFAnnotation;

import de.vonloesch.pdf4eclipse.Messages;
import de.vonloesch.pdf4eclipse.model.IPDFAnnotation;
import de.vonloesch.pdf4eclipse.model.IPDFPage;

public class SunPDFPage implements IPDFPage {
	
	PDFPage page;
	AffineTransform currentXform, currentTform;
	
	public SunPDFPage(PDFPage page) {
		this.page = page;
	}
	
	@Override
	public BufferedImage getImage(int height, int width) {
    	Dimension pageSize = page.getUnstretchedSize(width, height, null);

    	ImageInfo info = new ImageInfo(pageSize.width, pageSize.height, null, Color.WHITE);

    	PDFRenderer r = null;
    	BufferedImage currentImage = null;
    	
    	//First check if there is already an old renderer
    	if (page.renderers.containsKey(info)) {
    		r = (PDFRenderer) page.renderers.get(info).get();
    		//Note, this is a weak reference, so r could be null
    	}
    	
    	if (r != null) {
    		currentImage = r.getImage();
    	}
    	
    	if (currentImage == null) {
    		//We have no cached image :(
    		currentImage = new RefImage(pageSize.width, pageSize.height,
    				//BufferedImage.TYPE_INT_ARGB);
    				BufferedImage.TYPE_4BYTE_ABGR);

    		page.renderers.clear();
    		r = new PDFRenderer(page, info, (BufferedImage) currentImage);
    		page.renderers.put(info, new WeakReference<PDFRenderer>(r));
    	}
    	// calculate the transform from screen to page space
    	currentTform = page.getInitialTransform(pageSize.width,
    			pageSize.height, null);
    	try {
    		currentXform = currentTform.createInverse();
    	} catch (NoninvertibleTransformException nte) {
    		System.out.println(Messages.PDFPageViewer_Error1);
    		nte.printStackTrace();
    	}

    	//Render the image
    	if (r.getStatus() != Watchable.COMPLETED) {
    		r.go(true);
    		if (r.getStatus() != Watchable.COMPLETED) return null;
    	}
    	return currentImage;
	}
	
	@Override
	public Rectangle2D image2PdfCoordinates(Rectangle2D r) {
    	r.setFrame(r.getX(), r.getY(), 1, 1);
    	Rectangle2D tr = currentXform.createTransformedShape(r).getBounds2D();
    	tr.setFrame(tr.getX(), tr.getY(), tr.getWidth(), tr.getHeight());
    	return tr;    	
	}

	@Override
	public Rectangle2D pdf2ImageCoordinates(Rectangle2D r) {
    	Rectangle2D tr = currentTform.createTransformedShape(r).getBounds2D();
    	tr.setFrame(tr.getX(), tr.getY(), tr.getWidth(), tr.getHeight());
    	return tr;    	
	}
	
	@Override
	public int getPageNumber() {
		return page.getPageNumber();
	}

	@Override
	public float getWidth() {
		return page.getWidth();
	}

	@Override
	public float getHeight() {
		return page.getHeight();
	}
	
	@Override
	public IPDFAnnotation[] getAnnotations() {
		List<PDFAnnotation> annos = page.getAnnots(PDFAnnotation.LINK_ANNOTATION);
		IPDFAnnotation[] annotations = new SunPDFAnnotation[annos.size()];
		for (int i = 0; i < annotations.length; i++) {
			annotations[i] = new SunPDFAnnotation(annos.get(i));
		}
		return annotations;
	}
}
