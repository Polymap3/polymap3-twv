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

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;

import org.eclipse.jface.action.Action;

import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.ui.TwvDefaultFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegReportPage
        extends TwvDefaultFormEditorPage {

    private WegComposite weg;

    private Browser      browser;


    public WegReportPage( Feature feature, FeatureStore featureStore ) {
        super( WegReportPage.class.getName(), "Druckvorschau", feature, featureStore );

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        updateBrowser();
    }


    protected void updateBrowser() {
        // check if page has content created already
        if (pageSite == null) {
            return;
        }

        // unique URL for IE reload
        String url = "../rest/forms/weg.html?id=" + weg.id() + "&" + System.currentTimeMillis();
        if (browser == null) {
            browser = new Browser( pageSite.getPageBody(), SWT.NONE );
            browser.setUrl( url );

            browser.setLayoutData( new SimpleFormData().fill().left( 0 ).right( 100 ).create() );
            pageSite.getPageBody().layout( true );
        }

        // unique URL for IE reload
        browser.execute( "var current=window.location.href; window.location.href=current + '&"
                + System.currentTimeMillis() + "'; window.location.reload(true);" );
    }


    public Action[] getEditorActions() {
        return new Action[] { new PrintAction( weg ) };
    }
}
