package org.polymap.twv.ui.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Joiner;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerVisitor;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.data.SchildComposite;
import org.polymap.twv.model.data.WegAbschnittBeschaffenheitComposite;
import org.polymap.twv.model.data.WegComposite;
import org.polymap.twv.model.data.WegobjektComposite;

public class PrintAction
        extends Action {

    private WegComposite        weg;

    protected static DateFormat fileFormat = new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss" );


    protected PrintAction( WegComposite weg ) {
        super( "Datenblatt" );
        setImageDescriptor( ImageDescriptor
                .createFromURL( TwvPlugin.getDefault().getResource( "icons/pruefdruck.gif" ) ) );
        setToolTipText( "Ausdruck des Datenblattes mit dem Weg und seinen Schildern und Wegobjekten" );
        this.weg = weg;
    }


    public void run() {
        try {
            final String filename = "weg_" + fileFormat.format( new Date() );
            final File f = File.createTempFile( filename, ".html" );
            f.deleteOnExit();

            BufferedReader reader = new BufferedReader( new InputStreamReader( this.getClass().getResourceAsStream(
                    "weg.html" ) ) );
            String line = reader.readLine();
            StringBuffer templateBuffer = new StringBuffer();
            while (line != null) {
                templateBuffer.append( line ).append( "\n" );
                line = reader.readLine();
            }
            reader.close();
            String template = templateBuffer.toString();

            // replace all templates
            String content = replaceTemplates( template );

            // write template to file
            FileUtils.writeStringToFile( f, content );

            // open download
            Polymap.getSessionDisplay().asyncExec( new Runnable() {

                public void run() {
                    String url = DownloadServiceHandler.registerContent( new ContentProvider() {

                        public String getContentType() {
                            return "text/html; charset=ISO-8859-1";
                        }


                        public String getFilename() {
                            return filename + ".html";
                        }


                        public InputStream getInputStream()
                                throws Exception {
                            return new BufferedInputStream( new FileInputStream( f ) );
                        }


                        public boolean done( boolean success ) {
                            f.delete();
                            return true;
                        }

                    } );

                    ExternalBrowser.open( "weg_print_window", url, ExternalBrowser.NAVIGATION_BAR
                            | ExternalBrowser.STATUS | ExternalBrowser.LOCATION_BAR );
                }
            } );
        }
        catch (final Exception e) {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {

                public void run() {
                    MessageDialog.openError( PolymapWorkbench.getShellToParentOn(),
                            "Fehler beim Erstellen des Datenblattes", e.getLocalizedMessage() );
                }
            } );
        }
    }


    private String replaceTemplates( String template )
            throws Exception {
        WegGemeindenCalculator wegGemeindenCalculator = new WegGemeindenCalculator( weg );

        template = template.replaceAll( "WEGNAME", "Weg: " + unnull( weg.name().get() ) );
        template = template.replaceAll( "KOMMUNE", Joiner.on( ", " ).join( wegGemeindenCalculator.gemeindeNamen() ) );
        template = template.replaceAll( "UNTERKATEGORIE", unnull( weg.unterkategorie().get() != null ? weg
                .unterkategorie().get().name().get() : "" ) );
        template = template.replaceAll( "KATEGORIE", unnull( weg.kategorie().get() != null ? weg.kategorie().get()
                .name().get() : "" ) );
        template = template.replaceAll( "MARKIERUNG", unnull( weg.markierung().get() != null ? weg.markierung().get()
                .name().get() : "" ) );
        template = template.replaceAll( "WEGBESCHAFFENHEIT", unnull( weg.beschaffenheit().get() ) );
        template = template.replaceAll( "WEGBESCHREIBUNG", unnull( weg.beschreibung().get() ) );
        template = template.replaceAll( "MAENGEL", unnull( weg.maengel().get() ) );
        template = template.replaceAll( "BEMERKUNG", unnull( weg.bemerkung().get() ) );
        template = template.replaceAll( "ERFASSER", unnull( weg.erfasser().get() ) );
        template = template.replaceAll( "WEGEWART", unnull( weg.wegewart().get() ) );

        //
        ILayer gemeindeLayer = ProjectRepository.instance().visit( new LayerVisitor() {

            public boolean visit( ILayer layer ) {
                if (layer.getLabel().equalsIgnoreCase( "gemeinden" )) {
                    result = layer;
                }
                return result == null;
            }
        } );
        template = template.replaceAll( "WEGOBJEKT", getWegobjekte( gemeindeLayer ) );
        template = template.replaceAll( "SCHILD", getSchilder( gemeindeLayer ) );
        template = template.replaceAll( "WEGBESCHAFFENHEIT", getBeschaffenheiten() );


        return template;
    }


 
    private String getBeschaffenheiten() {
        StringBuffer ret = new StringBuffer();
        for (WegAbschnittBeschaffenheitComposite objekt : WegAbschnittBeschaffenheitComposite.Mixin.forEntity( weg )) {
            ret.append( "<tr><td>" );
            ret.append( objekt.objektVon().get() != null ? objekt.objektVon().get().name().get() : "" );
            ret.append( "</td><td>" );
            ret.append( objekt.objektBis().get() != null ? objekt.objektBis().get().name().get() : "" );
            ret.append( "</td><td>" );
            ret.append( unnull( objekt.name().get() ) );
            ret.append( "</td><td>" );
        }
        return ret.toString();
    }


    private String getWegobjekte( ILayer gemeindeLayer )
            throws Exception {

        StringBuffer ret = new StringBuffer();
        for (WegobjektComposite objekt : WegobjektComposite.Mixin.forEntity( weg )) {
            ret.append( "<tr><td>" );
            ret.append( objekt.laufendeNr().get() );
            ret.append( "</td><td>" );
            ret.append( unnull( objekt.typ().get() ) );
            ret.append( "</td><td>" );
            ret.append( unnull( objekt.beschreibung().get() ) );
            ret.append( "</td><td>" );
            if (gemeindeLayer != null && objekt.geom().get() != null) {
                final List<String> kommune = new ArrayList<String>();
                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( gemeindeLayer, false );
                FeatureCollection gemeinden = fs.getFeatures( DataPlugin.ff.intersects(
                        DataPlugin.ff.property( fs.getSchema().getGeometryDescriptor().getLocalName() ),
                        DataPlugin.ff.literal( objekt.geom().get() ) ) );
                gemeinden.accepts( new FeatureVisitor() {

                    public void visit( Feature gemeinde ) {
                        Property nameProp = gemeinde.getProperty( "ORTSNAME" );
                        kommune.add( nameProp != null ? nameProp.getValue().toString() : "-" );
                    }
                }, null );

                ret.append( Joiner.on( ", " ).join( kommune ) );
            }
            ret.append( "</td></tr>" );
        }
        return ret.toString();
    }

    private String getSchilder( ILayer gemeindeLayer )
            throws Exception {

        StringBuffer ret = new StringBuffer();
        for (SchildComposite objekt : SchildComposite.Mixin.forEntity( weg )) {
            ret.append( "<tr><td>" );
            ret.append( objekt.laufendeNr().get() );
            ret.append( "</td><td>" );
            ret.append( unnull( objekt.schildart().get() != null ? objekt.schildart().get().name().get() : "" ) );
            ret.append( "</td><td>" );
            ret.append( unnull( objekt.standort().get() ) );
            ret.append( "</td><td>" );
            ret.append( unnull( objekt.pfeilrichtung().get() != null ? objekt.pfeilrichtung().get().name().get() : "" ) );
            ret.append( "</td><td>" );
            ret.append( unnull( objekt.beschriftung().get() ) );
            ret.append( "</td><td>" );
            ret.append( unnull( objekt.material().get() != null ? objekt.material().get().name().get() : "" ) );
            ret.append( "</td><td>" );
            ret.append( unnull( objekt.befestigung().get() ) );
            ret.append( "</td><td>" );
            if (gemeindeLayer != null && objekt.geom().get() != null) {
                final List<String> kommune = new ArrayList<String>();
                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( gemeindeLayer, false );
                FeatureCollection gemeinden = fs.getFeatures( DataPlugin.ff.intersects(
                        DataPlugin.ff.property( fs.getSchema().getGeometryDescriptor().getLocalName() ),
                        DataPlugin.ff.literal( objekt.geom().get() ) ) );
                gemeinden.accepts( new FeatureVisitor() {

                    public void visit( Feature gemeinde ) {
                        Property nameProp = gemeinde.getProperty( "ORTSNAME" );
                        kommune.add( nameProp != null ? nameProp.getValue().toString() : "-" );
                    }
                }, null );

                ret.append( Joiner.on( ", " ).join( kommune ) );
            }
            ret.append( "</td></tr>" );
        }
        return ret.toString();
    }


    private String unnull( String string ) {
        return string == null ? "" : string.replace( "\n", "</br>" );
    }
}