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
public class Unterkategorie
        extends ConstantWithSynonyms<String> {

    public static final Type<Unterkategorie, String> all = new Type<Unterkategorie, String>();

    public static final Unterkategorie               w1  = new Unterkategorie( 0,
                                                                 "Qualitätsweg Wanderbares Deutschland" );

    public static final Unterkategorie               w2  = new Unterkategorie( 1,
                                                                 "Europäischer Fernwanderweg" );

    public static final Unterkategorie               w3  = new Unterkategorie( 2,
                                                                 "Nationaler Fernwanderweg" );

    public static final Unterkategorie               w4  = new Unterkategorie( 3,
                                                                 "überregionale und regionale Gebietswanderwege" );

    public static final Unterkategorie               w5  = new Unterkategorie( 4,
                                                                 "Orts-, Verbindungs- u. Rundwanderweg" );

    public static final Unterkategorie               w6  = new Unterkategorie( 5, "Lehrpfad" );

    public static final Unterkategorie               r1  = new Unterkategorie( 6,
                                                                 "sonstiger Wanderweg" );

    public static final Unterkategorie               r2  = new Unterkategorie( 7,
                                                                 "Radfernweg (SachsenNetz Rad)" );

    public static final Unterkategorie               r3  = new Unterkategorie( 8,
                                                                 "Regionale Hauptradroute (SachsenNetz Rad)" );

    public static final Unterkategorie               r4  = new Unterkategorie( 9,
                                                                 "sonstige Radroute (SachsenNetz Rad)" );

    public static final Unterkategorie               r5  = new Unterkategorie( 10,
                                                                 "sonstige Radroute" );

    public static final Unterkategorie               p1  = new Unterkategorie( 11,
                                                                 "Fernroute durch Sachsen" );

    public static final Unterkategorie               p2  = new Unterkategorie( 12,
                                                                 "Regionalaroute durch einen Landkreis" );

    public static final Unterkategorie               p3  = new Unterkategorie( 13, "Lokalroute" );

    public static final Unterkategorie               p4  = new Unterkategorie( 14,
                                                                 "Sonstiger Reitweg" );


    // instance *******************************************

    private Unterkategorie( int id, String label, String... synonyms ) {
        super( id, label, synonyms );
        all.add( this );
    }


    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }

}
