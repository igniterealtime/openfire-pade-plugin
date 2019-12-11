package org.jivesoftware.openfire.plugin.ofmeet;

import org.jivesoftware.openfire.container.PluginClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jitsi.videobridge.Videobridge;
import org.jitsi.videobridge.openfire.PluginImpl;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * A wrapper object for the Jitsi Videobridge Openfire plugin.
 *
 * This wrapper can be used to instantiate/initialize and tearing down an instance of that plugin. An instance of this
 * class is re-usable.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class JvbPluginWrapper
{
    private static final Logger Log = LoggerFactory.getLogger(JvbPluginWrapper.class);

    private PluginImpl jitsiPlugin;

    /**
     * Initialize the wrapped component.
     *
     * @throws Exception On any problem.
     */
    public synchronized void initialize(final PluginManager manager, final File pluginDirectory) throws Exception
    {
        Log.debug( "Initializing Jitsi Videobridge..." );

        if ( jitsiPlugin != null )
        {
            Log.warn( "Another Jitsi Videobridge appears to have been initialized earlier! Unexpected behavior might be the result of this new initialization!" );
        }

        // Disable health check. Our JVB is not an external component, so there's no need to check for its connectivity.
        //System.setProperty( "org.jitsi.videobridge.PING_INTERVAL", "-1" );

        jitsiPlugin = new PluginImpl();
        jitsiPlugin.initializePlugin( manager, pluginDirectory );
/*
        // Override the classloader used by the wrapped plugin with the classloader of ofmeet plugin.
        // TODO Find a way that does not depend on reflection.
        final Field field = manager.getClass().getDeclaredField( "classloaders" );
        final boolean wasAccessible = field.isAccessible();
        field.setAccessible( true );
        try
        {
            final Map<Plugin, PluginClassLoader> classloaders = (Map<Plugin, PluginClassLoader>) field.get( manager );
            classloaders.put( jitsiPlugin, manager.getPluginClassloader( manager.getPlugin( "ofmeet" ) ) );

            jitsiPlugin.initializePlugin( manager, pluginDirectory );

            // The reference to the classloader is no longer needed in the plugin manager. Better clean up immediately.
            // (ordinary, we'd do this in the 'destroy' method of this class, but there doesn't seem a need to wait).
            classloaders.remove( jitsiPlugin );
        }
        finally
        {
            field.setAccessible( wasAccessible );
        }
*/
        Log.trace( "Successfully initialized Jitsi Videobridge." );
    }

    /**
     * Destroying the wrapped component. After this call, the wrapped component can be re-initialized.
     *
     * @throws Exception On any problem.
     */
    public synchronized void destroy() throws Exception
    {
        Log.debug( "Destroying Jitsi Videobridge..." );

        if ( jitsiPlugin == null )
        {
            Log.warn( "Unable to destroy the Jitsi Videobridge, as none appears to be running!" );
        }

        jitsiPlugin.destroyPlugin();
        jitsiPlugin = null;
        Log.trace( "Successfully destroyed Jitsi Videobridge." );
    }

    public Videobridge getVideobridge() {
        return jitsiPlugin.getComponent().getVideobridge();
    }
}
