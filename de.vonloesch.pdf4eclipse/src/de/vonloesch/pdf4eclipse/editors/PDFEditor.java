/*******************************************************************************
 * Copyright (c) 2011 Boris von Loesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Boris von Loesch - initial API and implementation
 *     MeisterYeti - pseudo-continuous scrolling and zooming by mouse wheel
 ******************************************************************************/
package de.vonloesch.pdf4eclipse.editors;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.AcceptAllFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.vonloesch.pdf4eclipse.Activator;
import de.vonloesch.pdf4eclipse.Messages;
import de.vonloesch.pdf4eclipse.PDFPageViewer;
import de.vonloesch.pdf4eclipse.editors.StatusLinePageSelector.IPageChangeListener;
import de.vonloesch.pdf4eclipse.model.IOutlineNode;
import de.vonloesch.pdf4eclipse.model.IPDFDestination;
import de.vonloesch.pdf4eclipse.model.IPDFFile;
import de.vonloesch.pdf4eclipse.model.IPDFPage;
import de.vonloesch.pdf4eclipse.model.PDFFactory;
import de.vonloesch.pdf4eclipse.model.jpedal.JPedalPDFFile;
import de.vonloesch.pdf4eclipse.model.sun.SunPDFFile;
import de.vonloesch.pdf4eclipse.outline.PDFFileOutline;
import de.vonloesch.pdf4eclipse.preferences.PreferenceConstants;
import de.vonloesch.synctex.SimpleSynctexParser;

/**
 * 
 * @author Boris von Loesch
 *
 */
public class PDFEditor extends EditorPart implements IResourceChangeListener, 
	INavigationLocationProvider, IPageChangeListener, IPreferenceChangeListener{

	public static final String ID = "de.vonloesch.pdf4eclipse.editors.PDFEditor"; //$NON-NLS-1$
	public static final String CONTEXT_ID = "PDFViewer.editors.contextid"; //$NON-NLS-1$

	public static final int FORWARD_SEARCH_OK = 0;
	public static final int FORWARD_SEARCH_NO_SYNCTEX = -1;
	public static final int FORWARD_SEARCH_FILE_NOT_FOUND = -2;
	public static final int FORWARD_SEARCH_POS_NOT_FOUND = -3;
	public static final int FORWARD_SEARCH_UNKNOWN_ERROR = -4;
	
	private static final float MOUSE_ZOOMFACTOR = 0.2f;
	
	private static final int SCROLLING_WAIT_TIME = 200;

	static final String PDFPOSITION_ID = "PDFPosition"; //$NON-NLS-1$
	
	public PDFPageViewer pv;
	private File file;

	private IPDFFile f;
	private ScrolledComposite sc;
	int currentPage;
	private PDFFileOutline outline;
	private StatusLinePageSelector position;
	
	private Listener mouseWheelPageListener;
	private boolean isListeningForMouseWheel;

	private Cursor cursorHand;
	private Cursor cursorArrow;
	
	public PDFEditor() {
		super();
	}

	@Override
	public void dispose() {
		super.dispose();
		
		if (sc != null) sc.dispose();
		if (pv != null) pv.dispose();
		if (outline != null) outline.dispose();
		if (cursorArrow != null) cursorArrow.dispose();
		if (cursorHand != null) cursorHand.dispose();
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		if (position != null) position.removePageChangeListener(this);
		
        IEclipsePreferences prefs = (new InstanceScope()).getNode(de.vonloesch.pdf4eclipse.Activator.PLUGIN_ID);
		prefs.removePreferenceChangeListener(this);
		
		if (f != null) f.close();
		f = null;
		pv = null;
	}
	

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		currentPage = 1;
		setPartName(input.getName());
		readPdfFile();
	}

	@Override
	public void pageChange(int pageNr) {
		showPage(pageNr);
		setOrigin(sc.getOrigin().x, 0);
	}
	
	public void readPdfFile() throws PartInitException{
		IEditorInput input = getEditorInput();
		if (input instanceof FileStoreEditorInput) {
			file = new File(((FileStoreEditorInput)input).getURI());
		}
		else if ((input instanceof IFileEditorInput)) {
			file = new File(((IFileEditorInput) input).getFile().getLocationURI());
		}
		else {
			throw new PartInitException(Messages.PDFEditor_ErrorMsg1);
		}
		f = null;
		try {
			f = PDFFactory.openPDFFile(file, PDFFactory.STRATEGY_SUN_JPEDAL);
		} catch (FileNotFoundException fnfe) {
			throw new PartInitException(Messages.PDFEditor_ErrorMsg3, fnfe);
		} catch (IOException ioe) {
			throw new PartInitException(Messages.PDFEditor_ErrorMsg4, ioe);
		} 
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}


	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if(event.getType() == IResourceChangeEvent.POST_CHANGE){
			try {

				if (!(getEditorInput() instanceof IFileEditorInput)) return;

				final IFile currentfile = ((IFileEditorInput) getEditorInput()).getFile();
				final IResourceDelta delta = event.getDelta().findMember(currentfile.getFullPath());
				if (delta != null && (delta.getKind() & IResourceDelta.REMOVED) == 0) {
					//readPdfFile();
					f.reload();
					final IOutlineNode n = f.getOutline();
					Display.getDefault().asyncExec(new Runnable() {										
						@Override
						public void run() {
							if (pv != null && !pv.isDisposed()) {
								showPage(currentPage);
								if (outline != null) outline.setInput(n);		
								pv.redraw();
							}
						}
					});
				}
			} 
			/*catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/ 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}				
	}

	@Override
	public void createPartControl(final Composite parent) {
		cursorHand = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
		cursorArrow = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);
		
		parent.setLayout(new FillLayout());
		sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		pv = new PDFPageViewer(sc, this);
		//pv = new PDFPageViewerAWT(sc, this);
		sc.setContent(pv);
		// Speed up scrolling when using a wheel mouse
		ScrollBar vBar = sc.getVerticalBar();
		vBar.setIncrement(10);
				
		
		isListeningForMouseWheel = false;
		mouseWheelPageListener = new Listener() {
			
			//last time the page number changed due to a scrolling event
			long lastTime;
			
			@Override
			public void handleEvent(Event e) {
				long time = e.time & 0xFFFFFFFFL;
				
				//If a scrolling event occurs within a very short period of time
				//after the last page change discard it. This avoids "overscrolling"
				//the beginning of the next page
				if (time - lastTime < SCROLLING_WAIT_TIME) {
					e.doit = false;
					return;
				}
				
				Point p = sc.getOrigin();
				
				int height = sc.getClientArea().height;
				int pheight = sc.getContent().getBounds().height;

				if (p.y >= pheight - height && e.count < 0) {
					//We are at the end of the page
					if (currentPage < f.getNumPages()) {
						showPage(currentPage + 1);
						setOrigin(sc.getOrigin().x, 0);
						e.doit = false;
						lastTime = time;
					}
				} else if (p.y <= 0 && e.count > 0) {
					//We are at the top of the page
					if (currentPage > 1) {
						showPage(currentPage - 1);
						setOrigin(sc.getOrigin().x, pheight);
						e.doit = false;
						lastTime = time;
					}
				}
				
			}
		};
		
		//Add zooming by using mouse wheel and ctrl key (contributed by MeisterYeti)
		pv.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseScrolled(MouseEvent e) {
				if((e.stateMask & SWT.CTRL) > 0) {
					Point o = getOrigin();
					Point oldSize = pv.getSize();
					pv.setZoomFactor(Math.max(pv.getZoomFactor() + MOUSE_ZOOMFACTOR*(float)e.count/10, 0));
					int mx = Math.round((float)pv.getSize().x * ((float)e.x / oldSize.x)) - (e.x-o.x);
					int my = Math.round((float)pv.getSize().y * ((float)e.y / oldSize.y)) - (e.y-o.y);
					setOrigin(mx,my);
					return;
				}
				
			}
		});
		
		//Add panning of page using middle mouse button (contributed by MeisterYeti)
		pv.addMouseListener(new MouseListener() {
			
			Point start;
			MouseMoveListener mml = new MouseMoveListener() {
				
				@Override
				public void mouseMove(MouseEvent e) {
					if((e.stateMask & SWT.BUTTON2) == 0) {
						pv.removeMouseMoveListener(this);
						pv.setCursor(cursorArrow);
						return;
					}
					Point o = sc.getOrigin();
					sc.setOrigin(o.x-(e.x-start.x), o.y-(e.y-start.y));
				}
			};
			
			@Override
			public void mouseUp(MouseEvent e) {
				if(e.button != 2)
					return;
				pv.removeMouseMoveListener(mml);
				pv.setCursor(cursorArrow);
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button != 2)
					return;
				start = new Point(e.x, e.y);
				pv.addMouseMoveListener(mml);
				pv.setCursor(cursorHand);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});		

		pv.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int height = sc.getClientArea().height;
				int pInc = 3* height / 4;
				int lInc = height / 20;
				int hInc = sc.getClientArea().width / 20;
				int pheight = sc.getContent().getBounds().height;
				Point p = sc.getOrigin();
				if (e.keyCode == SWT.PAGE_DOWN) {
					if (p.y < pheight - height) {
						int y = p.y + pInc;
						if (y > pheight - height) {
							y = pheight - height;
						}
						sc.setOrigin(sc.getOrigin().x, y);
					}
					else {
						//We are at the end of the page
						if (currentPage < f.getNumPages()) {
							showPage(currentPage + 1);
							setOrigin(sc.getOrigin().x, 0);
						}
					}
				}
				else if (e.keyCode == SWT.PAGE_UP) {
					if (p.y > 0) {
						int y = p.y - pInc;
						if (y < 0) y = 0;
						sc.setOrigin(sc.getOrigin().x, y);
					}
					else {
						//We are at the top of the page
						if (currentPage > 1) {
							showPage(currentPage - 1);
							setOrigin(sc.getOrigin().x, pheight);
						}
					}					
				}
				else if (e.keyCode == SWT.ARROW_DOWN) {
					if (p.y < pheight - height) {
						sc.setOrigin(sc.getOrigin().x, p.y + lInc);
					}					
				}
				else if (e.keyCode == SWT.ARROW_UP) {
					if (p.y > 0) {
						int y = p.y - lInc;
						if (y < 0) y = 0;
						sc.setOrigin(sc.getOrigin().x, y);
					}					
				}
				else if (e.keyCode == SWT.ARROW_RIGHT) {
					if (p.x < sc.getContent().getBounds().width - sc.getClientArea().width) {
						sc.setOrigin(p.x + hInc, sc.getOrigin().y);
					}
				}
				else if (e.keyCode == SWT.ARROW_LEFT) {
					if (p.x > 0) {
						int x = p.x - hInc;
						if (x < 0) x = 0;
						sc.setOrigin(x, sc.getOrigin().y);
					}					
				}
				else if (e.keyCode == SWT.HOME) {
					showPage(1);
					setOrigin(sc.getOrigin().x, 0);
				}
				else if (e.keyCode == SWT.END) {
					showPage(f.getNumPages());
					setOrigin(sc.getOrigin().x, pheight);
				}	

			}
		});

		IStatusLineManager statusLineM = getEditorSite().getActionBars().getStatusLineManager();
		IContributionItem[] items = statusLineM.getItems();
		for (IContributionItem item : items) {
			if (PDFPOSITION_ID.equals(item.getId())) {
				position = (StatusLinePageSelector) item;
				position.setPageChangeListener(this);
			}
		}
		if (position == null) {
			position = new StatusLinePageSelector(PDFPOSITION_ID, 15);
			position.setPageChangeListener(this);
			statusLineM.add(position);
		}
		position.setPageInfo(1, 1);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);

		if (f != null) {
			showPage(currentPage);
		}
		
        IEclipsePreferences prefs = (new InstanceScope()).getNode(de.vonloesch.pdf4eclipse.Activator.PLUGIN_ID);
        
		prefs.addPreferenceChangeListener(this);
		
		if (prefs.getBoolean(PreferenceConstants.PSEUDO_CONTINUOUS_SCROLLING, true)) {
			pv.addListener(SWT.MouseWheel, mouseWheelPageListener);
			isListeningForMouseWheel = true;
		}

		initKeyBindingContext();
	}

    @Override
    public void preferenceChange(PreferenceChangeEvent event) {
    	//Check whether "Pseudo continuous scrolling" was changed
    	if (PreferenceConstants.PSEUDO_CONTINUOUS_SCROLLING.equals(event.getKey())) {
    		
    		boolean newValue = Boolean.parseBoolean((String)(event.getNewValue()));
    		//I do not know why this happens, but getNewValue() returns null instead of true
    		if (event.getNewValue() == null) newValue = true;
    		
    		if (isListeningForMouseWheel && newValue == false) {
    			pv.removeListener(SWT.MouseWheel, mouseWheelPageListener);
    			isListeningForMouseWheel = false;
    		}
    		else if (!isListeningForMouseWheel && newValue == true) {
    			pv.addListener(SWT.MouseWheel, mouseWheelPageListener);
    			isListeningForMouseWheel = true;
    		}
    	}
    }

	private void initKeyBindingContext() {
		final IContextService service = (IContextService)
				getSite().getService(IContextService.class);

		pv.addFocusListener(new FocusListener() {
			IContextActivation currentContext = null;
			public void focusGained(FocusEvent e) {
				if (currentContext == null)
					currentContext = service.activateContext(CONTEXT_ID);
			}

			public void focusLost(FocusEvent e) {
				if (currentContext != null) {
					service.deactivateContext(currentContext);
					currentContext = null;
				}
			}
		});
	}	

	private File getSyncTeXFile() {
		String name = file.getAbsolutePath();
		name = name.substring(0, name.lastIndexOf('.'));
		File f = new File(name + ".synctex.gz"); //$NON-NLS-1$
		if (f.exists()) return f;
		f = new File(name + ".synctex"); //$NON-NLS-1$
		if (f.exists()) return f;
		return null;
	}
	
	private SimpleSynctexParser createSimpleSynctexParser(File f) 
		throws IOException {
		InputStream in;
		if (f.getName().toLowerCase().endsWith(".gz")) {
			in = new GZIPInputStream(new FileInputStream(f));
		}
		else {
			in = new FileInputStream(f);
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		return new SimpleSynctexParser(r);
	}

	/**
	 * Starts a forward search in the current pdf-editor. The editor
	 * searches for the SyncTeX file and displays the position given by the user.
	 * 
	 * @param file The TeX file 
	 * @param lineNr The line number in the TeX file
	 * @return One of {@link FORWARD_SEARCH_OK}, 
	 * 		{@link FORWARD_SEARCH_NO_SYNCTEX}, {@link FORWARD_SEARCH_FILE_NOT_FOUND},
	 * 		{@link FORWARD_SEARCH_POS_NOT_FOUND}, {@link FORWARD_SEARCH_UNKNOWN_ERROR}
	 */
	public int forwardSearch(String file, int lineNr) {
		File syncTeXFile = getSyncTeXFile();
		if (syncTeXFile == null) return FORWARD_SEARCH_NO_SYNCTEX;
		try {
			//FIXME: Create a job for this
			SimpleSynctexParser p = createSimpleSynctexParser(syncTeXFile);
			//System.out.println("Start Forward search");
			p.setForwardSearchInformation(file, lineNr);
			p.startForward();
			p.close();
			
			double[] result = p.getForwardSearchResult();
			if (result == null) return FORWARD_SEARCH_FILE_NOT_FOUND;

			int page = (int) Math.round(result[0]);
			if (page > f.getNumPages() || page < 1) return FORWARD_SEARCH_UNKNOWN_ERROR;
			showPage(page);
			pv.highlight(result[1], result[2], result[3], result[4]);
			Rectangle2D re = pv.convertPDF2ImageCoord(new Rectangle((int)Math.round(result[1]), 
					(int)Math.round(pv.currentPage.getHeight() - result[2]), 
					1, 1));
			int x = sc.getOrigin().x;
			if (re.getX() < sc.getOrigin().x) x = (int)Math.round(re.getX() - 10);
			setOrigin(x, (int)Math.round(re.getY() - sc.getBounds().height / 4.));
			//System.out.println("Page: "+page);
			try {
				this.getSite().getPage().openEditor(this.getEditorInput(), PDFEditor.ID);
			} catch (PartInitException e) {
				e.printStackTrace();
				return FORWARD_SEARCH_UNKNOWN_ERROR;
			}
			this.setFocus();
			return FORWARD_SEARCH_OK;

		} catch (IOException ex) {
			//Do nothing
			return FORWARD_SEARCH_UNKNOWN_ERROR;
		}
	}

	public void reverseSearch(double pdfX, double pdfY) {
		File f = getSyncTeXFile();
		if (f == null) {
			writeStatusLineError(Messages.PDFEditor_SynctexMsg1);
			return;
		}
		//File f = new File (((IFileEditorInput) getEditorInput()).getFile().getRawLocation().removeFileExtension().addFileExtension("synctex.gz").toOSString());
		try {
			//FIXME: Create a job for this
			SimpleSynctexParser p = createSimpleSynctexParser(f);
			p.setReverseSearchInformation(currentPage, pdfX, pdfY);
			p.startReverse();
			p.close();

			if (p.sourceFilePath == null) {
				//Could not find a source file
				writeStatusLineError(Messages.PDFEditor_SynctexMsg2);
				return;
			}

			File sourceFile = new File(p.sourceFilePath);
			String path = p.sourceFilePath;
			if (!sourceFile.isAbsolute()) {
				//Append it to the path of the pdf
				path = f.getCanonicalPath();
				path = path.substring(0, path.lastIndexOf(File.separatorChar)+1) + p.sourceFilePath;
			}
			IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(new File(path));
			if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
				IWorkbenchPage page=  this.getSite().getPage();
				try {
					IEditorPart part = IDE.openEditorOnFileStore(page, fileStore);
					if (part instanceof AbstractTextEditor) {
						AbstractTextEditor t = (AbstractTextEditor) part;
						IDocument doc = t.getDocumentProvider().getDocument(t.getEditorInput());
						t.selectAndReveal(doc.getLineOffset(p.sourceLineNr - 1), doc.getLineLength(p.sourceLineNr - 1));
					}
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (BadLocationException e) {
					writeStatusLineError(NLS.bind(Messages.PDFEditor_SynctexMsg3, p.sourceLineNr - 1));
				}
			} else {
				writeStatusLineError(NLS.bind(Messages.PDFEditor_SynctexMsg4, path));
			}
		} catch (FileNotFoundException e) {
			writeStatusLineError(NLS.bind(Messages.PDFEditor_SynctexMsg5, f.getName()));
		}
		catch (IOException e1) {
			writeStatusLineError("Error while parsing SyncTeX file "+f.getName());
			e1.printStackTrace();
		}

	}

	public void showPage(IPDFPage page) {
		currentPage = page.getPageNumber();
		pv.showPage(page);
		updateStatusLine();
	}
	
	public void showPage(int pageNr) {
		if (pageNr < 1) pageNr = 1;
		if (pageNr > f.getNumPages()) pageNr = f.getNumPages();
		IPDFPage page = f.getPage(pageNr);
		currentPage = pageNr;
		pv.showPage(page);
		updateStatusLine();
	}

	@Override
	public void setFocus() {
		sc.setFocus();
		updateStatusLine();
		position.setPageChangeListener(this);
	}

	/**
	 * Shows the given page and reveals the destination
	 * @param dest
	 */
	public void gotoAction(IPDFDestination dest) {
		if (dest.getType() == IPDFDestination.TYPE_GOTO) {
			IPDFPage page = dest.getPage(f);
			if (page == null) return;

			IWorkbenchPage wpage = getSite().getPage();
			wpage.getNavigationHistory().markLocation(this);

			showPage(page);
			Rectangle2D r = dest.getPosition();
			if (r != null) {
				Rectangle2D re = pv.convertPDF2ImageCoord(r);
				int x = sc.getOrigin().x;
				if (re.getX() < sc.getOrigin().x) x = (int)Math.round(re.getX() - 10);
				setOrigin(x, (int)Math.round(re.getY() - sc.getBounds().height / 4.));
			}
			else {
				setOrigin(sc.getOrigin().x, 0);			
			}
			wpage.getNavigationHistory().markLocation(this);
		} 
		else if (dest.getType() == IPDFDestination.TYPE_URL) {
			String url = dest.getURL();
			if (url.toLowerCase().indexOf("://") < 0) { //$NON-NLS-1$
				url = "http://" + url; //$NON-NLS-1$
			}
			try {
				PlatformUI.getWorkbench().getBrowserSupport()
				.createBrowser("PDFBrowser").openURL(new URL(url));
			}
			catch (PartInitException e) {
				Activator.log("Problem opening browser", e);
			}
			catch (MalformedURLException e) {
				MessageDialog.openError(this.getSite().getShell(), "No valid url", e.getMessage());
			} //$NON-NLS-1$
		}
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (outline == null) {
				try {
					IOutlineNode n = f.getOutline();
					if (n == null) return null;
					outline = new PDFFileOutline(this);
					outline.setInput(n);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else return outline;
		}
		return super.getAdapter(required);
	}

	@Override
	public INavigationLocation createEmptyNavigationLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INavigationLocation createNavigationLocation() {
		return new PDFNavigationLocation(this);
	}

	private void updateStatusLine() {
		position.setPageInfo(currentPage, f.getNumPages());
	}

	public void fitHorizontal() {
		int w = sc.getClientArea().width;
		pv.setZoomFactor((1.0f*w)/pv.getPage().getWidth());
	}

	public void fit() {
		float w = 1.f * sc.getClientArea().width;
		float h = 1.f * sc.getClientArea().height;
		float pw = pv.getPage().getWidth();
		float ph = pv.getPage().getHeight();
		if (w/pw < h/ph) pv.setZoomFactor(w/pw);
		else pv.setZoomFactor(h/ph);
	}

	/**
	 * Writes an error message to the status line and deletes it after five seconds.
	 * @param text
	 */
	public void writeStatusLineError(String text) {
		final IStatusLineManager statusLineM = getEditorSite().getActionBars().getStatusLineManager();
		statusLineM.setErrorMessage(text);
		//FIXME: Should not be executed if there was another message in between the five secs.
		Display.getDefault().timerExec(5000, new Runnable() {

			@Override
			public void run() {
				statusLineM.setErrorMessage("");				 //$NON-NLS-1$
			}
		});
	}

	Point getOrigin() {
		if (!sc.isDisposed()) return sc.getOrigin();
		else return null;
	}

	private void setOrigin(int x, int y) {
		sc.setRedraw(false);
		sc.setOrigin(x, y);
		sc.setRedraw(true);
	}

	void setOrigin(Point p) {
		sc.setRedraw(false);
		if (p != null) sc.setOrigin(p);
		sc.setRedraw(true);
	}

}
