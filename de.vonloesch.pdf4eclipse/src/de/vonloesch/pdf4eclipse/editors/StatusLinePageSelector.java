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
package de.vonloesch.pdf4eclipse.editors;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import de.vonloesch.pdf4eclipse.Activator;
import de.vonloesch.pdf4eclipse.Messages;

public class StatusLinePageSelector extends ContributionItem {
	private final static int DEFAULT_CHAR_WIDTH = 40;
	
	private int charWidth;

	private Text pageField;
	private Label pageNrField;

	private Image imageFirst, imagePrev, imageNext, imageLast;
	private ToolItem firstPage, prevPage, nextPage, lastPage;
	private int page;
	private int lastPageNr;
	
	private List<IPageChangeListener> listeners;
	/**
	 * The composite into which this contribution item has been placed. This
	 * will be <code>null</code> if this instance has not yet been
	 * initialized.
	 */
	private Composite statusLine = null;

	/**
	 * Creates a status line contribution item with the given id.
	 * 
	 * @param id
	 *            the contribution item's id, or <code>null</code> if it is to
	 *            have no id
	 */
	public StatusLinePageSelector(String id) {
		this(id, DEFAULT_CHAR_WIDTH);
	}

	/**
	 * Creates a status line contribution item with the given id that displays
	 * the given number of characters.
	 * 
	 * @param id
	 *            the contribution item's id, or <code>null</code> if it is to
	 *            have no id
	 * @param charWidth
	 *            the number of characters to display. 
	 */
	public StatusLinePageSelector(String id, int charWidth) {
		super(id);
		this.charWidth = charWidth;
		setVisible(false); // no text to start with
	}

	public void addPageChangeListener(IPageChangeListener l) {
		if (listeners == null) listeners = new LinkedList<StatusLinePageSelector.IPageChangeListener>();
		//listeners.clear();
		listeners.add(l);
	}

	public void setPageChangeListener(IPageChangeListener l) {
		if (listeners == null) listeners = new LinkedList<StatusLinePageSelector.IPageChangeListener>();
		listeners.clear();
		listeners.add(l);
	}
	
	public void removePageChangeListener(IPageChangeListener l) {
		if (listeners == null) listeners = new LinkedList<StatusLinePageSelector.IPageChangeListener>();
		listeners.remove(l);
	}

	protected void firePageNrChangeListener() {
		if (listeners == null) return;
		for (IPageChangeListener l : listeners) {
			l.pageChange(Integer.parseInt(pageField.getText()));
		}
	}

	private boolean checkPage() {
		String st = pageField.getText();
		try {
			int i = Integer.parseInt(st);
			if (i < 1 || i > this.lastPageNr) return false;
			return true;
		}
		catch (NumberFormatException ex) {
			return false;
		}
	}
	
	public void fill(Composite parent) {
		statusLine = parent;

		Label sep = new Label(parent, SWT.SEPARATOR);
		Composite c = new Composite(parent, SWT.NO_FOCUS);
		c.setLayout(new GridLayout(4, false));
		
		ToolBar bar1 = new ToolBar(c, SWT.FLAT);

		firstPage = new ToolItem(bar1, SWT.FLAT);
		imageFirst = Activator.getImageDescriptor("icons/arrow-stop-180.png").createImage(); //$NON-NLS-1$
		firstPage.setImage(imageFirst);
		firstPage.setToolTipText(Messages.StatusLinePageSelector_ButtonFirst);
		firstPage.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				pageField.setText("1"); //$NON-NLS-1$
				firePageNrChangeListener();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		prevPage = new ToolItem(bar1, SWT.FLAT);
		imagePrev = Activator.getImageDescriptor("icons/arrow-180.png").createImage(); //$NON-NLS-1$
		prevPage.setImage(imagePrev);
		prevPage.setToolTipText(Messages.StatusLinePageSelector_ButtonPrevious);
		prevPage.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				pageField.setText("" + (page-1)); //$NON-NLS-1$
				firePageNrChangeListener();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		if (page == 1) {
			prevPage.setEnabled(false);
			firstPage.setEnabled(false);
		}
		
		pageField = new Text(c, SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		pageField.setToolTipText(Messages.StatusLinePageSelector_tooltip);
		pageField.setTextLimit((""+this.lastPageNr).length()); //$NON-NLS-1$
		
		pageNrField = new Label(c, SWT.SHADOW_NONE);
		pageNrField.setText(" / "+this.lastPageNr); //$NON-NLS-1$
		
		ToolBar bar2 = new ToolBar(c, SWT.FLAT);
		imageNext = Activator.getImageDescriptor("icons/arrow.png").createImage(); //$NON-NLS-1$
		nextPage = new ToolItem(bar2, SWT.FLAT);
		nextPage.setImage(imageNext);
		nextPage.setToolTipText(Messages.StatusLinePageSelector_ButtonNext);
		nextPage.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				pageField.setText("" + (page+1)); //$NON-NLS-1$
				firePageNrChangeListener();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		imageLast = Activator.getImageDescriptor("icons/arrow-stop.png").createImage(); //$NON-NLS-1$
		lastPage = new ToolItem(bar2, SWT.FLAT);
		lastPage.setImage(imageLast);
		lastPage.setToolTipText(Messages.StatusLinePageSelector_ButtonLast);
		lastPage.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				pageField.setText(""+lastPageNr); //$NON-NLS-1$
				firePageNrChangeListener();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		if (page == lastPageNr) {
			lastPage.setEnabled(false);
			nextPage.setEnabled(false);
		}
		
		pageField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 13 || e.keyCode == 8 || e.keyCode == 127
						|| (e.character >= '0' && e.character <= '9')) {
					return;
				}
				else if (e.keyCode == SWT.PAGE_UP && page > 1) {
					pageField.setText("" + (page-1)); //$NON-NLS-1$
					firePageNrChangeListener();
				}
				else if (e.keyCode == SWT.PAGE_DOWN && page < lastPageNr) {
					pageField.setText("" + (page+1)); //$NON-NLS-1$
					firePageNrChangeListener();
				}
				
				if (e.character == 0) return;
				e.doit = false;
			}
			
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if(e.keyCode == 13) {
					if (!checkPage()) {
						MessageDialog.openError(statusLine.getShell(), Messages.StatusLinePageSelector_errorMsg1, 
								Messages.StatusLinePageSelector_errorMsg2
								+lastPageNr);
						e.doit = false;
						return;
					}
					firePageNrChangeListener();
				}
			}
		});
		// Compute the size base on 'charWidth' average char widths
		GC gc = new GC(statusLine);
		gc.setFont(statusLine.getFont());
		FontMetrics fm = gc.getFontMetrics();
		int widthHint = fm.getAverageCharWidth() * charWidth;
		int heightHint = fm.getHeight();
		
		GridData d = new GridData(fm.getAverageCharWidth()*4 + pageField.getBorderWidth(), 
				fm.getHeight() + pageField.getBorderWidth());
		pageField.setLayoutData(d);
		pageField.setText(""+this.page); //$NON-NLS-1$
		gc.dispose();

		StatusLineLayoutData data = new StatusLineLayoutData();
		data.widthHint = widthHint + (imageFirst.getBounds().width + 9)*4;
		data.heightHint = 0;
		c.setLayoutData(data);

		data = new StatusLineLayoutData();
		data.heightHint = heightHint;
		sep.setLayoutData(data);
	}

	/**
	 * An accessor for the current location of this status line contribution
	 * item -- relative to the display.
	 * 
	 * @return The current location of this status line; <code>null</code> if
	 *         not yet initialized.
	 */
	public Point getDisplayLocation() {
		if ((pageField != null) && (statusLine != null)) {
			return statusLine.toDisplay(pageField.getLocation());
		}

		return null;
	}

	/**
	 * Sets the text to be displayed in the status line and updates the status of the 
	 * navigation items.
	 * 
	 */
	public void setPageInfo(int page, int pageNumbers) {
		this.page = page;
		this.lastPageNr = pageNumbers;

		if (pageField != null && !pageField.isDisposed()) {
			pageField.setTextLimit((""+this.lastPageNr).length()); //$NON-NLS-1$
			pageField.setText(""+this.page); //$NON-NLS-1$
		}
		if (pageNrField != null && !pageNrField.isDisposed()) {
			pageNrField.setText(" / "+this.lastPageNr); //$NON-NLS-1$
			
			if (page == 1) {
				prevPage.setEnabled(false);
				firstPage.setEnabled(false);
			} 
			else {
				prevPage.setEnabled(true);
				firstPage.setEnabled(true);
			}
			
			if (page == lastPageNr) {
				nextPage.setEnabled(false);
				lastPage.setEnabled(false);
			}
			else {
				nextPage.setEnabled(true);
				lastPage.setEnabled(true);					
			}

		}

		if (!isVisible()) {
			setVisible(true);
			IContributionManager contributionManager = getParent();

			if (contributionManager != null) {
				contributionManager.update(true);
			}
		}
	}

	public interface IPageChangeListener {
		public void pageChange(int pageNr);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		imageFirst.dispose();
		imageLast.dispose();
		imageNext.dispose();
		imagePrev.dispose();
	}
}
