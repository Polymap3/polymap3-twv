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
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.field.UploadFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegobjektComposite;
import org.polymap.twv.model.data.WegobjektNameComposite;
import org.polymap.twv.ui.rhei.ImageValuePropertyAdapter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegobjektFormEditorPage
        extends TwvDefaultFormEditorPage {

    public WegobjektFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegobjektFormEditorPage.class.getName(), "Wegobjekt", feature, featureStore );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        WegobjektComposite wegobjekt = twvRepository.findEntity( WegobjektComposite.class, feature
                .getIdentifier().getID() );
        site.setEditorTitle( "Wegobjekt" );

        Composite parent = site.getPageBody();
        parent.setLayout( new FormLayout() );

        Composite line1 = newFormField( "Wegobjektname" )
                .setParent( parent )
                .setProperty(
                        new AssociationAdapter<WegobjektNameComposite>( "wegobjektName", wegobjekt
                                .wegobjektName() ) )
                .setField( namedAssocationsPicklist( WegobjektNameComposite.class, true ) )
                .setLayoutData( left().create() ).create();

        Composite line2 = newFormField( "Beschreibung" ).setParent( parent )
                .setProperty( new PropertyAdapter( wegobjekt.beschreibung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().top( line1 ).height( 80 ).right( RIGHT ).create() )
                .setToolTipText( "Beschreibung des Wegobjektes" ).create();

        Composite line3 = newFormField( "Weg" ).setParent( parent )
                .setProperty( new AssociationAdapter<WegComposite>( "weg", wegobjekt.weg() ) )
                .setValidator( new NotNullValidator() )
                .setField( namedAssocationsPicklist( WegComposite.class ) )
                .setLayoutData( left().top( line2 ).create() ).create();

        Composite line4 = newFormField( "Bild" ).setParent( parent )
                .setProperty( new ImageValuePropertyAdapter( "bild", wegobjekt.bild() ) )
                .setField( new UploadFormField( TwvPlugin.getImagesRoot() ) )
                .setLayoutData( left().top( line3 ).create() ).create();

    }
}