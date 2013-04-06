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

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.field.UploadFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.ImageValue;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegobjektComposite;
import org.polymap.twv.ui.rhei.ReloadableImageValuePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.AssociationCallback;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.PropertyCallback;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegWegobjektFormEditorPage
        extends TwvDefaultFormEditorPageWithFeatureTable<WegobjektComposite> {

    private WegComposite        weg;

    private final static String prefix = WegWegobjektFormEditorPage.class.getSimpleName();


    public WegWegobjektFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegWegobjektFormEditorPage.class.getName(), "Wegobjekte", feature, featureStore );
        this.featureStore = featureStore;

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setFormTitle( formattedTitle( "Tourismusweg", weg.name().get(), getTitle() ) );

        Composite parent = site.getPageBody();
        Composite baseForm = createForm( parent );
        createTableForm( parent, baseForm );
        refreshFieldEnablement();
    }


    @Override
    public void doLoad( IProgressMonitor monitor )
            throws Exception {
        super.doLoad( monitor );
        // enable all Fields
        if (pageSite != null) {
            refreshFieldEnablement();
        }
    }


    /**
     *
     */
    private void refreshFieldEnablement() {
        boolean enabled = selectedComposite.get() != null;
        pageSite.setFieldEnabled( prefix + "wegobjektName", enabled );
        pageSite.setFieldEnabled( prefix + "beschreibung", enabled );
        pageSite.setFieldEnabled( prefix + "bild", enabled );
    }


    public boolean isValid() {
        return true;
    }


    // kopiert von WegobjektFormEditorPage
    public Composite createForm( Composite parent ) {

        Composite line1 = newFormField( "Name" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<WegobjektComposite>( selectedComposite,
                                prefix + "wegobjektName", new AssociationCallback<WegobjektComposite>() {

                                    public Association get( WegobjektComposite entity ) {
                                        return entity.wegobjektName();
                                    }
                                } ) )
                .setField( namedAssocationsPicklist( SchildartComposite.class, true ) )
                .setLayoutData( left().create() ).create();

        Composite line2 = newFormField( "Beschreibung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<WegobjektComposite>( selectedComposite,
                                prefix + "beschreibung", new PropertyCallback<WegobjektComposite>() {

                                    public Property get( WegobjektComposite entity ) {
                                        return entity.beschreibung();
                                    }
                                } ) ).setField( new TextFormField() )
                .setLayoutData( left().top( line1 ).height( 80 ).right( RIGHT ).create() )
                .setToolTipText( "Beschreibung des Wegobjektes" ).create();

        Composite line3 = newFormField( "Bild" )
                .setParent( parent )
                .setProperty(
                        new ReloadableImageValuePropertyAdapter<WegobjektComposite>(
                                selectedComposite,
                                prefix + "bild",
                                new ReloadableImageValuePropertyAdapter.PropertyCallback<WegobjektComposite>() {

                                    public Property<ImageValue> get( WegobjektComposite entity ) {
                                        return entity.bild();
                                    }
                                } ) ).setField( new UploadFormField( TwvPlugin.getImagesRoot() ) )
                .setLayoutData( left().top( line2 ).create() ).create();

        return line3;
    }


    protected EntityType<WegobjektComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final TwvRepository repo = TwvRepository.instance();
        final EntityType<WegobjektComposite> type = repo.entityType( WegobjektComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "beschreibung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Beschreibung" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Name" ) );
        return type;
    }


    public Iterable<WegobjektComposite> getElements() {
        return WegobjektComposite.Mixin.forEntity( weg );
    }
}