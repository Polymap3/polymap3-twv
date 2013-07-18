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
package org.polymap.twv.ui;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;

import org.polymap.rhei.data.entityfeature.EntityProvider;
import org.polymap.rhei.data.entityfeature.catalog.EntityGeoResourceImpl;
import org.polymap.rhei.filter.IFilter;
import org.polymap.rhei.filter.IFilterProvider;

import org.polymap.twv.model.TwvEntityProvider;
import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.VermarkterComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegobjektComposite;
import org.polymap.twv.ui.filter.DefaultEntityFilter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FilterProvider
        implements IFilterProvider {

    private static Log log = LogFactory.getLog( FilterProvider.class );

    private ILayer     layer;


    public List<IFilter> addFilters( ILayer _layer )
            throws Exception {
        this.layer = _layer;

        final TwvRepository repo = TwvRepository.instance();
        IGeoResource geores = layer.getGeoResource();

        List<IFilter> result = new ArrayList<IFilter>();

        if (geores instanceof EntityGeoResourceImpl) {
            EntityProvider provider = geores.resolve( EntityProvider.class, null );
            if (provider != null && provider instanceof TwvEntityProvider) {
                TwvEntityProvider pr = (TwvEntityProvider)provider;
                Class type = provider.getEntityType().getType();
                // egeo.
                if (type.isAssignableFrom( WegComposite.class )) {
                    result.add( new DefaultEntityFilter( layer, provider.getEntityType().getType(), repo ) {

                        @Override
                        protected String labelFor( String name ) {
                            return "entfernungskontrolle".equals( name ) ? "Kontrolle" : super.labelFor( name );

                        }
                    } );
                    // , "ausweisung", "bemerkung", "beschaffenheit", "beschreibung",
                    // "entfernungskontrolle", "erfasser", "kategorie", "name",
                    // "kategorie", "unterkategorie", "ausweisung" ) );
                }
                else if (type.isAssignableFrom( MarkierungComposite.class )) {
                    result.add( new DefaultEntityFilter( layer, provider.getEntityType().getType(), repo )
                            .exclude( "bildName" ) );
                }
                else if (type.isAssignableFrom( WegobjektComposite.class )) {
                    result.add( new DefaultEntityFilter( layer, provider.getEntityType().getType(), repo ).exclude(
                            "name", "bildName" ) );
                }
                else if (type.isAssignableFrom( VermarkterComposite.class )) {
                    result.add( new DefaultEntityFilter( layer, provider.getEntityType().getType(), repo ) );
                }

                else if (type.isAssignableFrom( SchildComposite.class )) {
                    // SchildComposite prototype =
                    // repo.prototypeFor(SchildComposite.class);
                    // prototype.standort()()()()
                    DefaultEntityFilter filter = new DefaultEntityFilter( layer, provider.getEntityType().getType(),
                            repo ) {

                        @Override
                        protected String labelFor( String name ) {
                            return "befestigung".equals( name ) ? "Träger" : super.labelFor( name );

                        }
                    };

                    result.add( filter.exclude( "bildName" ) );
                }
                else {
                    result.add( new DefaultEntityFilter( layer, provider.getEntityType().getType(), repo ) );
                }
            }
        }
        return result;
    }
}
