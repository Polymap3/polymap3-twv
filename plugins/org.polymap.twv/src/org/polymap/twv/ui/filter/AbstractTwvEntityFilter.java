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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.project.ILayer;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;

import org.polymap.twv.model.Named;
import org.polymap.twv.model.TwvRepository;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class AbstractTwvEntityFilter
        extends AbstractEntityFilter {

    private static Log            log = LogFactory.getLog( AbstractTwvEntityFilter.class );

    protected final TwvRepository module;

    private final List<String>    propertyNames;


    public <T extends Entity> AbstractTwvEntityFilter( ILayer layer, Class<T> type, String name, TwvRepository module ) {
        super( "__twv--" + name, layer, name, null, 10000, type );
        this.module = module;
        this.propertyNames = new ArrayList();
        EntityType<?> entityType = module.entityType( entityClass );
        for (Property property : entityType.getProperties()) {
            propertyNames.add( property.getName() );
        }
        // Collections.sort( this.propertyNames );
    }


    public boolean hasControl() {
        return true;
    }


    protected final Map<String, ? extends Object> valuesFor( Class propertyType ) {
        return module.entitiesWithNames( propertyType );
    }


    protected String labelFor( String name ) {
        return name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
    }


    protected BooleanExpression createStringExpression( Entity template, Object value, Method propertyMethod )
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


    protected BooleanExpression createNamedExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException,
            NoSuchMethodException {
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
                Method identity = entityClass.getMethod( "identity", new Class[0] );
                for (Object entity : module.findEntities( entityClass, null, 0, 10000 )) {
                    if (((ManyAssociation<Named>)propertyMethod.invoke( entity, new Object[0] )).contains( named )) {
                        BooleanExpression newExpr = QueryExpressions.eq(
                                (org.qi4j.api.property.Property<String>)identity.invoke( template, new Object[0] ),
                                ((Entity)entity).id() );
                        if (current == null) {
                            current = newExpr;
                        }
                        else {
                            current = QueryExpressions.or( current, newExpr );
                        }
                    }
                }
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


    protected BooleanExpression createIntegerExpression( Entity template, Object value, Method propertyMethod )
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


    protected BooleanExpression createDoubleExpression( Entity template, Object value, Method propertyMethod )
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


    protected BooleanExpression createDateExpression( Entity template, Object value, Method propertyMethod )
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


    @Override
    protected BooleanExpression and( BooleanExpression exp1, BooleanExpression exp2 ) {
        if (exp1 == null) {
            return exp2;
        }
        if (exp2 == null) {
            return exp1;
        }
        return QueryExpressions.and( exp1, exp2 );
    }
}
