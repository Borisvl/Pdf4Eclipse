package de.vonloesch.pdf4eclipse.model.jpedal;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;

import de.vonloesch.pdf4eclipse.Activator;
import de.vonloesch.pdf4eclipse.model.IPDFLinkAnnotation;
import de.vonloesch.pdf4eclipse.model.IPDFPage;

public class JPedalPDFPage implements IPDFPage{

	PdfDecoder decoder;
	int pageNr;
	int width;
	int height;

	int cropx;
	int cropy;

	int imgWidth;
	int imgHeight;
	IPDFLinkAnnotation[] annotations;

	public JPedalPDFPage(PdfDecoder decoder, int pageNr) {
		this.decoder = decoder;
		this.pageNr = pageNr;
		PdfPageData data = decoder.getPdfPageData();
		cropx = data.getCropBoxX(pageNr);
		cropy = data.getCropBoxY(pageNr);
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
			Activator.log("Error while decoding page", e);
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
		tr.setFrame(r.getX()*getWidth()/imgWidth + cropx, 
				getHeight() + cropy - r.getY()*getHeight()/imgHeight, 
				r.getWidth()*getWidth()/imgWidth, 
				r.getHeight()*getHeight()/imgHeight);
		return tr;
	}

	@Override
	public Rectangle2D pdf2ImageCoordinates(Rectangle2D r) {
		Rectangle2D tr = new Rectangle();
		tr.setFrame((r.getX() - cropx)*imgWidth/getWidth(), 
				(height - r.getY() - r.getHeight() + cropy)*imgHeight/getHeight(),
				r.getWidth()*imgWidth/getWidth(), 
				r.getHeight()*imgHeight/getHeight());
		return tr;
	}

	@Override
	public IPDFLinkAnnotation[] getAnnotations() {
		if (annotations != null) return annotations;

		List<IPDFLinkAnnotation> annotationsList = new LinkedList<IPDFLinkAnnotation>();
		PdfArrayIterator annotListForPage = decoder.getFormRenderer().getAnnotsOnPage(pageNr);

		if (annotListForPage != null && annotListForPage.getTokenCount() > 0) { //can have empty lists


			while (annotListForPage.hasMoreTokens()) {

				//get ID of annot which has already been decoded and get actual object
				String annotKey = annotListForPage.getNextValueAsString(true);

				Object[] rawObj = decoder.getFormRenderer().getCompData().getRawForm(annotKey);
				for (int i = 0; i < rawObj.length; i++) {
					if (rawObj[i] != null) {
						//each PDF annot object - extract data from it
						FormObject annotObj = (FormObject) rawObj[i];

						int subtype = annotObj.getParameterConstant(PdfDictionary.Subtype);

						if (subtype == PdfDictionary.Link) {
							annotationsList.add(new JPedalPDFLinkAnnotation(annotObj, decoder));        	
						}
					}
				}
			}
		}
		annotations = annotationsList.toArray(new JPedalPDFLinkAnnotation[0]);
		return annotations;
	}
	
}
