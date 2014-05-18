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

import org.polymap.twv.model.data.AusweisungComposite;
import org.polymap.twv.model.data.EntfernungskontrolleComposite;
import org.polymap.twv.model.data.FoerderregionComposite;
import org.polymap.twv.model.data.ProfilComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.WegbeschaffenheitComposite;
import org.polymap.twv.model.data.WegobjektNameComposite;
import org.polymap.twv.model.data.WidmungComposite;
import org.polymap.twv.ui.form.MarkierungFormEditorPage;
import org.polymap.twv.ui.form.NamedFormEditorPage;
import org.polymap.twv.ui.form.SchildFormEditorPage;
import org.polymap.twv.ui.form.VermarkterFormEditorPage;
import org.polymap.twv.ui.form.WegErfassungFormEditorPage;
import org.polymap.twv.ui.form.WegFormEditorPage;
import org.polymap.twv.ui.form.WegSchilderFormEditorPage;
import org.polymap.twv.ui.form.WegVermarkter2FormEditorPage;
import org.polymap.twv.ui.form.WegWegobjektFormEditorPage;
import org.polymap.twv.ui.form.WegobjektFormEditorPage;

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
            result.add( new WegErfassungFormEditorPage( feature, formEditor.getFeatureStore() ) );
//            result.add( new WegVermarkterFormEditorPage( feature, formEditor.getFeatureStore() ) );
          result.add( new WegVermarkter2FormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new WegWegobjektFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new WegSchilderFormEditorPage( feature, formEditor.getFeatureStore() ) );
//            result.add( new WegReportPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "schild" )) {
            result.add( new SchildFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "vermarkter" )) {
            result.add( new VermarkterFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "wegobjekt" )) {
            result.add( new WegobjektFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "ausweisung" )) {
            result.add( new NamedFormEditorPage<AusweisungComposite>( AusweisungComposite.class,
                    "Ausweisung", feature, formEditor.getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart()
                .equalsIgnoreCase( "entfernungskontrolle" )) {
            result.add( new NamedFormEditorPage<EntfernungskontrolleComposite>(
                    EntfernungskontrolleComposite.class, "Entfernungskontrolle", feature,
                    formEditor.getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "foerderregion" )) {
            result.add( new NamedFormEditorPage<FoerderregionComposite>(
                    FoerderregionComposite.class, "Förderregion", feature, formEditor
                            .getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "wegbeschaffenheit" )) {
            result.add( new NamedFormEditorPage<WegbeschaffenheitComposite>(
                    WegbeschaffenheitComposite.class, "Wegbeschaffenheit", feature, formEditor
                            .getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "widmung" )) {
            result.add( new NamedFormEditorPage<WidmungComposite>( WidmungComposite.class,
                    "Widmung", feature, formEditor.getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "wegobjektname" )) {
            result.add( new NamedFormEditorPage<WegobjektNameComposite>(
                    WegobjektNameComposite.class, "Wegobjektname", feature, formEditor
                            .getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "schildart" )) {
            result.add( new NamedFormEditorPage<SchildartComposite>( SchildartComposite.class,
                    "Schildart", feature, formEditor.getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "profil" )) {
            result.add( new NamedFormEditorPage<ProfilComposite>( ProfilComposite.class,
                    "Profil", feature, formEditor.getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "schildmaterial" )) {
            result.add( new NamedFormEditorPage<SchildmaterialComposite>(
                    SchildmaterialComposite.class, "Schildmaterial", feature, formEditor
                            .getFeatureStore() ) );
        }
        else if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "markierung" )) {
            result.add( new MarkierungFormEditorPage( "Markierung", feature, formEditor
                    .getFeatureStore() ) );
        }
        return result;
    }

}
