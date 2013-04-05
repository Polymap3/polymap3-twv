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

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.twv.model.Named;
import org.polymap.twv.model.NamedCreatorCallback;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ PrioritaetComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
public interface PrioritaetComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    public static abstract class Mixin
            implements PrioritaetComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        public static void createInitData( NamedCreatorCallback cb ) {
            cb.create( PrioritaetComposite.class, "1 - landesweite Bedeutung" );
            cb.create( PrioritaetComposite.class, "2 - überregionale Bedeutung" );
            cb.create( PrioritaetComposite.class, "3 - regionale Bedeutung" );
            cb.create( PrioritaetComposite.class, "4 - örtliche Bedeutung" );
            cb.create( PrioritaetComposite.class, "5 - geringe Bedeutung" );
        }

    }
}