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

import java.util.SortedMap;
import java.util.TreeMap;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.qi4j.api.entity.association.Association;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;

import org.polymap.rhei.data.entityfeature.PlainValuePropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormFieldWithSuggestions;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.TwvRepository;
import org.polymap.twv.model.data.WegAbschnittBeschaffenheitComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegbeschaffenheitComposite;
import org.polymap.twv.model.data.WegobjektComposite;
import org.polymap.twv.ui.TwvDefaultFormEditorPageWithFeatureTable;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter;
import org.polymap.twv.ui.rhei.ReloadablePropertyAdapter.AssociationCallback;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegBeschaffenheitFormEditorPage
        extends TwvDefaultFormEditorPageWithFeatureTable<WegAbschnittBeschaffenheitComposite> {

    private WegComposite                weg;

    private final static String         prefix = WegBeschaffenheitFormEditorPage.class.getSimpleName();

    private WegBeschaffenheitCalculator beschaffenheitCalculator;

    private StringFormField             beschaffenheitenField;


    public WegBeschaffenheitFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegBeschaffenheitFormEditorPage.class.getName(), "Beschaffenheit", feature, featureStore );
        this.featureStore = featureStore;

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
        beschaffenheitCalculator = new WegBeschaffenheitCalculator( weg );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setFormTitle( formattedTitle( "Tourismusweg", weg.name().get(), getTitle() ) );

        Composite parent = site.getPageBody();

        Composite line0 = newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText(
                        "Beschaffenheit insgesamter Weg z.B. naturnah, Asphalt, sandgeschlämmte Schotterdecke (technische Zustandsbeschreibung)" )
                .setProperty( new PropertyAdapter( weg.beschaffenheit() ) ).setParent( parent )
                .setField( 
                        //new TextFormField() )
                 new TextFormFieldWithSuggestions( twvRepository.entitiesWithNames(
                 WegbeschaffenheitComposite.class ).keySet(), 120 ) )
                .setLayoutData( left().right( 100 ).create() ).create();

        // create a section für Abschnitte
        Composite abschnitte = newSection( line0, "Abschnitte" );

        Control schildForm = createBeschaffenheitForm( abschnitte, line0 );
        createTableForm( parent, abschnitte, true, true );
    }


    protected void refreshReloadables()
            throws Exception {
        super.refreshReloadables();
        if (beschaffenheitenField != null) {
            beschaffenheitenField.setValue( beschaffenheitCalculator.setAbschnitte( getModel() )
                    .beschaffenheitenAsText() );
        }
    }


    @Override
    public void doSubmit( IProgressMonitor monitor )
            throws Exception {
        super.doSubmit( monitor );
        if (beschaffenheitenField != null) {
            beschaffenheitenField.setValue( beschaffenheitCalculator.setAbschnitte( getModel() )
                    .beschaffenheitenAsText() );
        }
    }


    @Override
    protected WegAbschnittBeschaffenheitComposite createNewComposite() {
        WegAbschnittBeschaffenheitComposite ret = TwvRepository.instance().newEntity(
                WegAbschnittBeschaffenheitComposite.class, null );
        ret.weg().set( weg );
        return ret;
    }


    // kopiert von SchildFormEditorPage
    public Control createBeschaffenheitForm( Composite parent, Composite top ) {

        // Label Abschnitt
        beschaffenheitenField = new StringFormField();
        Composite line1 = newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Beschaffenheit in %" )
                .setProperty(
                        new PlainValuePropertyAdapter<String>( "_abschnitte_", beschaffenheitCalculator
                                .beschaffenheitenAsText() ) ).setParent( parent ).setEnabled( false )
                .setField( beschaffenheitenField ).setLayoutData( left().top( top ).right( 100 ).create() ).create();

        Composite line2a = newFormField( "Von Wegobjekt" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<WegAbschnittBeschaffenheitComposite>( selectedComposite, prefix
                                + "objektVon", new AssociationCallback<WegAbschnittBeschaffenheitComposite>() {

                            public Association get( WegAbschnittBeschaffenheitComposite entity ) {
                                return entity.objektVon();
                            }
                        } ) ).setField( reloadable( wegobjectFormField( false ) ) )
                .setLayoutData( left().right( 100 ).top( line1 ).create() ).create();

        Composite line2b = newFormField( "Bis Wegobjekt" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<WegAbschnittBeschaffenheitComposite>( selectedComposite, prefix
                                + "objektBis", new AssociationCallback<WegAbschnittBeschaffenheitComposite>() {

                            public Association get( WegAbschnittBeschaffenheitComposite entity ) {
                                return entity.objektBis();
                            }
                        } ) ).setField( reloadable( wegobjectFormField( false ) ) )
                .setLayoutData( left().right( 100 ).top( line2a ).create() ).create();

        Composite line3 = newFormField( "Beschaffenheit" )
                .setToolTipText( "Beschaffenheit aktueller Abschnitt" )
                .setParent( parent )
                .setEnabled( false )
                .setProperty(
                        new ReloadablePropertyAdapter<WegAbschnittBeschaffenheitComposite>( selectedComposite, prefix
                                + "beschaffenheit", new AssociationCallback<WegAbschnittBeschaffenheitComposite>() {

                            public Association get( WegAbschnittBeschaffenheitComposite entity ) {
                                return entity.beschaffenheit();
                            }
                        } ) ).setField( reloadable( namedAssocationsPicklist( WegbeschaffenheitComposite.class ) ) )
                .setLayoutData( left().top( line2b ).right( 100 ).bottom( 100 ).create() ).create();
        return line3;
    }


    protected EntityType<WegAbschnittBeschaffenheitComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final TwvRepository repo = TwvRepository.instance();
        final EntityType<WegAbschnittBeschaffenheitComposite> type = repo
                .entityType( WegAbschnittBeschaffenheitComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "objektVonName" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Von" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "objektBisName" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Bis" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Beschaffenheit" ) );
        return type;
    }


    public Iterable<WegAbschnittBeschaffenheitComposite> getElements() {
        return WegAbschnittBeschaffenheitComposite.Mixin.forEntity( weg );
    }


    protected IFormField wegobjectFormField( boolean editable ) {
        return new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                SortedMap<String, Object> unterkategories = new TreeMap<String, Object>();
                for (WegobjektComposite c : WegobjektComposite.Mixin.forEntity( weg )) {
                    unterkategories.put( c.nameLang().get(), c );
                }
                return unterkategories;
            }
        } );
    }
}