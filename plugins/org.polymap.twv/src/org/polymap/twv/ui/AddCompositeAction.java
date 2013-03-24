/*
 * polymap.org Copyright 2012, Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.twv.ui;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.CompositesFeatureContentProvider;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;

import org.polymap.twv.TwvPlugin;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class AddCompositeAction<A extends Entity>
        extends Action
        implements ISelectionChangedListener {

    private static Log  log = LogFactory.getLog( AddCompositeAction.class );

    private Class<A>    artType;

    private Iterable<A> content;


    public AddCompositeAction() {
        super( "Hinzufügen" );
        setToolTipText( "Eintrag hinzufügen" );
        setImageDescriptor( TwvPlugin.imageDescriptorFromPlugin( TwvPlugin.PLUGIN_ID,
                "icons/add.gif" ) );
        setEnabled( true );
    }


    public void selectionChanged( SelectionChangedEvent ev ) {
    }


    protected abstract void execute()
            throws Exception;


    public void run() {
        try {
            execute();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( TwvPlugin.PLUGIN_ID, this, "Fehler beim Anlegen.", e );
        }
    }
}
