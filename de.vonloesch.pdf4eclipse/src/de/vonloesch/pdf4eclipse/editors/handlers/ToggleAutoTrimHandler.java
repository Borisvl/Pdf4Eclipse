/*******************************************************************************
 * Copyright (c) 2011 Boris von Loesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Boris von Loesch - initial API and implementation
 *     Robert Bamler - auto-trimming of page margins
 ******************************************************************************/
package de.vonloesch.pdf4eclipse.editors.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.menus.UIElement;
import org.osgi.service.prefs.BackingStoreException;

public class ToggleAutoTrimHandler extends AbstractHandler implements IElementUpdater{

	public final static String PREF_AUTOTRIM_ID = "de.vonloesch.pdf4eclipse.preferences.autoTrim";

	private final static String COMMAND_ID = "PDFViewer.command.ToggleAutoTrim";
	private final static String STATE_ID = RegistryToggleState.STATE_ID;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();
		State state = command.getState(STATE_ID);
		state.setValue(!(Boolean) state.getValue());

		ICommandService service =
				(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		service.refreshElements(command.getId(), null);
		IEclipsePreferences prefs = (new InstanceScope()).getNode(de.vonloesch.pdf4eclipse.Activator.PLUGIN_ID);
		prefs.putBoolean(PREF_AUTOTRIM_ID, ((Boolean) state.getValue()).booleanValue());
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			//Do nothing
		}
		return null;
	}	
	
	@Override
	public void updateElement(UIElement element, Map parameters) {
		ICommandService service =
				(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand(COMMAND_ID);
		State state = command.getState(STATE_ID);
		element.setChecked(((Boolean) state.getValue()).booleanValue());		
	}

}
