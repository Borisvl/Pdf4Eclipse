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
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.vonloesch.pdf4eclipse.Messages;

public class StatusLinePageSelector extends ContributionItem {
	private final static int DEFAULT_CHAR_WIDTH = 40;
	
	private int charWidth;

	private Text pageField;
	private Label pageNrField;

	private int page;
	private int pageNr;
	
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
		listeners.clear();
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
			if (i < 1 || i > this.pageNr) return false;
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
		c.setLayout(new GridLayout(2, false));
		
		pageField = new Text(c, SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		pageField.setToolTipText(Messages.StatusLinePageSelector_tooltip);
		pageField.setTextLimit((""+this.pageNr).length()); //$NON-NLS-1$
		
		pageNrField = new Label(c, SWT.SHADOW_NONE);
		pageNrField.setText(" / "+this.pageNr); //$NON-NLS-1$
		
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
				else if (e.keyCode == SWT.PAGE_DOWN && page < pageNr) {
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
								+pageNr);
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
		data.widthHint = widthHint;
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
	 * Sets the text to be displayed in the status line.
	 * 
	 * @param text
	 *            the text to be displayed, must not be <code>null</code>
	 */
	public void setPageInfo(int page, int pageNumbers) {
		this.page = page;
		this.pageNr = pageNumbers;

		if (pageField != null && !pageField.isDisposed()) {
			pageField.setTextLimit((""+this.pageNr).length()); //$NON-NLS-1$
			pageField.setText(""+this.page); //$NON-NLS-1$
		}
		if (pageNrField != null && !pageNrField.isDisposed()) {
			pageNrField.setText(" / "+this.pageNr); //$NON-NLS-1$
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
}
