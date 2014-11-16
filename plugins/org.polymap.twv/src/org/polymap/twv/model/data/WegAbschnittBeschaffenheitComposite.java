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
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.twv.model.JsonState;
import org.polymap.twv.model.Named;
import org.polymap.twv.model.TwvRepository;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ WegAbschnittBeschaffenheitComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class, JsonState.Mixin.class })
public interface WegAbschnittBeschaffenheitComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    @Optional
    Property<String> name();


    @Optional
    Association<WegobjektComposite> objektVon();


    @Optional
    Association<WegobjektComposite> objektBis();


    @Optional
    Association<WegComposite> weg();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements WegAbschnittBeschaffenheitComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public static Iterable<WegAbschnittBeschaffenheitComposite> forEntity( WegComposite weg ) {
            WegAbschnittBeschaffenheitComposite template = QueryExpressions
                    .templateFor( WegAbschnittBeschaffenheitComposite.class );

            Query<WegAbschnittBeschaffenheitComposite> abschnitte = TwvRepository.instance().findEntities(
                    WegAbschnittBeschaffenheitComposite.class, QueryExpressions.eq( template.weg(), weg ), 0, -1 );
            return abschnitte;
        }


        public static Iterable<WegAbschnittBeschaffenheitComposite> forEntity( WegobjektComposite entity ) {
            WegAbschnittBeschaffenheitComposite template = QueryExpressions
                    .templateFor( WegAbschnittBeschaffenheitComposite.class );

            Query<WegAbschnittBeschaffenheitComposite> abschnitte = TwvRepository.instance().findEntities(
                    WegAbschnittBeschaffenheitComposite.class,
                    QueryExpressions.or( QueryExpressions.eq( template.objektVon(), entity ),
                            QueryExpressions.eq( template.objektBis(), entity ) ), 0, -1 );
            return abschnitte;
        }
    }
}