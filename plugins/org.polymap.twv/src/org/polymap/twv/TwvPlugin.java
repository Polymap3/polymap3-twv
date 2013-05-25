package org.polymap.twv;

import java.util.Collections;

import java.io.File;
import java.net.URL;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.feature.Feature;
import org.opengis.filter.identity.FeatureId;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Iterables;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Layers;
import org.polymap.core.runtime.Polymap;

/**
 * The activator class controls the plug-in life cycle
 */
public class TwvPlugin
        extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.polymap.twv";

    // The shared instance
    private static TwvPlugin   plugin;

    private static File moduleRoot;

    private static File imagesRoot;


    public TwvPlugin() {
    }


    public void start( BundleContext context )
            throws Exception {
        super.start( context );
        plugin = this;
    }


    public void stop( BundleContext context )
            throws Exception {
        plugin = null;
        super.stop( context );
    }


    public static TwvPlugin getDefault() {
        return plugin;
    }


    public Image imageForDescriptor( ImageDescriptor imageDescriptor, String key ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( key );
        if (image == null || image.isDisposed()) {
            images.put( key, imageDescriptor );
            image = images.get( key );
        }
        return image;
    }


    public Image imageForName( String resName ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( resName );
        if (image == null || image.isDisposed()) {
            URL res = getBundle().getResource( resName );
            assert res != null : "Image resource not found: " + resName;
            images.put( resName, ImageDescriptor.createFromURL( res ) );
            image = images.get( resName );
        }
        return image;
    }


    public static File getImagesRoot() {
        if (imagesRoot == null) {
            File root = new File( Polymap.getWorkspacePath().toFile(), "images" );
            imagesRoot = new File( root, TwvPlugin.PLUGIN_ID );
            imagesRoot.mkdirs();
        }
        return imagesRoot;

    }


    public static File getModuleRoot() {
        if (moduleRoot == null) {
            File root = new File( Polymap.getWorkspacePath().toFile(), "data" );
            moduleRoot = new File( root, TwvPlugin.PLUGIN_ID );
            moduleRoot.mkdirs();
        }
        return moduleRoot;
    }


    /**
     *
     * @param string
     * @param composite
     */
    public static void openEditor( FeatureStore fs, String layerName, Entity composite ) {
        try {
            IMap map = ((PipelineFeatureSource)fs).getLayer().getMap();
            ILayer layer = Iterables.getOnlyElement( Iterables.filter( map.getLayers(),
                    Layers.hasLabel( layerName) ) );
            if (layer != null) {
                FeatureStore store = PipelineFeatureSource.forLayer( layer, false );

                String id = composite.id();
                FeatureId featureId = new FeatureIdImpl( id );

                FeatureCollection features = store.getFeatures( DataPlugin.ff.id( Collections
                        .singleton( featureId ) ) );
                // .features().next();
                Feature feature = features.features().next();
                org.polymap.rhei.form.FormEditor.open( store, feature, null, true );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

}
