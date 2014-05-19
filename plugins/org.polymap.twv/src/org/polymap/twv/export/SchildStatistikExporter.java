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
package org.polymap.twv.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.polymap.twv.model.data.FoerderregionComposite;
import org.polymap.twv.model.data.PfeilrichtungComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.WegComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class SchildStatistikExporter
        extends AbstractExcelExporter<SchildComposite> {

    public SchildStatistikExporter() {
        super( SchildComposite.class, SchildComposite.NAME, "statistik_schild", "Schilder" );
    }


    protected List<Value> createValues( SchildComposite schild, List<String> errors ) {

        List<Value> result = new ArrayList<Value>();
        result.add( new Value( "Nummer", schild.laufendeNr().get() ) );
        result.add( new Value( "Bestandsnummer", schild.bestandsNr().get() ) );
        SchildartComposite art = schild.schildart().get();
//        if (art == null) {
//            errors.add( "Keine Schildart gefunden!" );
//        }
        result.add( new Value( "Schildart", art != null ? art.name().get() : "" ) );
        PfeilrichtungComposite pfeil = schild.pfeilrichtung().get();
//        if (pfeil == null) {
//            errors.add( "Keine Pfeilrichtung gefunden!" );
//        }
        result.add( new Value( "Pfeilrichtung", pfeil != null ? pfeil.name().get() : "" ) );
        SchildmaterialComposite material = schild.material().get();
//        if (material == null) {
//            errors.add( "Kein Schildmaterial gefunden!" );
//        }
        result.add( new Value( "Material", material != null ? material.name().get() : "" ) );
        result.add( new Value( "Beschriftung", schild.beschriftung().get() ) );
        result.add( new Value( "Befestigung", schild.befestigung().get() ) );
        result.add( new Value( "Standort", schild.standort().get() ) );

        List<String> wege = new ArrayList<String>();
        List<String> foerderregionen = new ArrayList<String>();
        for (WegComposite weg : schild.wege().toSet()) {
            if (!wege.contains( weg.name().get() )) {
                wege.add( weg.name().get() );
                for (FoerderregionComposite fc : weg.foerderregionen().toSet()) {
                    foerderregionen.add( fc.name().get() );
                }
            }
        }

        Collections.sort( wege );
        result.add( new Value( "Weg", (wege.size() > 0 ? wege.get( 0 ) : "") ) );
        result.add( new Value( "Weg_1", (wege.size() > 1 ? wege.get( 1 ) : "") ) );
        result.add( new Value( "Weg_2", (wege.size() > 2 ? wege.get( 2 ) : "") ) );

        Collections.sort( foerderregionen );
        result.add( new Value( "Foerderregion", (foerderregionen.size() > 0 ? foerderregionen.get( 0 ) : "") ) );
        result.add( new Value( "Foerderregion_1", (foerderregionen.size() > 1 ? foerderregionen.get( 1 ) : "") ) );
        result.add( new Value( "Foerderregion_2", (foerderregionen.size() > 2 ? foerderregionen.get( 2 ) : "") ) );

//        // Kommune
//        final StringBuilder buf = new StringBuilder( 256 );
//        try {
//            IMap map = ((PipelineFeatureSource)fs).getLayer().getMap();
//            ILayer layer = Iterables
//                    .getOnlyElement( Iterables.filter( map.getLayers(), Layers.hasLabel( "Gemeinden" ) ) );
//
//            fs = PipelineFeatureSource.forLayer( layer, false );
//            FeatureCollection gemeinden = fs.getFeatures( DataPlugin.ff.intersects(
//                    DataPlugin.ff.property( fs.getSchema().getGeometryDescriptor().getLocalName() ),
//                    DataPlugin.ff.literal( schild.geom().get() ) ) );
//            gemeinden.accepts( new FeatureVisitor() {
//
//                public void visit( Feature gemeinde ) {
//                    buf.append( buf.length() > 0 ? ", " : "" );
//                    Property nameProp = gemeinde.getProperty( "ORTSNAME" );
//                    buf.append( nameProp != null ? nameProp.getValue().toString() : "-" );
//                }
//            }, null );
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            buf.append( "-konnten nicht ermittelt werden- (" + e.getLocalizedMessage() + ")" );
//        }

//        result.add( new Value( "letzte Änderung", new Date( schild.lastModified() ) ) );
        return result;
    }
}
