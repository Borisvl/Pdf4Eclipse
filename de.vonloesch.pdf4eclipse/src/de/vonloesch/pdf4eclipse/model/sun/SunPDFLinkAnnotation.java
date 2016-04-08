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

import java.awt.geom.Rectangle2D;

import com.sun.pdfview.action.GoToAction;
import com.sun.pdfview.action.UriAction;
import com.sun.pdfview.annotation.LinkAnnotation;
import com.sun.pdfview.annotation.PDFAnnotation;

import de.vonloesch.pdf4eclipse.model.IPDFLinkAnnotation;
import de.vonloesch.pdf4eclipse.model.IPDFDestination;


public class SunPDFLinkAnnotation implements IPDFLinkAnnotation {

	PDFAnnotation annotation;
	
	public SunPDFLinkAnnotation(PDFAnnotation annotation) {
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
		else if (a.getAction() instanceof UriAction) {
			final UriAction action = (UriAction) a.getAction();			
			String uri = action.getUri();
			return new SunPDFDestination(uri);
		}		
		return null;
	}

}
