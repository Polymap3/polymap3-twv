package org.polymap.twv.ui.rhei;

import java.util.Map;

import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.property.Property;

import org.polymap.rhei.field.UploadFormField.UploadedImage;

import org.polymap.twv.model.data.ImageValue;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.CompositeProvider;

public class ReloadableImageValuePropertyAdapter<T extends Entity>
        implements org.opengis.feature.Property {

    private Property<ImageValue> delegate;

    private String               name;

    private CompositeProvider<T> provider;

    private PropertyCallback<T>  pcb;


    public ReloadableImageValuePropertyAdapter( CompositeProvider<T> provider, String name,
            PropertyCallback<T> cb ) {
        this.provider = provider;
        this.name = name;
        this.pcb = cb;
    }


    public Name getName() {
        Property<ImageValue> p = getCurrentProperty();
        return new NameImpl( p == null ? name : p.qualifiedName().name() );
    }


    public Object getValue() {
        Property<ImageValue> p = getCurrentProperty();
        if (p != null) {
            ImageValue image = p.get();
            if (image != null) {
                return ImageValuePropertyAdapter.convertToUploadedImage( image );
            }
        }
        return null;
    }


    public void setValue( Object newValue ) {
        Property<ImageValue> p = getCurrentProperty();
        if (p != null) {
            if (newValue != null) {
                UploadedImage newImage = (UploadedImage)newValue;
                ImageValue newInstance = ImageValuePropertyAdapter
                        .convertToImageValue( newImage );
                p.set( newInstance );
            }
            else {
                p.set( null );
            }
        }
    }


    private Property<ImageValue> getCurrentProperty() {
        if (provider.get() != null) {
            return pcb.get( provider.get() );
        }
        return null;
    }


    @Override
    public PropertyType getType() {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public PropertyDescriptor getDescriptor() {
        return null;
    }


    @Override
    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }


    public interface PropertyCallback<T extends Entity> {

        Property<ImageValue> get( T entity );
    }
}