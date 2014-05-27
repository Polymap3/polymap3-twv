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

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;

import org.polymap.twv.model.data.WegobjektComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegobjektEntityProvider
        extends TwvEntityProvider<WegobjektComposite> {

    public WegobjektEntityProvider( QiModule repo, Name entityName ) {
        super( repo, WegobjektComposite.class, entityName );
    }


    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        FeatureType type = super.buildFeatureType( schema );

        // aussortieren für die Tabelle
        SimpleFeatureType filtered = SimpleFeatureTypeBuilder.retype( (SimpleFeatureType)type, new String[] { "geom",
                "laufendeNr", "wegobjektName", "beschreibung", "bedarf" } );
        return filtered;
    }


    @Override
    public WegobjektComposite newEntity( EntityCreator<WegobjektComposite> creator )
            throws Exception {
        WegobjektComposite composite = super.newEntity( creator );
        composite.laufendeNr().set( TwvRepository.instance().nextWegobjektNummer() );
        return composite;
    }
}
