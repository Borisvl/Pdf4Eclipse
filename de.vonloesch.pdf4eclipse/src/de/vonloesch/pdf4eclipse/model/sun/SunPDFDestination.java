package de.vonloesch.pdf4eclipse.model.sun;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import com.sun.pdfview.PDFDestination;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPage;

import de.vonloesch.pdf4eclipse.model.IPDFDestination;
import de.vonloesch.pdf4eclipse.model.IPDFFile;
import de.vonloesch.pdf4eclipse.model.IPDFPage;


public class SunPDFDestination implements IPDFDestination {

	PDFDestination dest;
	String url;
	int type;

	public SunPDFDestination(PDFDestination dest) {
		this.dest = dest;
		type = IPDFDestination.TYPE_GOTO;
	}
	
	public SunPDFDestination(String url) {
		this.url = url;
		type = IPDFDestination.TYPE_URL;
	}

	@Override
	public IPDFPage getPage(IPDFFile ipdfFile) {
		if (type == IPDFDestination.TYPE_URL) return null;
		
		PDFFile pdfFile = ((SunPDFFile) ipdfFile).pdfFile;
		
		PDFObject o = dest.getPage();
		if (o != null) {
			int pageNr;
			try {
				pageNr = pdfFile.getPageNumber(o) + 1;
				if (pageNr < 1) return null;
				if (pageNr > pdfFile.getNumPages()) 
					pageNr = pdfFile.getNumPages();
				PDFPage page = pdfFile.getPage(pageNr);
				return new SunPDFPage(page);
			}
			catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public Rectangle2D getPosition() {
		if (type == IPDFDestination.TYPE_URL) return null;
		return new Rectangle((int)Math.round(dest.getLeft()), 
				(int)Math.round(dest.getTop()), 1, 1);
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
