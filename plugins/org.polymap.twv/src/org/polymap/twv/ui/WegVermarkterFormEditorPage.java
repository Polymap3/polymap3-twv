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

import org.qi4j.api.property.Property;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.VermarkterComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.PropertyCallback;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegVermarkterFormEditorPage
        extends TwvDefaultFormEditorPageWithFeatureTable<VermarkterComposite> {

    private WegComposite weg;

    public WegVermarkterFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegVermarkterFormEditorPage.class.getName(), "Vermarkter", feature, featureStore );
        this.featureStore = featureStore;
        this.weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setFormTitle( formattedTitle( "Tourismusweg", weg.name().get(), getTitle() ) );

        Composite parent = site.getPageBody();
        Composite form = createForm( parent );
        createTableForm( parent, form, true );
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
        pageSite.setFieldEnabled( "name", enabled );
        pageSite.setFieldEnabled( "ansprechpartner", enabled );
        pageSite.setFieldEnabled( "strasse", enabled );
        pageSite.setFieldEnabled( "hausnummer", enabled );
        pageSite.setFieldEnabled( "plz", enabled );
        pageSite.setFieldEnabled( "ort", enabled );
        pageSite.setFieldEnabled( "telefon", enabled );
        pageSite.setFieldEnabled( "email", enabled );
        pageSite.setFieldEnabled( "angebot", enabled );
        pageSite.setFieldEnabled( "url", enabled );
        
        // TODO enable NotNullValidator für name
        // FormFieldComposite-API gibt das aber nicht her
    }


    public boolean isValid() {
        return true;
    }


    // kopiert von SchildFormEditorPage
    public Composite createForm( Composite parent ) {

        Composite name = newFormField( "Name" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "name", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.name();
                                    }
                                } ) )
                .setField( new StringFormField() ).setLayoutData( left().create() ).create();

        newFormField( "Ansprechpartner" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "ansprechpartner", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.ansprechpartner();
                                    }
                                } ) ).setField( new StringFormField() )
                .setLayoutData( right().create() ).create();

        Composite line2 = newFormField( "Straße" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "strasse", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.strasse();
                                    }
                                } ) ).setField( new StringFormField() )
                .setLayoutData( left().right( 70 ).top( name ).create() ).create();

        newFormField( "Hausnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "hausnummer", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.hausnummer();
                                    }
                                } ) ).setField( new StringFormField() )
                .setLayoutData( right().left( 70 ).top( name ).create() ).create();

        Composite line3 = newFormField( "PLZ" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "plz", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.plz();
                                    }
                                } ) ).setField( new StringFormField() )
                .setLayoutData( left().right( 30 ).top( line2 ).create() ).create();

        newFormField( "Ort" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "ort", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.ort();
                                    }
                                } ) ).setField( new StringFormField() )
                .setLayoutData( right().left( 30 ).top( line2 ).create() ).create();
        Composite line4 = newFormField( "Telefon" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "telefon", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.telefon();
                                    }
                                } ) ).setField( new StringFormField() )
                .setLayoutData( left().top( line3 ).create() ).create();

        newFormField( "E-Mail" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "email", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.email();
                                    }
                                } ) )
                .setField( new StringFormField() ).setLayoutData( right().top( line3 ).create() )
                .create();

        Composite line5 = newFormField( "URL" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "url", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.url();
                                    }
                                } ) ).setField( new StringFormField() )
                .setLayoutData( left().right( RIGHT ).top( line4 ).create() ).create();
        
        Composite line6 = newFormField( "Angebot" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<VermarkterComposite>( selectedComposite,
                                "angebot", new PropertyCallback<VermarkterComposite>() {

                                    public Property get( VermarkterComposite entity ) {
                                        return entity.angebot();
                                    }
                                } ) ).setField( new StringFormField() )
                .setLayoutData( left().right( RIGHT ).top( line5 ).height( 80 ).create() ).create();
        return line6;
    }


    protected EntityType<VermarkterComposite> addViewerColumns( FeatureTableViewer viewer ) {
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
        return type;
    }


    public Iterable<VermarkterComposite> getElements() {
        return VermarkterComposite.Mixin.forEntity( weg );
    }
    
    @Override
    protected VermarkterComposite createNewComposite() {
        VermarkterComposite composite = TwvRepository.instance().newEntity( VermarkterComposite.class, null );
        composite.weg().set( weg );
        return composite;
    }
}