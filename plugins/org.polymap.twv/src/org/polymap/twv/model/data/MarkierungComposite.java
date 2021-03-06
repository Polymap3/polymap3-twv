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
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.twv.model.JsonState;
import org.polymap.twv.model.Named;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ MarkierungComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class, JsonState.Mixin.class })
public interface MarkierungComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    @Optional
    Property<String> name();


    @Optional
    Property<ImageValue> bild();


    @Optional
    @Computed
    Property<String> bildName();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements MarkierungComposite {

        private static Log   log              = LogFactory.getLog( Mixin.class );

        private PropertyInfo bildNameProperty = new GenericPropertyInfo( MarkierungComposite.class, "bildName" );


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
                }
            };
        }
    }
}