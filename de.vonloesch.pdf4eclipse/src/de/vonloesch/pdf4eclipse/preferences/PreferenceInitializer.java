/*******************************************************************************
 * Copyright (c) 2012 Boris von Loesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Boris von Loesch - initial API and implementation
 ******************************************************************************/

package de.vonloesch.pdf4eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.vonloesch.pdf4eclipse.Activator;
import de.vonloesch.pdf4eclipse.editors.handlers.ToggleAutoTrimHandler;
import de.vonloesch.pdf4eclipse.model.PDFFactory;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PSEUDO_CONTINUOUS_SCROLLING, true);
		store.setDefault(PreferenceConstants.PDF_RENDERER, PDFFactory.STRATEGY_SUN_JPEDAL);
		store.setDefault(ToggleAutoTrimHandler.PREF_AUTOTRIM_ID, true);
	}

}
