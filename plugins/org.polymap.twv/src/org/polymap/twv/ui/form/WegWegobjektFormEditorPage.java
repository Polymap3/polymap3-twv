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
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegobjektComposite;
import org.polymap.twv.model.data.WegobjektNameComposite;
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
public class WegWegobjektFormEditorPage
        extends TwvDefaultFormEditorPageWithFeatureTable<WegobjektComposite> {

    private WegComposite        weg;

    private IFormFieldListener  uploadListener;

    private ImageViewer         imagePreview;

    private ImageViewer         imagePreview2;

    private final static String prefix = WegWegobjektFormEditorPage.class.getSimpleName();


    public WegWegobjektFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegWegobjektFormEditorPage.class.getName(), "Wegobjekte", feature, featureStore );
        this.featureStore = featureStore;

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setFormTitle( formattedTitle( "Tourismusweg", weg.name().get(), getTitle() ) );

        Composite parent = site.getPageBody();
        Control baseForm = createForm( parent );
        createTableForm( parent, baseForm, false, false );
    }


    @Override
    protected void refreshReloadables()
            throws Exception {
        super.refreshReloadables();
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


    // kopiert von WegobjektFormEditorPage
    public Control createForm( Composite parent ) {

        Composite line0 = newFormField( "Nummer" )
                .setParent( parent )
                .setEnabled( false )
                .setProperty(
                        new ReloadablePropertyAdapter<WegobjektComposite>( selectedComposite, prefix + "laufendeNr",
                                new PropertyCallback<WegobjektComposite>() {

                                    public Property get( WegobjektComposite entity ) {
                                        return entity.laufendeNr();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale(), 12, 0 ) )
                .setLayoutData( left().create() ).setToolTipText( "laufende Wegobjektnummer" ).create();
        
        Composite line1 = newFormField( "Name" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<WegobjektComposite>( selectedComposite, prefix + "wegobjektName",
                                new AssociationCallback<WegobjektComposite>() {

                                    public Association get( WegobjektComposite entity ) {
                                        return entity.wegobjektName();
                                    }
                                } ) )
                .setField( reloadable( namedAssocationsPicklist( WegobjektNameComposite.class, true ) ) )
                .setLayoutData( left().top( line0 ).create() ).create();

        Composite line2 = newFormField( "Beschreibung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<WegobjektComposite>( selectedComposite, prefix + "beschreibung",
                                new PropertyCallback<WegobjektComposite>() {

                                    public Property get( WegobjektComposite entity ) {
                                        return entity.beschreibung();
                                    }
                                } ) ).setField( reloadable( new TextFormField() ) )
                .setLayoutData( left().top( line1 ).height( 80 ).right( RIGHT ).create() )
                .setToolTipText( "Beschreibung des Wegobjektes" ).create();

        Composite line3 = newFormField( "Bild" )
                .setParent( parent )
                .setProperty(
                        new ReloadableImageValuePropertyAdapter<WegobjektComposite>( selectedComposite,
                                prefix + "bild",
                                new ReloadableImageValuePropertyAdapter.PropertyCallback<WegobjektComposite>() {

                                    public Property<ImageValue> get( WegobjektComposite entity ) {
                                        return entity.bild();
                                    }
                                } ) ).setField( reloadable( new UploadFormField( TwvPlugin.getImagesRoot(), false ) ) )
                .setLayoutData( left().top( line2 ).create() ).create();

        imagePreview = new ImageViewer( parent, left().top( line3 ).height( 250 ).width( 250 ).create() );

        Composite line4 = newFormField( "Detailbild" )
                .setParent( parent )
                .setProperty(
                        new ReloadableImageValuePropertyAdapter<WegobjektComposite>( selectedComposite, prefix
                                + "detailBild",
                                new ReloadableImageValuePropertyAdapter.PropertyCallback<WegobjektComposite>() {

                                    public Property<ImageValue> get( WegobjektComposite entity ) {
                                        return entity.detailBild();
                                    }
                                } ) ).setField( reloadable( new UploadFormField( TwvPlugin.getImagesRoot(), false ) ) )
                .setLayoutData( right().top( line2 ).create() ).create();

        imagePreview2 = new ImageViewer( parent, right().top( line4 ).height( 250 ).width( 250 ).create() );

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

        return imagePreview2.getControl();
    }


    protected EntityType<WegobjektComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final TwvRepository repo = TwvRepository.instance();
        final EntityType<WegobjektComposite> type = repo.entityType( WegobjektComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "laufendeNr" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Nummer" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "beschreibung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Beschreibung" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Name" ) );
        return type;
    }


    public Iterable<WegobjektComposite> getElements() {
        return WegobjektComposite.Mixin.forEntity( weg );
    }
}