package org.jivesoftware.openfire.plugin.ofmeet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jitsi.videobridge.openfire.*;

import de.mxro.process.*;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.util.JiveGlobals;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.*;
import java.util.*;
import java.net.*;
import org.jitsi.util.OSUtils;
import java.util.Properties;

import de.mxro.process.*;
import org.jivesoftware.util.JiveGlobals;
import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;

import org.apache.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import net.sf.json.JSONObject;
import org.ice4j.StackProperties;
import org.ice4j.ice.harvest.MappingCandidateHarvesters;

/**
 * A wrapper object for the Jitsi Videobridge Openfire plugin.
 *
 * This wrapper can be used to instantiate/initialize and tearing down an instance of that plugin. An instance of this
 * class is re-usable.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class JitsiJvbWrapper implements ProcessListener
{
    private static final Logger Log = LoggerFactory.getLogger(JitsiJvbWrapper.class);
    public static JitsiJvbWrapper self;

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
        System.setProperty("ofmeet.jvb.started", "false");
        JitsiJvbWrapper self = this;

        jitsiPlugin = new PluginImpl();
        jitsiPlugin.initializePlugin( manager, pluginDirectory );

        final OFMeetConfig config = new OFMeetConfig();
        ensureJvbUser(config);

        final String jvbHomePath = pluginDirectory.getPath() + File.separator + "classes" + File.separator + "jvb";
        final String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
        final String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
        final String main_muc = JiveGlobals.getProperty( "ofmeet.main.muc", "conference." + domain);
        final String username =  config.getJvbName();
        final String password = config.getJvbPassword();
        final String ipAddress = getIpAddress();

        final String rest_port = JiveGlobals.getProperty( "ofmeet.videobridge.rest.port", "8188");
        final String public_port = JiveGlobals.getProperty( "httpbind.port.secure", "7443");

        String plain_port = JiveGlobals.getProperty( "ofmeet.websockets.plainport", "8180");
        if (rest_port.equals(plain_port)) plain_port = "8180";

        String local_ip = JiveGlobals.getProperty( PluginImpl.MANUAL_HARVESTER_LOCAL_PROPERTY_NAME, ipAddress);
        String public_ip = JiveGlobals.getProperty( PluginImpl.MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME, ipAddress);

        if (local_ip == null || local_ip.isEmpty()) local_ip = ipAddress;
        if (public_ip == null || public_ip.isEmpty()) public_ip = ipAddress;

        List<String> lines = Arrays.asList(
            "videobridge {",
            "",
            "    http-servers {",
            "        private {",
            "            port = " + rest_port,
            "        }",
            "        public {",
            "            port = " + plain_port,
            "        }",
            "    }",
            "",
            "    websockets {",
            "        server-id = ofmeet",
            "        enabled = true",
            "        domain = \"" + JiveGlobals.getProperty( "ofmeet.websockets.domain", domain) + ":" + JiveGlobals.getProperty( "ofmeet.websockets.publicport", public_port) + "\"",
            "        tls = true",
            "    }",
            "",
            "    ice {",
            "        tcp {",
            "            enabled = " + (RuntimeConfiguration.isTcpEnabled() ? "true" : "false"),
            "            port = \"" + RuntimeConfiguration.getTcpPort() + "\"",
            "            mapped-port = \"" + RuntimeConfiguration.getTcpMappedPort() +"\"",
            "        }",
            "        udp {",
            "            port = \"" + JiveGlobals.getProperty( PluginImpl.SINGLE_PORT_NUMBER_PROPERTY_NAME, "10000" ) + "\"",
            "            local-address = " + local_ip,
            "            public-address = " + public_ip,
            "        }",
            "    }",
            "",
            "    sctp {",
            "        # Whether SCTP data channels are enabled",
            "        enabled = " + (JiveGlobals.getBooleanProperty( "ofmeet.bridge.ws.channel", OSUtils.IS_WINDOWS) ? "false" : "true"),
            "    }",
            "",
            "",
            "    apis {",
            "        xmpp-client {",
            "            configs {",
            "                # Connect to the first XMPP server",
            "                shard {",
            "                    hostname= \"" + hostname + "\"",
            "                    domain = \"" + domain + "\"",
            "                    username = \"" + username + "\"",
            "                    password = \"" + password + "\"",
            "                    muc_jids = \"ofmeet@" + main_muc + "\"",
            "                    muc_nickname = \"" + username + "\"",
            "                    disable_certificate_verification = true",
            "                }",
            "            }",
            "        }",
            "        rest {",
            "            enabled = true",
            "        }",
            "    }",
            "",
            "    health {",
            "        interval = 300 seconds",
            "    }",
            "",
            "    stats {",
            "        # Enable broadcasting stats/presence in a MUC",
            "        enabled = true",
            "        transports = [",
            "            { type = \"muc\" }",
            "        ]",
            "    }",
            "",
            "    rest {",
            "        debug {",
            "            enabled = true",
            "        }",
            "        health {",
            "            enabled = true",
            "        }",
            "        shutdown {",
            "            enabled = true",
            "        }",
            "        version {",
            "            enabled = true",
            "         }",
            "    }",
            "",
            "}"
        );

        Path configFile = Paths.get(jvbHomePath + File.separator + "application.conf");
        try
        {
            Files.write(configFile, lines, Charset.forName("UTF-8"));
        } catch (Exception e) {
            Log.error("createConfigFile error", e);
        }

        String defaultOptions = "-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp";
        final String javaHome = System.getProperty("java.home");
        String javaExec = javaHome + File.separator + "bin" + File.separator + "java";

        if(OSUtils.IS_WINDOWS)
        {
            javaExec = javaExec + ".exe";
            defaultOptions = "";
        }

        final File props_file = new File(jvbHomePath + File.separator + "config" + File.separator + "sip-communicator.properties");
        writeProperties(props_file, local_ip, public_ip);


        final String customOptions = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.jvb.jvm.customOptions", defaultOptions);
        final String cmdLine = javaExec + " " + customOptions + " -Dconfig.file=" + configFile + " -Dnet.java.sip.communicator.SC_HOME_DIR_LOCATION=" + jvbHomePath + " -Dnet.java.sip.communicator.SC_HOME_DIR_NAME=config -Djava.util.logging.config.file=./logging.properties -Djdk.tls.ephemeralDHKeySize=2048 -cp ./jitsi-videobridge-2.1-SNAPSHOT.jar" + File.pathSeparator + "./jitsi-videobridge-2.1-SNAPSHOT-jar-with-dependencies.jar org.jitsi.videobridge.MainKt  --apis=rest";
        jvbThread = Spawn.startProcess(cmdLine, new File(jvbHomePath), this);

        Log.info( "Successfully initialized Jitsi Videobridge.\n" + cmdLine);
        Log.debug( "JVB config.\n" + String.join("\n", lines));
    }

    private void writeProperties( File props_file, String local_ip, String public_ip )
    {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(props_file));

            writeProperty(props, PluginImpl.INTERFACES_ALLOWED_PROPERTY_NAME);
            writeProperty(props, PluginImpl.ADDRESSES_ALLOWED_PROPERTY_NAME);
            writeProperty(props, PluginImpl.ADDRESSES_BLOCKED_PROPERTY_NAME );
            writeProperty(props, PluginImpl.INTERFACES_BLOCKED_PROPERTY_NAME );
            writeProperty(props, PluginImpl.AWS_HARVESTER_CONFIG_PROPERTY_NAME);
            writeProperty(props, PluginImpl.STUN_HARVESTER_ADDRESS_PROPERTY_NAME);
            writeProperty(props, PluginImpl.STUN_HARVESTER_PORT_PROPERTY_NAME);
            writeProperty(props, PluginImpl.MANUAL_HARVESTER_LOCAL_PROPERTY_NAME);
            writeProperty(props, PluginImpl.MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME);
            writeProperty(props, PluginImpl.SINGLE_PORT_ENABLED_PROPERTY_NAME );
            writeProperty(props, PluginImpl.SINGLE_PORT_NUMBER_PROPERTY_NAME );
            writeProperty(props, PluginImpl.MINMAX_PORT_ENABLED_PROPERTY_NAME );
            writeProperty(props, PluginImpl.MAX_PORT_NUMBER_PROPERTY_NAME );
            writeProperty(props, PluginImpl.MIN_PORT_NUMBER_PROPERTY_NAME );
            writeProperty(props, PluginImpl.TCP_ENABLED_PROPERTY_NAME );
            writeProperty(props, PluginImpl.TCP_MAPPED_PORT_PROPERTY_NAME );
            writeProperty(props, PluginImpl.TCP_PORT_PROPERTY_NAME );
            writeProperty(props, PluginImpl.TCP_SSLTCP_ENABLED_PROPERTY_NAME );

            props.setProperty( "org.jitsi.videobridge.octo.BIND_ADDRESS", local_ip);
            props.setProperty( "org.jitsi.videobridge.octo.PUBLIC_ADDRESS", public_ip);
            props.setProperty( "org.jitsi.videobridge.octo.BIND_PORT", JiveGlobals.getProperty("org.jitsi.videobridge.ofmeet.octo.port", "4096"));
            props.setProperty( "org.jitsi.videobridge.REGION", "region1");

            props.setProperty( "org.jitsi.videobridge.TRUST_BWE", Boolean.toString( !JiveGlobals.getBooleanProperty( "ofmeet.jicofo.force.vp9", false)));

            Log.debug("sip-communicator.properties");

            for (Object key: props.keySet()) {
                Log.debug(key + ": " + props.getProperty(key.toString()));
            }

            props.store(new FileOutputStream(props_file), "Jisti Video Bridge");
        } catch (Exception e) {
            Log.error("writeProperties", e);
        }
    }

    private void writeProperty(Properties props, String name)
    {
        String value = JiveGlobals.getProperty(name);

        switch ( name )
        {
            case PluginImpl.INTERFACES_ALLOWED_PROPERTY_NAME:
                final Collection<String> allowedInterfaces = JiveGlobals.getListProperty( PluginImpl.INTERFACES_ALLOWED_PROPERTY_NAME, null );
                if (allowedInterfaces != null) props.setProperty( StackProperties.ALLOWED_INTERFACES, String.join( ";", (List<String>) allowedInterfaces) );
                break;

            case PluginImpl.INTERFACES_BLOCKED_PROPERTY_NAME:
                final Collection<String> blockedInterfaces = JiveGlobals.getListProperty( PluginImpl.INTERFACES_BLOCKED_PROPERTY_NAME, null );
                if (blockedInterfaces != null) props.setProperty( StackProperties.BLOCKED_INTERFACES, String.join( ";", (List<String>) blockedInterfaces ) );
                break;

            case PluginImpl.ADDRESSES_ALLOWED_PROPERTY_NAME:
                final List<String> addressesAllowed = JiveGlobals.getListProperty( PluginImpl.ADDRESSES_ALLOWED_PROPERTY_NAME, null );
                if (addressesAllowed != null) props.setProperty( StackProperties.ALLOWED_ADDRESSES, String.join( ";", (List<String>) addressesAllowed ) );
                break;

            case PluginImpl.ADDRESSES_BLOCKED_PROPERTY_NAME:
                final List<String> addressesBlocked = JiveGlobals.getListProperty( PluginImpl.ADDRESSES_BLOCKED_PROPERTY_NAME, null );
                if (addressesBlocked != null) props.setProperty( StackProperties.BLOCKED_ADDRESSES, String.join( ";", (List<String>) addressesBlocked ) );
                break;

            case PluginImpl.AWS_HARVESTER_CONFIG_PROPERTY_NAME:
                if (value == null || value.isEmpty()) break;
                switch ( value ) {
                    case "disabled":
                        props.setProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER", "true" );
                        props.remove( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER" );
                        break;
                    case "forced":
                        props.setProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER", "false" );
                        props.setProperty( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER", "true" );
                        break;
                    default:
                        props.remove( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER" );
                        props.remove( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER" );
                        break;
                }
                break;

            case PluginImpl.STUN_HARVESTER_ADDRESS_PROPERTY_NAME:
            case PluginImpl.STUN_HARVESTER_PORT_PROPERTY_NAME: // intended fall-through;
                final String stunAddress = JiveGlobals.getProperty( PluginImpl.STUN_HARVESTER_ADDRESS_PROPERTY_NAME );
                final String stunPort = JiveGlobals.getProperty( PluginImpl.STUN_HARVESTER_PORT_PROPERTY_NAME );
                // Only set when both address and port are defined.
                if ( stunAddress != null && !stunAddress.isEmpty() && stunPort != null && !stunPort.isEmpty() )
                {
                    props.setProperty( "org.ice4j.ice.harvest.STUN_MAPPING_HARVESTER_ADDRESSES", stunAddress + ":" + stunPort );
                }
                break;

            case PluginImpl.MANUAL_HARVESTER_LOCAL_PROPERTY_NAME:
                if (value == null || value.isEmpty()) break;
                props.setProperty( "org.ice4j.ice.harvest.NAT_HARVESTER_LOCAL_ADDRESS", value );
                break;

            case PluginImpl.MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME:
                if (value == null || value.isEmpty()) break;
                props.setProperty( "org.ice4j.ice.harvest.NAT_HARVESTER_PUBLIC_ADDRESS", value );
                break;

            case PluginImpl.SINGLE_PORT_ENABLED_PROPERTY_NAME: // intended fall-through
            case PluginImpl.SINGLE_PORT_NUMBER_PROPERTY_NAME:
                if (value == null || value.isEmpty()) break;
                props.setProperty("org.jitsi.videobridge.SINGLE_PORT_HARVESTER_PORT",
                    JiveGlobals.getBooleanProperty( PluginImpl.SINGLE_PORT_ENABLED_PROPERTY_NAME, true )
                        ? JiveGlobals.getProperty( PluginImpl.SINGLE_PORT_NUMBER_PROPERTY_NAME, String.valueOf(RuntimeConfiguration.SINGLE_PORT_DEFAULT_VALUE))
                        : "-1"
                );
                break;

            case PluginImpl.MINMAX_PORT_ENABLED_PROPERTY_NAME:
                props.setProperty( "org.ice4j.ice.harvest.USE_DYNAMIC_HOST_HARVESTER", Boolean.toString( JiveGlobals.getBooleanProperty( PluginImpl.MINMAX_PORT_ENABLED_PROPERTY_NAME, false ) ) );
                break;

            case PluginImpl.MAX_PORT_NUMBER_PROPERTY_NAME:
                if (value == null || value.isEmpty()) break;
                props.setProperty("net.java.sip.communicator.service.media.MAX_PORT_NUMBER", value);
                break;

            case PluginImpl.MIN_PORT_NUMBER_PROPERTY_NAME:
                if (value == null || value.isEmpty()) break;
                props.setProperty("net.java.sip.communicator.service.media.MIN_PORT_NUMBER", value);
                break;

            case PluginImpl.TCP_ENABLED_PROPERTY_NAME:
                props.setProperty("org.jitsi.videobridge.DISABLE_TCP_HARVESTER", String.valueOf(!RuntimeConfiguration.isTcpEnabled()) );
                break;

            case PluginImpl.TCP_PORT_PROPERTY_NAME:
                if (value == null || value.isEmpty()) break;
                props.setProperty("org.jitsi.videobridge.TCP_HARVESTER_PORT", value  );
                break;

            case PluginImpl.TCP_MAPPED_PORT_PROPERTY_NAME:
                if (value == null || value.isEmpty()) break;
                props.setProperty( "org.jitsi.videobridge.TCP_HARVESTER_MAPPED_PORT", value );
                break;

            case PluginImpl.TCP_SSLTCP_ENABLED_PROPERTY_NAME:
                props.setProperty( "org.jitsi.videobridge.TCP_HARVESTER_SSLTCP", String.valueOf(!RuntimeConfiguration.isSslTcpEnabled()) );
                break;
        }
    }

    public String getIpAddress()
    {
        String ourHostname = XMPPServer.getInstance().getServerInfo().getHostname();
        String ourIpAddress = "127.0.0.1";

        try {
            ourIpAddress = InetAddress.getByName(ourHostname).getHostAddress();
        } catch (Exception e) {

        }

        return ourIpAddress;
    }

    public String getConferenceStats()
    {
        String server = getIpAddress();

        try {
            HttpClient client = new DefaultHttpClient();
            final String rest_port = JiveGlobals.getProperty( "ofmeet.videobridge.rest.port", "8188");
            HttpGet get = new HttpGet("http://localhost:" + rest_port + "/colibri/stats");
            HttpResponse response2 = client.execute(get);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response2.getEntity().getContent()));

            String line;
            String json = "";

            while ((line = rd.readLine()) != null) {
                json = json + line;
            }
            Log.debug( "getConferenceStats\n" + json );
            return json;

        } catch (Exception e) {
            Log.error("getConferenceStats " + server, e);
            return "{\"current_timestamp\":\"Initialising...\", \"total_conference_seconds\":0, \"total_participants\":0, \"total_failed_conferences\":0, \"total_conferences_created\":0, \"total_conferences_completed\":0, \"conferences\":0, \"participants\":0, \"largest_conference\":0, \"p2p_conferences\":0}";
        }

    }

    public synchronized void destroy() throws Exception
    {
        Log.debug( "Destroying jvb process." );

        if (jvbThread != null) jvbThread.destory();
        if (jitsiPlugin != null ) jitsiPlugin.destroyPlugin();

        Log.debug( "Successfully destroyed jvb process." );
    }

    public void onOutputLine(final String line)
    {
        Log.info("onOutputLine " + line);
    }

    public void onProcessQuit(int code)
    {
        Log.info("onProcessQuit " + code);
        System.setProperty("ofmeet.jvb.started", "false");
    }

    public void onOutputClosed() {
        Log.error("onOutputClosed");
    }

    public void onErrorLine(final String line)
    {
        Log.info(line);
        if (line.contains("Server.doStart: Started")) System.setProperty("ofmeet.jvb.started", "true");
    }

    public void onError(final Throwable t)
    {
        Log.error("Thread error", t);
    }

    private void ensureJvbUser(OFMeetConfig config)
    {
        final UserManager userManager = XMPPServer.getInstance().getUserManager();
        final String username =  config.getJvbName();

        if ( !userManager.isRegisteredUser( username ) )
        {
            Log.info( "No pre-existing 'jvb' user detected. Generating one." );
            String password = config.getJvbPassword();

            if ( password == null || password.isEmpty() )
            {
                password = StringUtils.randomString( 40 );
            }

            try
            {
                userManager.createUser( username, password, "JVB User (generated)", null);
                config.setJvbPassword( password );
            }
            catch ( Exception e )
            {
                Log.error( "Unable to provision a 'jvb' user.", e );
            }
        }
    }
}
