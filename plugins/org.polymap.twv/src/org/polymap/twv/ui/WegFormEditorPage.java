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

import java.util.SortedMap;
import java.util.TreeMap;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Layers;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.field.TextFormFieldWithSuggestions;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.data.AusweisungComposite;
import org.polymap.twv.model.data.KategorieComposite;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.model.data.PrioritaetComposite;
import org.polymap.twv.model.data.UnterkategorieComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegbeschaffenheitComposite;
import org.polymap.twv.model.data.WidmungComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegFormEditorPage
        extends TwvDefaultFormEditorPage {

    private static Log         log               = LogFactory.getLog( WegFormEditorPage.class );

    private final WegComposite weg;

    private KategorieComposite selectedKategorie = null;

    private IFormFieldListener kategorieSelectionListener;


    public WegFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegFormEditorPage.class.getName(), "Basisdaten", feature, featureStore );

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( formattedTitle( "Weg", weg.name().get(), null ) );
        site.setFormTitle( formattedTitle( "Tourismusweg", weg.name().get(), getTitle() ) );

        Composite line1 = newFormField( "Name" ).setProperty( new PropertyAdapter( weg.name() ) )
                .setValidator( new NotNullValidator() ).setField( new StringFormField() )
                .setLayoutData( left().right( 100 ).create() ).create();

        Composite line2 = newFormField( "Kategorie" )
                .setProperty(
                        new AssociationAdapter<KategorieComposite>( "kategorie", weg.kategorie() ) )
                .setField( namedAssocationsPicklist( KategorieComposite.class ) )
                .setLayoutData( left().top( line1 ).create() ).create();
        selectedKategorie = weg.kategorie().get();

        final PicklistFormField unterkategorieList = new PicklistFormField(
                new PicklistFormField.ValueProvider() {

                    @Override
                    public SortedMap<String, Object> get() {
                        SortedMap<String, Object> unterkategories = new TreeMap<String, Object>();
                        if (selectedKategorie != null) {
                            int unterkategorieCount = selectedKategorie.unterkategories().count();
                            for (int i = 0; i < unterkategorieCount; i++) {
                                UnterkategorieComposite uk = selectedKategorie.unterkategories()
                                        .get( i );
                                unterkategories.put( uk.name().get(), uk );
                            }
                        }
                        return unterkategories;
                    }
                } );
        newFormField( "Unterkategorie" )
                .setProperty(
                        new AssociationAdapter<UnterkategorieComposite>( "unterkategorie", weg
                                .unterkategorie() ) ).setField( unterkategorieList )
                .setLayoutData( right().top( line1 ).create() ).create();

        Composite line3 = newFormField( "Ausweisung" )
                .setProperty(
                        new AssociationAdapter<AusweisungComposite>( "ausweisung", weg.ausweisung() ) )
                .setField( namedAssocationsPicklist( AusweisungComposite.class ) )
                .setLayoutData( left().top( line2 ).create() ).create();

        Composite line4 = newFormField( "Priorität" )
                .setProperty(
                        new AssociationAdapter<PrioritaetComposite>( "prioritaet", weg.prioritaet() ) )
                .setField( namedAssocationsPicklist( PrioritaetComposite.class ) )
                .setLayoutData( right().top( line2 ).create() ).create();

        // Gemeinden
        final StringBuilder buf = new StringBuilder( 256 );
        try {
            IMap map = ((PipelineFeatureSource)fs).getLayer().getMap();
            ILayer layer = Iterables.getOnlyElement( Iterables.filter( map.getLayers(),
                    Layers.hasLabel( "Gemeinden" ) ) );

            fs = PipelineFeatureSource.forLayer( layer, false );
            FeatureCollection gemeinden = fs
                    .getFeatures( DataPlugin.ff.intersects(
                            DataPlugin.ff.property( fs.getSchema().getGeometryDescriptor()
                                    .getLocalName() ), DataPlugin.ff.literal( weg.geom().get() ) ) );
            gemeinden.accepts( new FeatureVisitor() {

                public void visit( Feature gemeinde ) {
                    buf.append( buf.length() > 0 ? ", " : "" );
                    Property nameProp = gemeinde.getProperty( "ORTSNAME" );
                    buf.append( nameProp != null ? nameProp.getValue().toString() : "-" );
                }
            }, null );
            log.info( "Kommunen: " + buf.toString() );
        }
        catch (Exception e) {
            log.warn( "", e );
            buf.append( "-konnten nicht ermittelt werden- (" + e.getLocalizedMessage() + ")" );
        }
        Composite line5 = newFormField( "Kommunen" ).setEnabled( false )
                .setField( new StringFormField() ).setLayoutData( right().top( line4 ).create() )
                .setProperty( new PropertyAdapter( weg.geom() ) {

                    public Object getValue() {
                        return buf.toString();
                    }
                } ).create();

        // TODO falko laengeImLandkreis wird über Berechnung eingeblendet

        // Composite line4 = newFormField( "Länge Landkreis" )
        // .setProperty( new PropertyAdapter( weg.laengeImLandkreis() ) )
        // .setField( new StringFormField() ).setLayoutData( left().top( line3
        // ).create() )
        // .setToolTipText( "Länge im Landkreis Mittelsachsen" ).create();

        Composite line6 = newFormField( "Gesamtlänge" )
                .setProperty( new PropertyAdapter( weg.laengeUeberregional() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( line5 ).create() )
                .setToolTipText( "Überregionale Gesamtlänge" ).create();

        Composite line7 = newFormField( "Wegbeschreibung" )
                .setProperty( new PropertyAdapter( weg.beschreibung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( 100 ).height( 50 ).top( line6 ).create() ).create();

        Composite line8 = newFormField( "Wegbeschaffenheit" )
                .setProperty( new PropertyAdapter( weg.beschaffenheit() ) )
                .setField(
                        new TextFormFieldWithSuggestions( twvRepository.entitiesWithNames(
                                WegbeschaffenheitComposite.class ).keySet() ) )
                .setLayoutData( left().right( 100 ).top( line7 ).create() ).create();

        Composite line9 = newFormField( "Widmung" )
                .setProperty( new AssociationAdapter<WidmungComposite>( "widmung", weg.widmung() ) )
                .setField( namedAssocationsPicklist( WidmungComposite.class ) )
                .setLayoutData( left().top( line8 ).create() ).create();

        newFormField( "Markierung" )
                .setProperty(
                        new AssociationAdapter<MarkierungComposite>( "markierung", weg.markierung() ) )
                .setField( namedAssocationsPicklist( MarkierungComposite.class ) )
                .setLayoutData( right().top( line8 ).create() ).create();

        site.addFieldListener( kategorieSelectionListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE
                        && ev.getFieldName().equalsIgnoreCase( "kategorie" )) {
                    if ((ev.getNewValue() == null && selectedKategorie != null)
                            || !ev.getNewValue().equals( selectedKategorie )) {
                        selectedKategorie = ev.getNewValue();
                        unterkategorieList.reloadValues();
                    }
                }
            }
        } );
    }
}
