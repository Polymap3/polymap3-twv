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
package org.polymap.twv.ui.rhei;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;

import org.polymap.rhei.data.entityfeature.ValuePropertyAdapter;
import org.polymap.rhei.field.UploadFormField.UploadedImage;

import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.ImageValue;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ImageValuePropertyAdapter
        extends ValuePropertyAdapter<Property<ImageValue>> {

    private final Property<ImageValue> imageValueProperty;


    public ImageValuePropertyAdapter( String name, Property<ImageValue> imageValueProperty ) {
        super( name, imageValueProperty );
        this.imageValueProperty = imageValueProperty;
    }


    public Object getValue() {
        final ImageValue image = imageValueProperty.get();
        if (image != null) {
            return convertToUploadedImage( image );
        }
        return null;
    }


    /**
     * 
     * @param image
     * @return
     */
    public static UploadedImage convertToUploadedImage( final ImageValue image ) {
        return new UploadedImage() {

            public String originalFilePath() {
                return image.originalFilePath().get();
            }


            public String originalFileName() {
                return image.originalFileName().get();
            }


            public String internalFileName() {
                return image.internalFileName().get();
            }


            public Long fileSize() {
                return image.fileSize().get();
            }


            public String contentType() {
                return image.contentType().get();
            }
        };
    }


    public void setValue( Object newValue ) {
        if (newValue != null) {
            UploadedImage newImage = (UploadedImage)newValue;
            ImageValue newInstance = convertToImageValue( newImage );
            imageValueProperty.set( newInstance );
        }
    }


    /**
     * 
     * @param newImage
     * @param imageValueProperty
     * @return
     */
    public static ImageValue convertToImageValue( UploadedImage newImage) {

        ValueBuilder<ImageValue> imageBuilder = TwvRepository.instance().newValueBuilder(
                ImageValue.class );
        ImageValue prototype = imageBuilder.prototype();
        prototype.contentType().set( newImage.contentType() );
        prototype.fileSize().set( newImage.fileSize() );
        prototype.internalFileName().set( newImage.internalFileName() );
        prototype.originalFileName().set( newImage.originalFileName() );
        prototype.originalFilePath().set( newImage.originalFilePath() );
        return imageBuilder.newInstance();
    }
}
