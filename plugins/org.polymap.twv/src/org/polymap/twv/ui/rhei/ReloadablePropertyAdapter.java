package org.polymap.twv.ui.rhei;

import java.util.Map;

import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;

public class ReloadablePropertyAdapter<T extends Entity>
        implements org.opengis.feature.Property {

    private Property               delegate;

    private String                 name;

    private CompositeProvider<T>   provider;

    private PropertyCallback<T>    pcb;

    private AssociationCallback<T> acb;


    public ReloadablePropertyAdapter( CompositeProvider<T> provider, String name,
            PropertyCallback<T> cb ) {
        this.provider = provider;
        this.name = name;
        this.pcb = cb;
    }


    public ReloadablePropertyAdapter( CompositeProvider<T> provider, String name,
            AssociationCallback<T> cb ) {
        this.provider = provider;
        this.name = name;
        this.acb = cb;
    }


    public Name getName() {
        return new NameImpl( name );
    }


    public Object getValue() {
        if (pcb != null) {
            Property p = getCurrentProperty();
            return (p == null ? null : p.get());
        }
        else {
            Association p = getCurrentAssociation();
            return (p == null ? null : p.get());
        }
    }


    public void setValue( Object value ) {
        if (pcb != null) {
            Property p = getCurrentProperty();
            if (p != null) {
                p.set( value );
            }
        }
        else {
            Association p = getCurrentAssociation();
            if (p != null) {
                p.set( value );
            }
        }
    }


    private Property getCurrentProperty() {
        if (provider.get() != null) {
            return pcb.get( provider.get() );
        }
        return null;
    }


    private Association getCurrentAssociation() {
        if (provider.get() != null) {
            return acb.get( provider.get() );
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


    public static class CompositeProvider<T> {

        private T composite;


        public T get() {
            return composite;
        }


        public void set( T composite ) {
            this.composite = composite;
        }
    }


    public interface PropertyCallback<T extends Entity> {

        Property get( T entity );
    }


    public interface AssociationCallback<T extends Entity> {

        Association get( T entity );
    }
}