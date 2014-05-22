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

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerVisitor;
import org.polymap.core.project.ProjectRepository;

import org.polymap.twv.model.data.FoerderregionComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegobjektComposite;
import org.polymap.twv.model.data.WegobjektNameComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegObjektStatistikExporter
        extends AbstractExcelExporter<WegobjektComposite> {

    private static Log log = LogFactory.getLog( WegObjektStatistikExporter.class );


    public WegObjektStatistikExporter() {
        super( WegobjektComposite.class, WegobjektComposite.NAME, "statistik_wegobjekte", "Wegobjekte" );
    }


    protected List<Value> createValues( WegobjektComposite wegobjekt, List<String> errors ) {

        List<Value> result = new ArrayList<Value>();
        result.add( new Value( "Nummer", wegobjekt.laufendeNr().get() ) );
        WegobjektNameComposite name = wegobjekt.wegobjektName().get();
        result.add( new Value( "Name", name != null ? name.name().get() : "" ) );
        result.add( new Value( "Beschreibung", wegobjekt.beschreibung().get() ) );

        List<String> wege = new ArrayList<String>();
        List<String> foerderregionen = new ArrayList<String>();
        for (WegComposite weg : wegobjekt.wege().toSet()) {
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

        // Kommune
        final StringBuilder buf = new StringBuilder( 256 );
        try {
            ILayer gemeindeLayer = ProjectRepository.instance().visit( new LayerVisitor() {

                public boolean visit( ILayer layer ) {
                    if (layer.getLabel().equalsIgnoreCase( "gemeinden" )) {
                        result = layer;
                    }
                    return result == null;
                }
            } );
            if (gemeindeLayer != null && wegobjekt.geom().get() != null) {
                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( gemeindeLayer, false );
                FeatureCollection gemeinden = fs.getFeatures( DataPlugin.ff.intersects(
                        DataPlugin.ff.property( fs.getSchema().getGeometryDescriptor().getLocalName() ),
                        DataPlugin.ff.literal( wegobjekt.geom().get() ) ) );
                gemeinden.accepts( new FeatureVisitor() {

                    public void visit( Feature gemeinde ) {
                        buf.append( buf.length() > 0 ? ", " : "" );
                        Property nameProp = gemeinde.getProperty( "ORTSNAME" );
                        buf.append( nameProp != null ? nameProp.getValue().toString() : "-" );
                    }
                }, null );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            buf.append( "konnte nicht ermittelt werden- (" + e.getLocalizedMessage() + ")" );
        }
        result.add( new Value( "Kommune", buf.toString() ) );

        // result.add( new Value( "letzte Änderung", new Date( schild.lastModified()
        // ) ) );
        return result;
    }
}
