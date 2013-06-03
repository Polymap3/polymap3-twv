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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.idgen.HRIdentityGeneratorService;

import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreInfo;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreQueryService;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.NamedCreatorCallback.Impl;
import org.polymap.twv.model.data.AusweisungComposite;
import org.polymap.twv.model.data.EntfernungskontrolleComposite;
import org.polymap.twv.model.data.FoerderregionComposite;
import org.polymap.twv.model.data.ImageValue;
import org.polymap.twv.model.data.KategorieComposite;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.model.data.PfeilrichtungComposite;
import org.polymap.twv.model.data.PrioritaetComposite;
import org.polymap.twv.model.data.ProfilComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.UnterkategorieComposite;
import org.polymap.twv.model.data.VermarkterComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegbeschaffenheitComposite;
import org.polymap.twv.model.data.WegobjektComposite;
import org.polymap.twv.model.data.WegobjektNameComposite;
import org.polymap.twv.model.data.WidmungComposite;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class TwvRepositoryAssembler
        extends QiModuleAssembler {

    /** TWV_MODULE */
    private static final String TWV_MODULE = "twv-module";

    private static Log          log        = LogFactory.getLog( TwvRepositoryAssembler.class );

    private Application         app;

    private UnitOfWorkFactory   uowf;

    private Module              module;


    public TwvRepositoryAssembler() {
    }


    public Module getModule() {
        return module;
    }


    protected void setApp( Application app ) {
        this.app = app;
        this.module = app.findModule( "application-layer", TWV_MODULE );
        this.uowf = module.unitOfWorkFactory();
    }


    public QiModule newModule() {
        return new TwvRepository( this );
    }


    public void assemble( ApplicationAssembly _app )
            throws Exception {
        log.info( "Assembling: org.polymap.twv ..." );

        // project layer / module
        LayerAssembly domainLayer = _app.layerAssembly( "application-layer" );
        ModuleAssembly domainModule = domainLayer.moduleAssembly( TWV_MODULE );
        domainModule.addEntities( AusweisungComposite.class, MarkierungComposite.class, SchildartComposite.class,
                SchildComposite.class, SchildmaterialComposite.class, VermarkterComposite.class,
                WegbeschaffenheitComposite.class, WegComposite.class, WegobjektComposite.class,
                WegobjektNameComposite.class, WidmungComposite.class, EntfernungskontrolleComposite.class,
                FoerderregionComposite.class, PfeilrichtungComposite.class, KategorieComposite.class,
                UnterkategorieComposite.class, PrioritaetComposite.class, ProfilComposite.class );

        // persistence: workspace/Lucene
        domainModule.addValues( ImageValue.class );

        domainModule.addServices( LuceneEntityStoreService.class )
                .setMetaInfo( new LuceneEntityStoreInfo( TwvPlugin.getModuleRoot() ) ).instantiateOnStartup()
                .identifiedBy( "lucene-repository" );

        // indexer
        domainModule.addServices( LuceneEntityStoreQueryService.class )
        // .visibleIn( indexingVisibility )
        // .setMetaInfo( namedQueries )
                .instantiateOnStartup();

        domainModule.addServices( HRIdentityGeneratorService.class, SchildNummerGeneratorService.class );

        // FIXME nötige Änderungen in Rhei fehlen noch
        // FilterFactory.instance().disableStandardFilter();
    }


    public void createInitData()
            throws Exception {

        // create the composites
        final UnitOfWork uow = uowf.newUnitOfWork();
        final Impl creator = new NamedCreatorCallback.Impl( uow );

        if (!isDBInitialized( uow )) {

            log.info( "Create Init Data" );
            EntfernungskontrolleComposite.Mixin.createInitData( creator );
            FoerderregionComposite.Mixin.createInitData( creator );
            PfeilrichtungComposite.Mixin.createInitData( creator );
            KategorieComposite.Mixin.createInitData( creator );
            PrioritaetComposite.Mixin.createInitData( creator );

            // Ausweisung
            creator.create( AusweisungComposite.class, "Pilgerweg" );
            creator.create( AusweisungComposite.class, "Sportwanderweg" );
            creator.create( AusweisungComposite.class, "Themenwanderweg" );
            creator.create( AusweisungComposite.class, "Nordic-Walking" );
            creator.create( AusweisungComposite.class, "Inlineskating" );
            creator.create( AusweisungComposite.class, "Mountainbike-Strecke" );
            creator.create( AusweisungComposite.class, "Themenradweg" );

            // Markierung
            creator.create( MarkierungComposite.class, "Blau-Strich" );
            creator.create( MarkierungComposite.class, "Rot-Strich" );
            creator.create( MarkierungComposite.class, "Gelb-Strich" );
            creator.create( MarkierungComposite.class, "Grün-Strich" );
            creator.create( MarkierungComposite.class, "Blau-Punkt" );
            creator.create( MarkierungComposite.class, "Rot-Punkt" );
            creator.create( MarkierungComposite.class, "Gelb-Punkt" );
            creator.create( MarkierungComposite.class, "Grün-Punkt" );
            creator.create( MarkierungComposite.class, "Grün - diagonal (Lehrpfad)" );
            creator.create( MarkierungComposite.class, "individuelles Logo" );
            creator.create( MarkierungComposite.class, "Fahrradsymbol grün" );
            creator.create( MarkierungComposite.class, "individuelles Logo" );
            creator.create( MarkierungComposite.class, "Pferdekopf Fern" );
            creator.create( MarkierungComposite.class, "individuelles Logo" );
            creator.create( MarkierungComposite.class, "Pferdekopf Regioinal" );
            creator.create( MarkierungComposite.class, "Pferdekopf Lokal" );

            // Wegbeschaffenheit
            creator.create( WegbeschaffenheitComposite.class, "naturnah" );
            creator.create( WegbeschaffenheitComposite.class, "fein" );
            creator.create( WegbeschaffenheitComposite.class, "schlecht" );
            creator.create( WegbeschaffenheitComposite.class, "Verbund/sandgeschlämmt" );
            creator.create( WegbeschaffenheitComposite.class, "Asphalt" );
            creator.create( WegbeschaffenheitComposite.class, "Ökopflaster" );
            creator.create( WegbeschaffenheitComposite.class, "Entfernungsangabe von bis" );
            creator.create( WegbeschaffenheitComposite.class, "straßenbegleitend" );
            creator.create( WegbeschaffenheitComposite.class, "öffentliche Straßen" );
            creator.create( WegbeschaffenheitComposite.class, "Waldweg" );

            // Widmung
            creator.create( WidmungComposite.class, "öffentlich gewidmet auf Basis Eigentum" );
            creator.create( WidmungComposite.class, "öffentlich gewidmet auf Basis Gestattungsvertrag" );
            creator.create( WidmungComposite.class, "nicht öffentlich gewidmet" );

            // Wegobjektname
            creator.create( WegobjektNameComposite.class, "Überblickskarte/Informationstafel" );
            creator.create( WegobjektNameComposite.class, "Thementafel" );
            creator.create( WegobjektNameComposite.class, "Aussichtstafel" );
            creator.create( WegobjektNameComposite.class, "Bank/Sitzgruppe/Rastplatz" );
            creator.create( WegobjektNameComposite.class, "Unterstand/Schutzhütte" );
            creator.create( WegobjektNameComposite.class, "Papierkorb" );
            creator.create( WegobjektNameComposite.class, "Skulptur/ Denkmal" );
            creator.create( WegobjektNameComposite.class, "Sonstiges" );

            // Schildart
            creator.create( SchildartComposite.class, "Wegweiser lang" );
            creator.create( SchildartComposite.class, "Tabellenwegweiser" );
            creator.create( SchildartComposite.class, "Pfeilwegweiser" );
            creator.create( SchildartComposite.class, "Zwischenwegweiser" );
            creator.create( SchildartComposite.class, "Ortseingangsschilder" );
            creator.create( SchildartComposite.class, "Vorwegweiser" );
            creator.create( SchildartComposite.class, "Hauptwegweiser" );
            creator.create( SchildartComposite.class, "Wegmarke/Richtungszeichen" );

            // Schildmaterial
            creator.create( SchildmaterialComposite.class, "Holz" );
            creator.create( SchildmaterialComposite.class, "Aludibond" );
            creator.create( SchildmaterialComposite.class, "PVC-Hartschaum" );
            creator.create( SchildmaterialComposite.class, "PVC-Hartschaum" );
            creator.create( SchildmaterialComposite.class, "sonstige" );

            log.info( "Create Init Data Completed" );
        }
        // next version
        if (!isDBInitializedV2( uow )) {
            log.info( "Create Init Data V2" );
            ProfilComposite.Mixin.createInitData( creator );

            log.info( "Create Init Data V2 completed" );
        }
        // next version
        migrateVermarkter( uow );
        fixIncorrectManyAssociations( uow );
        renumberSchilder( uow );

        uow.complete();
    }


    private void renumberSchilder( UnitOfWork uow ) {
        QueryBuilder<SchildComposite> builder = module.queryBuilderFactory().newQueryBuilder( SchildComposite.class );
        // SchildComposite template = QueryExpressions.templateFor(
        // SchildComposite.class );
        // builder = builder.where( QueryExpressions.eq( template.laufendeNr(), null
        // ) );
        Query<SchildComposite> query = builder.newQuery( uow ).maxResults( 10000 ).firstResult( 0 );

        SchildNummerGeneratorService schildNummer = (SchildNummerGeneratorService)module.serviceFinder()
                .findService( SchildNummerGeneratorService.class ).get();
        for (SchildComposite schild : query) {
            if (schild.laufendeNr().get() == null) {
                log.info( "Setting new number..." );
                schild.laufendeNr().set( schildNummer.generate() );
            }
        }
    }


    /**
     * 
     * @param uow
     * @return
     */
    private boolean isDBInitialized( UnitOfWork uow ) {
        QueryBuilder<AusweisungComposite> builder = getModule().queryBuilderFactory().newQueryBuilder(
                AusweisungComposite.class );
        Query<AusweisungComposite> query = builder.newQuery( uow ).maxResults( 1 ).firstResult( 0 );
        return query.iterator().hasNext();
    }


    private boolean isDBInitializedV2( UnitOfWork uow ) {
        QueryBuilder<ProfilComposite> builder = getModule().queryBuilderFactory().newQueryBuilder(
                ProfilComposite.class );
        Query<ProfilComposite> query = builder.newQuery( uow ).maxResults( 1 ).firstResult( 0 );
        return query.iterator().hasNext();
    }


    private void migrateVermarkter( UnitOfWork uow ) {
        log.info( "Migrate Vermarkter" );
        QueryBuilder<VermarkterComposite> builder = getModule().queryBuilderFactory().newQueryBuilder(
                VermarkterComposite.class );
        Query<VermarkterComposite> query = builder.newQuery( uow ).maxResults( 100000 ).firstResult( 0 );
        for (VermarkterComposite vermarkterComposite : query) {
            WegComposite wegComposite = vermarkterComposite.weg().get();
            if (wegComposite != null) {
                wegComposite.vermarkter().add( vermarkterComposite );
                vermarkterComposite.weg().set( null );
                // } else {
                // // abbrechen wenn der erste Vermarter ohne Weg gefunden wird, da
                // dann alle Vermarkter
                // // migriert sein müssten
                // return;
            }
        }
    }


    private void fixIncorrectManyAssociations( UnitOfWork uow )
            throws ConcurrentEntityModificationException, UnitOfWorkCompletionException {
        log.info( "Create Vermarkters to be removed later by hand" );
        EntityBuilder<VermarkterComposite> entityBuilder = uow.newEntityBuilder( VermarkterComposite.class,
                "VermarkterComposite-20130415-0942-0" );
        entityBuilder.instance().name().set( "_delete_me_" );
        entityBuilder.newInstance();
    }
}
