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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


import com.sun.pdfview.ImageInfo;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;
import com.sun.pdfview.RefImage;
import com.sun.pdfview.Watchable;
import com.sun.pdfview.action.GoToAction;
import com.sun.pdfview.action.UriAction;
import com.sun.pdfview.annotation.LinkAnnotation;
import com.sun.pdfview.annotation.PDFAnnotation;

import de.vonloesch.pdf4eclipse.editors.PDFEditor;
import de.vonloesch.pdf4eclipse.editors.handlers.ToggleLinkHighlightHandler;
import de.vonloesch.pdf4eclipse.model.IPDFDestination;
import de.vonloesch.pdf4eclipse.model.IPDFLinkAnnotation;
import de.vonloesch.pdf4eclipse.model.IPDFPage;


/**
 * SWT Canvas which shows a whole pdf-page. It also handles click on links.
 * Since the pdf library returns an awt BufferedImage, we need to convert it
 * to an SWT image. This was avoided in {@link PDFPageViewerAWT}.
 * 
 * @author Boris von Loesch
 *
 */
public class PDFPageViewer extends Canvas implements PaintListener, IPreferenceChangeListener{
    /** The image of the rendered PDF page being displayed */
    private Image currentImage;
    
    /** The current PDFPage that was rendered into currentImage */
    public IPDFPage currentPage;
    /** the current transform from device space to page space */
    AffineTransform currentXform;
    /** The horizontal offset of the image from the left edge of the panel */
    int offx;
    /** The vertical offset of the image from the top of the panel */
    int offy;
    
    private boolean highlightLinks;
    private Rectangle2D highlight;
    
    private Display display;
    
    private float zoomFactor;
    
    private org.eclipse.swt.graphics.Image swtImage;

    /**
     * Create a new PagePanel.
     */
    public PDFPageViewer(Composite parent, final PDFEditor editor) {
        //super(parent, SWT.NO_BACKGROUND|SWT.NO_REDRAW_RESIZE);
    	//super(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
    	super(parent, SWT.NO_BACKGROUND);

    	this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
			}
			
			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				
				if (e.button != 1) return;
				
				IPDFLinkAnnotation[] annos = getPage().getAnnotations();
            	for (final IPDFLinkAnnotation a : annos) {
            		Rectangle2D r = convertPDF2ImageCoord(a.getPosition());
            		if (r.contains(e.x, e.y)) {
            			if (a.getDestination() != null) {	
            				Display.getDefault().asyncExec(new Runnable() {
            					@Override
            					public void run() {
            						editor.gotoAction(a.getDestination());
            					}
            				});
            				return;
            			}
            		}
            	}
				/*List<PDFAnnotation> annos = getPage().getAnnots(PDFAnnotation.LINK_ANNOTATION);
            	for (PDFAnnotation a : annos) {
            		LinkAnnotation aa = (LinkAnnotation) a;
            		Rectangle2D r = convertPDF2ImageCoord(aa.getRect());
            		if (r.contains(e.x, e.y)) {
            			if (aa.getAction() instanceof GoToAction){
            				final GoToAction action = (GoToAction) aa.getAction();

            				Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									editor.gotoAction(action.getDestination());
								}
							});
            				return;
            			}
            			else if (aa.getAction() instanceof UriAction) {
            				final UriAction action = (UriAction) aa.getAction();
            				Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									
									try {
										String uri = action.getUri();
										if (uri.toLowerCase().indexOf("://") < 0) { //$NON-NLS-1$
											uri = "http://"+uri; //$NON-NLS-1$
										}
										PlatformUI.getWorkbench().getBrowserSupport()
										.createBrowser("PDFBrowser").openURL(new URL(uri)); //$NON-NLS-1$
									} catch (PartInitException e) {
										e.printStackTrace();
									} catch (MalformedURLException e) {
										editor.writeStatusLineError(e.getMessage());
									}
								}
							});
            				return;
            			}
            		}
				}*/
			}
			
			@Override
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
				
				if (e.button != 1) return;
				
				final Rectangle2D r = convertImage2PDFCoord(new java.awt.Rectangle(e.x, e.y, 1, 1));

				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						editor.reverseSearch(r.getX(), currentPage.getHeight() - r.getY());
					}
				});
			}
		});


    	display = parent.getDisplay();
        setSize(800, 600);
        zoomFactor = 1.f;
        this.addPaintListener(this);
        
        IEclipsePreferences prefs = (new InstanceScope()).getNode(de.vonloesch.pdf4eclipse.Activator.PLUGIN_ID);
		prefs.addPreferenceChangeListener(this);
		
		highlightLinks = prefs.getBoolean(ToggleLinkHighlightHandler.PREF_LINKHIGHTLIGHT_ID, true);
    }

    
    /**
     * Converts a buffered image to SWT <code>ImageData</code>.
     *
     * @param bufferedImage  the buffered image (<code>null</code> not
     *         permitted).
     *
     * @return The image data.
     */
    public static ImageData convertToSWT(BufferedImage bufferedImage) {
        if (bufferedImage.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
        	byte[] datas =
        			((DataBufferByte) bufferedImage.getRaster()
        				.getDataBuffer())
        				.getData();
        	ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), 32,
                    new PaletteData(0x000000FF, 0x0000FF00, 0x00FF0000));
        	data.data = datas;
        	return data;
        }

    	if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel
                    = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(),
                    colorModel.getGreenMask(), colorModel.getBlueMask());
            ImageData data = null;
            if (bufferedImage.getType() == BufferedImage.TYPE_INT_ARGB) {
            	data = new ImageData(bufferedImage.getWidth(),
                        bufferedImage.getHeight(), colorModel.getPixelSize(),
                        palette);
            	//We get this type from PDFPage
            	int[] rbgs = new int[data.width];
                for (int y = 0; y < data.height; y += 1) {
                	bufferedImage.getRGB(0, y, data.width, 1, rbgs, 0, data.width);
                	data.setPixels(0, y, data.width, rbgs, 0);
                }
            }
            else if (bufferedImage.getType() == BufferedImage.TYPE_INT_RGB){
            	data = new ImageData(bufferedImage.getWidth(),
                        bufferedImage.getHeight(), colorModel.getPixelSize(),
                        palette);
                WritableRaster raster = bufferedImage.getRaster();
            	int[] pixelArray = new int[3];
            	for (int y = 0; y < data.height; y++) {
            		for (int x = 0; x < data.width; x++) {
            			raster.getPixel(x, y, pixelArray);
            			int pixel = palette.getPixel(new RGB(pixelArray[0],
            					pixelArray[1], pixelArray[2]));
            			data.setPixel(x, y, pixel);
            		}
            	}
            }
            return data;
        }
        else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel)
                    bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
                        blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }
        return null;
    }

    /**
     * Highlights the rectangle given by the upper left and lower right 
     * coordinates. The highlight is visible after the next redraw.
     * @param x
     * @param y
     * @param x2
     * @param y2
     */
    public void highlight(double x, double y, double x2, double y2) {
    	Rectangle2D r = new Double(x, currentPage.getHeight() - y2, x2-x, y2 - y);
    	highlight = convertPDF2ImageCoord(r);
    }

    /**
     * Stop the generation of any previous page, and draw the new one.
     * @param page the PDFPage to draw.
     */
    public void showPage(IPDFPage page) {
    	// stop drawing the previous page
/*    	if (currentPage != null && prevSize != null && currentPage.isFinished()) {
    		currentPage.stop(prevSize.width, prevSize.height, null);
    	}*/

    	// set up the new page
    	currentPage = page;

    	//Reset highlight
    	highlight = null;

    	boolean resize = false;
    	int newW = Math.round(zoomFactor*page.getWidth());
    	int newH = Math.round(zoomFactor*page.getHeight());

    	Point sz = getSize();

    	if (sz.x != newW || sz.y != newH) {
    		sz.x = newW;
    		sz.y = newH;
    		resize = true;
    	}

    	if (sz.x == 0 || sz.y == 0) return;
    	currentImage = page.getImage(sz.y, sz.x);


    	long time = System.currentTimeMillis();
    	if (swtImage != null) swtImage.dispose();
    	swtImage = new org.eclipse.swt.graphics.Image(display, convertToSWT((BufferedImage)currentImage));
    	System.out.println(System.currentTimeMillis() - time);
    	
    	if (resize) {
    		//TODO: Non-exact size of JPedal
    		//Resize triggers repaint
    		setSize(currentImage.getWidth(null), currentImage.getHeight(null));
    		//setSize(Math.round(zoomFactor*page.getWidth()), Math.round(zoomFactor*page.getHeight()));
    		redraw();
    	}
    }

    private Rectangle getRectangle(Rectangle2D r) {
    	return new Rectangle((int)Math.round(r.getX()), (int)Math.round(r.getY()), (int)Math.round(r.getWidth()), (int)Math.round(r.getHeight()));
    }
    
    public Rectangle2D convertPDF2ImageCoord(Rectangle2D r) {
    	return currentPage.pdf2ImageCoordinates(r);
    }
    
    public Rectangle2D convertImage2PDFCoord(Rectangle2D r) {
    	return currentPage.image2PdfCoordinates(r);
    }
    
    /**
     * Sets the zoom factor and rerenders the current page.
     * @param factor 0 < factor < \infty
     */
    public void setZoomFactor(float factor) {
    	assert (factor > 0);
    	zoomFactor = factor;
    	showPage(currentPage);
    }
    
    /**
     * Returns the current used zoom factor
     * @return
     */
    public float getZoomFactor() {
    	return zoomFactor;
    }
    
    /**
     * Draw the image.
     */
    public void paintControl(PaintEvent event) {
    	GC g = event.gc;
        Point sz = getSize();
        if (currentImage == null) {
            g.setForeground(getBackground());
            g.fillRectangle(0, 0, sz.x, sz.y);
            g.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
            g.drawString(Messages.PDFPageViewer_1, sz.x / 2 - 30, sz.y / 2);
        } else {
            // draw the image
            int imwid = currentImage.getWidth(null);
            int imhgt = currentImage.getHeight(null);

            // draw it centered within the panel
            offx = (sz.x - imwid) / 2;
            offy = (sz.y - imhgt) / 2;

            if ((imwid == sz.x && imhgt <= sz.y) ||
                    (imhgt == sz.y && imwid <= sz.x)) {
            	
            	if (swtImage != null) g.drawImage(swtImage, offx, offy);

            	if (highlightLinks) {
            		IPDFLinkAnnotation[] anno = currentPage.getAnnotations();
            		g.setForeground(display.getSystemColor(SWT.COLOR_RED));
            		for (IPDFLinkAnnotation a : anno) {
            			Rectangle r = getRectangle(convertPDF2ImageCoord(a.getPosition()));
            			g.drawRectangle(r);
            		}
            	}
            	//Draw highlight frame
            	if (highlight != null) {
                	g.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
            		g.drawRectangle(getRectangle(highlight));
            	}

            } else {
                // the image is bogus.  try again, or give up.
                if (currentPage != null) {
                    showPage(currentPage);
                }
                g.setForeground(getBackground());
                g.fillRectangle(0, 0, sz.x, sz.y);                
                g.setForeground(display.getSystemColor(SWT.COLOR_RED));
                g.drawLine(0, 0, sz.x, sz.y);
                g.drawLine(0, sz.y, sz.x, 0);
            }
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent event) {
    	if (ToggleLinkHighlightHandler.PREF_LINKHIGHTLIGHT_ID.equals(event.getKey())) {
    		highlightLinks = Boolean.parseBoolean((String)(event.getNewValue()));
    		redraw();
    	}
    }
    
    /**
     * Gets the page currently being displayed
     */
    public IPDFPage getPage() {
        return currentPage;
    }

    @Override
    public void dispose() {
    	super.dispose();

    	currentImage.flush();
    	if (swtImage != null) swtImage.dispose();
		currentImage = null;
    	
    	//IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(de.vonloesch.pdf4eclipse.Activator.PLUGIN_ID);
    	IEclipsePreferences prefs = (new InstanceScope()).getNode(de.vonloesch.pdf4eclipse.Activator.PLUGIN_ID);
    	prefs.removePreferenceChangeListener(this);
    }
}