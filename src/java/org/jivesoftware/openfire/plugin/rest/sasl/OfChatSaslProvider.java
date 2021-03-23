package org.jivesoftware.openfire.plugin.rest.sasl;

import java.security.Provider;

/**
 * A Provider implementation for an OfChat-specific SASL mechanisms.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class OfChatSaslProvider extends Provider
{
    /**
     * The provider name.
     */
    public static final String NAME = "OfChatSasl";

    /**
     * The provider version number.
     */
    public static final double VERSION = 1.0;

    /**
     * A description of the provider and its services.
     */
    public static final String INFO = "OfChat-specific SASL mechansims.";

    public OfChatSaslProvider()
    {
        super( NAME, VERSION, INFO );

        put( "SaslServerFactory." + OfChatSaslServer.MECHANISM_NAME, OfChatSaslServerFactory.class.getCanonicalName() );
    }
}