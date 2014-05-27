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

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.UploadFormField;
import org.polymap.rhei.field.UploadFormField.UploadedImage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.ui.ImageViewer;
import org.polymap.twv.ui.TwvDefaultFormEditorPage;
import org.polymap.twv.ui.rhei.ImageValuePropertyAdapter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MarkierungFormEditorPage
        extends TwvDefaultFormEditorPage {

    private final MarkierungComposite markierung;

    private final String              editorTitle;

    private IFormFieldListener        uploadListener;


    public MarkierungFormEditorPage( String editorTitle, Feature feature, FeatureStore featureStore ) {
        super( MarkierungFormEditorPage.class.getName(), "Basisdaten", feature, featureStore );

        markierung = twvRepository.findEntity( MarkierungComposite.class, feature.getIdentifier().getID() );
        this.editorTitle = editorTitle;
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( editorTitle );

        Composite parent = site.getPageBody();
        parent.setLayout( new FormLayout() );

        Composite line1 = newFormField( "Name" ).setProperty( new PropertyAdapter( markierung.name() ) )
                .setValidator( new NotNullValidator() ).setField( new StringFormField() )
                .setLayoutData( left().right( 100 ).create() ).create();

        newFormField( "Bild" ).setProperty( new ImageValuePropertyAdapter( "bild", markierung.bild() ) )
                .setField( new UploadFormField( TwvPlugin.getImagesRoot(), false ) )
                .setLayoutData( left().top( line1 ).create() ).create();

        final ImageViewer viewer = new ImageViewer( site.getPageBody(), right().top( line1 ).height( 250 ).width( 250 )
                .create(), createBildname() );

        if (markierung.bild().get().thumbnailFileName().get() != null) {
            viewer.setImage( ImageValuePropertyAdapter.convertToUploadedImage( markierung.bild().get() ) );
        }

        site.addFieldListener( uploadListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getNewValue() != null && "bild".equals( ev.getFieldName() )) {
                    // repaint image preview
                    UploadedImage uploadedImage = (UploadedImage)ev.getNewValue();

                    viewer.setImage( uploadedImage );
                }
            }
        } );
    }


    private String createBildname() {
        String name = markierung.name().get();
        if (name == null || name.isEmpty()) {
            name = markierung.id();
        }
        name = name.replaceAll( "\\W", "_" );
        return name;
    }
}