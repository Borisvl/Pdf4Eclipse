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

	public SunPDFDestination(PDFDestination dest) {
		this.dest = dest;
	}

	@Override
	public IPDFPage getPage(IPDFFile ipdfFile) {
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
		return new Rectangle((int)Math.round(dest.getLeft()), 
				(int)Math.round(dest.getTop()), 1, 1);
	}

}
