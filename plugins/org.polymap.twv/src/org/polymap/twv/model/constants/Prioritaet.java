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
public class Prioritaet
        extends ConstantWithSynonyms<String> {

    public static final Type<Prioritaet, String> all = new Type<Prioritaet, String>();

    public static final Prioritaet               p1  = new Prioritaet( 0,
                                                             " 1 - landesweite Bedeutung" );

    public static final Prioritaet               p2  = new Prioritaet( 1,
                                                             "2 - überregionale Bedeutung" );

    public static final Prioritaet               p3  = new Prioritaet( 2, "3 - regionale Bedeutung" );

    public static final Prioritaet               p4  = new Prioritaet( 3, "4 - örtliche Bedeutung" );

    public static final Prioritaet               p5  = new Prioritaet( 4, "5 - geringe Bedeutung" );


    // instance *******************************************

    private Prioritaet( int id, String label, String... synonyms ) {
        super( id, label, synonyms );
        all.add( this );
    }


    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }

}
