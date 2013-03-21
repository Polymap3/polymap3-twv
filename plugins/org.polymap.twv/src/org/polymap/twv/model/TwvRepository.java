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
package org.polymap.twv.model;

import java.util.Map;
import java.util.TreeMap;

import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.catalog.model.CatalogRepository;
import org.polymap.core.model.CompletionException;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.rhei.data.entityfeature.EntityProvider.FidsQueryProvider;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;
import org.polymap.rhei.data.entitystore.lucene.LuceneQueryProvider;

import org.polymap.twv.model.data.AusweisungComposite;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.VermarkterComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegbeschaffenheitComposite;
import org.polymap.twv.model.data.WegobjektComposite;
import org.polymap.twv.model.data.WegobjektNameComposite;
import org.polymap.twv.model.data.WidmungComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class TwvRepository
        extends QiModule {

    private static Log         log       = LogFactory.getLog( TwvRepository.class );

    public static final String NAMESPACE = "http://polymap.org/twv";


    /**
     * Get or create the repository for the current user session.
     */
    public static final TwvRepository instance() {
        return Qi4jPlugin.Session.instance().module( TwvRepository.class );
    }

    // instance *******************************************

    private IOperationSaveListener operationListener = new OperationSaveListener();

    /** Allow direct access for operations. */
    protected TwvService           twvService;


    public static class SimpleEntityProvider<T extends Entity>
            extends TwvEntityProvider<T> {

        public SimpleEntityProvider( QiModule repo, Class<T> entityClass, Name entityName,
                FidsQueryProvider queryProvider ) {
            super( repo, entityClass, entityName, queryProvider );
        }
    };


    public TwvRepository( final QiModuleAssembler assembler ) {
        super( assembler );
        log.debug( "Initializing Twv module..." );

        if (Polymap.getSessionDisplay() != null) {
            OperationSupport.instance().addOperationSaveListener( operationListener );
        }
    }


    public void init( final Session session ) {
        try {
            // build the queryProvider
            ServiceReference<LuceneEntityStoreService> storeService = assembler.getModule()
                    .serviceFinder().findService( LuceneEntityStoreService.class );
            LuceneEntityStoreService luceneStore = storeService.get();
            FidsQueryProvider queryProvider = new LuceneQueryProvider( luceneStore.getStore() );

            twvService = new TwvService(
                    new SimpleEntityProvider<AusweisungComposite>( this, AusweisungComposite.class,
                            new NameImpl( TwvRepository.NAMESPACE, "Ausweisung" ), queryProvider ),
                    new SimpleEntityProvider<MarkierungComposite>( this, MarkierungComposite.class,
                            new NameImpl( TwvRepository.NAMESPACE, "Markierung" ), queryProvider ),
                    new SimpleEntityProvider<SchildComposite>( this, SchildComposite.class,
                            new NameImpl( TwvRepository.NAMESPACE, "Schildart" ), queryProvider ),
                    new SimpleEntityProvider<SchildComposite>( this, SchildComposite.class,
                            new NameImpl( TwvRepository.NAMESPACE, "Schild" ), queryProvider ),
                    new SimpleEntityProvider<SchildmaterialComposite>( this,
                            SchildmaterialComposite.class, new NameImpl( TwvRepository.NAMESPACE,
                                    "Schildmaterial" ), queryProvider ),
                    new SimpleEntityProvider<VermarkterComposite>( this, VermarkterComposite.class,
                            new NameImpl( TwvRepository.NAMESPACE, "Vermarkter" ), queryProvider ),
                    new SimpleEntityProvider<WegbeschaffenheitComposite>( this,
                            WegbeschaffenheitComposite.class, new NameImpl(
                                    TwvRepository.NAMESPACE, "Wegbeschaffenheit" ), queryProvider ),
                    new SimpleEntityProvider<WegComposite>( this, WegComposite.class, new NameImpl(
                            TwvRepository.NAMESPACE, "Weg" ), queryProvider ),
                    new SimpleEntityProvider<WegobjektComposite>( this, WegobjektComposite.class,
                            new NameImpl( TwvRepository.NAMESPACE, "Wegobjekt" ), queryProvider ),
                    new SimpleEntityProvider<WegobjektNameComposite>( this,
                            WegobjektNameComposite.class, new NameImpl( TwvRepository.NAMESPACE,
                                    "Wegobjektname" ), queryProvider ),
                    new SimpleEntityProvider<WidmungComposite>( this, WidmungComposite.class,
                            new NameImpl( TwvRepository.NAMESPACE, "Widmung" ), queryProvider ) );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        CatalogRepository catalogRepo = session.module( CatalogRepository.class );
        catalogRepo.getCatalog().addTransient( twvService );
    }


    @Override
    protected void dispose() {
        if (operationListener != null) {
            OperationSupport.instance().removeOperationSaveListener( operationListener );
            operationListener = null;
        }
        if (twvService != null) {
            twvService.dispose( new NullProgressMonitor() );
        }
    }


    public <T> Query<T> findEntities( Class<T> compositeType, BooleanExpression expression,
            int firstResult, int maxResults ) {
        // Lucene does not like Integer.MAX_VALUE!?
        maxResults = Math.min( maxResults, 1000000 );
        return super.findEntities( compositeType, expression, firstResult, maxResults );
    }


    public void applyChanges()
            throws ConcurrentModificationException, CompletionException {
        try {
            uow.apply();
        }
        catch (ConcurrentEntityModificationException e) {
            throw new ConcurrentModificationException( e );
        }
        catch (UnitOfWorkCompletionException e) {
            throw new CompletionException( e );
        }
    }


    public <T extends Named> Map<String, T> entitiesWithNames( Class<T> entityClass ) {

        Query<T> entities = findEntities( entityClass, null, 0, 1000 );
        Map<String, T> names = new TreeMap<String, T>();
        for (T entity : entities) {
            try {
                String key = (String)entity.name().get();
                names.put( key, entity );
            }
            catch (Exception e) {
                throw new IllegalStateException( "Exception on name() on entity " + entity, e );
            }
        }
        return names;
    }

    public <T extends Named> T newNamedEntity( final Class<T> type, final String name )
            throws Exception  {
        return newEntity( type, null, new EntityCreator<T>() {

            @Override
            public void create( T prototype ) {
                prototype.name().set( name );
            }
        } );
    }
}