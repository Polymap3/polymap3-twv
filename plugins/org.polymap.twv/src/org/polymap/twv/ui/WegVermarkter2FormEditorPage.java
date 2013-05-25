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

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.ManyAssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.VermarkterComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.ui.rhei.SelectableCompositesFeatureContentProvider;
import org.polymap.twv.ui.rhei.SelectableCompositesFeatureContentProvider.FeatureTableElement;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegVermarkter2FormEditorPage
        extends TwvDefaultFormEditorPage
        implements IFormEditorPage2 {

    private WegComposite                    weg;

    private FeatureTableViewer              viewer;

    private boolean                         isDirty;

    // private ManyAssociationAdapter<VermarkterComposite> vermarkterAdapter;
    // list der vermarkter die noch am Weg dran sind, Änderungen passieren nur hier
    // als Adapter
    private final List<VermarkterComposite> wegVermarkter      = new ArrayList<VermarkterComposite>();

    // liste der Vermarkter die gerade ausgewählt um diese bspw. zu Löschen
    private final List<VermarkterComposite> selectedVermarkter = new ArrayList<VermarkterComposite>();


    public WegVermarkter2FormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegVermarkter2FormEditorPage.class.getName(), "Vermarkter", feature, featureStore );
        this.weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
        wegVermarkter.addAll( weg.vermarkter().toList() );
        // selectedVermarkter = weg.vermarkter().toList();
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setFormTitle( formattedTitle( "Tourismusweg", weg.name().get(), getTitle() ) );

        final Composite parent = site.getPageBody();

        int TOPSPACING = 3;

        viewer = new FeatureTableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().left( 2 ).height( 200 ).right( 90 ).create() );

        VermarkterSelector addAction = new VermarkterSelector(wegVermarkter) {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                // TODO Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }


            @Override
            protected void addSelected( List<VermarkterComposite> elements )
                    throws Exception {
                wegVermarkter.addAll( elements );
                // if something returns, add them to wegVermarkter
                // and reload
                doLoad( new NullProgressMonitor() );
                isDirty = true;
                site.fireEvent( this, "vermarkter", IFormFieldListener.VALUE_CHANGE,
                        wegVermarkter );
            }
        };
        ActionButton addBtn = new ActionButton( parent, addAction );
        addBtn.setLayoutData( new SimpleFormData().left( viewer.getTable(), SPACING ).right( 98, -SPACING ).height( 30 )
                .create() );

        DeleteCompositeAction deleteAction = new DeleteCompositeAction() {

            protected void execute()
                    throws Exception {
                for (VermarkterComposite vermarkter : selectedVermarkter) {
                    wegVermarkter.remove( vermarkter );
                }
                doLoad( new NullProgressMonitor() );
                isDirty = true;
                site.fireEvent( this, "vermarkter", IFormFieldListener.VALUE_CHANGE,
                        wegVermarkter );
            }
        };
        ActionButton delBtn = new ActionButton( parent, deleteAction );
        delBtn.setLayoutData( new SimpleFormData().left( viewer.getTable(), SPACING ).top( addBtn, SPACING )
                .right( 98, -SPACING ).height( 30 ).create() );

        parent.layout( true );

        viewer.addSelectionChangedListener( new ISelectionChangedListener() {

            @Override
            public void selectionChanged( SelectionChangedEvent event ) {
                StructuredSelection selection = (StructuredSelection)event.getSelection();
                List<FeatureTableElement> selections = selection.toList();
                selectedVermarkter.clear();
                for (FeatureTableElement tableRow : selections) {
                    selectedVermarkter.add( (VermarkterComposite)tableRow.getComposite() );
                }
            }
        } );

        // columns
        EntityType<VermarkterComposite> type = addViewerColumns( viewer );

        // model/content
        viewer.setContent( new SelectableCompositesFeatureContentProvider( null, type ) );

        try {
            doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        parent.layout( true );

        viewer.addDoubleClickListener( new IDoubleClickListener() {

            @Override
            public void doubleClick( DoubleClickEvent event ) {
                StructuredSelection selection = (StructuredSelection)event.getSelection();
                FeatureTableElement tableRow = (FeatureTableElement)selection.getFirstElement();
                TwvPlugin.openEditor( fs, "Vermarkter", (VermarkterComposite)tableRow.getComposite() );
            }
        } );
    }


    protected EntityType<VermarkterComposite> addViewerColumns( FeatureTableViewer viewer ) {
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
        return type;
    }


    public List<VermarkterComposite> getElements() {
        // List<VermarkterComposite> allVC = new ArrayList<VermarkterComposite>();
        // for (VermarkterComposite vc : weg.vermarkter()) {
        // allVC.add( vc );
        // }
        return wegVermarkter;
    }


    @Override
    public boolean isDirty() {
        return isDirty;
    }


    @Override
    public boolean isValid() {
        return true;
    }


    @Override
    public void doLoad( IProgressMonitor monitor )
            throws Exception {
        if (viewer != null) {
            viewer.setInput( getElements() );

            // vermarkter aus adapter holen und selektieren
            // Umweg über selectedIndices, da ich sonst nicht mehr an die
            // TableElemente
            selectedVermarkter.clear();
            // selectedVermarkter.addAll( weg.vermarkter().toList() );
            viewer.getTable().deselectAll();
            // viewer.getTable().select(
            // ((SelectableCompositesFeatureContentProvider)viewer.getContentProvider())
            // .getIndicesForElements( selectedVermarkter ) );
        }
        isDirty = false;
    }


    @Override
    public void doSubmit( IProgressMonitor monitor )
            throws Exception {
        new ManyAssociationAdapter<VermarkterComposite>( "vermarkter", weg.vermarkter() ).setValue( wegVermarkter );
        isDirty = false;
    }


    @Override
    public void dispose() {
    }
}