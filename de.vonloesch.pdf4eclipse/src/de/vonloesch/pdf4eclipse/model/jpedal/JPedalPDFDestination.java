package de.vonloesch.pdf4eclipse.model.jpedal;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.jpedal.PdfDecoder;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

import de.vonloesch.pdf4eclipse.model.IPDFDestination;
import de.vonloesch.pdf4eclipse.model.IPDFPage;


public class JPedalPDFDestination implements IPDFDestination{

	PdfObject dest;
	PdfDecoder decoder;

	public JPedalPDFDestination(PdfObject o, PdfDecoder d) {
		dest = o;
		decoder = d;
	}

	@Override
	public IPDFPage getPage() {
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

}
