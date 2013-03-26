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
package org.polymap.twv.model.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.vividsolutions.jts.geom.Point;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.twv.model.Named;
import org.polymap.twv.model.TwvRepository;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ SchildComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class,
// JsonState.Mixin.class
})
public interface SchildComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    @Optional
    @Computed
    Property<String> name();


    @Optional
    Association<SchildartComposite> schildart();


    @Optional
    Property<String> laufendeNr();


    @Optional
    Property<Point> geom();


    @Optional
    Association<PfeilrichtungComposite> pfeilrichtung();


    @Optional
    Property<String> beschriftung();


    @Optional
    Association<SchildmaterialComposite> material();


    @Optional
    Property<String> befestigung();


    @Optional
    Property<ImageValue> bild();


    @Optional
    @Computed
    Property<String> bildName();


    /** bidrectional navigierbar? */
    @Optional
    Association<WegComposite> weg();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements SchildComposite {

        private static Log   log          = LogFactory.getLog( Mixin.class );

        // @Override
        // public void beforeCompletion()
        // throws UnitOfWorkCompletionException {
        // throw new RuntimeException( "not yet implemented." );
        // }
        // @Override
        // public void afterCompletion(EntityStatus entityState)
        // throws UnitOfWorkCompletionException {
        // // EntityStatus entityState = EntityStatus.NEW;
        // // try {
        // // entityState = EntityInstance.getEntityInstance( this
        // ).entityState().status();
        // // }
        // // catch (Exception e) {
        // // // bei neuen Objekten gibts hier ne IllegalArgumentException
        // // }
        // switch (entityState) {
        // case NEW:
        // if (weg().get() != null) {
        // weg().get().schilder().add( this );
        // }
        // break;
        // case UPDATED: {
        // if (weg().get() != null && !weg().get().schilder().contains( this )) {
        // weg().get().schilder().add( this );
        // }
        //
        // break;
        // }
        // case REMOVED: {
        // if (weg().get() != null) {
        // weg().get().schilder().remove( this );
        // }
        //
        // break;
        // }
        // default:
        // throw new IllegalStateException( "unknwon entity state " + entityState );
        // }
        // }

        private PropertyInfo nameProperty = new GenericPropertyInfo( SchildComposite.class, "name" );


        @Override
        public Property<String> name() {
            return new ComputedPropertyInstance<String>( nameProperty ) {

                public String get() {
                    if (schildart().get() != null) {
                        return schildart().get().name().get();
                    }
                    return "";
                }
            };
        }

        private PropertyInfo bildNameProperty = new GenericPropertyInfo( SchildComposite.class,
                                                      "bildName" );


        @Override
        public Property<String> bildName() {
            return new ComputedPropertyInstance<String>( bildNameProperty ) {

                public String get() {
                    if (bild().get() != null) {
                        return bild().get().originalFileName().get();
                    }
                    return "";
                }
            };
        }


        // TODO Schilder filtern
        public static Iterable<SchildComposite> forEntity( WegComposite weg ) {
            SchildComposite template = QueryExpressions.templateFor( SchildComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.weg(), weg );
            // Query<SchildComposite> matches =
            // TwvRepository.instance().findEntities( SchildComposite.class,
            // expr, 0, -1 );
            Query<SchildComposite> matches = TwvRepository.instance().findEntities(
                    SchildComposite.class, null, 0, -1 );
            return matches;
        }
    }
}
