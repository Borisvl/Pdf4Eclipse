package de.vonloesch.pdf4eclipse.model.jpedal;

import java.io.File;
import java.io.IOException;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.w3c.dom.Node;

import de.vonloesch.pdf4eclipse.model.IOutlineNode;
import de.vonloesch.pdf4eclipse.model.IPDFFile;
import de.vonloesch.pdf4eclipse.model.IPDFPage;

public class JPedalPDFFile implements IPDFFile {
	File input;
	PdfDecoder decoder;
	int pageNumbers;

	public JPedalPDFFile(File f) throws IOException {
		input = f;
		decoder = new PdfDecoder(true);
		reload();
	}

	@Override
	public int getNumPages() {
		return pageNumbers;
	}

	@Override
	public IOutlineNode getOutline() throws IOException {
		Node n = decoder.getOutlineAsXML();
		if (n != null) return new JPedalOutlineNode(n.getFirstChild(), decoder);
		return null;
	}

	@Override
	public void reload() throws IOException {
		decoder.closePdfFile();
		try {
			decoder.openPdfFile(input.getAbsolutePath());
			pageNumbers = decoder.getPageCount();
		} catch (PdfException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public IPDFPage getPage(int pageNr) {
		return new JPedalPDFPage(decoder, pageNr);
	}

	@Override
	public void close() {
		decoder.closePdfFile();
		decoder.dispose();
	}
}
