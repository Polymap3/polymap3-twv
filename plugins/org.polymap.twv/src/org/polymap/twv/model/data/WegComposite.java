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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;

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
@Mixins({ WegComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class,
// JsonState.Mixin.class
})
public interface WegComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    @Optional
    Property<String> name();


    @Optional
    Association<KategorieComposite> kategorie();


    @Optional
    Association<UnterkategorieComposite> unterkategorie();


    @Optional
    Association<AusweisungComposite> ausweisung();


    @Optional
    Association<PrioritaetComposite> prioritaet();


    // wird nur im Formular angezeigt
    // @Optional
    // // TODO geometrie wie?
    // Property<String> gemeinde();
    //
    // @Optional
    // // TODO Länge im LAndkreis
    // Property<String> laengeImLandkreis();

    @Optional
    Property<String> laengeUeberregional();


    @Optional
    Property<String> beschreibung();


    /** @see WegbeschaffenheitComposite als Textbausteine */
    @Optional
    // Association<WegbeschaffenheitComposite> beschaffenheit();
    Property<String> beschaffenheit();


    @Optional
    Association<WidmungComposite> widmung();


    @Optional
    Association<MarkierungComposite> markierung();


    @Optional
    ManyAssociation<WegobjektComposite> wegobjekte();


    @Optional
    ManyAssociation<SchildComposite> schilder();


    @Optional
    ManyAssociation<FoerderregionComposite> foerderregionen();


    @Optional
    Property<String> erfasser();


    @Optional
    Property<String> wegewart();


    @Optional
    Property<Date> begehungAm();


    @Optional
    Association<EntfernungskontrolleComposite> entfernungskontrolle();


    @Optional
    Property<String> bemerkung();


    @Optional
    Property<String> maengel();


    @Optional
    ManyAssociation<VermarkterComposite> vermarkter();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements WegComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public void beforeCompletion()
                throws UnitOfWorkCompletionException {
            EntityStatus entityState = EntityStatus.NEW;
            try {
                entityState = EntityInstance.getEntityInstance( this ).entityState().status();
            } catch (Exception e) {
                // bei neuen Objekten gibts hier ne IllegalArgumentException
            }    

            // kaskadierendes löschen
            switch (entityState) {
                case NEW:
                case UPDATED:

                    break;

                    // TODO REMOVED kommt hier nicht an
                case REMOVED: {
                    TwvRepository repo = TwvRepository.instance();
                    int count = schilder().count();
                    if (count > 0) {
                        for (int i=0; i<count; i++) {
                            repo.removeEntity( schilder().get(i) );
                        }
                    }
                    count = wegobjekte().count();
                    if (count > 0) {
                        for (int i=0; i<count; i++) {
                            repo.removeEntity( wegobjekte().get(i) );
                        }
                    }
                    count = vermarkter().count();
                    if (count > 0) {
                        for (int i=0; i<count; i++) {
                            repo.removeEntity( vermarkter().get(i) );
                        }
                    }
                    break;
                }
                default:
                    throw new IllegalStateException( "unknwon entity state " + entityState );
            }
        }
    }
}
