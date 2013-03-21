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
public class Entfernungskontrolle
        extends ConstantWithSynonyms<String> {

    public static final Type<Entfernungskontrolle, String> all = new Type<Entfernungskontrolle, String>();

    public static final Entfernungskontrolle               f1  = new Entfernungskontrolle( 0,
                                                                       "erfolgt - Angaben richtig" );

    public static final Entfernungskontrolle               f2  = new Entfernungskontrolle( 1,
                                                                       "erfolgt - Angaben falsch" );

    public static final Entfernungskontrolle               f3  = new Entfernungskontrolle( 2,
                                                                       "nicht erfolgt" );


    // instance *******************************************

    private Entfernungskontrolle( int id, String label, String... synonyms ) {
        super( id, label, synonyms );
        all.add( this );
    }


    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }

}
