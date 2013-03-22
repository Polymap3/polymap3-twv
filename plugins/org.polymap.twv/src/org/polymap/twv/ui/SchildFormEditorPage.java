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
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.data.AusweisungComposite;
import org.polymap.twv.model.data.KategorieComposite;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.model.data.PfeilrichtungComposite;
import org.polymap.twv.model.data.PrioritaetComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.UnterkategorieComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WidmungComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class SchildFormEditorPage
        extends TwvDefaultFormEditorPage {

    private final SchildComposite schild;


    public SchildFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( SchildFormEditorPage.class.getName(), "Schild", feature, featureStore );

        schild = twvRepository.findEntity( SchildComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( "Schild"
                + ((schild.laufendeNr().get() != null) ? " - " + schild.laufendeNr().get() : "") );

        Composite parent = site.getPageBody();
        parent.setLayout( new FormLayout() );

        Composite line1 = newFormField( "Schildart" )
                .setProperty(
                        new AssociationAdapter<SchildartComposite>( "schildart", schild.schildart() ) )
                .setField( namedAssocationsPicklist( SchildartComposite.class, true ) )
                .setLayoutData( left().create() ).create();

        newFormField( "Nummer" ).setProperty( new PropertyAdapter( schild.laufendeNr() ) )
                .setField( new StringFormField() ).setLayoutData( right().create() )
                .setToolTipText( "laufende Schild Nummer" ).create();

        Composite line2 = newFormField( "Pfeilrichtung" )
                .setProperty(
                        new AssociationAdapter<PfeilrichtungComposite>( "pfeilrichtung", schild
                                .pfeilrichtung() ) )
                .setField( namedAssocationsPicklist( PfeilrichtungComposite.class ) )
                .setLayoutData( left().top( line1 ).create() ).create();

        newFormField( "Material" )
                .setProperty(
                        new AssociationAdapter<SchildmaterialComposite>( "material", schild
                                .material() ) )
                .setField( namedAssocationsPicklist( SchildmaterialComposite.class ) )
                .setLayoutData( right().top( line2 ).create() ).create();

        Composite line3 = newFormField( "Beschriftung" )
                .setProperty( new PropertyAdapter( schild.beschriftung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().top( line2 ).height( 50 ).right( RIGHT ).create() )
                .setToolTipText( "Schildbeschriftung mit Entfernungsangabe und Zusatzinfo" )
                .create();

        Composite line4 = newFormField( "Befestigung" )
                .setProperty( new PropertyAdapter( schild.beschriftung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().top( line3 ).height( 50 ).create() ).create();

        // TODO Schildbild fehlt noch

        // TODO und Auswahlbox f�r Weg fehlt noch
        Composite line5 = newFormField( "Weg" )
                .setProperty( new AssociationAdapter<WegComposite>( "weg", schild.weg() ) )
                .setField( namedAssocationsPicklist( WegComposite.class ) )
                .setLayoutData( left().top( line4 ).create() ).create();

    }
}