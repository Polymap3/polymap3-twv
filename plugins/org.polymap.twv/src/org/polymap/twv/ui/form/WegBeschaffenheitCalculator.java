/*
 * polymap.org Copyright (C) 2014, Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.twv.ui.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import org.polymap.twv.model.data.WegAbschnittBeschaffenheitComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegbeschaffenheitComposite;
import org.polymap.twv.model.data.WegobjektComposite;

/**
 * Berechnungen von Längen eines Weges bis zu vorgebenen Punkten und Berechung der
 * Beschaffenheiten in den durch Punkten angebenen Teilen.
 *
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WegBeschaffenheitCalculator {

    private static Log                                    log = LogFactory.getLog( WegBeschaffenheitCalculator.class );

    private WegComposite                                  weg;

    private Iterable<WegAbschnittBeschaffenheitComposite> abschnitte;


    public WegBeschaffenheitCalculator( WegComposite weg ) {
        this.weg = weg;
        abschnitte = WegAbschnittBeschaffenheitComposite.Mixin.forEntity( weg );
    }


    public String beschaffenheitenAsText() {
        final MultiLineString wegGeom = weg.geom().get();
        // log.info( "Weg " + weg.name().get() + " hat Gesamtlänge " +
        // wegGeom.getLength() );

        // WegObjekte und Ihre Reihenfolge bestimmen
        SortedMap<Double, WegbeschaffenheitComposite> beschaffenheitenSortiert = new TreeMap<Double, WegbeschaffenheitComposite>();
        Map<WegobjektComposite, Double> positionen = new HashMap<WegobjektComposite, Double>();
        for (WegobjektComposite wegObjekt : WegobjektComposite.Mixin.forEntity( weg )) {
            if (wegGeom != null && wegObjekt.geom().get() != null) {
                double length = lengthFromStartToPoint( wegGeom, wegObjekt.geom().get() );
                beschaffenheitenSortiert.put( length, null );
                positionen.put( wegObjekt, length );
            }
        }
        // log.info( "beschaffenheitenSortiert " + beschaffenheitenSortiert );
        // log.info( "Positionen " + positionen );

        for (WegAbschnittBeschaffenheitComposite wabc : getAbschnitte()) {
            WegobjektComposite von = wabc.objektVon().get();
            WegobjektComposite bis = wabc.objektBis().get();
            if (von != null && bis != null && !von.equals( bis )) {
                // abschnitt gefunden
                Double vPos = positionen.get( von );
                Double bPos = positionen.get( bis );
                // subabschnitte in Map finden und bei allen beschaffenheit setzen
                SortedMap<Double, WegbeschaffenheitComposite> beschaffenheitenSubMap = (vPos < bPos) ? beschaffenheitenSortiert
                        .subMap( vPos, bPos ) : beschaffenheitenSortiert.subMap( bPos, vPos );
                for (Entry<Double, WegbeschaffenheitComposite> entry : beschaffenheitenSubMap.entrySet()) {
                    entry.setValue( wabc.beschaffenheit().get() );
                }
            }
        }
        // log.info( "beschaffenheitenSortiert " + beschaffenheitenSortiert );

        // nun alle beschaffenheiten in Map finden und jeweils mit ihren Längen
        // zusammenaddieren
        double lastPosition = 0.0d;
        Map<WegbeschaffenheitComposite, Double> abschnitteMitLaenge = new HashMap<WegbeschaffenheitComposite, Double>();
        for (Entry<Double, WegbeschaffenheitComposite> entry : beschaffenheitenSortiert.entrySet()) {
            double currentLength = entry.getKey() - lastPosition;
            lastPosition = entry.getKey();
            WegbeschaffenheitComposite b = entry.getValue();
            Double abschnittLaenge = abschnitteMitLaenge.get( b );
            if (abschnittLaenge == null) {
                abschnittLaenge = currentLength;
            }
            else {
                abschnittLaenge += currentLength;
            }
            abschnitteMitLaenge.put( b, abschnittLaenge );
        }
        // log.info( "abschnitteMitLaenge " + abschnitteMitLaenge );

        // nun noch Verhältnisse ausrechnen zur Gesamtlänge
        StringBuffer result = new StringBuffer();
        int unknown = 100;
        double overallLength = wegGeom.getLength();

        for (Entry<WegbeschaffenheitComposite, Double> abschnitt : abschnitteMitLaenge.entrySet()) {
            double length = abschnitt.getValue().doubleValue();
            WegbeschaffenheitComposite wbc = abschnitt.getKey();
            if (wbc != null) {
                if (result.length() > 0) {
                    result.append( ", " );
                }
                int percent = new Double( length * 100 / overallLength ).intValue();
                result.append( percent ).append( "% " ).append( wbc.name().get() );
                unknown -= percent;
            }
        }
        if (unknown > 0) {
            if (result.length() > 0) {
                result.append( ", " );
            }
            result.append( unknown ).append( "% unbekannt" );
        }

        return result.toString();
    }


    private Iterable<WegAbschnittBeschaffenheitComposite> getAbschnitte() {
        return abschnitte;
    }


    /**
     *
     * @param line
     * @param seg
     * @param point
     */
    private double lengthFromStartToPoint( final MultiLineString line, final Point point ) {
        LineSegment seg = new LineSegment();
        Coordinate wo = point.getCoordinate();
        double shortestDistance = Double.MAX_VALUE;
        double completeLength = 0.0d;
        double currentLength = 0.0d;
        for (int i = 0; i < line.getNumGeometries(); i++) {
            LineString lineString = (LineString)line.getGeometryN( i );
            CoordinateSequence seq = lineString.getCoordinateSequence();
            for (int j = 0; j < seq.size() - 1; j++) {
                seg.setCoordinates( seq.getCoordinate( j ), seq.getCoordinate( j + 1 ) );
                double dist = seg.distance( wo );
                if (dist < shortestDistance) {
                    shortestDistance = dist;
                    currentLength = completeLength + seg.p0.distance( seg.closestPoint( wo ) );
                }
                completeLength += seg.getLength();
            }
        }
        return currentLength;
    }


    public double gesamtLaenge() {
        return weg.geom().get().getLength();
    }


    public WegBeschaffenheitCalculator setAbschnitte( List<WegAbschnittBeschaffenheitComposite> model ) {
        this.abschnitte = model;
        return this;
    }

}
