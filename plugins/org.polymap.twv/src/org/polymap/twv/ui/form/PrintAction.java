package org.polymap.twv.ui.form;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.twv.TwvPlugin;
import org.polymap.twv.model.data.WegComposite;

public class PrintAction
        extends Action {

    private WegComposite weg;


    protected PrintAction( WegComposite beleg ) {
        super( "Drucken" );
        setImageDescriptor( ImageDescriptor
                .createFromURL( TwvPlugin.getDefault().getResource( "icons/pruefdruck.gif" ) ) );
        // setToolTipText( "Anzeige Prüfdruck in neuem Fenster für Ausdruck." );
        this.weg = beleg;
        
        //new HtmlReportFactory.HtmlReport -> eigene Instanz mit EntityJsonProvider der auch assoziationen kann und HTML Datei nach HOME/workspace-polymap/Scripts/src/reports
    }


    public void run() {
        ExternalBrowser.open( "weg_print_window", "../rest/forms/weg.html?id=" + weg.id(),
                ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS | ExternalBrowser.LOCATION_BAR );
    }
}