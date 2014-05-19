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
package org.polymap.twv.ui.form;

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

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.ui.ActionButton;
import org.polymap.twv.ui.DeleteCompositeAction;
import org.polymap.twv.ui.TwvDefaultFormEditorPage;
import org.polymap.twv.ui.rhei.SelectableCompositesFeatureContentProvider;
import org.polymap.twv.ui.rhei.SelectableCompositesFeatureContentProvider.FeatureTableElement;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegSchilder2FormEditorPage
        extends TwvDefaultFormEditorPage
        implements IFormEditorPage2 {

    private WegComposite                    weg;

    private FeatureTableViewer              viewer;

    private boolean                         isDirty;

    // private ManyAssociationAdapter<VermarkterComposite> vermarkterAdapter;
    // list der vermarkter die noch am Weg dran sind, Änderungen passieren nur hier
    // als Adapter
    private final List<SchildComposite> wegSchilder      = new ArrayList<SchildComposite>();
    private final List<SchildComposite> toDelete      = new ArrayList<SchildComposite>();

    // liste der Vermarkter die gerade ausgewählt um diese bspw. zu Löschen
    private final List<SchildComposite> selectedSchild = new ArrayList<SchildComposite>();


    public WegSchilder2FormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegSchilder2FormEditorPage.class.getName(), "Schilder", feature, featureStore );
        this.weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
        wegSchilder.addAll( SchildComposite.Mixin.forEntity( weg ) );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setFormTitle( formattedTitle( "Tourismusweg", weg.name().get(), getTitle() ) );

        final Composite parent = site.getPageBody();

        int TOPSPACING = 3;

        viewer = new FeatureTableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().left( 2 ).height( 400 ).right( 90 ).create() );

        DeleteCompositeAction deleteAction = new DeleteCompositeAction() {

            protected void execute()
                    throws Exception {
                for (SchildComposite schild : selectedSchild) {
                    toDelete.add( schild );
                    wegSchilder.remove( schild );
                }
                doLoad( new NullProgressMonitor() );
                isDirty = true;
                site.fireEvent( this, "schilder", IFormFieldListener.VALUE_CHANGE,
                        wegSchilder );
            }
        };
        ActionButton delBtn = new ActionButton( parent, deleteAction );
        delBtn.setLayoutData( new SimpleFormData().left( viewer.getTable(), SPACING )
                .right( 98, -SPACING ).height( 30 ).create() );

        parent.layout( true );

        viewer.addSelectionChangedListener( new ISelectionChangedListener() {

            @Override
            public void selectionChanged( SelectionChangedEvent event ) {
                StructuredSelection selection = (StructuredSelection)event.getSelection();
                List<FeatureTableElement> selections = selection.toList();
                selectedSchild.clear();
                for (FeatureTableElement tableRow : selections) {
                    selectedSchild.add( (SchildComposite)tableRow.getComposite() );
                }
            }
        } );

        // columns
        EntityType<SchildComposite> type = addViewerColumns( viewer );

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
                TwvPlugin.openEditor( fs, "Schild", (SchildComposite)tableRow.getComposite() );
            }
        } );
    }


    protected EntityType<SchildComposite> addViewerColumns( FeatureTableViewer viewer ) {
        final TwvRepository repo = TwvRepository.instance();
        final EntityType<SchildComposite> type = repo.entityType( SchildComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "laufendeNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Nummer" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "bestandsNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Bestandsnummer" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "beschriftung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Beschriftung" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "schildart" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Schildart" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "bildName" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Bildname" ) );
        return type;
    }


    public List<SchildComposite> getElements() {
        // List<VermarkterComposite> allVC = new ArrayList<VermarkterComposite>();
        // for (VermarkterComposite vc : weg.vermarkter()) {
        // allVC.add( vc );
        // }
        return wegSchilder;
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
            selectedSchild.clear();
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
        for (SchildComposite schild : toDelete) {
            schild.wege().remove( weg );
        }
        isDirty = false;
    }


    @Override
    public void dispose() {
    }
}