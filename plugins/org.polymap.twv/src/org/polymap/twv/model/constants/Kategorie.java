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

import java.util.ArrayList;
import java.util.List;

import org.polymap.rhei.model.ConstantWithSynonyms;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */

public class Kategorie
        extends ConstantWithSynonyms<String> {

    public static final Type<Kategorie, String> all       = new Type<Kategorie, String>();

    public static final Kategorie               wanderweg = new Kategorie( 0, "Wanderweg" );

    public static final Kategorie               radweg    = new Kategorie( 1, "Radweg" );

    public static final Kategorie               reitweg   = new Kategorie( 2, "Reitweg" );

    static {
        // add subcategories
        wanderweg.add( Unterkategorie.w1 );
        wanderweg.add( Unterkategorie.w2 );
        wanderweg.add( Unterkategorie.w3 );
        wanderweg.add( Unterkategorie.w4 );
        wanderweg.add( Unterkategorie.w5 );
        wanderweg.add( Unterkategorie.w6 );

        radweg.add( Unterkategorie.r1 );
        radweg.add( Unterkategorie.r2 );
        radweg.add( Unterkategorie.r3 );
        radweg.add( Unterkategorie.r4 );
        radweg.add( Unterkategorie.r5 );

        reitweg.add( Unterkategorie.p1 );
        reitweg.add( Unterkategorie.p2 );
        reitweg.add( Unterkategorie.p3 );
        reitweg.add( Unterkategorie.p4 );
    }

    private List<Unterkategorie>                unterkategories;


    // instance *******************************************

    private Kategorie( int id, String label, String... synonyms ) {
        super( id, label, synonyms );
        all.add( this );
        unterkategories = new ArrayList<Unterkategorie>();
    }


    private void add( Unterkategorie unterkategorie ) {
        this.unterkategories.add( unterkategorie );
    }
    
    public List<Unterkategorie> getUnterkategories() {
        return unterkategories;
    }


    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }

}
