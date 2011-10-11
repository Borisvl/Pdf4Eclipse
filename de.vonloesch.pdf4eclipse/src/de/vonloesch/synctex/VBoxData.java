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
package de.vonloesch.synctex;

public class VBoxData {
	public int[] pos;
	public int[] size;
	
	public VBoxData(int[] pos, int[] size) {
		this.pos = pos;
		this.size = size;
		assert size.length == 3;
	}
}
