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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.project.ILayer;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
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
        extends AbstractEntityFilter {

    private static Log         log = LogFactory.getLog( DefaultEntityFilter.class );

    private QiModule           module;

    private final List<String> propertyNames;


    public <T extends Entity> DefaultEntityFilter( ILayer layer, Class<T> type, QiModule module ) {
        super( "__twv--", layer, "Standard (neu)...", null, 10000, type );
        this.module = module;
        this.propertyNames = new ArrayList();
        EntityType<?> entityType = module.entityType( entityClass );
        for (Property property : entityType.getProperties()) {
            propertyNames.add( property.getName() );
        }
        Collections.sort( this.propertyNames );
    }


    public <T extends Entity> DefaultEntityFilter( ILayer layer, Class<T> type, QiModule module,
            String... propertyNames ) {
        super( "__twv--", layer, "Standard (neu)...", null, 10000, type );
        this.module = module;
        this.propertyNames = new ArrayList();
        for (String name : propertyNames) {
            this.propertyNames.add( name );
        }
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        EntityType<?> entityType = module.entityType( entityClass );
        for (String propertyName : propertyNames) {
            Property property = entityType.getProperty( propertyName );
            Class propertyType = property.getType();
            if (String.class.isAssignableFrom( propertyType )) {
                site.addStandardLayout( site.newFormField( result, property.getName(), String.class,
                        new StringFormField(), null, labelFor( property.getName() ) ) );
            }
            else if (Integer.class.isAssignableFrom( propertyType )) {
                site.addStandardLayout( site.newFormField( result, property.getName(), Integer.class,
                        new BetweenFormField( new StringFormField(), new StringFormField() ), new BetweenValidator(
                                new NumberValidator( Integer.class, Polymap.getSessionLocale() ) ), labelFor( property
                                .getName() ) ) );
            }
            else if (Double.class.isAssignableFrom( propertyType )) {
                site.addStandardLayout( site.newFormField( result, property.getName(), Double.class,
                        new BetweenFormField( new StringFormField(), new StringFormField() ), new BetweenValidator(
                                new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) ),
                        labelFor( property.getName() ) ) );
            }
            else if (Date.class.isAssignableFrom( propertyType )) {
                site.addStandardLayout( site.newFormField( result, property.getName(), Date.class,
                        new BetweenFormField( new DateTimeFormField(), new DateTimeFormField() ), null,
                        labelFor( property.getName() ) ) );
            }
            else if (Named.class.isAssignableFrom( propertyType )) {
                SelectlistFormField field = new SelectlistFormField( valuesFor( propertyType ) );
                field.setIsMultiple( true );
                Composite formField = site.newFormField( result, property.getName(), propertyType, field, null,
                        labelFor( property.getName() ) );
                site.addStandardLayout( formField );
                ((FormData)formField.getLayoutData()).height = 100;
            }
        }

        return result;
    }


    private Map<String, ? extends Object> valuesFor( Class propertyType ) {
        return ((TwvRepository)module).entitiesWithNames( propertyType );
    }


    private String labelFor( String name ) {
        return name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
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


    private BooleanExpression createStringExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String match = (String)value;
        if (!match.isEmpty()) {
            if (match.indexOf( '*' ) == -1 && match.indexOf( '?' ) == -1) {
                match = '*' + match + '*';
            }
            return QueryExpressions.matches(
                    (org.qi4j.api.property.Property<String>)propertyMethod.invoke( template, new Object[0] ), match );
        }
        return null;
    }


    private BooleanExpression createNamedExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        List<Named> values = (List<Named>)value;
        BooleanExpression expr = null;
        for (Named named : values) {
            Object p = propertyMethod.invoke( template, new Object[0] );
            BooleanExpression current = null;
            if (p instanceof Association) {
                current = QueryExpressions.eq( (Association<Named>)p, named );
            }
            else {
                // must be manyassociation
                current = QueryExpressions.contains( (ManyAssociation<Named>)p, named );
            }
            if (expr == null) {
                expr = current;
            }
            else {
                expr = QueryExpressions.or( expr, current );
            }
        }
        return expr;
    }


    private BooleanExpression createIntegerExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalAccessException, InvocationTargetException {
        BooleanExpression currentExpression;
        Object[] betweenValues = (Object[])value;
        BooleanExpression ge = betweenValues[0] != null ? QueryExpressions.ge(
                (org.qi4j.api.property.Property<Integer>)propertyMethod.invoke( template, new Object[0] ),
                Integer.parseInt( (String)betweenValues[0] ) ) : null;

        BooleanExpression le = betweenValues[1] != null ? QueryExpressions.le(
                (org.qi4j.api.property.Property<Integer>)propertyMethod.invoke( template, new Object[0] ),
                Integer.parseInt( (String)betweenValues[1] ) ) : null;

        currentExpression = ge;
        if (le != null) {
            currentExpression = currentExpression == null ? le : QueryExpressions.and( ge, le );
        }
        return currentExpression;
    }


    private BooleanExpression createDoubleExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalAccessException, InvocationTargetException {
        BooleanExpression currentExpression;
        Object[] betweenValues = (Object[])value;
        BooleanExpression ge = betweenValues[0] != null ? QueryExpressions.ge(
                (org.qi4j.api.property.Property<Double>)propertyMethod.invoke( template, new Object[0] ),
                Double.parseDouble( (String)betweenValues[0] ) ) : null;

        BooleanExpression le = betweenValues[1] != null ? QueryExpressions.le(
                (org.qi4j.api.property.Property<Double>)propertyMethod.invoke( template, new Object[0] ),
                Double.parseDouble( (String)betweenValues[1] ) ) : null;

        currentExpression = ge;
        if (le != null) {
            currentExpression = currentExpression == null ? le : QueryExpressions.and( ge, le );
        }
        return currentExpression;
    }


    private BooleanExpression createDateExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalAccessException, InvocationTargetException {
        BooleanExpression currentExpression;
        Object[] betweenValues = (Object[])value;
        BooleanExpression ge = betweenValues[0] != null ? QueryExpressions.ge(
                (org.qi4j.api.property.Property<Date>)propertyMethod.invoke( template, new Object[0] ),
                (Date)betweenValues[0] ) : null;

        BooleanExpression le = betweenValues[1] != null ? QueryExpressions.le(
                (org.qi4j.api.property.Property<Date>)propertyMethod.invoke( template, new Object[0] ),
                (Date)betweenValues[1] ) : null;

        currentExpression = ge;
        if (le != null) {
            currentExpression = currentExpression == null ? le : QueryExpressions.and( ge, le );
        }
        return currentExpression;
    }
}
