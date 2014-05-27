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
package org.polymap.twv.ui.form;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;

import com.google.common.collect.Iterables;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Layers;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.ManyAssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.field.UploadFormField;
import org.polymap.rhei.field.UploadFormField.UploadedImage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegobjektComposite;
import org.polymap.twv.model.data.WegobjektNameComposite;
import org.polymap.twv.ui.ImageViewer;
import org.polymap.twv.ui.TwvDefaultFormEditorPage;
import org.polymap.twv.ui.rhei.ImageValuePropertyAdapter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegobjektFormEditorPage
        extends TwvDefaultFormEditorPage {

    private IFormFieldListener uploadListener;


    public WegobjektFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegobjektFormEditorPage.class.getName(), "Wegobjekt", feature, featureStore );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        final WegobjektComposite wegobjekt = twvRepository.findEntity( WegobjektComposite.class, feature
                .getIdentifier().getID() );
        site.setEditorTitle( formattedTitle( "Wegobjekt", wegobjekt.name().get(), null ) );
        site.setFormTitle( formattedTitle( "Wegobjekt", wegobjekt.name().get(), getTitle() ) );
        
        Composite parent = site.getPageBody();
        Composite line0 = newFormField( "Nummer" ).setProperty( new PropertyAdapter( wegobjekt.laufendeNr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale(), 12, 0 ) )
                .setEnabled( false ).setLayoutData( left().create() ).setToolTipText( "eindeutige Wegobjektnummer" )
                .create();

        Composite line1 = newFormField( "Wegobjektname" )
                .setParent( parent )
                .setProperty(
                        new AssociationAdapter<WegobjektNameComposite>( wegobjekt
                                .wegobjektName() ) )
                .setField( namedAssocationsPicklist( WegobjektNameComposite.class, true ) )
                .setLayoutData( left().top( line0 ).create() ).create();

        Composite line2 = newFormField( "Beschreibung" ).setParent( parent )
                .setProperty( new PropertyAdapter( wegobjekt.beschreibung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().top( line1 ).height( 80 ).right( RIGHT ).create() )
                .setToolTipText( "Beschreibung des Wegobjektes" ).create();

        Composite line3 = newFormField( "Wege" ).setParent( parent )
                .setProperty( new ManyAssociationAdapter<WegComposite>( wegobjekt.wege() ) )
                .setValidator( new NotNullValidator() )
                .setField( namedAssocationsSelectlist( WegComposite.class, true ) )
                .setLayoutData( left().top( line2 ).height( 120 ).create() ).create();
        
        // Gemeinden
        final StringBuilder buf = new StringBuilder( 256 );
        try {
            IMap map = ((PipelineFeatureSource)fs).getLayer().getMap();
            ILayer layer = Iterables
                    .getOnlyElement( Iterables.filter( map.getLayers(), Layers.hasLabel( "Gemeinden" ) ) );

            fs = PipelineFeatureSource.forLayer( layer, false );
            FeatureCollection gemeinden = fs.getFeatures( DataPlugin.ff.intersects(
                    DataPlugin.ff.property( fs.getSchema().getGeometryDescriptor().getLocalName() ),
                    DataPlugin.ff.literal( wegobjekt.geom().get() ) ) );
            gemeinden.accepts( new FeatureVisitor() {

                public void visit( Feature gemeinde ) {
                    buf.append( buf.length() > 0 ? ", " : "" );
                    Property nameProp = gemeinde.getProperty( "ORTSNAME" );
                    buf.append( nameProp != null ? nameProp.getValue().toString() : "-" );
                }
            }, null );
        }
        catch (Exception e) {
            e.printStackTrace();
            buf.append( "-konnten nicht ermittelt werden- (" + e.getLocalizedMessage() + ")" );
        }
        Composite line5a = newFormField( "Kommune" ).setEnabled( false ).setField( new StringFormField() )
                .setLayoutData( right().top( line2 ).create() ).setProperty( new PropertyAdapter( wegobjekt.geom() ) {

                    public Object getValue() {
                        return buf.toString();
                    }
                } ).create();

        Composite line5b = newFormField( "Bedarf" ).setToolTipText( "fehlende bzw. zu errichtende Schilder und Objekte" ).setProperty( new PropertyAdapter( wegobjekt.bedarf() ) )
                .setField( new CheckboxFormField() ).setLayoutData( left().top( line3 ).create() ).create();
        
        Composite line4 = newFormField( "Bild" ).setParent( parent )
                .setProperty( new ImageValuePropertyAdapter( "bild", wegobjekt.bild() ) )
                .setField( new UploadFormField( TwvPlugin.getImagesRoot(), false ) )
                .setLayoutData( left().top( line5b ).create() ).create();

        final ImageViewer imagePreview = new ImageViewer( site.getPageBody(), left().top( line4 )
                .height( 250 ).width( 250 ).create(), (wegobjekt.laufendeNr().get() != null ? wegobjekt.laufendeNr().get() : "neu") + "" );

        if (wegobjekt.bild().get().thumbnailFileName().get() != null) {
            imagePreview.setImage( ImageValuePropertyAdapter.convertToUploadedImage( wegobjekt.bild()
                    .get() ) );
        }

        Composite line5 = newFormField( "Detailbild" ).setParent( parent )
                .setProperty( new ImageValuePropertyAdapter( "detailBild", wegobjekt.detailBild() ) )
                .setField( new UploadFormField( TwvPlugin.getImagesRoot(), false ) )
                .setLayoutData( right().top( line5b ).create() ).create();

        final ImageViewer imagePreview2 = new ImageViewer( site.getPageBody(), right().top( line5 )
                .height( 250 ).width( 250 ).create(), (wegobjekt.laufendeNr().get() != null ? wegobjekt.laufendeNr().get() : "neu") + "_detail" );

        if (wegobjekt.detailBild().get().thumbnailFileName().get() != null) {
            imagePreview2.setImage( ImageValuePropertyAdapter.convertToUploadedImage( wegobjekt.detailBild()
                    .get() ) );
        }

        site.addFieldListener( uploadListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getNewValue() != null && wegobjekt.bild().qualifiedName().name().equals( ev.getFieldName() )) {
                    UploadedImage uploadedImage = (UploadedImage)ev.getNewValue();
                    imagePreview.setImage( uploadedImage );
                }
                if (ev.getNewValue() != null && wegobjekt.detailBild().qualifiedName().name().equals( ev.getFieldName() )) {
                    UploadedImage uploadedImage = (UploadedImage)ev.getNewValue();
                    imagePreview2.setImage( uploadedImage );
                }
            }
        } );
    }
}