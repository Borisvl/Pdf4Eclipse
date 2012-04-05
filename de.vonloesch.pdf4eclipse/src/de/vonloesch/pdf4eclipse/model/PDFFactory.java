package de.vonloesch.pdf4eclipse.model;

import java.io.File;
import java.io.IOException;

import org.jpedal.PdfDecoder;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.parser.PdfStreamDecoder;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import de.vonloesch.pdf4eclipse.model.jpedal.JPedalPDFFile;
import de.vonloesch.pdf4eclipse.model.sun.SunPDFFile;


public class PDFFactory {
	
	private static PDFFactory instance;
	
	public final static int STRATEGY_SUN = 1;
	public final static int STRATEGY_JPEDAL = 2;
	public final static int STRATEGY_SUN_JPEDAL = 3;
	
	protected PDFFactory() {
		// Just to hide the default constructor
	}
	
	public static IPDFFile openPDFFile (File file, int strategy) throws IOException {
		if (instance == null) instance = new PDFFactory();
		if (strategy == STRATEGY_SUN) {
			return new SunPDFFile(file);
		}
		else if (strategy == STRATEGY_JPEDAL || strategy == STRATEGY_SUN_JPEDAL) {
			JPedalPDFFile f2 = new JPedalPDFFile(file);
			
			if (strategy == STRATEGY_JPEDAL) return f2;
			
			PdfDecoder decoder = f2.getInternalDecoder();
			decoder.setRenderMode(decoder.RENDERTEXT);
			try {
				decoder.decodePage(1);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			if (decoder.getInfo(PdfDictionary.Font).indexOf("CIDFontType0") > 0) {
				decoder.setRenderMode(7);
				return f2;
			}
			else {
				f2.close();
				return new SunPDFFile(file);
			}
		}
		return null;
	}
}
