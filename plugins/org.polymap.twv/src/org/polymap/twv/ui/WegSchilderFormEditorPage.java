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



import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.PfeilrichtungComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.AssociationCallback;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.CompositeProvider;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.PropertyCallback;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegSchilderFormEditorPage
        extends 
TwvDefaultFormEditorPageWithFeatureTable {



    private WegComposite                       weg;

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
    
        // TODO erstes Schild selektieren, falls vorhanden
        // falls nicht vorhanden leeren Prototyp reintun
        // TODO das sollte nur ein Prototyp sein, der nicht gespeichert wird
        // TODO, dann sollten aber auch alle Felder readonly sein und nach Auswahl
        // auf readwrite
        Composite schildForm = createSchildForm( parent );
    
        // TODO FeatureTabelle mit allen Schilder, per Klick auf ein Schild
        // aktualisieren der schildform
        // selectionListener an table the schildForm neu läd
        createTableForm( parent, schildForm );
    }

    // kopiert von SchildFormEditorPage
    // TODO benutzt irgendwelche Variablen der anderen Page, weshalb
    // SelectionListener nicht funktionieren
    public Composite createSchildForm( Composite parent ) {

        Composite line1 = newFormField( "Schildart" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, "schildart",
                                new AssociationCallback<SchildComposite>() {

                                    public Association get( SchildComposite entity ) {
                                        return entity.schildart();
                                    }
                                } ) )
                .setField( namedAssocationsPicklist( SchildartComposite.class, true ) )
                .setLayoutData( left().create() ).create();

        newFormField( "Nummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, "laufendeNr",
                                new PropertyCallback<SchildComposite>() {

                                    public Property get( SchildComposite entity ) {
                                        return entity.laufendeNr();
                                    }
                                } ) ).setValidator( new NotNullValidator() )
                .setField( new StringFormField() ).setLayoutData( right().create() )
                .setToolTipText( "laufende Schild Nummer" ).create();

        Composite line2 = newFormField( "Pfeilrichtung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, "pfeilrichtung",
                                new AssociationCallback<SchildComposite>() {

                                    public Association get( SchildComposite entity ) {
                                        return entity.pfeilrichtung();
                                    }
                                } ) )
                .setField( namedAssocationsPicklist( PfeilrichtungComposite.class ) )
                .setLayoutData( left().top( line1 ).create() ).create();

        newFormField( "Material" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, "material",
                                new AssociationCallback<SchildComposite>() {

                                    public Association get( SchildComposite entity ) {
                                        return entity.material();
                                    }
                                } ) )
                .setField( namedAssocationsPicklist( SchildmaterialComposite.class ) )
                .setLayoutData( right().top( line1 ).create() ).create();

        Composite line3 = newFormField( "Beschriftung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, "beschriftung",
                                new PropertyCallback<SchildComposite>() {

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
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, "befestigung",
                                new PropertyCallback<SchildComposite>() {

                                    public Property get( SchildComposite entity ) {
                                        return entity.befestigung();
                                    }
                                } ) ).setField( new TextFormField() )
                .setLayoutData( left().top( line3 ).height( 50 ).create() ).create();

        // TODO Schildbild fehlt noch

        return line4;
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