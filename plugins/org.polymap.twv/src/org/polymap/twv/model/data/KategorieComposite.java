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
import org.qi4j.api.entity.association.ManyAssociation;
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
@Mixins({ KategorieComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
public interface KategorieComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    @Optional
    Property<String> name();


    @Optional
    ManyAssociation<UnterkategorieComposite> unterkategories();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements KategorieComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        public static void createInitData( NamedCreatorCallback cb ) {
            KategorieComposite wanderweg = cb.create( KategorieComposite.class, "Wanderweg" );

            wanderweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class,
                            "Qualitätsweg Wanderbares Deutschland" ) );

            wanderweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class, "Europäischer Fernwanderweg" ) );

            wanderweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class, "Nationaler Fernwanderweg" ) );

            wanderweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class,
                            "Überregionale und regionale Gebietswanderwege" ) );

            wanderweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class,
                            "Orts-, Verbindungs- u. Rundwanderweg" ) );

            wanderweg.unterkategories()
                    .add( cb.create( UnterkategorieComposite.class, "Lehrpfad" ) );
            wanderweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class, "sonstiger Wanderweg" ) );

            KategorieComposite radweg = cb.create( KategorieComposite.class, "Radweg" );

            radweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class, "Radfernweg (SachsenNetz Rad)" ) );

            radweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class,
                            "Regionale Hauptradroute (SachsenNetz Rad)" ) );

            radweg.unterkategories()
                    .add( cb.create( UnterkategorieComposite.class,
                            "sonstige Radroute (SachsenNetz Rad)" ) );

            radweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class, "sonstige Radroute" ) );

            KategorieComposite reitweg = cb.create( KategorieComposite.class, "Reitweg" );

            reitweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class, "Fernroute durch Sachsen" ) );

            reitweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class,
                            "Regionalaroute durch einen Landkreis" ) );

            reitweg.unterkategories()
                    .add( cb.create( UnterkategorieComposite.class, "Lokalroute" ) );

            reitweg.unterkategories().add(
                    cb.create( UnterkategorieComposite.class, "Sonstiger Reitweg" ) );
        }

    }
}