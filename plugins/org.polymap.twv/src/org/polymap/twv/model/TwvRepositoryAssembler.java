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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.idgen.HRIdentityGeneratorService;
import org.polymap.core.runtime.Polymap;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreInfo;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreQueryService;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;

import org.polymap.twv.TwvPlugin;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class TwvRepositoryAssembler
        extends QiModuleAssembler {

    /** TWV_MODULE */
    private static final String TWV_MODULE = "twv-module";

    private static Log        log = LogFactory.getLog( TwvRepositoryAssembler.class );

    private Application       app;

    private UnitOfWorkFactory uowf;

    private Module            module;


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
        domainModule.addEntities( KaufvertragComposite.class, VertragsArtComposite.class,
                KaeuferKreisComposite.class, StalaComposite.class, GemeindeComposite.class,
                GebaeudeArtComposite.class, NutzungComposite.class, StrasseComposite.class, BodennutzungComposite.class, FlurComposite.class, GemarkungComposite.class
                );
        // domainModule.addTransients(
        // PflanzeComposite.class,
        // TierComposite.class
        // );
        // domainModule.addValues(
        // AktivitaetValue.class,
        // BiotoptypValue.class,
        // PflanzeValue.class,
        // PilzValue.class,
        // TierValue.class,
        // GefahrValue.class,
        // StoerungValue.class,
        // WertValue.class
        // );

        // persistence: workspace/Lucene
        File root = new File( Polymap.getWorkspacePath().toFile(), "data" );

        File moduleRoot = new File( root, TwvPlugin.PLUGIN_ID );
        moduleRoot.mkdir();

        domainModule.addServices( LuceneEntityStoreService.class )
                .setMetaInfo( new LuceneEntityStoreInfo( moduleRoot ) ).instantiateOnStartup()
                .identifiedBy( "lucene-repository" );

        // indexer
        domainModule.addServices( LuceneEntityStoreQueryService.class )
        // .visibleIn( indexingVisibility )
        // .setMetaInfo( namedQueries )
                .instantiateOnStartup();

        domainModule.addServices( HRIdentityGeneratorService.class );

        // additional services
        // domainModule.addServices( BiotopnummerGeneratorService.class )
        // .identifiedBy( "biotopnummer" );
    }


    public void createInitData()
            throws Exception {
    }

}
