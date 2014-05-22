/* 
 * polymap.org
 * Copyright (C) 2014, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.twv.ui.form;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.polymap.core.data.DataPlugin.ff;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.LayerVisitor;
import org.polymap.core.project.Layers;
import org.polymap.core.project.ProjectRepository;

import org.polymap.twv.model.data.WegComposite;

/**
 * Berechnungen für einen Weg-Geometrie innerhalb von (Gemeinden) Polygonen.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WegGemeindenCalculator {

    private static Log log = LogFactory.getLog( WegGemeindenCalculator.class );
    
    private WegComposite            weg;

    private ILayer                  gemeindeLayer;


    /**
     * 
     * 
     * @param weg
     * @param map Die {@link IMap}, in der der "Gemeinden" Layer gesucht wird.
     */
    public WegGemeindenCalculator( WegComposite weg, IMap map ) {
        this.weg = weg;
        this.gemeindeLayer = getOnlyElement( filter( map.getLayers(), Layers.hasLabel( "Gemeinden" ) ) );
    }
     
    
    /**
     * Der "Gemeinden" Layer wird in allen Projekten gesucht.
     * 
     * @param weg
     */
    public WegGemeindenCalculator( WegComposite weg ) {
        this.weg = weg;
        this.gemeindeLayer = ProjectRepository.instance().visit( new LayerVisitor() {
            public boolean visit( ILayer layer ) {
                if (layer.getLabel().equalsIgnoreCase( "gemeinden" )) {
                    result = layer;
                }
                return result == null;
            }
        });
        if (gemeindeLayer == null) {
            throw new RuntimeException( "Es existiert keine Ebene mit dem Namen \"Gemeinden\"." );
        }
    }
     
    
    public FeatureCollection gemeinden() throws Exception {
        MultiLineString wegGeom = weg.geom().get();

        PipelineFeatureSource fs = PipelineFeatureSource.forLayer( gemeindeLayer, false );
        return fs.getFeatures( ff.intersects(
                ff.property( fs.getSchema().getGeometryDescriptor().getLocalName() ), ff.literal( wegGeom ) ) );
    }
     
    
    public List<String> gemeindeNamen() throws Exception {
        final List<String> result = new ArrayList();
        
        gemeinden().accepts( new FeatureVisitor() {
            public void visit( Feature gemeinde ) {
                Property nameProp = gemeinde.getProperty( "ORTSNAME" );
                result.add( nameProp != null ? nameProp.getValue().toString() : "-" );
            }
        }, null );
        return result;
    }
    
    
    public double laengeImLandkreis() throws Exception {
        final MultiLineString wegGeom = weg.geom().get();
        final AtomicReference<Double> result = new AtomicReference( 0d );

        gemeinden().accepts( new FeatureVisitor() {
            public void visit( Feature gemeinde ) {
                Geometry intersection = wegGeom.intersection( (Geometry)gemeinde.getDefaultGeometryProperty().getValue() );
                log.info( "INTERSECTION: " + intersection );
                
                double sum = result.get().doubleValue() + intersection.getLength();
                result.set( sum );
                log.info( "LENGHT: " + sum );
            }
        }, null );
        return result.get().doubleValue();
    }


    public double gesamtLaenge() {
        return weg.geom().get().getLength();
    }
    
}
