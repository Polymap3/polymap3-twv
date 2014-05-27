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
import org.polymap.twv.model.data.PfeilrichtungComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.ui.ImageViewer;
import org.polymap.twv.ui.TwvDefaultFormEditorPage;
import org.polymap.twv.ui.rhei.ImageValuePropertyAdapter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class SchildFormEditorPage
        extends TwvDefaultFormEditorPage {

    private IFormFieldListener uploadListener;


    public SchildFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( SchildFormEditorPage.class.getName(), "Schild", feature, featureStore );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        final SchildComposite schild = twvRepository
                .findEntity( SchildComposite.class, feature.getIdentifier().getID() );

        site.setEditorTitle( formattedTitle( "Schild", schild.laufendeNr().get(), null ) );
        site.setFormTitle( formattedTitle( "Schild", schild.laufendeNr().get(), getTitle() ) );

        Composite line0 = newFormField( "Nummer" ).setProperty( new PropertyAdapter( schild.laufendeNr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale(), 12, 0 ) )
                .setEnabled( false ).setLayoutData( left().create() ).setToolTipText( "eindeutige Schildnummer" )
                .create();

        newFormField( "Bestandsnr." ).setProperty( new PropertyAdapter( schild.bestandsNr() ) )
                .setField( new StringFormField() ).setLayoutData( right().create() )
                .setToolTipText( "Nummer des Schildes bei importierten Datenbeständen" ).create();

        Composite line1 = newFormField( "Schildart" )
                .setProperty( new AssociationAdapter<SchildartComposite>( schild.schildart() ) )
                .setField( namedAssocationsPicklist( SchildartComposite.class, true ) )
                .setLayoutData( left().top( line0 ).create() ).create();

        Composite line2 = newFormField( "Pfeilrichtung" )
                .setProperty( new AssociationAdapter<PfeilrichtungComposite>( schild.pfeilrichtung() ) )
                .setField( namedAssocationsPicklist( PfeilrichtungComposite.class ) )
                .setLayoutData( left().top( line1 ).create() ).create();

        newFormField( "Material" ).setProperty( new AssociationAdapter<SchildmaterialComposite>( schild.material() ) )
                .setField( namedAssocationsPicklist( SchildmaterialComposite.class ) )
                .setLayoutData( right().top( line1 ).create() ).create();

        Composite line3 = newFormField( "Beschriftung" ).setProperty( new PropertyAdapter( schild.beschriftung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().top( line2 ).height( 50 ).right( RIGHT ).create() )
                .setToolTipText( "Schildbeschriftung mit Entfernungsangabe und Zusatzinfo" ).create();

        Composite line4 = newFormField( "Träger" ).setProperty( new PropertyAdapter( schild.befestigung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().top( line3 ).height( 50 ).right( RIGHT ).create() ).create();

        Composite line41 = newFormField( "Standort" ).setProperty( new PropertyAdapter( schild.standort() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().top( line4 ).height( 50 ).right( RIGHT ).create() ).create();

        Composite line5 = newFormField( "Wege" )
                .setProperty( new ManyAssociationAdapter<WegComposite>( schild.wege() ) )
                .setValidator( new NotNullValidator() )
                .setField( namedAssocationsSelectlist( WegComposite.class, true ) )
                .setLayoutData( left().top( line41 ).height( 120 ).create() ).create();

        // Gemeinden
        final StringBuilder buf = new StringBuilder( 256 );
        try {
            IMap map = ((PipelineFeatureSource)fs).getLayer().getMap();
            ILayer layer = Iterables
                    .getOnlyElement( Iterables.filter( map.getLayers(), Layers.hasLabel( "Gemeinden" ) ) );

            fs = PipelineFeatureSource.forLayer( layer, false );
            FeatureCollection gemeinden = fs.getFeatures( DataPlugin.ff.intersects(
                    DataPlugin.ff.property( fs.getSchema().getGeometryDescriptor().getLocalName() ),
                    DataPlugin.ff.literal( schild.geom().get() ) ) );
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
                .setLayoutData( right().top( line41 ).create() ).setProperty( new PropertyAdapter( schild.geom() ) {

                    public Object getValue() {
                        return buf.toString();
                    }
                } ).create();

        Composite line5b = newFormField( "Bedarf" ).setToolTipText( "fehlende bzw. zu errichtende Schilder und Objekte" ).setProperty( new PropertyAdapter( schild.bedarf() ) )
                .setField( new CheckboxFormField() ).setLayoutData( left().top( line5 ).create() ).create();

        Composite line6 = newFormField( "Bild" ).setProperty( new ImageValuePropertyAdapter( "bild", schild.bild() ) )
                .setField( new UploadFormField( TwvPlugin.getImagesRoot(), false ) )
                .setLayoutData( left().top( line5b ).create() ).create();

        final ImageViewer viewer = new ImageViewer( site.getPageBody(), left().top( line6 ).height( 250 ).width( 250 )
                .create(), (schild.laufendeNr().get() != null ? schild.laufendeNr().get() : "neu") + "" );

        if (schild.bild().get().thumbnailFileName().get() != null) {
            viewer.setImage( ImageValuePropertyAdapter.convertToUploadedImage( schild.bild().get() ) );
        }

        Composite line6a = newFormField( "Detailbild" )
                .setProperty( new ImageValuePropertyAdapter( "detailBild", schild.detailBild() ) )
                .setField( new UploadFormField( TwvPlugin.getImagesRoot(), false ) )
                .setLayoutData( right().top( line5b ).create() ).create();

        final ImageViewer imagePreview2 = new ImageViewer( site.getPageBody(), right().top( line6a ).height( 250 )
                .width( 250 ).create(), (schild.laufendeNr().get() != null ? schild.laufendeNr().get() : "neu")
                + "_detail" );

        if (schild.detailBild().get().thumbnailFileName().get() != null) {
            viewer.setImage( ImageValuePropertyAdapter.convertToUploadedImage( schild.detailBild().get() ) );
        }

        site.addFieldListener( uploadListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getNewValue() != null && "bild".equals( ev.getFieldName() )) {
                    // repaint image preview
                    UploadedImage uploadedImage = (UploadedImage)ev.getNewValue();

                    viewer.setImage( uploadedImage );
                }
                if (ev.getNewValue() != null && schild.detailBild().qualifiedName().name().equals( ev.getFieldName() )) {
                    UploadedImage uploadedImage = (UploadedImage)ev.getNewValue();
                    imagePreview2.setImage( uploadedImage );
                }
            }
        } );
    }
}
