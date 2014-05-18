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

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.ManyAssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.data.EntfernungskontrolleComposite;
import org.polymap.twv.model.data.FoerderregionComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.ui.TwvDefaultFormEditorPage;

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
        site.setFormTitle( formattedTitle( "Tourismusweg", weg.name().get(), getTitle() ) );

        Composite line1 = newFormField( "Förderregionen" )
                .setProperty(
                        new ManyAssociationAdapter<FoerderregionComposite>( weg.foerderregionen() ) )
                .setField( namedAssocationsSelectlist( FoerderregionComposite.class, true ) )
                .setLayoutData( left().right( RIGHT ).height( 120 ).create() ).create();

        Composite line2 = newFormField( "Erfasser" ).setProperty( new PropertyAdapter( "erfasser", weg.erfasser() ) )
                .setField( new TextFormField() ).setLayoutData( left().top( line1 ).height( 70 ).create() ).create();

        newFormField( "Wegewarte" ).setProperty( new PropertyAdapter( "wegewart", weg.wegewart() ) )
                .setField( new TextFormField() ).setLayoutData( right().height( 70 ).top( line1 ).create() ).create();

        Composite line3 = newFormField( "Begehung am" ).setProperty( new PropertyAdapter( weg.begehungAm() ) )
                .setField( new DateTimeFormField() ).setLayoutData( left().top( line2 ).create() ).create();

        newFormField( "Kontrolle" ).setToolTipText( "Entfernungskontrolle" )
                .setProperty( new AssociationAdapter<EntfernungskontrolleComposite>( weg.entfernungskontrolle() ) )
                .setField( namedAssocationsPicklist( EntfernungskontrolleComposite.class ) )
                .setLayoutData( right().top( line2 ).create() ).create();

        Composite line4 = newFormField( "Bemerkung" ).setProperty( new PropertyAdapter( weg.bemerkung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( RIGHT ).height( 50 ).top( line3 ).create() ).create();

        Composite line5 = newFormField( "Mängel" ).setProperty( new PropertyAdapter( weg.maengel() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( RIGHT ).height( 50 ).top( line4 ).create() ).create();
    }
}
