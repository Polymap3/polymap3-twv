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
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.idgen.HRIdentityGeneratorService;
import org.polymap.core.runtime.Polymap;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreInfo;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreQueryService;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;

import org.polymap.twv.TwvPlugin;
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
        domainModule.addEntities( AusweisungComposite.class, MarkierungComposite.class,
                SchildartComposite.class, SchildComposite.class, SchildmaterialComposite.class,
                VermarkterComposite.class, WegbeschaffenheitComposite.class, WegComposite.class,
                WegobjektComposite.class, WegobjektNameComposite.class, WidmungComposite.class );

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
    }


    public void createInitData()
            throws Exception {
//        // create the composites
//        TwvRepository twv = TwvRepository.instance();
//
//        // Ausweisung
//        twv.newNamedEntity( AusweisungComposite.class, "Pilgerweg" );
//        twv.newNamedEntity( AusweisungComposite.class, "Sportwanderweg" );
//        twv.newNamedEntity( AusweisungComposite.class, "Themenwanderweg" );
//        twv.newNamedEntity( AusweisungComposite.class, "Nordic-Walking" );
//        twv.newNamedEntity( AusweisungComposite.class, "Inlineskating" );
//        twv.newNamedEntity( AusweisungComposite.class, "Mountainbike-Strecke" );
//        twv.newNamedEntity( AusweisungComposite.class, "Themenradweg" );
//
//        // Markierung
//        twv.newNamedEntity( MarkierungComposite.class, "Blau-Strich" );
//        twv.newNamedEntity( MarkierungComposite.class, "Rot-Strich" );
//        twv.newNamedEntity( MarkierungComposite.class, "Gelb-Strich" );
//        twv.newNamedEntity( MarkierungComposite.class, "Grün-Strich" );
//        twv.newNamedEntity( MarkierungComposite.class, "Blau-Punkt" );
//        twv.newNamedEntity( MarkierungComposite.class, "Rot-Punkt" );
//        twv.newNamedEntity( MarkierungComposite.class, "Gelb-Punkt" );
//        twv.newNamedEntity( MarkierungComposite.class, "Grün-Punkt" );
//        twv.newNamedEntity( MarkierungComposite.class, "Grün - diagonal (Lehrpfad)" );
//        twv.newNamedEntity( MarkierungComposite.class, "individuelles Logo" );
//        twv.newNamedEntity( MarkierungComposite.class, "Fahrradsymbol grün" );
//        twv.newNamedEntity( MarkierungComposite.class, "individuelles Logo" );
//        twv.newNamedEntity( MarkierungComposite.class, "Pferdekopf Fern" );
//        twv.newNamedEntity( MarkierungComposite.class, "individuelles Logo" );
//        twv.newNamedEntity( MarkierungComposite.class, "Pferdekopf Regioinal" );
//        twv.newNamedEntity( MarkierungComposite.class, "Pferdekopf Lokal" );
//
//        // Wegbeschaffenheit
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "naturnah" );
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "fein" );
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "schlecht" );
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "Verbund/sandgeschlämmt" );
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "Asphalt" );
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "Ökopflaster" );
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "Entfernungsangabe von bis" );
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "straßenbegleitend" );
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "öffentliche Straßen" );
//        twv.newNamedEntity( WegbeschaffenheitComposite.class, "Waldweg" );
//
//        // Widmung
//        twv.newNamedEntity( WidmungComposite.class, "öffentlich gewidmet auf Basis Eigentum" );
//        twv.newNamedEntity( WidmungComposite.class,
//                "öffentlich gewidmet auf Basis Gestattungsvertrag" );
//        twv.newNamedEntity( WidmungComposite.class, "nicht öffentlich gewidmet" );
//
//        // Wegobjektname
//        twv.newNamedEntity( WegobjektNameComposite.class, "Überblickskarte/Informationstafel" );
//        twv.newNamedEntity( WegobjektNameComposite.class, "Thementafel" );
//        twv.newNamedEntity( WegobjektNameComposite.class, "Aussichtstafel" );
//        twv.newNamedEntity( WegobjektNameComposite.class, "Bank/Sitzgruppe/Rastplatz" );
//        twv.newNamedEntity( WegobjektNameComposite.class, "Unterstand/Schutzhütte" );
//        twv.newNamedEntity( WegobjektNameComposite.class, "Papierkorb" );
//        twv.newNamedEntity( WegobjektNameComposite.class, "Skulptur/ Denkmal" );
//        twv.newNamedEntity( WegobjektNameComposite.class, "Sonstiges" );
//
//        // Schildart
//        twv.newNamedEntity( SchildartComposite.class, "Wegweiser lang" );
//        twv.newNamedEntity( SchildartComposite.class, "Tabellenwegweiser" );
//        twv.newNamedEntity( SchildartComposite.class, "Pfeilwegweiser" );
//        twv.newNamedEntity( SchildartComposite.class, "Zwischenwegweiser" );
//        twv.newNamedEntity( SchildartComposite.class, "Ortseingangsschilder" );
//        twv.newNamedEntity( SchildartComposite.class, "Vorwegweiser" );
//        twv.newNamedEntity( SchildartComposite.class, "Hauptwegweiser" );
//        twv.newNamedEntity( SchildartComposite.class, "Wegmarke/Richtungszeichen" );
//
//        // Schildmaterial
//        twv.newNamedEntity( SchildmaterialComposite.class, "Holz" );
//        twv.newNamedEntity( SchildmaterialComposite.class, "Aludibond" );
//        twv.newNamedEntity( SchildmaterialComposite.class, "PVC-Hartschaum" );
//        twv.newNamedEntity( SchildmaterialComposite.class, "PVC-Hartschaum" );
//        twv.newNamedEntity( SchildmaterialComposite.class, "sonstige" );
    }
}
