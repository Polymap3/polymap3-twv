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

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.Named;
import org.polymap.twv.model.constants.Kategorie;
import org.polymap.twv.model.constants.Prioritaet;
import org.polymap.twv.model.constants.Unterkategorie;
import org.polymap.twv.model.data.AusweisungComposite;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WidmungComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegFormEditorPage
        extends TwvDefaultFormEditorPage {

    private IFormFieldListener vollpreisRefresher;

    private final WegComposite weg;


    public WegFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegFormEditorPage.class.getName(), "Vertragsdaten", feature, featureStore );

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( "Weg" + ((weg.name().get() != null) ? " - " + weg.name().get() : "") );

        Composite parent = site.getPageBody();
        parent.setLayout( new FormLayout() );

        // readonly
        Composite line1 = newFormField( "Name" ).setProperty( new PropertyAdapter( weg.name() ) )
                .setField( new StringFormField() ).setLayoutData( left().right( 100 ).create() )
                .create();

        Composite line2 = newFormField( "Kategorie" )
                .setProperty( new PropertyAdapter( weg.kategorie() ) )
                .setField( new PicklistFormField( Kategorie.all ) )
                .setLayoutData( left().top( line1 ).create() ).create();

        // TODO beim Wechsel von Kategorie auch die Unterkategorie-Auswahlwerte
        // anpassen
        // und den Wert der Unterkategorie() auf die kleinste ID der Auswahl setzen
        newFormField( "Unterkategorie" ).setProperty( new PropertyAdapter( weg.unterkategorie() ) )
                .setField( new PicklistFormField( Unterkategorie.all ) )
                .setLayoutData( right().top( line1 ).create() ).create();

        Composite line3 = newFormField( "Ausweisung" )
                .setProperty(
                        new AssociationAdapter<AusweisungComposite>( "ausweisung", weg.ausweisung() ) )
                .setField( namedAssocationsPicklist( AusweisungComposite.class ) )
                .setLayoutData( left().top( line2 ).create() ).create();

        newFormField( "Priorität" ).setProperty( new PropertyAdapter( weg.prioritaet() ) )
                .setField( new PicklistFormField( Prioritaet.all ) )
                .setLayoutData( right().top( line2 ).create() ).create();

        // TODO Gemeinde GEOMETRIE?
        Composite line4 = newFormField( "Länge Landkreis" )
                .setProperty( new PropertyAdapter( weg.laengeImLandkreis() ) )
                .setField( new StringFormField() ).setLayoutData( left().top( line3 ).create() )
                .setToolTipText( "Länge im Landkreis Mittelsachsen" ).create();

        newFormField( "Gesamtlänge" )
                .setProperty( new PropertyAdapter( weg.laengeUeberregional() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( line3 ).create() )
                .setToolTipText( "überregionale Gesamtlänge" ).create();

        Composite line5 = newFormField( "Wegbeschreibung" )
                .setProperty( new PropertyAdapter( weg.beschreibung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( 100 ).height( 50 ).top( line4 ).create() ).create();

        Composite line6 = newFormField( "Wegbeschaffenheit" )
                .setProperty( new PropertyAdapter( weg.beschaffenheit() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( 100 ).height( 50 ).top( line5 ).create() ).create();
        // TODO Picklist mit Textvorlagen ergänzen  

        Composite line7 = newFormField( "Widmung" )
                .setProperty( new AssociationAdapter<WidmungComposite>( "widmung", weg.widmung() ) )
                .setField( namedAssocationsPicklist( WidmungComposite.class ) )
                .setLayoutData( left().top( line6 ).create() ).create();

        newFormField( "Markierung" )
                .setProperty( new AssociationAdapter<WidmungComposite>( "widmung", weg.widmung() ) )
                .setField( namedAssocationsPicklist( MarkierungComposite.class ) )
                .setLayoutData( right().top( line6 ).create() ).create();

    }


    private SimpleFormData right() {
        return new SimpleFormData( SPACING ).left( MIDDLE ).right( RIGHT );
    }


    private SimpleFormData left() {
        return new SimpleFormData( SPACING ).left( LEFT ).right( MIDDLE );
    }


    // IFormEditorPage2 *******************************

    private <T extends Named> IFormField namedAssocationsPicklist( Class<T> type ) {
        PicklistFormField picklist = new PicklistFormField( twvRepository.entitiesWithNames( type ) );
        return picklist;
    }
}