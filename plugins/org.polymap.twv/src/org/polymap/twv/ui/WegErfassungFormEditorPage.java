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

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.ManyAssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.data.EntfernungskontrolleComposite;
import org.polymap.twv.model.data.FoerderregionComposite;
import org.polymap.twv.model.data.WegComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegErfassungFormEditorPage
        extends TwvDefaultFormEditorPage {

    private final WegComposite weg;


    public WegErfassungFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegErfassungFormEditorPage.class.getName(), "Erfassung", feature, featureStore );

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite parent = site.getPageBody();
        parent.setLayout( new FormLayout() );

        Composite line1 = newFormField( "Förderregionen" )
                .setProperty(
                        new ManyAssociationAdapter<FoerderregionComposite>( "foerderregion", weg
                                .foerderregionen() ) )
                .setField( namedAssocationsPicklist( FoerderregionComposite.class ) )
                .setLayoutData( left().height( 30 ).create() ).create();

        Composite line2 = newFormField( "Erfasser" )
                .setProperty( new PropertyAdapter( "erfasser", weg.erfasser() ) )
                .setField( new StringFormField() ).setLayoutData( left().top( line1 ).create() )
                .create();

        newFormField( "Wegewart" ).setProperty( new PropertyAdapter( "wegewart", weg.wegewart() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( line1 ).create() )
                .create();

        Composite line3 = newFormField( "Begehung am" )
                .setProperty( new PropertyAdapter( weg.begehungAm() ) )
                .setField( new DateTimeFormField() ).setLayoutData( left().top( line2 ).create() )
                .create();

        newFormField( "Entfernungskontrolle" )
                .setProperty(
                        new AssociationAdapter<EntfernungskontrolleComposite>( "widmung", weg
                                .entfernungskontrolle() ) )
                .setField( namedAssocationsPicklist( EntfernungskontrolleComposite.class ) )
                .setLayoutData( right().top( line2 ).create() ).create();

        Composite line4 = newFormField( "Bemerkung" )
                .setProperty( new PropertyAdapter( weg.bemerkung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( RIGHT ).height( 50 ).top( line3 ).create() ).create();

        Composite line5 = newFormField( "Mängel" )
                .setProperty( new PropertyAdapter( weg.maengel() ) ).setField( new TextFormField() )
                .setLayoutData( left().right( RIGHT ).height( 50 ).top( line4 ).create() ).create();
    }
}