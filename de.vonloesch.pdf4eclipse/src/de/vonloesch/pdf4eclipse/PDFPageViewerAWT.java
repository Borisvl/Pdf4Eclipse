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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


import com.sun.pdfview.ImageInfo;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;
import com.sun.pdfview.RefImage;
import com.sun.pdfview.Watchable;
import com.sun.pdfview.action.GoToAction;
import com.sun.pdfview.annotation.LinkAnnotation;
import com.sun.pdfview.annotation.PDFAnnotation;

import de.vonloesch.pdf4eclipse.editors.PDFEditor;


/**
 * Does not work reliable on all platforms :(.
 * 
 * @author Boris von Loesch
 *
 */
@Deprecated
public class PDFPageViewerAWT extends Composite {
    /** The image of the rendered PDF page being displayed */
    Image currentImage;
    
    /** The current PDFPage that was rendered into currentImage */
    public PDFPage currentPage;
    /* the current transform from device space to page space */
    AffineTransform currentXform;
    /** The horizontal offset of the image from the left edge of the panel */
    Rectangle2D clip;
    /** the clipping region used for the image */
    Rectangle2D prevClip;
    /** the size of the image */
    Dimension prevSize;
    
    Rectangle2D highlight;
    
    float zoomFactor;

    /** a flag indicating whether the current page is done or not. */
    private Frame frame;
    private Canvas canvas;
    PDFEditor editor;
    
    /**
     * Create a new PagePanel, with a default size of 800 by 600 pixels.
     */
    @SuppressWarnings("serial")
	public PDFPageViewerAWT(Composite parent, PDFEditor editor) {
        //super(parent, SWT.NO_BACKGROUND|SWT.NO_REDRAW_RESIZE);
    	super(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
    	this.editor = editor;
    	this.setLayout(new FillLayout());
		//Seems to fix a deadlock in SWT_AWT.new_Frame
    	while  (Display.getDefault().readAndDispatch() == true);
		frame = SWT_AWT.new_Frame(this);
    	final PDFEditor ed = editor;

		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				canvas = new Canvas() {
					
					@Override
					public void update(Graphics g) {
						paint(g);
					};
					
					@Override
		    		public void paint(Graphics g) {
		    			Dimension sz = getSize();
		    			if (currentImage == null) {
		    				g.setColor(getBackground());
		    				g.fillRect(0, 0, sz.width, sz.height);
		    				g.setColor(Color.BLACK);
		    				g.drawString("No page selected", sz.width / 2 - 30, sz.height / 2);

		    			} else {
		    				// draw the image

		    					g.drawImage(currentImage, 0, 0, null);

		    					List<PDFAnnotation> anno = currentPage.getAnnots(PDFAnnotation.LINK_ANNOTATION);
		    					g.setColor(Color.RED);
		    					for (PDFAnnotation a : anno) {
		    						Rectangle2D r = convertPDF2ImageCoord(a.getRect());
		    						g.drawRect((int)Math.round(r.getX()), (int)Math.round(r.getY()), 
		    								(int)Math.round(r.getWidth()), (int)Math.round(r.getHeight()));
		    					}

		    					//Draw highlight frame
		    					if (highlight != null) {
		    						g.setColor(Color.BLUE);
		    						g.drawRect((int)Math.round(highlight.getX()), (int)Math.round(highlight.getY()), 
		    								(int)Math.round(highlight.getWidth()), (int)Math.round(highlight.getHeight()));
		    					}
		    			}
		    		}
		    	};
		    	//canvas.setIgnoreRepaint(true);
		    	canvas.enableInputMethods(false);
		    	//Beware: This seem to be necessary to enable MouseWheel scrolling
		    	//on some systems. A riddle though.
		    	canvas.addMouseWheelListener(new MouseWheelListener() {
					
					@Override
					public void mouseWheelMoved(MouseWheelEvent e) {
						//System.out.println("Scroll");
					}
				});
		    	canvas.addMouseListener(new MouseAdapter() {
		    				    		
		    		@Override
					public void mouseClicked(final MouseEvent e) {
		        		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
		    				List<PDFAnnotation> annos = getPage().getAnnots(PDFAnnotation.LINK_ANNOTATION);
		                	for (PDFAnnotation a : annos) {
		                		LinkAnnotation aa = (LinkAnnotation) a;
		                		Rectangle2D r = convertPDF2ImageCoord(aa.getRect());
		                		if (r.contains(e.getX(), e.getY())) {
		                			if (aa.getAction() instanceof GoToAction){
		                				final GoToAction action = (GoToAction) aa.getAction();

		                				Display.getDefault().asyncExec(new Runnable() {
											
											@Override
											public void run() {
												ed.gotoAction(action.getDestination());
											}
										});
		                				return;
		                			}
		                		}
		    				}
		        		}
						if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
							final Rectangle2D r = convertImage2PDFCoord(new java.awt.Rectangle(e.getX(), e.getY(), 1, 1));

							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									ed.reverseSearch(r.getX(), currentPage.getHeight() - r.getY());
								}
							});
						}
					}
				});
		        frame.add(canvas);
		        frame.enableInputMethods(false);
		        frame.setFocusable(false);
		        frame.setIgnoreRepaint(true);
			}
		}); 
			
        //setSize(800, 600);
        zoomFactor = 1.f;
    }

    
    @Override
    public void addFocusListener(final org.eclipse.swt.events.FocusListener listener) {
    	canvas.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						listener.focusLost(null);
					}
				});
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						listener.focusGained(null);
					}
				});
			}
		});
    }
    
    public void highlight(double x, double y, double x2, double y2) {
    	Rectangle2D r = new Double(x, currentPage.getHeight() - y2, x2-x, y2 - y);
    	highlight = convertPDF2ImageCoord(r);

    }

    /**
     * Stop the generation of any previous page, and draw the new one.
     * @param page the PDFPage to draw.
     */
    public void showPage(PDFPage page) {
        // stop drawing the previous page
        if (currentPage != null && prevSize != null) {
            currentPage.stop(prevSize.width, prevSize.height, prevClip);
        }

        // set up the new page
        currentPage = page;

        if (page == null) {
            // no page
            currentImage = null;
            clip = null;
            currentXform = null;
            canvas.repaint();
        } else {
        	//Reset highlight
        	highlight = null;
        	boolean resize = false;
        	            
            int newW = Math.round(zoomFactor*page.getWidth());
            int newH = Math.round(zoomFactor*page.getHeight());
        	//setSize(Math.round(zoomFactor*page.getWidth()), Math.round(zoomFactor*page.getHeight()));

            Point sz = getSize();
            
            if (sz.x != newW || sz.y != newH) {
            	sz.x = newW;
            	sz.y = newH;
            	resize = true;
            }
            
            if (sz.x + sz.y == 0) {
                // no image to draw.
                return;
            }

            // calculate the clipping rectangle in page space from the
            // desired clip in screen space.
            Rectangle2D useClip = clip;
            if (clip != null && currentXform != null) {
                useClip = currentXform.createTransformedShape(clip).getBounds2D();
            }

            Dimension pageSize = page.getUnstretchedSize(sz.x, sz.y,
                    useClip);

            ImageInfo info = new ImageInfo(pageSize.width, pageSize.height, useClip, null);

            currentImage = new RefImage(pageSize.width, pageSize.height,
            	BufferedImage.TYPE_INT_ARGB);
            
            Rectangle rect = new Rectangle(0, 0, pageSize.width, pageSize.height);
            PDFRenderer r = new PDFRenderer(page, ((BufferedImage)currentImage).createGraphics(), rect, 
            		useClip, Color.WHITE);
            page.renderers.put(info, new WeakReference<PDFRenderer>(r));
            // get the new image
            /*currentImage = page.getImage(pageSize.width, pageSize.height,
                    useClip, this, true, false);*/

            // calculate the transform from screen to page space
            currentXform = page.getInitialTransform(pageSize.width,
                    pageSize.height,
                    useClip);
            try {
                currentXform = currentXform.createInverse();
            } catch (NoninvertibleTransformException nte) {
                System.out.println("Error inverting page transform!");
                nte.printStackTrace();
            }

            prevClip = useClip;
            prevSize = pageSize;

            r.go(true);
        	if (r.getStatus() != Watchable.COMPLETED) return;
            
            if (resize)
            	//Resize triggers repaint
        		setSize(Math.round(zoomFactor*page.getWidth()), Math.round(zoomFactor*page.getHeight()));
            else {
            	EventQueue.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						//canvas.repaint();			
					}
				});
            }
        }
    }
    
    public Rectangle2D convertPDF2ImageCoord(Rectangle2D r) {
    	if (currentImage == null) return null;
    	int imwid = currentImage.getWidth(null);
        int imhgt = currentImage.getHeight(null);
    	AffineTransform t = currentPage.getInitialTransform(imwid,
                imhgt, prevClip);
    	Rectangle2D tr = t.createTransformedShape(r).getBounds2D();
    	tr.setFrame(tr.getX(), tr.getY(), tr.getWidth(), tr.getHeight());
    	return tr;    	
    }
    
    public Rectangle2D convertImage2PDFCoord(Rectangle2D r) {
    	if (currentImage == null) return null;
    	int imwid = currentImage.getWidth(null);
        int imhgt = currentImage.getHeight(null);
        
    	AffineTransform t;
		try {
			t = currentPage.getInitialTransform(imwid,
			        imhgt, prevClip).createInverse();
			r.setFrame(r.getX(), r.getY(), 1, 1);
	    	Rectangle2D tr = t.createTransformedShape(r).getBounds2D();
	    	tr.setFrame(tr.getX(), tr.getY(), tr.getWidth(), tr.getHeight());
	    	return tr;    	
		} catch (NoninvertibleTransformException e) {
			return null;
		}
    }
    
    public void setZoomFactor(float factor) {
    	zoomFactor = factor;
    	showPage(currentPage);
    }
    
    public float getZoomFactor() {
    	return zoomFactor;
    }


    /**
     * Gets the page currently being displayed
     */
    public PDFPage getPage() {
        return currentPage;
    }

}
