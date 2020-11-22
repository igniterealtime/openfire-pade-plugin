package org.jivesoftware.openfire.plugin.ofmeet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jitsi.videobridge.openfire.PluginImpl;

import de.mxro.process.*;
import org.jivesoftware.util.JiveGlobals;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.*;
import java.util.*;
import org.jitsi.util.OSUtils;
import java.util.Properties;

import de.mxro.process.*;
import org.jivesoftware.util.JiveGlobals;

/**
 * A wrapper object for the Jitsi Videobridge Openfire plugin.
 *
 * This wrapper can be used to instantiate/initialize and tearing down an instance of that plugin. An instance of this
 * class is re-usable.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class JvbPluginWrapper implements ProcessListener
{
    private static final Logger Log = LoggerFactory.getLogger(JvbPluginWrapper.class);
    private PluginImpl jitsiPlugin;
    private XProcess jvbThread = null;

    /**
     * Initialize the wrapped component.
     *
     * @throws Exception On any problem.
     */
    public synchronized void initialize(final PluginManager manager, final File pluginDirectory) throws Exception
    {
        Log.debug( "Initializing Jitsi Videobridge..." );

        jitsiPlugin = new PluginImpl();
        jitsiPlugin.initializePlugin( manager, pluginDirectory );

        final String jvbHomePath = pluginDirectory.getAbsolutePath() + File.separator + "classes" + File.separator + "jvb";
        final String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
        final String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
        final String main_muc = JiveGlobals.getProperty( "ofmeet.main.muc", "conference." + domain);


        List<String> lines = Arrays.asList(
            "videobridge {",
            "    stats {",
            "        # Enable broadcasting stats/presence in a MUC",
            "        enabled = true",
            "        transports = [",
            "            { type = \"muc\" }",
            "        ]",
            "    }",
            "",
            "    http-servers {",
            "      public {",
            "          port = 6060",
            "      }",
            "    }",
            "    websockets {",
            "      enabled = true",
            "      domain = \"" + domain + "\"",
            "      tls = true",
            "    }",
            "",
            "    apis {",
            "        xmpp-client {",
            "               configs {",
            "                # Connect to the first XMPP server",
            "                shard {",
            "                    hostname= \"" + hostname + "\"",
            "                    domain = \"" + domain + "\"",
            "                    username = \"admin\"",
            "                    password = \"admin\"",
            "                    muc_jids = \"admin@" + main_muc + "\"",
            "                    muc_nickname = \"Administrator\"",
            "                    disable_certificate_verification = true",
            "                }",
            "               }",
            "        }",
            "    }",
            "",
            "    ice {",
            "        tcp {",
            "            enabled = true",
            "            port = \"4443\"",
            "            mapped-port = \"4443\"",
            "        }",
            "        udp {",
            "            port = \"10000\"",
            "            local-address = 192.168.1.1",
            "            public-address = 90.248.44.212",
            "        }",
            "    }",
            "}"
        );

        try
        {
            Path file = Paths.get(jvbHomePath + File.separator + "application.conf");
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (Exception e) {
            Log.error("createConfigFile error", e);
        }

        String jvbExePath = jvbHomePath + File.separator + "ofmeet";

        if(OSUtils.IS_LINUX64)
        {
            jvbExePath = jvbExePath + ".sh";
        }
        else if(OSUtils.IS_WINDOWS64)
        {
            jvbExePath = jvbExePath + ".bat";
        }

         jvbThread = Spawn.startProcess(jvbExePath + " --apis=none", new File(jvbHomePath), this);

        Log.trace( "Successfully initialized Jitsi Videobridge." );
    }

    /**
     * Destroying the wrapped component. After this call, the wrapped component can be re-initialized.
     *
     * @throws Exception On any problem.
     */
    public synchronized void destroy() throws Exception
    {
        if (jvbThread != null) jvbThread.destory();
        if (jitsiPlugin != null ) jitsiPlugin.destroyPlugin();

        Log.trace( "Successfully destroyed Jitsi Videobridge." );
    }

    public void onOutputLine(final String line)
    {
        Log.info("onOutputLine " + line);
    }

    public void onProcessQuit(int code)
    {
        Log.info("onProcessQuit " + code);
    }

    public void onOutputClosed() {
        Log.error("onOutputClosed");
    }

    public void onErrorLine(final String line)
    {
        Log.info(line);
    }

    public void onError(final Throwable t)
    {
        Log.error("Thread error", t);
    }
}
