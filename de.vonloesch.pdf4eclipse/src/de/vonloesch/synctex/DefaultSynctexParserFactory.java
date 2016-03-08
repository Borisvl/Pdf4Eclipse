/*******************************************************************************
 * Copyright (c) 2016 Boris von Loesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andreas Turban - initial API and implementation
 ******************************************************************************/
package de.vonloesch.synctex;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * Factory to create {@link ISynctexParser} instances and wrap them with
 * contributed adapters of the {@link ISynctexParser}, with
 * {@link ISynctexParserAdapterFactory}.
 * 
 * <p>
 * The {@link ISynctexParserAdapterFactory} instances are contributed via an
 * eclipse extension point.
 * </p>
 * 
 * @author Andreas Turban
 *
 */
public class DefaultSynctexParserFactory {

	private static final String EXT_ID = "de.vonloesch.pdf4Eclipse.synctexParserAdapter";

	private static final List<ISynctexParserAdapterFactory> adapters;

	static {
		ArrayList<ISynctexParserAdapterFactory> adaptersLoc = new ArrayList<ISynctexParserAdapterFactory>();
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXT_ID);
		for (IConfigurationElement elem : configurationElements) {
			try {
				Object obj = elem.createExecutableExtension("class");
				if (obj instanceof ISynctexParserAdapterFactory) {
					adaptersLoc.add((ISynctexParserAdapterFactory) obj);
				}
			} catch (CoreException e) {
				// Ignore wrong contributed adapter
			}
		}
		adaptersLoc.trimToSize();
		adapters = Collections.unmodifiableList(adaptersLoc);
	}

	public static ISynctexParser create(BufferedReader r) {
		ISynctexParser parser = new SimpleSynctexParser(r);
		for (ISynctexParserAdapterFactory factory : adapters) {
			parser = factory.createAdapter(parser);
		}
		return parser;
	}

}
