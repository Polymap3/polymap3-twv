/*
 * polymap.org Copyright 2013 Polymap GmbH. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import org.polymap.core.model.EntityType;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.IFormFieldListener;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.VermarkterComposite;
import org.polymap.twv.ui.rhei.SelectableCompositesFeatureContentProvider;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class VermarkterSelector
        extends Action
        implements IFormFieldListener {

    private static Log                      log     = LogFactory.getLog( VermarkterSelector.class );

    private List<VermarkterComposite>       content = new ArrayList();                               ;

    private final List<VermarkterComposite> allwaysAdded;


    public VermarkterSelector( List<VermarkterComposite> allwaysAdded ) {
        super( "Auswählen" );
        this.allwaysAdded = allwaysAdded;

        setToolTipText( "Vermarkter auswählen" );
    }


    protected abstract void addSelected( List<VermarkterComposite> elements )
            throws Exception;


    public void run() {
        try {

            content.clear();
            for (VermarkterComposite fc : TwvRepository.instance().findEntities( VermarkterComposite.class, null, 0,
                    10000 )) {
                if (!allwaysAdded.contains( fc )) {
                    content.add( fc );
                }
            }

            FlurstueckTableDialog dialog = new FlurstueckTableDialog();
            dialog.setBlockOnOpen( true );

            if (dialog.open() == Window.OK && dialog.sel.length > 0) {
                List<VermarkterComposite> elements = new ArrayList<VermarkterComposite>();
                for (final IFeatureTableElement sel : dialog.sel) {
                    SelectableCompositesFeatureContentProvider.FeatureTableElement elem = (SelectableCompositesFeatureContentProvider.FeatureTableElement)sel;
                    elements.add( (VermarkterComposite)elem.getComposite() );
                }
                addSelected( elements );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( TwvPlugin.PLUGIN_ID, this, "Fehler beim Öffnen der Vermarktertabelle.", e );
        }
    }


    /**
     * 
     */
    class FlurstueckTableDialog
            extends TitleAreaDialog {

        private FeatureTableViewer     viewer;

        private IFeatureTableElement[] sel;


        public FlurstueckTableDialog() {
            super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
            setShellStyle( getShellStyle() | SWT.RESIZE );
        }


        protected Image getImage() {
            return getShell().getDisplay().getSystemImage( SWT.ICON_QUESTION );
        }


        protected Point getInitialSize() {
            return new Point( 800, 600 );
            // return super.getInitialSize();
        }


        protected Control createDialogArea( Composite parent ) {
            Composite area = (Composite)super.createDialogArea( parent );

            setTitle( "Wählen Sie die Vermarkter." );
            // setMessage( "." );

            viewer = new FeatureTableViewer( area, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
            viewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

            // entity types
            final TwvRepository repo = TwvRepository.instance();
            final EntityType<VermarkterComposite> type = repo.entityType( VermarkterComposite.class );

            PropertyDescriptor prop = null;
            prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Name" ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "ansprechpartner" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Ansprechpartner" ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "telefon" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Telefon" ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "email" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "EMail" ) );

            viewer.setContent( new SelectableCompositesFeatureContentProvider( null, type ) );
            viewer.setInput( content );

            // selection
            viewer.addSelectionChangedListener( new ISelectionChangedListener() {

                public void selectionChanged( SelectionChangedEvent ev ) {
                    sel = viewer.getSelectedElements();
                    getButton( IDialogConstants.OK_ID ).setEnabled( sel.length > 0 );
                }
            } );

            area.pack();
            return area;
        }


        protected void createButtonsForButtonBar( Composite parent ) {
            super.createButtonsForButtonBar( parent );
            // createButton( parent, RESET_BUTTON, "Zurücksetzen", false );

            getButton( IDialogConstants.OK_ID ).setEnabled( false );
        }
    }
}
