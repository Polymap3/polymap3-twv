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

import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;


/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public interface ImageValue
        extends ValueComposite, Composite {

    @Optional
    Property<String> contentType();

    @Optional
    Property<String> originalFileName();
    
    @Optional
    Property<String> originalFilePath();
    
    @Optional
    Property<Long> fileSize();
    
    @Optional
    Property<String> internalFileName();
    
    @Optional
    Property<String> thumbnailFileName();
}
