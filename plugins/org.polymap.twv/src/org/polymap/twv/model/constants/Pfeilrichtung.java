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
package org.polymap.twv.model.constants;

import org.polymap.rhei.model.ConstantWithSynonyms;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class Pfeilrichtung
        extends ConstantWithSynonyms<String> {

    public static final Type<Pfeilrichtung, String> all        = new Type<Pfeilrichtung, String>();

    public static final Pfeilrichtung               links      = new Pfeilrichtung( 0, "links" );

    public static final Pfeilrichtung               rechts     = new Pfeilrichtung( 1, "rechts" );

    public static final Pfeilrichtung               beidseitig = new Pfeilrichtung( 2, "beidseitig" );


    // instance *******************************************

    private Pfeilrichtung( int id, String label, String... synonyms ) {
        super( id, label, synonyms );
        all.add( this );
    }


    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }

}
