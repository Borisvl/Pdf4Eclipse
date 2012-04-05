package de.vonloesch.pdf4eclipse.model.jpedal;

import java.awt.geom.Rectangle2D;

import org.jpedal.PdfDecoder;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.OutlineObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

import de.vonloesch.pdf4eclipse.model.IPDFDestination;
import de.vonloesch.pdf4eclipse.model.IPDFLinkAnnotation;


public class JPedalPDFLinkAnnotation implements IPDFLinkAnnotation{

	FormObject annotObj;
	PdfDecoder decoder;
	Rectangle2D r;
	
	public JPedalPDFLinkAnnotation (FormObject annotObj, PdfDecoder decoder) {
		this.annotObj = annotObj;
		this.decoder = decoder;
	}
	
	@Override
	public Rectangle2D getPosition() {
		if (r == null) {
			float[] coords = annotObj.getFloatArray(PdfDictionary.Rect);

			float x = coords[0];
			float w = coords[2] - coords[0];
			
			float y = coords[1];
			float h = coords[3] - coords[1];
			//float y = pageH - coords[1] - h; //note we remove h from y

			r = new Rectangle2D.Float(x, y, w, h);
		}
		return r;
	}

	@Override
	public IPDFDestination getDestination() {
        PdfObject aData = annotObj.getDictionary(PdfDictionary.A);
        if (aData == null) aData = annotObj;
        if (aData.getNameAsConstant(PdfDictionary.S) == PdfDictionary.URI) {
        	String text = aData.getTextStreamValue(PdfDictionary.URI);
        	return new JPedalPDFDestination(text);
        }
        
        PdfArrayIterator destIt = aData.getMixedArray(PdfDictionary.Dest);
        String ref = decoder.getIO().convertNameToRef(destIt.getNextValueAsString(false));
        if (ref != null) {
        	aData=new OutlineObject(ref);
        	decoder.getIO().readObject(aData);
        	destIt = aData.getMixedArray(PdfDictionary.Dest);
        }
        return new JPedalPDFDestination(aData);
	}

}
