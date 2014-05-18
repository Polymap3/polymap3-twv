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

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.data.VermarkterComposite;
import org.polymap.twv.ui.TwvDefaultFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VermarkterFormEditorPage
        extends TwvDefaultFormEditorPage {

    private VermarkterComposite vermarkter;


    public VermarkterFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( VermarkterFormEditorPage.class.getName(), "Basisdaten", feature, featureStore );
        this.vermarkter = twvRepository.findEntity( VermarkterComposite.class, feature
                .getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( formattedTitle( "Vermarkter", vermarkter.name().get(), null ) );
        site.setFormTitle( formattedTitle( "Vermarkter", vermarkter.name().get(), getTitle() ) );

        Composite line1 = newFormField( "Name" )
                .setProperty( new PropertyAdapter( vermarkter.name() ) )
                .setField( new StringFormField() ).setLayoutData( left().create() ).create();

        newFormField( "Ansprechpartner" )
                .setProperty( new PropertyAdapter( vermarkter.ansprechpartner() ) )
                .setField( new StringFormField() ).setLayoutData( right().create() ).create();

        Composite line2 = newFormField( "Straße" )
                .setProperty( new PropertyAdapter( vermarkter.strasse() ) )
                .setField( new StringFormField() )
                .setLayoutData( left().right( 70 ).top( line1 ).create() ).create();

        newFormField( "Hausnummer" ).setProperty( new PropertyAdapter( vermarkter.hausnummer() ) )
                .setField( new StringFormField() )
                .setLayoutData( right().left( 70 ).top( line1 ).create() ).create();

        Composite line3 = newFormField( "PLZ" )
                .setProperty( new PropertyAdapter( vermarkter.plz() ) )
                .setField( new StringFormField() )
                .setLayoutData( left().right( 30 ).top( line2 ).create() ).create();

        newFormField( "Ort" ).setProperty( new PropertyAdapter( vermarkter.ort() ) )
                .setField( new StringFormField() )
                .setLayoutData( right().left( 30 ).top( line2 ).create() ).create();

        Composite line4 = newFormField( "Telefon" )
                .setProperty( new PropertyAdapter( vermarkter.telefon() ) )
                .setField( new StringFormField() ).setLayoutData( left().top( line3 ).create() )
                .create();

        newFormField( "E-Mail" ).setProperty( new PropertyAdapter( vermarkter.email() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( line3 ).create() )
                .create();

        Composite line5 = newFormField( "URL" )
                .setProperty( new PropertyAdapter( vermarkter.url() ) )
                .setField( new StringFormField() )
                .setLayoutData( left().right( RIGHT ).top( line4 ).create() ).create();

        Composite line6 = newFormField( "Angebot" )
                .setProperty( new PropertyAdapter( vermarkter.angebot() ) )
                .setField( new StringFormField() )
                .setLayoutData( left().right( RIGHT ).top( line5 ).height( 80 ).create() ).create();
    }
}