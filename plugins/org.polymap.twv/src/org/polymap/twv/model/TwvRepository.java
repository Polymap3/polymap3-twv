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

import java.util.SortedMap;
import java.util.TreeMap;

import org.geotools.feature.NameImpl;

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
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.twv.model.data.AusweisungComposite;
import org.polymap.twv.model.data.EntfernungskontrolleComposite;
import org.polymap.twv.model.data.FoerderregionComposite;
import org.polymap.twv.model.data.KategorieComposite;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.model.data.PfeilrichtungComposite;
import org.polymap.twv.model.data.ProfilComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.UnterkategorieComposite;
import org.polymap.twv.model.data.VermarkterComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegbeschaffenheitComposite;
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

    //
    // public static final TwvRepository globalInstance() {
    // return Qi4jPlugin.Session..globalInstance().module( Anta2Repository.class );
    // }

    // instance *******************************************

    private IOperationSaveListener                            operationListener = new OperationSaveListener();

    /** Allow direct access for operations. */
    protected TwvService                                      twvService;

    private ServiceReference<SchildNummerGeneratorService>    schildNummer;

    private ServiceReference<WegobjektNummerGeneratorService> wegobjektNummer;


    // public static class SimpleEntityProvider<T extends Entity>
    // extends TwvEntityProvider<T> {
    //
    // public SimpleEntityProvider( QiModule repo, Class<T> entityClass, Name
    // entityName ) {
    // super( repo, entityClass, entityName );
    // }
    // };

    public TwvRepository( final QiModuleAssembler assembler ) {
        super( assembler );
        log.debug( "Initializing Twv module..." );

        if (Polymap.getSessionDisplay() != null) {
            OperationSupport.instance().addOperationSaveListener( operationListener );
        }
        schildNummer = assembler.getModule().serviceFinder().findService( SchildNummerGeneratorService.class );
        wegobjektNummer = assembler.getModule().serviceFinder().findService( WegobjektNummerGeneratorService.class );
    }


    public void init( final Session session ) {
        try {
            twvService = new TwvService( new WegEntityProvider( this, new NameImpl( TwvRepository.NAMESPACE, "Weg" ) ),
                    new SchildEntityProvider( this, new NameImpl( TwvRepository.NAMESPACE, "Schild" ) ),
                    new TwvEntityProvider<AusweisungComposite>( this, AusweisungComposite.class, new NameImpl(
                            TwvRepository.NAMESPACE, "Ausweisung" ) ), new TwvEntityProvider<MarkierungComposite>(
                            this, MarkierungComposite.class, new NameImpl( TwvRepository.NAMESPACE, "Markierung" ) ),
                    new TwvEntityProvider<SchildartComposite>( this, SchildartComposite.class, new NameImpl(
                            TwvRepository.NAMESPACE, "Schildart" ) ),
                    new TwvEntityProvider<EntfernungskontrolleComposite>( this, EntfernungskontrolleComposite.class,
                            new NameImpl( TwvRepository.NAMESPACE, "Entfernungskontrolle" ) ),
                    new TwvEntityProvider<SchildComposite>( this, SchildComposite.class, new NameImpl(
                            TwvRepository.NAMESPACE, SchildComposite.NAME ) ), new TwvEntityProvider<SchildmaterialComposite>(
                            this, SchildmaterialComposite.class, new NameImpl( TwvRepository.NAMESPACE,
                                    "Schildmaterial" ) ), new TwvEntityProvider<VermarkterComposite>( this,
                            VermarkterComposite.class, new NameImpl( TwvRepository.NAMESPACE, "Vermarkter" ) ),
                    new TwvEntityProvider<WegbeschaffenheitComposite>( this, WegbeschaffenheitComposite.class,
                            new NameImpl( TwvRepository.NAMESPACE, "Wegbeschaffenheit" ) ),
                    new WegobjektEntityProvider( this, new NameImpl( TwvRepository.NAMESPACE, "Wegobjekt" ) ),
                    new TwvEntityProvider<WegobjektNameComposite>( this, WegobjektNameComposite.class, new NameImpl(
                            TwvRepository.NAMESPACE, "Wegobjektname" ) ),
                    new TwvEntityProvider<FoerderregionComposite>( this, FoerderregionComposite.class, new NameImpl(
                            TwvRepository.NAMESPACE, "Förderregion" ) ), new TwvEntityProvider<ProfilComposite>( this,
                            ProfilComposite.class, new NameImpl( TwvRepository.NAMESPACE, "Profil" ) ),
                    new TwvEntityProvider<PfeilrichtungComposite>( this, PfeilrichtungComposite.class, new NameImpl(
                            TwvRepository.NAMESPACE, "Pfeilrichtung" ) ), new TwvEntityProvider<KategorieComposite>(
                            this, KategorieComposite.class, new NameImpl( TwvRepository.NAMESPACE, "Kategorie" ) ),
                    new TwvEntityProvider<UnterkategorieComposite>( this, UnterkategorieComposite.class, new NameImpl(
                            TwvRepository.NAMESPACE, "Unterkategorie" ) ), new TwvEntityProvider<WidmungComposite>(
                            this, WidmungComposite.class, new NameImpl( TwvRepository.NAMESPACE, "Widmung" ) ) );
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


    public <T> Query<T> findEntities( Class<T> compositeType, BooleanExpression expression, int firstResult,
            int maxResults ) {
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


    public Integer nextSchildNummer() {
        return schildNummer.get().generate();
    }


    public Integer nextWegobjektNummer() {
        return wegobjektNummer.get().generate();
    }


    public <T extends Named> SortedMap<String, T> entitiesWithNames( Class<T> entityClass ) {

        Query<T> entities = findEntities( entityClass, null, 0, 1000 );
        SortedMap<String, T> names = new TreeMap<String, T>();
        for (T entity : entities) {
            try {
                String key = entity.name().get();
                if (key == null) {
                    key = "";
                }
                names.put( key, entity );
            }
            catch (Exception e) {
                throw new IllegalStateException( "Exception on name() on entity " + entity, e );
            }
        }
        return names;
    }


    @Override
    public void removeEntity( Entity entity ) {
        if (entity instanceof WegComposite) {
            WegComposite.Mixin.beforeRemove( (WegComposite)entity );
        }
        else if (entity instanceof VermarkterComposite) {
            VermarkterComposite vermarkter = (VermarkterComposite)entity;
            for (WegComposite weg : findEntities( WegComposite.class, null, 0, 10000 )) {
                if (weg.vermarkter().contains( vermarkter )) {
                    weg.vermarkter().remove( vermarkter );
                }
            }
        }
        super.removeEntity( entity );
    }


    public SchildComposite prototypeFor( Class<SchildComposite> clazz ) {
        return uow.newEntityBuilder( clazz ).instance();
    }
}
