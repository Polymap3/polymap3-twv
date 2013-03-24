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
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

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
@Mixins({ VermarkterComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class,
// JsonState.Mixin.class
})
public interface VermarkterComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    @Optional
    Property<String> name();


    @Optional
    Property<String> ansprechpartner();


    @Optional
    Property<String> strasse();


    @Optional
    Property<String> hausnummer();


    @Optional
    Property<String> plz();


    @Optional
    Property<String> ort();


    @Optional
    Property<String> telefon();


    @Optional
    Property<String> email();


    @Optional
    Property<String> url();


    @Optional
    Property<String> angebot();


    /** bidrectional navigierbar? */
    @Optional
    Association<WegComposite> weg();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements VermarkterComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public void beforeCompletion()
                throws UnitOfWorkCompletionException {
        }


        // TODO Vermarkter filtern
        public static Iterable<VermarkterComposite> forEntity( WegComposite weg ) {
            VermarkterComposite template = QueryExpressions.templateFor( VermarkterComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.weg(), weg );
            // Query<VermarkterComposite> matches =
            // TwvRepository.instance().findEntities( VermarkterComposite.class,
            // expr, 0, -1 );
            Query<VermarkterComposite> matches = TwvRepository.instance().findEntities(
                    VermarkterComposite.class, null, 0, -1 );
            return matches;
        }
    }
}
