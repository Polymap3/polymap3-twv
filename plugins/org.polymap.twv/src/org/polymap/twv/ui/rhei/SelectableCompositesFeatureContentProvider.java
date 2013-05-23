/*
 * polymap.org Copyright 2011, Falko Br�utigam, and other contributors as indicated
 * by the @authors tag. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.primitives.Ints;

import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.data.ui.featuretable.IFeatureContentProvider;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.Composite;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;

import org.polymap.twv.model.Named;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class SelectableCompositesFeatureContentProvider
        implements IFeatureContentProvider {

    private static Log                       log    = LogFactory
                                                            .getLog( SelectableCompositesFeatureContentProvider.class );

    private List<? extends Composite>        composites;

    private EntityType                       compositeType;

    private final List<IFeatureTableElement> result = new ArrayList<IFeatureTableElement>();


    public SelectableCompositesFeatureContentProvider( List<? extends Composite> composites,
            EntityType<? extends Composite> compositeType ) {
        assert compositeType != null;
        this.composites = composites;
        this.compositeType = compositeType;
        result.clear();
    }


    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        this.composites = (List<? extends Composite>)newInput;
        result.clear();
    }


    public Object[] getElements( Object input ) {
        log.debug( "getElements(): input=" + input.getClass().getName() );
        if (result.isEmpty()) {
            for (final Composite composite : composites) {
                result.add( new FeatureTableElement( composite ) );
            }
        }
        return result.toArray();
    }

    public int[] getIndicesForElements( Composite... input ) {
        List<Composite> composites = new ArrayList<Composite>();
        for (final Composite composite : input) {
            composites.add( composite );
        }
        return getIndicesForElements( composites );
    }
    
    public int[] getIndicesForElements( Iterable<? extends Composite> input ) {
        List<Integer> indices = new ArrayList<Integer>();
        log.debug( "getIndicesForElements()" );
        if (!result.isEmpty()) {
            for (final Composite composite : input) {
                int index = composites.indexOf( composite );
                if (index != -1) {
                    indices.add( index );
                }
            }
        }
        return Ints.toArray( indices );
    }


    public void dispose() {
    }


    /**
     *
     */
    public class FeatureTableElement
            implements IFeatureTableElement {

        private Composite composite;


        protected FeatureTableElement( Composite composite ) {
            this.composite = composite;
        }


        public Composite getComposite() {
            return composite;
        }


        public Object getValue( String name ) {
            try {
                Object value = compositeType.getProperty( name ).getValue( composite );
                if (value instanceof Named) {
                    return ((Named)value).name().get();
                }
                return value;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }


        public void setValue( String name, Object value ) {
            try {
                compositeType.getProperty( name ).setValue( composite, value );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }


        public String fid() {
            if (composite instanceof Entity) {
                return ((Entity)composite).id();
            }
            else {
                throw new RuntimeException( "Don't know how to build fid out of: " + composite );
            }
        }

    }

}
