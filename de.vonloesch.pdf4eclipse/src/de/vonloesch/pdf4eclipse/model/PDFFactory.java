package de.vonloesch.pdf4eclipse.model;

import java.io.File;
import java.io.IOException;

import org.jpedal.PdfDecoder;
import org.jpedal.objects.raw.PdfDictionary;

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
	
	/**
	 * Returns a pdf file. If strategy is set to <tt>STRATEGY_SUN_JPEDAL</tt> then try to
	 * open the file with the sun renderer but use jpedal when CIDType0 fonts are used 
	 * in the pdf.
	 * @param file
	 * @param strategy 
	 * @return
	 * @throws IOException
	 */
	public static IPDFFile openPDFFile (File file, int strategy) throws IOException {
		if (instance == null) instance = new PDFFactory();
		if (strategy == STRATEGY_SUN) {
			return new SunPDFFile(file);
		}
		else if (strategy == STRATEGY_JPEDAL || strategy == STRATEGY_SUN_JPEDAL) {
			JPedalPDFFile f2 = new JPedalPDFFile(file);
			
			if (strategy == STRATEGY_JPEDAL) return f2;
			
			PdfDecoder decoder = f2.getInternalDecoder();
			decoder.setRenderMode(PdfDecoder.TEXT);

			boolean hasCIDFont = false;
			try {
				//long t = System.currentTimeMillis();
				for (int i=1; i <= decoder.getPageCount(); i++) {
					decoder.decodePage(i);
					if (decoder.getInfo(PdfDictionary.Font).indexOf("CIDFontType0") > 0) {
						hasCIDFont = true;
						break;
					}
				}
				//System.out.println("Detection time: "+(System.currentTimeMillis() - t));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			if (hasCIDFont) {
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
