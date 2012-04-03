package de.vonloesch.pdf4eclipse.model.sun;

import java.awt.geom.Rectangle2D;

import com.sun.pdfview.action.GoToAction;
import com.sun.pdfview.annotation.LinkAnnotation;
import com.sun.pdfview.annotation.PDFAnnotation;

import de.vonloesch.pdf4eclipse.model.IPDFAnnotation;
import de.vonloesch.pdf4eclipse.model.IPDFDestination;


public class SunPDFAnnotation implements IPDFAnnotation {

	PDFAnnotation annotation;
	
	public SunPDFAnnotation(PDFAnnotation annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public Rectangle2D getPosition() {
		return annotation.getRect();
	}

	@Override
	public IPDFDestination getDestination() {
		LinkAnnotation a = (LinkAnnotation) annotation;
		if (a.getAction() instanceof GoToAction){
			final GoToAction action = (GoToAction) a.getAction();
			return new SunPDFDestination(action.getDestination());
		}
		return null;
	}

}
