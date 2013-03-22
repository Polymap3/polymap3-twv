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
import org.qi4j.api.property.Property;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.twv.model.Named;
import org.polymap.twv.model.NamedCreatorCallback;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ UnterkategorieComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
public interface UnterkategorieComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    @Optional
    Property<String> name();

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements UnterkategorieComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        public static void createInitData( NamedCreatorCallback cb ) {
            cb.create( UnterkategorieComposite.class,
                    "LEADER-Region Vorerzgebirgsregion Augustusburger Land" );
            cb.create( UnterkategorieComposite.class, "LEADER-Region Klosterbezirk Altzella" );
            cb.create( UnterkategorieComposite.class, "LEADER-Region Land des Roten Porphyr" );
            cb.create( UnterkategorieComposite.class, "LEADER-Region Lommatzscher Pflege" );
            cb.create( UnterkategorieComposite.class, "ILE-Region SachsenKreuz+" );
            cb.create( UnterkategorieComposite.class, "ILE-Region Silbernes Erzgebirge" );
        }

    }
}