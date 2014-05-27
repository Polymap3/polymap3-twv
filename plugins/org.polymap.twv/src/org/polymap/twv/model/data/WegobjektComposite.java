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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import com.vividsolutions.jts.geom.Point;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.twv.model.JsonState;
import org.polymap.twv.model.Named;
import org.polymap.twv.model.TwvRepository;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ WegobjektComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class, JsonState.Mixin.class })
public interface WegobjektComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    String NAME = "Wegobjekt";


    @Optional
    Property<Integer> laufendeNr();
    
    @Computed
    Property<String> name();


    @Optional
    Association<WegobjektNameComposite> wegobjektName();


    @Optional
    Property<String> beschreibung();


    @Optional
    Property<Boolean> bedarf();


    @Optional
    Property<Point> geom();


    @Optional
    Property<ImageValue> bild();


    @Optional
    Property<ImageValue> detailBild();


    @Optional
    @Computed
    Property<String> bildName();


    @Optional
    @Deprecated
    Association<WegComposite> weg();

    @Optional
    ManyAssociation<WegComposite> wege();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements WegobjektComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public void beforeCompletion()
                throws UnitOfWorkCompletionException {
        }

        private PropertyInfo nameProperty = new GenericPropertyInfo( WegobjektComposite.class, "name" );


        @Override
        public Property<String> name() {
            return new ComputedPropertyInstance<String>( nameProperty ) {

                public String get() {
                    if (wegobjektName().get() != null) {
                        return wegobjektName().get().name().get();
                    }
                    return null;
                }


                @Override
                public void set( String anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignored
                }
            };
        }

        private PropertyInfo bildNameProperty = new GenericPropertyInfo( WegobjektComposite.class, "bildName" );


        @Override
        public Property<String> bildName() {
            return new ComputedPropertyInstance<String>( bildNameProperty ) {

                public String get() {
                    if (bild().get() != null) {
                        return bild().get().originalFileName().get();
                    }
                    return "";
                }


                @Override
                public void set( String anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignored
                    log.info( anIgnoredValue + " on search?" );
                }
            };
        }


        public static Set<WegobjektComposite> forEntity( WegComposite weg ) {
            Query<WegobjektComposite> all = TwvRepository.instance().findEntities( WegobjektComposite.class, null,
                    0, -1 );
            Set<WegobjektComposite> alleFound = new HashSet<WegobjektComposite>();
            for (WegobjektComposite wegObjekt : all) {
                if (wegObjekt.wege().contains( weg )) {
                    alleFound.add( wegObjekt );
                }
            }
            return alleFound;
        }
    }

}
