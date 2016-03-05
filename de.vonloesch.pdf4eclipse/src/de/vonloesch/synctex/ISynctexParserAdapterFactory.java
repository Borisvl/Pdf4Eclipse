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


/**
 * {@link ISynctexParserAdapterFactory} wraps an passed {@link ISynctexParser} with an adapter to change the forward and reverse search behavior.
 * contributed adapters of the {@link ISynctexParser}, with
 * {@link ISynctexParserAdapterFactory}.
 * 
 * <p>
 * The {@link ISynctexParserAdapterFactory} instances are contributed via the 
 * eclipse extension point "de.vonloesch.pdf4Eclipse.synctexParserAdapter".
 * </p>
 * 
 * @author Andreas Turban
 *
 */
public interface ISynctexParserAdapterFactory {

	/**
	 * Shall wrap the passed {@link ISynctexParser} to adapt the forward and reverse search behavior.
	 */
	ISynctexParser createAdapter(ISynctexParser parser);
}
