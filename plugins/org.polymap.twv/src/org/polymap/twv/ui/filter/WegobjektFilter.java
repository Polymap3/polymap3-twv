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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.google.common.collect.Iterables;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Layers;

import org.polymap.rhei.data.entityfeature.SpatialPredicate;
import org.polymap.rhei.field.SelectlistFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.FoerderregionComposite;
import org.polymap.twv.model.data.KategorieComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegobjektComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegobjektFilter
        extends AbstractTwvEntityFilter {

    private static Log log = LogFactory.getLog( WegobjektFilter.class );


    public WegobjektFilter( ILayer layer, TwvRepository module ) {
        super( layer, WegobjektComposite.class, "nach Kommune, Kategorie, Förderregion...", module );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        // kommunen
        final Map<String, Object> kommunen = new HashMap<String, Object>();
        try {
            long s = System.currentTimeMillis();
            IMap map = getLayer().getMap();
            ILayer layer = Iterables
                    .getOnlyElement( Iterables.filter( map.getLayers(), Layers.hasLabel( "Gemeinden" ) ) );

            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
            FeatureCollection gemeinden = fs.getFeatures();
            gemeinden.accepts( new FeatureVisitor() {

                public void visit( Feature gemeinde ) {
                    Property nameProp = gemeinde.getProperty( "ORTSNAME" );
                    kommunen.put( nameProp != null ? nameProp.getValue().toString() : "-", gemeinde );
                }
            }, null );
            log.info( (System.currentTimeMillis() - s) + "ms Kommunen: " + kommunen.keySet().size() );
        }
        catch (Exception e) {
            log.warn( "", e );
            kommunen.put( "-konnten nicht ermittelt werden- (" + e.getLocalizedMessage() + ")", null );
        }
        SelectlistFormField kofield = new SelectlistFormField( kommunen );
        kofield.setIsMultiple( true );
        Composite koformField = site.newFormField( result, "kommunen", KategorieComposite.class, kofield, null,
                "Kommune" );
        site.addStandardLayout( koformField );
        ((FormData)koformField.getLayoutData()).height = 100;
        ((FormData)koformField.getLayoutData()).width = 100;

        // kategorie
        SelectlistFormField kfield = new SelectlistFormField( valuesFor( KategorieComposite.class ) );
        kfield.setIsMultiple( true );
        Composite kformField = site.newFormField( result, "kategorie", KategorieComposite.class, kfield, null,
                "Kategorie" );
        site.addStandardLayout( kformField );
        ((FormData)kformField.getLayoutData()).height = 100;
        ((FormData)kformField.getLayoutData()).width = 100;

        // foerderregion
        SelectlistFormField ffield = new SelectlistFormField( valuesFor( FoerderregionComposite.class ) );
        ffield.setIsMultiple( true );
        Composite fformField = site.newFormField( result, "foerderregionen", FoerderregionComposite.class, ffield,
                null, "Förderregion" );
        site.addStandardLayout( fformField );
        ((FormData)fformField.getLayoutData()).height = 100;
        ((FormData)fformField.getLayoutData()).width = 100;

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {

        // EntityType<?> entityType = module.entityType( entityClass );
        BooleanExpression expr = null;
        WegComposite wegTemplate = QueryExpressions.templateFor( WegComposite.class );

        // kategorie
        Collection<KategorieComposite> kategorien = (Collection<KategorieComposite>)site.getFieldValue( "kategorie" );
        if (kategorien != null && !kategorien.isEmpty()) {
            BooleanExpression kExpr = null;
            for (KategorieComposite kategorie : kategorien) {
                BooleanExpression newExpr = QueryExpressions.eq( wegTemplate.kategorie(), kategorie );
                if (kExpr == null) {
                    kExpr = newExpr;
                }
                else {
                    kExpr = QueryExpressions.or( kExpr, newExpr );
                }
            }
            expr = and( expr, kExpr );
        }

        // Förderregion
        Collection<FoerderregionComposite> foerderregionen = (Collection<FoerderregionComposite>)site
                .getFieldValue( "foerderregionen" );
        if (foerderregionen != null && !foerderregionen.isEmpty()) {

            Query<WegComposite> alleWege = module.findEntities( WegComposite.class, expr, 0, getMaxResults() );
            Set<WegComposite> alleGefundenenWege = new HashSet<WegComposite>();
            BooleanExpression fExpr = null;
            for (WegComposite weg : alleWege) {
                for (FoerderregionComposite foerderRegion : foerderregionen) {
                    if (weg.foerderregionen().contains( foerderRegion )) {
                        alleGefundenenWege.add( weg );
                    }
                }
            }

            if (alleGefundenenWege.isEmpty()) {
                expr = QueryExpressions.eq( wegTemplate.identity(), "unknown" );
            } else {    
                BooleanExpression inExpr = null;
                for (WegComposite w : alleGefundenenWege) {
                    BooleanExpression newExpr = QueryExpressions.eq( wegTemplate.identity(), w.identity().get() );
                    if (inExpr == null) {
                        inExpr = newExpr;
                    }
                    else {
                        inExpr = QueryExpressions.or( inExpr, newExpr );
                    }
                }
                expr = inExpr;
            }
        }
        // alle wege suchen und daraus die query für WegObjekte bauen
        WegobjektComposite template = QueryExpressions.templateFor( WegobjektComposite.class );
        if (expr != null) {
            Query<WegobjektComposite> alleWegobjekte = module.findEntities( WegobjektComposite.class, null, 0, getMaxResults() );
            Query<WegComposite> alleWege = module.findEntities( WegComposite.class, expr, 0, getMaxResults() );
            Set<WegobjektComposite> alleGefundenenWegobjekte = new HashSet<WegobjektComposite>();
            BooleanExpression fExpr = null;
            for (WegComposite weg : alleWege) {
                for (WegobjektComposite wegobjekt : alleWegobjekte) {
                    if (wegobjekt.wege().contains( weg )) {
                        alleGefundenenWegobjekte.add( wegobjekt );
                    }
                }
            }

            if (alleGefundenenWegobjekte.isEmpty()) {
                expr = QueryExpressions.eq( wegTemplate.identity(), "unknown" );
            } else {    
                BooleanExpression inExpr = null;
                for (WegobjektComposite w : alleGefundenenWegobjekte) {
                    BooleanExpression newExpr = QueryExpressions.eq( template.identity(), w.identity().get() );
                    if (inExpr == null) {
                        inExpr = newExpr;
                    }
                    else {
                        inExpr = QueryExpressions.or( inExpr, newExpr );
                    }
                }
                expr = inExpr;
            }
        }

        // Kommune
        Collection<Feature> kommunen = (Collection<Feature>)site.getFieldValue( "kommunen" );
        if (kommunen != null && !kommunen.isEmpty()) {
            BooleanExpression kExpr = null;
            for (Feature kommune : kommunen) {
                BooleanExpression newExpr = new SpatialPredicate.Intersects(
                        QueryExpressions.asPropertyExpression( template.geom() ),
                        QueryExpressions.asTypedValueExpression( kommune.getDefaultGeometryProperty().getValue() ) );
                if (kExpr == null) {
                    kExpr = newExpr;
                }
                else {
                    kExpr = QueryExpressions.or( kExpr, newExpr );
                }
            }
            expr = and( expr, kExpr );
        }


        return module.findEntities( WegobjektComposite.class, expr, 0, getMaxResults() );
    }
}
