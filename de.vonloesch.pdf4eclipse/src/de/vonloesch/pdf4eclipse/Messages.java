/*******************************************************************************
 * Copyright (c) 2011 Boris von Loesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Boris von Loesch - initial API and implementation
 ******************************************************************************/
package de.vonloesch.pdf4eclipse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.vonloesch.pdf4eclipse.messages"; //$NON-NLS-1$
	public static String MainPreferencePage_adaptive;
	public static String MainPreferencePage_jpedalRenderer;
	public static String MainPreferencePage_pdfRenderer;
	public static String MainPreferencePage_PseudoContScroll;
	public static String MainPreferencePage_Summary;
	public static String MainPreferencePage_sunRenderer;
	public static String PDFEditor_ErrorMsg1;
	public static String PDFEditor_ErrorMsg2;
	public static String PDFEditor_ErrorMsg3;
	public static String PDFEditor_ErrorMsg4;
	public static String PDFEditor_ErrorMsg5;
	public static String PDFEditor_SynctexMsg1;
	public static String PDFEditor_SynctexMsg2;
	public static String PDFEditor_SynctexMsg3;
	public static String PDFEditor_SynctexMsg4;
	public static String StatusLinePageSelector_ButtonFirst;
	public static String StatusLinePageSelector_ButtonLast;
	public static String StatusLinePageSelector_ButtonNext;
	public static String StatusLinePageSelector_ButtonPrevious;
	public static String StatusLinePageSelector_errorMsg1;
	public static String StatusLinePageSelector_errorMsg2;
	public static String StatusLinePageSelector_tooltip;
	public static String PDFEditor_SynctexMsg5;
	public static String PDFPageViewer_1;
	public static String PDFPageViewer_Error1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
