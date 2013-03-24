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
import org.opengis.feature.Feature;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.data.AusweisungComposite;
import org.polymap.twv.model.data.KategorieComposite;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.model.data.PrioritaetComposite;
import org.polymap.twv.model.data.UnterkategorieComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WidmungComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegFormEditorPage
        extends TwvDefaultFormEditorPage {

    private final WegComposite weg;

    // private UnterkategorieReloader unterkategorieReloader;

    private KategorieComposite selectedKategorie = null;

    private IFormFieldListener kategorieSelectionListener;


    public WegFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegFormEditorPage.class.getName(), "Basisdaten", feature, featureStore );

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( "Weg" + ((weg.name().get() != null) ? " - " + weg.name().get() : "") );

        Composite parent = site.getPageBody();
        parent.setLayout( new FormLayout() );

        // readonly
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

        newFormField( "Priorität" )
                .setProperty(
                        new AssociationAdapter<PrioritaetComposite>( "prioritaet", weg.prioritaet() ) )
                .setField( namedAssocationsPicklist( PrioritaetComposite.class ) )
                .setLayoutData( right().top( line2 ).create() ).create();

        // TODO falko Gemeinde wird über Berechnung eingeblendet
        // TODO falko laengeImLandkreis wird über Berechnung eingeblendet

        // Composite line4 = newFormField( "Länge Landkreis" )
        // .setProperty( new PropertyAdapter( weg.laengeImLandkreis() ) )
        // .setField( new StringFormField() ).setLayoutData( left().top( line3
        // ).create() )
        // .setToolTipText( "Länge im Landkreis Mittelsachsen" ).create();

        Composite line4 = newFormField( "Gesamtlänge" )
                .setProperty( new PropertyAdapter( weg.laengeUeberregional() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( line3 ).create() )
                .setToolTipText( "überregionale Gesamtlänge" ).create();

        Composite line5 = newFormField( "Wegbeschreibung" )
                .setProperty( new PropertyAdapter( weg.beschreibung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( 100 ).height( 50 ).top( line4 ).create() ).create();

        Composite line6 = newFormField( "Wegbeschaffenheit" )
                .setProperty( new PropertyAdapter( weg.beschaffenheit() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( 100 ).height( 50 ).top( line5 ).create() ).create();
        // TODO Picklist mit Textvorlagen ergänzen

        Composite line7 = newFormField( "Widmung" )
                .setProperty( new AssociationAdapter<WidmungComposite>( "widmung", weg.widmung() ) )
                .setField( namedAssocationsPicklist( WidmungComposite.class ) )
                .setLayoutData( left().top( line6 ).create() ).create();

        newFormField( "Markierung" )
                .setProperty( new AssociationAdapter<WidmungComposite>( "widmung", weg.widmung() ) )
                .setField( namedAssocationsPicklist( MarkierungComposite.class ) )
                .setLayoutData( right().top( line6 ).create() ).create();

        // unterkategorieReloader
        // site.addFieldListener( unterkategorieReloader = new
        // UnterkategorieReloader( site, unterkategorieList ) );
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