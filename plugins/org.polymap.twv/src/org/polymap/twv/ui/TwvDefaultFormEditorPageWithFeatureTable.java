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
import java.util.Collection;
import java.util.List;

import java.beans.PropertyChangeEvent;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.CompositeProvider;
import org.polymap.twv.ui.rhei.SelectableCompositesFeatureContentProvider;
import org.polymap.twv.ui.rhei.SelectableCompositesFeatureContentProvider.FeatureTableElement;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class TwvDefaultFormEditorPageWithFeatureTable<T extends Entity>
        extends TwvDefaultFormEditorPage
        implements IFormEditorPage2 {

    protected FeatureStore         featureStore;

    private FeatureTableViewer     viewer;

    private boolean                dirty;

    private List<T>                model             = new ArrayList<T>();

    protected CompositeProvider<T> selectedComposite = new CompositeProvider<T>();

    private List<IFormField>       reloadables       = new ArrayList<IFormField>();


    protected List<T> getModel() {
        return model;
    }


    /**
     * 
     * @param id
     * @param title
     * @param feature
     * @param featureStore
     */
    public TwvDefaultFormEditorPageWithFeatureTable( String id, String title, Feature feature, FeatureStore featureStore ) {
        super( id, title, feature, featureStore );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
    }


    protected abstract EntityType addViewerColumns( FeatureTableViewer viewer );


    protected void refreshReloadables()
            throws Exception {
        boolean enabled = selectedComposite.get() != null;
        for (IFormField field : reloadables) {
            field.setEnabled( enabled );
            field.load();
        }
    }


    protected IFormField reloadable( IFormField formField ) {
        reloadables.add( formField );
        return formField;
    }


    protected Composite createTableForm( Composite parent, Control top, boolean addAllowed, boolean deleteAllowed ) {

        int TOPSPACING = 3;
        viewer = new FeatureTableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData(
                new SimpleFormData().fill().left( 2 ).height( 100 ).right( 90 ).top( top, TOPSPACING ).create() );

        // columns
        EntityType<T> type = addViewerColumns( viewer );

        // model/content
        viewer.setContent( new SelectableCompositesFeatureContentProvider( null, type ) );
        try {
            doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        ActionButton addBtn = null;
        if (addAllowed) {
            AddCompositeAction<T> addAction = new AddCompositeAction<T>() {

                protected void execute()
                        throws Exception {

                    dirty = true;
                    T newComposite = createNewComposite();
                    selectedComposite.set( newComposite );
                    model.add( 0, newComposite );

                    doLoad( new NullProgressMonitor() );
                    viewer.getTable().deselectAll();
                    viewer.getTable().select(
                            ((SelectableCompositesFeatureContentProvider)viewer.getContentProvider())
                                    .getIndicesForElements( newComposite ) );
                    // refreshReloadables();
                }
            };
            addBtn = new ActionButton( parent, addAction );
            addBtn.setLayoutData( new SimpleFormData().left( viewer.getTable(), SPACING ).top( top, TOPSPACING )
                    .right( 98, -SPACING ).height( 30 ).create() );
        }

        if (deleteAllowed) {
            DeleteCompositeAction<T> deleteAction = new DeleteCompositeAction<T>() {

                protected void execute()
                        throws Exception {

                    if (selectedComposite.get() != null) {
                        T toSelect = selectedComposite.get();
                        model.remove( toSelect );
                        if (viewer != null) {
                            Collection<T> viewerInput = (Collection<T>)viewer.getInput();
                            viewerInput.remove( toSelect );
                        }

                        deleteComposite( toSelect );

                        doLoad( new NullProgressMonitor() );
                        refreshReloadables();

                        dirty = true;
                        // pageSite.fireEvent( this, this.getClass().getSimpleName(),
                        // IFormFieldListener.VALUE_CHANGE, null );
                    }
                    // Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    //
                    // public void run() {
                    // // update dirty/valid flags of the editor
                    // pageSite.fireEvent( this, getClass().getSimpleName(),
                    // IFormFieldListener.VALUE_CHANGE, null );
                    //
                    // viewer.refresh( true );
                    // viewer.getTable().layout( true );
                    // }
                    //
                    // } );
                }
            };
            ActionButton delBtn = new ActionButton( parent, deleteAction );
            delBtn.setLayoutData( new SimpleFormData().left( viewer.getTable(), SPACING )
                    .top( addBtn != null ? addBtn : top, addBtn != null ? SPACING : TOPSPACING ).right( 98, -SPACING )
                    .height( 30 ).create() );
        }
        parent.layout( true );

        viewer.addSelectionChangedListener( new ISelectionChangedListener() {

            @Override
            public void selectionChanged( SelectionChangedEvent event ) {
                StructuredSelection selection = (StructuredSelection)event.getSelection();
                FeatureTableElement tableRow = (FeatureTableElement)selection.getFirstElement();
                if (tableRow != null) {
                    selectedComposite.set( (T)tableRow.getComposite() );
                    try {
                        refreshReloadables();
                    }
                    catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } );
        return viewer.getTable();
    }


    protected void deleteComposite( T toSelect ) {
        TwvRepository.instance().removeEntity( toSelect );
        selectedComposite.set( null );
    }


    /**
     * 
     * @return
     */
    protected T createNewComposite() {
        // must only be implemented if add-action on table is enabled
        throw new RuntimeException( "not yet implemented." );
    }


    public void updateElements( Collection<T> coll ) {
        if (viewer != null && !viewer.isBusy()) {
            // Polymap.getSessionDisplay().asyncExec( new Runnable() {
            //
            // public void run() {
            // viewer.refresh( true );
            // viewer.getTable().layout( true );
            // viewer.getTable().getParent().redraw();
            // }
            // } );
            viewer.refresh( true );
            // viewer.getTable().layout();
            // viewer.getTable().getParent().redraw();
        }
    }


    protected abstract Iterable<T> getElements();


    public void doLoad( IProgressMonitor monitor )
            throws Exception {
        if (viewer != null && !viewer.isBusy()) {
            // model = new HashMap();
            for (T elm : getElements()) {
                if (!model.contains( elm )) {
                    // TODO wie wird der EventHandler registriert?
                    // elm.addPropertyChangeListener( this );
                    model.add( elm );
                } // TODO otherwise keep the current loaded object, but whats with
                  // deletion?
            }
            viewer.setInput( model );
            // Polymap.getSessionDisplay().asyncExec( new Runnable() {
            //
            // public void run() {
            // viewer.refresh( true );
            // viewer.getTable().layout( true );
            // }
            // } );
        }
        if (pageSite != null) {
            refreshReloadables();
        }
        dirty = false;
    }


    public void doSubmit( IProgressMonitor monitor )
            throws Exception {
        if (model != null) {
            updateElements( model );
        }
        dirty = false;
    }


    public boolean isDirty() {
        return dirty;
    }


    public boolean isValid() {
        return true;
    }


    public void dispose() {
    }


    /**
     * Handles Value property changes.
     */
    public void propertyChange( PropertyChangeEvent evt ) {
        try {
            dirty = true;
            // update dirty/valid flags of the editor
            pageSite.fireEvent( this, this.getClass().getSimpleName(), IFormFieldListener.VALUE_CHANGE, null );
            if (!viewer.isBusy()) {
                viewer.refresh( true );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

}