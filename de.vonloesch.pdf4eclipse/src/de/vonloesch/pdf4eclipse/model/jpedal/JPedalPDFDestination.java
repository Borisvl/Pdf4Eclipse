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

package de.vonloesch.pdf4eclipse.model.jpedal;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.jpedal.PdfDecoder;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

import de.vonloesch.pdf4eclipse.model.IPDFDestination;
import de.vonloesch.pdf4eclipse.model.IPDFFile;
import de.vonloesch.pdf4eclipse.model.IPDFPage;


public class JPedalPDFDestination implements IPDFDestination{

	PdfObject dest;
	String url;
	int type;
	
	public JPedalPDFDestination(PdfObject o) {
		dest = o;
		type = IPDFDestination.TYPE_GOTO;
	}
	
	public JPedalPDFDestination(String url) {
		this.url = url;
		type = IPDFDestination.TYPE_URL;
	}

	@Override
	public IPDFPage getPage(IPDFFile pdfFile) {
		if (type == IPDFDestination.TYPE_URL) return null;
		
		PdfDecoder decoder = ((JPedalPDFFile) pdfFile).decoder;
		PdfArrayIterator destIt = dest.getMixedArray(PdfDictionary.Dest);
		String ref = dest.getObjectRefAsString();
		int pageNr = -1;
		if (destIt != null && destIt.getTokenCount() > 0) {
			
			int possiblePage = destIt.getNextValueAsInteger(false) + 1;
			ref = destIt.getNextValueAsString(true);

			//convert to target page if ref or ignore

			if(ref.endsWith(" R")) pageNr = decoder.getPageFromObjectRef(ref);
			else if(possiblePage > 0) { //can also be a number
				pageNr = possiblePage;
			}

			//allow for named destinations
			if(pageNr == -1){
				String newRef = decoder.getIO().convertNameToRef(ref);


				if(newRef!=null && newRef.endsWith(" R"))
					pageNr = decoder.getPageFromObjectRef(newRef);
				//if(count>0 && destIt.hasMoreTokens() && destIt.isNextValueRef())
				//	ref = destIt.getNextValueAsString(true);
			}
			
		}
		
		if (pageNr == -1) return null;
		return new JPedalPDFPage(decoder, pageNr);
	}

	@Override
	public Rectangle2D getPosition() {
		if (type == IPDFDestination.TYPE_URL) return null;
		
		PdfArrayIterator destIt = dest.getMixedArray(PdfDictionary.Dest);
		//Try to get position
		if (destIt != null && destIt.getTokenCount() > 1) {
			destIt.getNextValueAsString(true);
			int type = destIt.getNextValueAsConstant(true);
			if (type == PdfDictionary.XYZ) {
				float x = destIt.getNextValueAsFloat();
				float y = destIt.getNextValueAsFloat();

				//third value is zoom which is not implemented yet

				//create Rectangle to scroll to
				return new Rectangle((int)x,(int)y,10,10);
			}
		}
		return null;
	}
	
	@Override
	public int getType() {
		return type;
	}

	@Override
	public String getURL() {
		return url;
	}
}
