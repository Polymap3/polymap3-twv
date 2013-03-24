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

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.Named;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NamedFormEditorPage<T extends Named>
        extends TwvDefaultFormEditorPage {

    private final T      named;

    private final String editorTitle;


    public NamedFormEditorPage( Class<T> type, String editorTitle, Feature feature,
            FeatureStore featureStore ) {
        super( NamedFormEditorPage.class.getName(), "Basisdaten", feature, featureStore );

        named = twvRepository.findEntity( type, feature.getIdentifier().getID() );
        this.editorTitle = editorTitle;
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( editorTitle );

        Composite parent = site.getPageBody();
        parent.setLayout( new FormLayout() );

        newFormField( "Name" ).setProperty( new PropertyAdapter( named.name() ) )
                .setValidator( new NotNullValidator() ).setField( new StringFormField() )
                .setLayoutData( left().right( 100 ).create() ).create();
    }
}