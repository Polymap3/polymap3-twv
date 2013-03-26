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

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.field.UploadFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.ImageValue;
import org.polymap.twv.model.data.PfeilrichtungComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.ui.rhei.ReloadableImageValuePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.AssociationCallback;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.PropertyCallback;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegSchilderFormEditorPage
        extends TwvDefaultFormEditorPageWithFeatureTable<SchildComposite> {

    private WegComposite        weg;

    private final static String prefix = WegSchilderFormEditorPage.class.getSimpleName();


    public WegSchilderFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegSchilderFormEditorPage.class.getName(), "Schilder", feature, featureStore );
        this.featureStore = featureStore;

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite parent = site.getPageBody();
        parent.setLayout( new FormLayout() );

        Composite schildForm = createSchildForm( parent );
        createTableForm( parent, schildForm );
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
        pageSite.setFieldEnabled( prefix + "schildart", enabled );
        pageSite.setFieldEnabled( prefix + "laufendeNr", enabled );
        pageSite.setFieldEnabled( prefix + "pfeilrichtung", enabled );
        pageSite.setFieldEnabled( prefix + "material", enabled );
        pageSite.setFieldEnabled( prefix + "beschriftung", enabled );
        pageSite.setFieldEnabled( prefix + "befestigung", enabled );
        pageSite.setFieldEnabled( prefix + "bild", enabled );

        // TODO validator not null an der Number
    }


    public boolean isValid() {
        return true;
    }


    // kopiert von SchildFormEditorPage
    public Composite createSchildForm( Composite parent ) {

        Composite line1 = newFormField( "Schildart" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite,
                                prefix + "schildart", new AssociationCallback<SchildComposite>() {

                                    public Association get( SchildComposite entity ) {
                                        return entity.schildart();
                                    }
                                } ) )
                .setField( namedAssocationsPicklist( SchildartComposite.class, true ) )
                .setLayoutData( left().create() ).create();

        newFormField( "Nummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite,
                                prefix + "laufendeNr", new PropertyCallback<SchildComposite>() {

                                    public Property get( SchildComposite entity ) {
                                        return entity.laufendeNr();
                                    }
                                } ) ).setField( new StringFormField() )
                .setLayoutData( right().create() ).setToolTipText( "laufende Schild Nummer" )
                .create();

        Composite line2 = newFormField( "Pfeilrichtung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite,
                                prefix + "pfeilrichtung", new AssociationCallback<SchildComposite>() {

                                    public Association get( SchildComposite entity ) {
                                        return entity.pfeilrichtung();
                                    }
                                } ) )
                .setField( namedAssocationsPicklist( PfeilrichtungComposite.class ) )
                .setLayoutData( left().top( line1 ).create() ).create();

        newFormField( "Material" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite,
                                prefix + "material", new AssociationCallback<SchildComposite>() {

                                    public Association get( SchildComposite entity ) {
                                        return entity.material();
                                    }
                                } ) )
                .setField( namedAssocationsPicklist( SchildmaterialComposite.class ) )
                .setLayoutData( right().top( line1 ).create() ).create();

        Composite line3 = newFormField( "Beschriftung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite,
                                prefix + "beschriftung", new PropertyCallback<SchildComposite>() {

                                    public Property get( SchildComposite entity ) {
                                        return entity.beschriftung();
                                    }
                                } ) ).setField( new TextFormField() )
                .setLayoutData( left().top( line2 ).height( 50 ).right( RIGHT ).create() )
                .setToolTipText( "Schildbeschriftung mit Entfernungsangabe und Zusatzinfo" )
                .create();

        Composite line4 = newFormField( "Befestigung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite,
                                prefix + "befestigung", new PropertyCallback<SchildComposite>() {

                                    public Property get( SchildComposite entity ) {
                                        return entity.befestigung();
                                    }
                                } ) ).setField( new TextFormField() )
                .setLayoutData( left().top( line3 ).height( 50 ).create() ).create();

        Composite line5 = newFormField( "Bild" )
                .setParent( parent )
                .setProperty(
                        new ReloadableImageValuePropertyAdapter<SchildComposite>(
                                selectedComposite,
                                prefix + "bild",
                                new ReloadableImageValuePropertyAdapter.PropertyCallback<SchildComposite>() {

                                    public Property<ImageValue> get( SchildComposite entity ) {
                                        return entity.bild();
                                    }
                                } ) ).setField( new UploadFormField( TwvPlugin.getImagesRoot() ) )
                .setLayoutData( left().top( line4 ).create() ).create();

        return line5;
    }


    protected EntityType<SchildComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final TwvRepository repo = TwvRepository.instance();
        final EntityType<SchildComposite> type = repo.entityType( SchildComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "laufendeNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "laufende Nr." ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Schildart" ) );
        return type;
    }


    public Iterable<SchildComposite> getElements() {
        return SchildComposite.Mixin.forEntity( weg );
    }
}