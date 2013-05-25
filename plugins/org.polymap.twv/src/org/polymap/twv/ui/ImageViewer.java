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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.widgets.ExternalBrowser;

import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.field.UploadFormField.UploadedImage;

import org.polymap.twv.TwvPlugin;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ImageViewer {

    private static Log    log = LogFactory.getLog( ImageViewer.class );

    private final Label   imageView;

    private UploadedImage imageData;


    public ImageViewer( Composite parent, FormData layout ) {

        imageView = new Label( parent, SWT.NONE );
        // imageView.setEnabled( true );
        imageView.setBackground( parent.getBackground() );
        imageView.setForeground( parent.getBackground() );
        imageView.setLayoutData( layout );
        imageView.setSize( 250, 250 );

        // imageView.pack();
        imageView.addMouseListener( new MouseListener() {

            @Override
            public void mouseUp( MouseEvent e ) {
                show();
            }


            @Override
            public void mouseDown( MouseEvent e ) {
            }


            @Override
            public void mouseDoubleClick( MouseEvent e ) {
                show();
            }


            private void show() {
                if (imageData != null) {
                    String url = DownloadServiceHandler.registerContent( new ContentProvider() {

                        @Override
                        public String getFilename() {
                            return imageData.originalFileName();
                        }


                        @Override
                        public String getContentType() {
                            return imageData.contentType();
                        }


                        @Override
                        public InputStream getInputStream()
                                throws Exception {
                            return new FileInputStream( new File( TwvPlugin.getImagesRoot(), imageData
                                    .internalFileName() ) );
                        }


                        @Override
                        public boolean done( boolean success ) {
                            return true;
                        }
                    } );
                    // url = "polymap?meins.jpg&" + url.substring(
                    // "polymap?".length() );
                    ExternalBrowser.open( "download_window", url, ExternalBrowser.NAVIGATION_BAR
                            | ExternalBrowser.STATUS );

                    // Browser browser = new Browser( shell, SWT.NONE );
                    // // create the image
                    // BufferedImage image = createImage();
                    // // store the image in the SessionStore for the service handler
                    // RWT.getSessionStore().setAttribute( IMAGE_KEY, image );
                    // create the HTML with a single <img> tag.
                    // browser.setText( "<img src=\"" + url + "\"/>" );
                }
            }

        } );
    }


    public void setImage( UploadedImage imageData ) {
        this.imageData = imageData;
        refresh();
    }


    private void refresh() {
        if (imageData == null) {
            imageView.setEnabled( false );
            imageView.setCursor( Display.getCurrent().getSystemCursor( SWT.CURSOR_ARROW ) );

            imageView.setImage( null );
        }
        else {
            if (imageData.thumbnailFileName() != null) {
                imageView.setEnabled( true );
                imageView.setCursor( Display.getCurrent().getSystemCursor( SWT.CURSOR_HAND ) );

                Image image = null;
                try {
                    image = Graphics
                            .getImage(
                                    imageData.thumbnailFileName(),
                                    new FileInputStream( new File( TwvPlugin.getImagesRoot(), imageData
                                            .thumbnailFileName() ) ) );
                }
                catch (FileNotFoundException e) {
                    PolymapWorkbench.handleError( TwvPlugin.PLUGIN_ID, ImageViewer.this,
                            "Fehler beim Anzeige des Bildes.", e );
                }
                imageView.setImage( image );
            }
        }
    }


    public Control getControl() {
        return imageView;
    }
}
