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
package org.polymap.twv.ui.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.BetweenValidator;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.SelectlistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.twv.model.Named;
import org.polymap.twv.model.TwvRepository;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class DefaultEntityFilter
        extends AbstractTwvEntityFilter {

    private static Log         log = LogFactory.getLog( DefaultEntityFilter.class );

    private final List<String> propertyNames;


    public <T extends Entity> DefaultEntityFilter( ILayer layer, Class<T> type, TwvRepository module ) {
        super(layer, type, "Standard...", module );
        this.propertyNames = new ArrayList();
        EntityType<?> entityType = module.entityType( entityClass );
        for (Property property : entityType.getProperties()) {
            propertyNames.add( property.getName() );
        }
    }


    public <T extends Entity> DefaultEntityFilter( ILayer layer, Class<T> type, TwvRepository module,
            String... propertyNames ) {
        super( layer, type, "Standard...", module );
        this.propertyNames = new ArrayList();
        for (String name : propertyNames) {
            this.propertyNames.add( name );
        }
    }

    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        EntityType<?> entityType = module.entityType( entityClass );
        // sort after labeling
        Map<String, String> labels = new TreeMap<String, String>();
        for (String propertyName : propertyNames) {
            labels.put( labelFor( propertyName ), propertyName );
        }
        
        for (String label: labels.keySet()) {
            String propertyName = labels.get( label );
            Property property = entityType.getProperty( propertyName );
//            if (!(property instanceof EntityType.ManyAssociation)) {
                Class propertyType = property.getType();
                if (String.class.isAssignableFrom( propertyType )) {
                    site.addStandardLayout( site.newFormField( result, property.getName(), String.class,
                            new StringFormField(), null, label ) );
                }
                else if (Integer.class.isAssignableFrom( propertyType )) {
                    site.addStandardLayout( site.newFormField( result, property.getName(), Integer.class,
                            new BetweenFormField( new StringFormField(), new StringFormField() ), new BetweenValidator(
                                    new NumberValidator( Integer.class, Polymap.getSessionLocale() ) ),
                            label ) );
                }
                else if (Double.class.isAssignableFrom( propertyType )) {
                    site.addStandardLayout( site.newFormField( result, property.getName(), Double.class,
                            new BetweenFormField( new StringFormField(), new StringFormField() ), new BetweenValidator(
                                    new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) ),
                            label ) );
                }
                else if (Date.class.isAssignableFrom( propertyType )) {
                    site.addStandardLayout( site.newFormField( result, property.getName(), Date.class,
                            new BetweenFormField( new DateTimeFormField(), new DateTimeFormField() ), null,
                            label ) );
                }
                else if (Named.class.isAssignableFrom( propertyType )) {
                    SelectlistFormField field = new SelectlistFormField( valuesFor( propertyType ) );
                    field.setIsMultiple( true );
                    Composite formField = site.newFormField( result, property.getName(), propertyType, field, null,
                            label );
                    site.addStandardLayout( formField );
                    ((FormData)formField.getLayoutData()).height = 100;
                    ((FormData)formField.getLayoutData()).width = 100;
                }
//            }
        }
       result.pack();
        return result;
    }

    public DefaultEntityFilter exclude( String... names ) {
        for (String name : names) {
            this.propertyNames.remove( name );
        }
        return this;
    }

    @Override
    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {
        try {

            BooleanExpression expr = null;

            EntityType<?> entityType = module.entityType( entityClass );
            Entity template = QueryExpressions.templateFor( entityClass );

            for (Property property : entityType.getProperties()) {
                Object value = site.getFieldValue( property.getName() );
                if (value != null) {
                    BooleanExpression currentExpression = null;
                    Class propertyType = property.getType();
                    Method propertyMethod = entityClass.getDeclaredMethod( property.getName(), new Class[0] );
                    if (String.class.isAssignableFrom( propertyType )) {
                        currentExpression = createStringExpression( template, value, propertyMethod );
                    }
                    else if (Integer.class.isAssignableFrom( propertyType )) {
                        currentExpression = createIntegerExpression( template, value, propertyMethod );
                    }
                    else if (Double.class.isAssignableFrom( propertyType )) {
                        currentExpression = createDoubleExpression( template, value, propertyMethod );
                    }
                    else if (Date.class.isAssignableFrom( propertyType )) {
                        currentExpression = createDateExpression( template, value, propertyMethod );
                    }
                    else if (Named.class.isAssignableFrom( propertyType )) {
                        currentExpression = createNamedExpression( template, value, propertyMethod );
                    }
                    if (currentExpression != null) {
                        expr = (expr == null) ? currentExpression : QueryExpressions.and( expr, currentExpression );
                    }
                }
            }
            return module.findEntities( entityClass, expr, 0, getMaxResults() );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
}
