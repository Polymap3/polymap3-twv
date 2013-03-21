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
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.twv.model.constants.Entfernungskontrolle;
import org.polymap.twv.model.constants.Foerderregion;
import org.polymap.twv.model.constants.Kategorie;
import org.polymap.twv.model.constants.Prioritaet;
import org.polymap.twv.model.constants.Unterkategorie;

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
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    @Optional
    Property<String> name();

    /** @see Kategorie */
    @Optional
    @UseDefaults
    Property<Integer> kategorie();

    /** @see Unterkategorie */
    @Optional
    @UseDefaults
    Property<Integer> unterkategorie();
    
    @Optional
    Association<AusweisungComposite> ausweisung();

    /** @see Prioritaet */
    @Optional
    @UseDefaults
    Property<Integer> prioritaet(); 

    @Optional
    // TODO
    Property<String> gemeinde();
    
    @Optional
    // TODO Länge im LAndkreis
    Property<String> laengeImLandkreis();
    
    @Optional
    Property<String> laengeUeberregional();
    
    @Optional
    Property<String> beschreibung();
    
    /** @see WegbeschaffenheitComposite als Textbausteine */
    @Optional
    //Association<WegbeschaffenheitComposite> beschaffenheit();
    Property<String> beschaffenheit();
    
    @Optional
    Association<WidmungComposite> widmung();

    @Optional
    Association<MarkierungComposite> markierung();
    
    @Optional
    ManyAssociation<WegobjektComposite> wegobjekte();
    
    @Optional
    ManyAssociation<SchildComposite> schilder();

    /** @see Foerderregion */
    @Optional
    @UseDefaults
    Property<Integer> foerderregion();
    
    @Optional
    Property<String> erfasser();
    
    @Optional
    Property<Date> begehungAm();
 
    /** @see Entfernungskontrolle */
    @Optional
    @UseDefaults
    Property<Integer> entfernungskontrolle();
    
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
        }

    }

}
