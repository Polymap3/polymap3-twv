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
package org.polymap.twv.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.JSONObject;
import org.json.JSONTokener;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

import org.polymap.core.runtime.Polymap;

import org.polymap.twv.model.data.SchildComposite;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Mixins(WegobjektNummerGeneratorService.Mixin.class)
public interface WegobjektNummerGeneratorService
        extends ServiceComposite {

    /**
     * Generate the next {@link SchildComposite#nummer()}.
     */
    public Integer generate();


    public abstract class Mixin
            implements WegobjektNummerGeneratorService {

        private static final Log log = LogFactory.getLog( WegobjektNummerGeneratorService.class );

        private int              count;

        private File             file;


        public Mixin() {

            InputStreamReader in = null;
            try {
                file = new File( Polymap.getWorkspacePath().toFile(), "WegobjektnummerGenerator.json" );
                if (file.exists()) {
                    in = new InputStreamReader( new BufferedInputStream( new FileInputStream( file ) ), "UTF-8" );
                    JSONObject json = new JSONObject( new JSONTokener( in ) );

                    count = json.getInt( "count" );
                }
                else {
                    count = 1;
                }
            }
            catch (Exception e) {
                throw new RuntimeException( "Fehler beim Initialisieren.", e );
            }
            finally {
                IOUtils.closeQuietly( in );
            }
        }


        public synchronized Integer generate() {
            count = count + 1;
            storeCount();
            return count;
        }


        protected void storeCount() {
            OutputStreamWriter out = null;
            try {
                JSONObject json = new JSONObject();
                json.put( "count", count );

                out = new OutputStreamWriter( new BufferedOutputStream( new FileOutputStream( file, false ) ), "UTF-8" );
                out.write( json.toString( 4 ) );
            }
            catch (Exception e) {
                throw new RuntimeException( "Fehler beim Anlegen einer neuen Wegobjektnummer.", e );
            }
            finally {
                IOUtils.closeQuietly( out );
            }
        }
    }

}
