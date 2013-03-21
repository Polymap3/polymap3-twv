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
package org.polymap.twv.model;

import net.refractions.udig.catalog.ITransientResolve;

import org.polymap.rhei.data.entityfeature.EntityProvider;
import org.polymap.rhei.data.entityfeature.catalog.EntityServiceImpl;

/**
 * The catalog service for the biotop related features.
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class TwvService
        extends EntityServiceImpl
        implements ITransientResolve {

    public TwvService( EntityProvider... providers ) {
        super( "Twv", TwvRepository.NAMESPACE, providers );
    }


    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee.equals( ITransientResolve.class )) {
            return true;
        }
        return super.canResolve( adaptee );
    }

}
