/*******************************************************************************
 * Copyright (c) 2016 Boris von Loesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andreas Turban - initial API
 ******************************************************************************/
package de.vonloesch.synctex;

import java.io.IOException;

/**
 * {@link ISynctexParser} defines the API for the Synctex search feature.
 * 
 * @author Andreas Turban
 * @author Boris von Loesch
 * @see DefaultSynctexParserFactory
 */
public interface ISynctexParser {

	void setReverseSearchInformation(int currentPage, double pdfX, double pdfY);

	void startReverse() throws IOException;

	void startForward() throws IOException;

	void close() throws IOException;

	String getSourceFilePath();

	int getSourceLineNr();

	void setForwardSearchInformation(String file, int lineNr);

	double[] getForwardSearchResult();

}
