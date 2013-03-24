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

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.Feature;

import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormPageProvider;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FormPageProvider
        implements IFormPageProvider {

    public FormPageProvider() {
    }


    @Override
    public List<IFormEditorPage> addPages( FormEditor formEditor, Feature feature ) {
        List<IFormEditorPage> result = new ArrayList<IFormEditorPage>();
        if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "weg" )) {
            result.add( new WegFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new WegSchilderFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "schild" )) {
            result.add( new SchildFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }

        return result;
    }

}
