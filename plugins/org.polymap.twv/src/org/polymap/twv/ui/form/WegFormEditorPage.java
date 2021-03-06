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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventHandler;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PlainValuePropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.field.TextFormFieldWithSuggestions;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.twv.model.data.AusweisungComposite;
import org.polymap.twv.model.data.KategorieComposite;
import org.polymap.twv.model.data.MarkierungComposite;
import org.polymap.twv.model.data.PrioritaetComposite;
import org.polymap.twv.model.data.ProfilComposite;
import org.polymap.twv.model.data.UnterkategorieComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WidmungComposite;
import org.polymap.twv.ui.TwvDefaultFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegFormEditorPage
        extends TwvDefaultFormEditorPage
        implements IFormEditorPage2 {

    private static Log         log               = LogFactory.getLog( WegFormEditorPage.class );

    private final WegComposite      weg;

    private KategorieComposite      selectedKategorie = null;

    private IFormFieldListener      kategorieSelectionListener;

    private PropertyChangeListener  laengeListener;


    public WegFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WegFormEditorPage.class.getName(), "Basisdaten", feature, featureStore );

        weg = twvRepository.findEntity( WegComposite.class, feature.getIdentifier().getID() );
    }

    
    @Override
    public void dispose() {
        if (laengeListener != null && weg != null) {
            weg.removePropertyChangeListener( laengeListener );
            laengeListener = null;
        }
    }
    
    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void doLoad( IProgressMonitor monitor ) throws Exception {
    }

    @Override
    public void doSubmit( IProgressMonitor monitor ) throws Exception {
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
                .setProperty( new AssociationAdapter<KategorieComposite>( weg.kategorie() ) )
                .setField( namedAssocationsPicklist( KategorieComposite.class ) )
                .setLayoutData( left().top( line1 ).create() ).create();
        selectedKategorie = weg.kategorie().get();

        final PicklistFormField unterkategorieList = new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                SortedMap<String, Object> unterkategories = new TreeMap<String, Object>();
                if (selectedKategorie != null) {
                    int unterkategorieCount = selectedKategorie.unterkategories().count();
                    for (int i = 0; i < unterkategorieCount; i++) {
                        UnterkategorieComposite uk = selectedKategorie.unterkategories().get( i );
                        unterkategories.put( uk.name().get(), uk );
                    }
                }
                return unterkategories;
            }
        } );
        newFormField( "Unterkategorie" )
                .setProperty( new AssociationAdapter<UnterkategorieComposite>( weg.unterkategorie() ) )
                .setField( unterkategorieList ).setLayoutData( right().top( line1 ).create() ).create();

        Composite line3 = newFormField( "Ausweisung" )
                .setProperty( new AssociationAdapter<AusweisungComposite>( weg.ausweisung() ) )
                .setField( namedAssocationsPicklist( AusweisungComposite.class ) )
                .setLayoutData( left().top( line2 ).create() ).create();

        Composite line4 = newFormField( "Priorität" )
                .setProperty( new AssociationAdapter<PrioritaetComposite>( weg.prioritaet() ) )
                .setField( namedAssocationsPicklist( PrioritaetComposite.class ) )
                .setLayoutData( right().top( line2 ).create() ).create();

        // Gemeinden
        IMap map = ((PipelineFeatureSource)fs).getLayer().getMap();
        final WegGemeindenCalculator calculator = new WegGemeindenCalculator( weg, map );
        String gemeindeNamen = null;
        try {
            gemeindeNamen = Joiner.on( ", " ).join( calculator.gemeindeNamen() );
        }
        catch (Exception e) {
            log.warn( "", e );
            gemeindeNamen = "-konnten nicht ermittelt werden- (" + e.getLocalizedMessage() + ")";
        }
        Composite line5 = newFormField( "Kommunen" )
                .setEnabled( false )
                .setField( new StringFormField() )
                .setLayoutData( right().top( line4 ).create() )
                .setProperty( new PlainValuePropertyAdapter<String>( "_kommunen_", gemeindeNamen ) )
                .create();

        // laengeImLandkreis: wird über Berechnung eingeblendet
        double laengeImLandkreis = -1d;
        try {
            laengeImLandkreis = calculator.laengeImLandkreis();
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        NumberValidator lengthValidator = new NumberValidator( Double.class, Polymap.getSessionLocale(), 10, 0, 0, 0 );
        Composite line55 = newFormField( "Länge Landkreis (m)" )
                .setEnabled( false )
                .setProperty( new PlainValuePropertyAdapter<Double>( "_laengeImLandkreis_", laengeImLandkreis ) )
                .setField( new StringFormField() )
                .setValidator( lengthValidator )
                .setLayoutData( left().top( line3 ).create() )
                .setToolTipText( "Länge im Landkreis Mittelsachsen in Metern\nGesamtlänge: " + lengthValidator.getNumberFormat().format( calculator.gesamtLaenge() ) )
                .create();

        // listen to geom changes
        weg.addPropertyChangeListener( laengeListener = new PropertyChangeListener() {
            @EventHandler(display=true)
            public void propertyChange( PropertyChangeEvent ev ) {
                if (ev.getPropertyName().equals( weg.geom().qualifiedName().name() )) {
                    try {
                        site.setFieldValue( "_kommunen_", Joiner.on( ", " ).join( calculator.gemeindeNamen() ) );
                        site.setFieldValue( "_laengeImLandkreis_", calculator.laengeImLandkreis() );
                    }
                    catch (Exception e) {
                        log.warn( "", e );
                    }
                }
            }
        });
        
        // Gesamtlänge: als Feld
        Composite line6 = newFormField( "Gesamtlänge" ).setProperty( new PropertyAdapter( weg.laengeUeberregional() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( line5 ).create() )
                .setToolTipText( "Überregionale Gesamtlänge" ).create();

        Composite line7 = newFormField( "Wegbeschreibung" )
                .setProperty( new PropertyAdapter( weg.beschreibung() ) )
                .setToolTipText(
                        "z.B. Ortsangaben, überwiegend Nutzung öffentlicher Straßen und Wege, separate Radverkehrsanlagen, Feld- und Waldwege (für den Touristen nützliche Hinweise)" )
                .setField( new TextFormField() ).setLayoutData( left().right( 100 ).height( 150 ).top( line6 ).create() )
                .create();

//        Composite line8 = newFormField( "Wegbeschaffenheit" )
//                .setToolTipText(
//                        "z.B. naturnah, Asphalt, sandgeschlämmte Schotterdecke (technische Zustandsbeschreibung)" )
//                .setProperty( new PropertyAdapter( weg.beschaffenheit() ) )
//                .setField(
//                        new TextFormFieldWithSuggestions( twvRepository.entitiesWithNames(
//                                WegbeschaffenheitComposite.class ).keySet() ) )
//                .setLayoutData( left().right( 100 ).top( line7 ).create() ).create();

        Composite line9 = newFormField( "Widmung" )
                .setProperty( new PropertyAdapter( weg.widmung() ) )
                .setField(
                        new TextFormFieldWithSuggestions( twvRepository.entitiesWithNames( WidmungComposite.class )
                                .keySet()/*, 100*/ ) ).setLayoutData( left().right( 100 ).top( line7 ).create() ).create();

        Composite line10 = newFormField( "Profil" )
                .setToolTipText(
                        "leicht \n"
                                + "Weg gut ausgebaut, Gelände flach oder leicht ansteigend, keine Absturzgefahr, \n"
                                + "auch mit Turnschuhen/ ohne Spezialräder geeignet, \n"
                                + "Orientierung problemlos möglich, wenig Ausdauer erforderlich\n" + "\nmittel \n"
                                + "durchgehend gut ersichtlicher und begeh- oder befahrbarer Weg,\n"
                                + "teilweise Abhänge und Böschungen, teilweise Steigungen und Neigungen, \n"
                                + "Trekkingschuhe und Trittsicherheit/ Mountainbike empfehlenswert, \n"
                                + "elementares Orientierungsvermögen, etwas Ausdauer\n" + "\nschwierig \n"
                                + "Weg nicht durchgehend sichtbar, heikle Stellen, Trittsicherheit, \n"
                                + "Trekkingschuhe/ Mountainbike erforderlich, Abhänge und Böschungen nur mit \n"
                                + "Sichtschutz, erhebliche Steigungen und Neigungen, gutes \n"
                                + "Orientierungsvermögen und Ausdauer notwendig" )
                .setProperty( new AssociationAdapter<ProfilComposite>( weg.profil() ) )
                .setField( namedAssocationsPicklist( ProfilComposite.class ) )
                .setLayoutData( left().top( line9 ).create() ).create();

        newFormField( "Markierung" ).setProperty( new AssociationAdapter<MarkierungComposite>( weg.markierung() ) )
                .setField( namedAssocationsPicklist( MarkierungComposite.class ) )
                .setLayoutData( right().top( line9 ).create() ).create();

        site.addFieldListener( kategorieSelectionListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE
                        && ev.getFieldName().equalsIgnoreCase( weg.kategorie().qualifiedName().name() )) {
                    if ((ev.getNewValue() == null && selectedKategorie != null)
                            || !ev.getNewValue().equals( selectedKategorie )) {
                        selectedKategorie = ev.getNewValue();
                        unterkategorieList.reloadValues();
                    }
                }
            }
        } );
    }
    
    public Action[] getEditorActions() {
        return new Action[] { new PrintAction( weg ) };
    }
}
