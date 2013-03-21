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
public class Foerderregion
        extends ConstantWithSynonyms<String> {

    public static final Type<Foerderregion, String> all = new Type<Foerderregion, String>();

    public static final Foerderregion               f1  = new Foerderregion( 0,
                                                                "LEADER-Region Vorerzgebirgsregion Augustusburger Land" );

    public static final Foerderregion               f2  = new Foerderregion( 1,
                                                                "LEADER-Region Klosterbezirk Altzella" );

    public static final Foerderregion               f3  = new Foerderregion( 2,
                                                                "LEADER-Region Land des Roten Porphyr" );

    public static final Foerderregion               f4  = new Foerderregion( 3,
                                                                "LEADER-Region Lommatzscher Pflege" );

    public static final Foerderregion               f5  = new Foerderregion( 4,
                                                                "ILE-Region SachsenKreuz+" );

    public static final Foerderregion               f6  = new Foerderregion( 5,
                                                                " ILE-Region Silbernes Erzgebirge" );


    // instance *******************************************

    private Foerderregion( int id, String label, String... synonyms ) {
        super( id, label, synonyms );
        all.add( this );
    }


    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }

}
