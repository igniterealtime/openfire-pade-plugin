package org.jivesoftware.openfire.plugin.rest.sasl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.user.*;

import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import org.jitsi.util.OSUtils;
import org.ifsoft.sso.Password;

/**
 * A SaslServer implementation that is specific to OfChat.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class OfChatSaslServer implements SaslServer
{
    private final static Logger Log = LoggerFactory.getLogger( OfChatSaslServer.class );
    public static final String MECHANISM_NAME = "OFCHAT";
    private String authorizationID = null;

    public OfChatSaslServer()
    {

    }

    @Override
    public String getMechanismName()
    {
        return MECHANISM_NAME;
    }

    @Override
    public byte[] evaluateResponse( byte[] response ) throws SaslException
    {
        if ( response == null )
        {
            throw new IllegalArgumentException( "Argument 'response' cannot be null." );
        }

        Log.debug( "Parsing data from client response..." );

        final String data = new String( response, StandardCharsets.UTF_8);
        final StringTokenizer tokens = new StringTokenizer( data, ":");

        if ( tokens.countTokens() != 2 )
        {
            throw new SaslException( "Exactly two colon-separated values are expected (a username, followed by a TOTP token). Instead " +  tokens.countTokens() + " were found." );
        }

        final String username = tokens.nextToken();
        final String token = tokens.nextToken().trim();

        Log.debug("OFCHAT authentication " + username + ":" + token);

        try {
            User user = XMPPServer.getInstance().getUserManager().getUser(username);

            String base32Secret = user.getProperties().get("ofchat.totp.secret");
            String passcode = user.getProperties().get("ofchat.totp.passcode");

            if (base32Secret != null)
            {
                String code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);

                if (!token.equals(code))
                {
                    Log.debug("code=" + code + ", token=" + token);

                    // exception to be fixed when single Pade xmpp session is implemented
                    // allow old TOTP code if existing session used it.

                    if (SessionManager.getInstance().getSessions(username).size() == 0 || passcode == null || !token.equals(passcode))
                    {
                       throw new SaslException("TOTP authentication failure");
                    }
                }

                Log.debug( "Authentication successful for user " + username + ", code=" + code + ", token=" + token);
                user.getProperties().put("ofchat.totp.passcode", token);

            } else {

                if (Password.passwords.containsKey(username))     // SSO
                {
                    String passkey = Password.passwords.get(username).trim();

                    Log.debug("OFCHAT winsso authentication " + token + " " + passkey);

                    if (!token.equals(passkey))
                    {
                       throw new SaslException("Windows SSO authentication failure");
                    }

                    // TODO - can I keep this here for convienience?
                    //Password.passwords.remove(username);
                }
                else throw new SaslException("OFCHAT authentication failure");
            }

            authorizationID = username;
            return null;

        } catch (Exception e) {
            Log.error("OFCHAT authentication failure", e);
            throw new SaslException("OFCHAT authentication failure - " + e.toString());
        }
    }

    public boolean isComplete()
    {
        return true;
    }

    public String getAuthorizationID()
    {
        if ( !isComplete() )
        {
            throw new IllegalStateException( MECHANISM_NAME + " authentication has not completed." );
        }

        return authorizationID;
    }

    public Object getNegotiatedProperty( String propName )
    {
        if ( !isComplete() )
        {
            throw new IllegalStateException( MECHANISM_NAME + " authentication has not completed." );
        }

        if ( Sasl.QOP.equals( propName ) )
        {
            return "auth";
        }
        return null;
    }

    public void dispose() throws SaslException
    {
        authorizationID = null;
    }

    public byte[] unwrap( byte[] incoming, int offset, int len ) throws SaslException
    {
        if ( !isComplete() )
        {
            throw new IllegalStateException( MECHANISM_NAME + " authentication has not completed." );
        }

        throw new IllegalStateException( MECHANISM_NAME + " supports neither integrity nor privacy." );
    }

    public byte[] wrap( byte[] outgoing, int offset, int len ) throws SaslException
    {
        if ( !isComplete() )
        {
            throw new IllegalStateException( MECHANISM_NAME + " authentication has not completed." );
        }

        throw new IllegalStateException( MECHANISM_NAME + " supports neither integrity nor privacy." );
    }
}
