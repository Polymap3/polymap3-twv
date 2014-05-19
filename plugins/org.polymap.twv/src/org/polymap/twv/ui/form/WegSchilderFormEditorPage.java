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
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.field.UploadFormField;
import org.polymap.rhei.field.UploadFormField.UploadedImage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.ImageValue;
import org.polymap.twv.model.data.PfeilrichtungComposite;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.SchildartComposite;
import org.polymap.twv.model.data.SchildmaterialComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.ui.ImageViewer;
import org.polymap.twv.ui.TwvDefaultFormEditorPageWithFeatureTable;
import org.polymap.twv.ui.rhei.ImageValuePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadableImageValuePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.AssociationCallback;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.PropertyCallback;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegSchilderFormEditorPage
        extends TwvDefaultFormEditorPageWithFeatureTable<SchildComposite> {

    private WegComposite        weg;

    private IFormFieldListener  uploadListener;

    private ImageViewer         imagePreview;

    private ImageViewer         imagePreview2;

    private final static String prefix = WegSchilderFormEditorPage.class.getSimpleName();


    public WegSchilderFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegSchilderFormEditorPage.class.getName(), "Schilder", feature, featureStore );
        this.featureStore = featureStore;

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setFormTitle( formattedTitle( "Tourismusweg", weg.name().get(), getTitle() ) );

        Composite parent = site.getPageBody();
        Control schildForm = createSchildForm( parent );
        createTableForm( parent, schildForm, false, false );
    }


    protected void refreshReloadables()
            throws Exception {
        super.refreshReloadables();

        // TODO validator not null an der Number
        if (selectedComposite.get() != null) {
            if (selectedComposite.get().bild().get().thumbnailFileName().get() != null) {
                imagePreview.setImage( ImageValuePropertyAdapter.convertToUploadedImage( selectedComposite.get().bild()
                        .get() ) );
            }
            if (selectedComposite.get().detailBild().get().thumbnailFileName().get() != null) {
                imagePreview2.setImage( ImageValuePropertyAdapter.convertToUploadedImage( selectedComposite.get()
                        .detailBild().get() ) );
            }
        }
        else {
            if (imagePreview != null) {
                // bild löschen wenn ein Objekt ohne Bild selektiert wird
                imagePreview.setImage( null );
                imagePreview2.setImage( null );
            }
        }
    }


    public boolean isValid() {
        return true;
    }


    // kopiert von SchildFormEditorPage
    public Control createSchildForm( Composite parent ) {

        Composite line0 = newFormField( "Nummer" )
                .setParent( parent )
                .setEnabled( false )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, prefix + "laufendeNr",
                                new PropertyCallback<SchildComposite>() {

                                    public Property get( SchildComposite entity ) {
                                        return entity.laufendeNr();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale(), 12, 0 ) )
                .setLayoutData( left().create() ).setToolTipText( "laufende Schildnummer" ).create();

        newFormField( "Bestandsnr." )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, prefix + "bestandsNr",
                                new PropertyCallback<SchildComposite>() {

                                    public Property get( SchildComposite entity ) {
                                        return entity.bestandsNr();
                                    }
                                } ) ).setField( reloadable( new StringFormField() ) ).setLayoutData( right().create() )
                .setToolTipText( "Nummer des Schildes bei importierten Datenbeständen" ).create();

        Composite line1 = newFormField( "Schildart" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, prefix + "schildart",
                                new AssociationCallback<SchildComposite>() {

                                    public Association get( SchildComposite entity ) {
                                        return entity.schildart();
                                    }
                                } ) )
                .setField( reloadable( namedAssocationsPicklist( SchildartComposite.class, true ) ) )
                .setLayoutData( left().top( line0 ).create() ).create();

        Composite line2 = newFormField( "Pfeilrichtung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, prefix + "pfeilrichtung",
                                new AssociationCallback<SchildComposite>() {

                                    public Association get( SchildComposite entity ) {
                                        return entity.pfeilrichtung();
                                    }
                                } ) ).setField( reloadable( namedAssocationsPicklist( PfeilrichtungComposite.class ) ) )
                .setLayoutData( left().top( line1 ).create() ).create();

        newFormField( "Material" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, prefix + "material",
                                new AssociationCallback<SchildComposite>() {

                                    public Association get( SchildComposite entity ) {
                                        return entity.material();
                                    }
                                } ) )
                .setField( reloadable( namedAssocationsPicklist( SchildmaterialComposite.class ) ) )
                .setLayoutData( right().top( line1 ).create() ).create();

        Composite line3 = newFormField( "Beschriftung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, prefix + "beschriftung",
                                new PropertyCallback<SchildComposite>() {

                                    public Property get( SchildComposite entity ) {
                                        return entity.beschriftung();
                                    }
                                } ) ).setField( reloadable( new TextFormField() ) )
                .setLayoutData( left().top( line2 ).height( 50 ).right( RIGHT ).create() )
                .setToolTipText( "Schildbeschriftung mit Entfernungsangabe und Zusatzinfo" ).create();

        Composite line4 = newFormField( "Träger" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<SchildComposite>( selectedComposite, prefix + "befestigung",
                                new PropertyCallback<SchildComposite>() {

                                    public Property get( SchildComposite entity ) {
                                        return entity.befestigung();
                                    }
                                } ) ).setField( reloadable( new TextFormField() ) )
                .setLayoutData( left().top( line3 ).height( 50 ).create() ).create();

        Composite line5 = newFormField( "Bild" )
                .setParent( parent )
                .setProperty(
                        new ReloadableImageValuePropertyAdapter<SchildComposite>( selectedComposite, prefix + "bild",
                                new ReloadableImageValuePropertyAdapter.PropertyCallback<SchildComposite>() {

                                    public Property<ImageValue> get( SchildComposite entity ) {
                                        return entity.bild();
                                    }
                                } ) ).setField( reloadable( new UploadFormField( TwvPlugin.getImagesRoot(), false ) ) )
                .setLayoutData( left().top( line4 ).create() ).create();

        imagePreview = new ImageViewer( parent, left().top( line5 ).height( 250 ).width( 250 ).create() );

        Composite line5a = newFormField( "Detailbild" )
                .setParent( parent )
                .setProperty(
                        new ReloadableImageValuePropertyAdapter<SchildComposite>( selectedComposite, prefix + "detailBild",
                                new ReloadableImageValuePropertyAdapter.PropertyCallback<SchildComposite>() {

                                    public Property<ImageValue> get( SchildComposite entity ) {
                                        return entity.detailBild();
                                    }
                                } ) ).setField( reloadable( new UploadFormField( TwvPlugin.getImagesRoot(), false ) ) )
                .setLayoutData( right().top( line4 ).create() ).create();

        imagePreview2 = new ImageViewer( parent, right().top( line5a ).height( 250 ).width( 250 ).create() );

        pageSite.addFieldListener( uploadListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getNewValue() != null && (prefix + "bild").equals( ev.getFieldName() )) {
                    UploadedImage uploadedImage = (UploadedImage)ev.getNewValue();
                    imagePreview.setImage( uploadedImage );
                }
                if (ev.getNewValue() != null && (prefix + "detailBild").equals( ev.getFieldName() )) {
                    UploadedImage uploadedImage = (UploadedImage)ev.getNewValue();
                    imagePreview2.setImage( uploadedImage );
                }
            }
        } );
        return imagePreview.getControl();
    }


    protected EntityType<SchildComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final TwvRepository repo = TwvRepository.instance();
        final EntityType<SchildComposite> type = repo.entityType( SchildComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "laufendeNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Nummer" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "bestandsNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Bestandsnummer" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "bildName" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Bildname" ) );
        return type;
    }


    public Iterable<SchildComposite> getElements() {
        return SchildComposite.Mixin.forEntity( weg );
    }
}