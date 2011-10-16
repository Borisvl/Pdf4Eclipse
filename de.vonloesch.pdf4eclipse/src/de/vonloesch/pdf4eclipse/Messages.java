package de.vonloesch.pdf4eclipse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.vonloesch.pdf4eclipse.messages"; //$NON-NLS-1$
	public static String PDFEditor_ErrorMsg1;
	public static String PDFEditor_ErrorMsg2;
	public static String PDFEditor_ErrorMsg3;
	public static String PDFEditor_ErrorMsg4;
	public static String PDFEditor_ErrorMsg5;
	public static String PDFEditor_SynctexMsg1;
	public static String PDFEditor_SynctexMsg2;
	public static String PDFEditor_SynctexMsg3;
	public static String PDFEditor_SynctexMsg4;
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
