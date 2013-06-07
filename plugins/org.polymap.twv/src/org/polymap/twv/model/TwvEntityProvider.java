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

import java.util.Collection;
import java.util.Map;

import org.geotools.data.Query;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Association;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.qi4j.QiModule;

import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider3;

/**
 * Basisklasse für alle TWV {@link EntityProvider}. Die Klasse liefert einfache
 * Implementationen für Methoden. Die Geometrie muss immer im Property "geom" liegen.
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class TwvEntityProvider<T extends Entity>
        extends DefaultEntityProvider<T>
        implements EntityProvider<T>, EntityProvider3<T> {

    public TwvEntityProvider( QiModule repo, Class<T> entityClass, Name entityName ) {
        super( repo, entityClass, entityName );
    }


    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName ) {
        try {
            return Geometries.crs( "EPSG:31468" );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public String getDefaultGeometry() {
        return "geom";
    }


    @Override
    public ReferencedEnvelope getBounds() {
        Property geomProp = getEntityType().getProperty( getDefaultGeometry() );
        if (geomProp != null) {
            return super.getBounds();
        }
        else {
            // fake values
            return new ReferencedEnvelope( 4000000, 5000000, 5000000, 6000000, getCoordinateReferenceSystem( null ) );
        }
    }


    //
    // @Override
    // public FeatureType buildFeatureType() {
    //
    // EntityType type = getEntityType();
    // SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    // builder.setName( getEntityName() );
    // return buildFeatureType( builder.buildFeatureType() );
    // }
    //
    //
    // @Override
    // public Feature buildFeature( Entity entity, FeatureType schema ) {
    // // TODO Auto-generated method stub
    // throw new RuntimeException( "not yet implemented." );
    // }

    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init( (SimpleFeatureType)schema );

        // alle mit einem Type der ein Property Name hat
        EntityType entityType = getEntityType();
        Collection<EntityType.Property> p = entityType.getProperties();
        for (EntityType.Property prop : p) {
            Class propType = prop.getType();
            if (prop instanceof Association) {
                Association association = (Association)prop;
                if (Named.class.isAssignableFrom( association.getType() )) {
                    builder.add( association.getName(), String.class );
                }
            }
        }

        return builder.buildFeatureType();
    }


    @Override
    public Feature buildFeature( T entity, Feature feature, FeatureType schema ) {
        // VertragsArtComposite vertragsArt = entity.vertragsArt().get();
        // feature.getProperty("Vertragsart").setValue(
        // vertragsArt != null ? vertragsArt.name().get() : "");
        // assoziationen ergänzen, alle mit Name Property
        try {
            EntityType entityType = getEntityType();
            Collection<EntityType.Property> p = entityType.getProperties();
            for (EntityType.Property prop : p) {
                if (prop instanceof Association) {
                    Association association = (Association)prop;
                    org.opengis.feature.Property property = feature.getProperty( association.getName() );
                    if (property != null) {
                        if (Named.class.isAssignableFrom( association.getType() )) {
                            Named associationValue = (Named)association.getValue( entity );
                            String name = null;
                            if (associationValue != null) {
                                name = associationValue.name().get();
                            }
                            property.setValue( (name == null) ? "" : name );
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new IllegalStateException( e );
        }
        return feature;
    }


    @Override
    public boolean modifyFeature( T entity, String propName, Object value )
            throws Exception {
        EntityType.Property prop = type.getProperty( propName );
        if (prop instanceof Association) {
            Association association = (Association)prop;
            if (Named.class.isAssignableFrom( association.getType() )) {
                if (value != null) {
                    Map entitiesWithNames = TwvRepository.instance().entitiesWithNames( association.getType() );
                    prop.setValue( entity, entitiesWithNames.get( value ) );
                }
                else {
                    prop.setValue( entity, null );
                }
                return true;
            }
        }
        return false;
    }


    @Override
    public Query transformQuery( Query query ) {
        return query;
        // mapping von benamsten assoziationen zurück auf properties krieg ich jetzt
        // nicht gebacken
        // String typeName = query.getTypeName();
        // Filter filter = query.getFilter();
        // Filter dublicate = filter == null ? null : (Filter)filter.accept( new
        // DuplicatingFilterVisitor() {
        // // query.getFilter().accept( new FilterVisitor() {
        //
        // // @Override
        // // public Object visitNullFilter( Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        // //
        // // @Override
        // // public Object visit( Within filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        // //
        // //
        // // @Override
        // // public Object visit( Touches filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        // //
        // // @Override
        // // public Object visit( Overlaps filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        // //
        // //
        // // @Override
        // // public Object visit( Intersects filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        //
        // @Override
        // public Object visit( Equals filter, Object extraData ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        //
        // // @Override
        // // public Object visit( DWithin filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        // //
        // //
        // // @Override
        // // public Object visit( Disjoint filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        // //
        // //
        // // @Override
        // // public Object visit( Crosses filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        //
        // @Override
        // public Object visit( Contains filter, Object extraData ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        // //
        // // @Override
        // // public Object visit( Beyond filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        // //
        // //
        // // @Override
        // // public Object visit( BBOX filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        //
        // @Override
        // public Object visit( PropertyIsNull filter, Object extraData ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        //
        // @Override
        // public Object visit( PropertyIsLike filter, Object extraData ) {
        // PropertyName propName = (PropertyName)visit(
        // (PropertyName)filter.getExpression(), extraData );
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        //
        // @Override
        // public Object visit( PropertyIsLessThanOrEqualTo filter, Object extraData
        // ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        //
        // @Override
        // public Object visit( PropertyIsLessThan filter, Object extraData ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        //
        // @Override
        // public Object visit( PropertyIsGreaterThanOrEqualTo filter, Object
        // extraData ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        //
        // @Override
        // public Object visit( PropertyIsGreaterThan filter, Object extraData ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        //
        // @Override
        // public Object visit( PropertyIsNotEqualTo filter, Object extraData ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        //
        // @Override
        // public Object visit( PropertyIsEqualTo filter, Object extraData ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        //
        // @Override
        // public Object visit( PropertyIsBetween filter, Object extraData ) {
        // // TODO Auto-generated method stub
        // throw new RuntimeException( "not yet implemented." );
        // }
        //
        // //
        // // @Override
        // // public Object visit( Or filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        //
        // // @Override
        // // public Object visit( Not filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        //
        // // @Override
        // // public Object visit( Id filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        //
        // // @Override
        // // public Object visit( And filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        // //
        // // @Override
        // // public Object visit( IncludeFilter filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        //
        //
        // // @Override
        // // public Object visit( ExcludeFilter filter, Object extraData ) {
        // // // TODO Auto-generated method stub
        // // throw new RuntimeException( "not yet implemented." );
        // // }
        // }, null );
        //
        // DefaultQuery result = new DefaultQuery( query );
        // result.setFilter( dublicate );
        // return result;
    }
}
