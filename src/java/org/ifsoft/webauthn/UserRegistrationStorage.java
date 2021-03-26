package org.ifsoft.webauthn;

import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.JiveGlobals;

import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.json.*;
import org.jivesoftware.openfire.plugin.rest.dao.PropertyDAO;

public class UserRegistrationStorage implements CredentialRepository {

  private Logger Log = LoggerFactory.getLogger(UserRegistrationStorage.class);
  private UserManager userManager = XMPPServer.getInstance().getUserManager(); 

  public void addCredential(String username, byte[] userId, byte[] credentialId, byte[] publicKeyCose) {
	Map<String, String> properties = getUserProperties(username);

	if (properties != null)
	{
		JSONObject json = new JSONObject();
		String keyId = (new ByteArray(credentialId)).getBase64Url();
		json.put("userId", BytesUtil.bytesToLong(userId));
		json.put("credentialId", keyId);		
		json.put("publicKeyCose", (new ByteArray(publicKeyCose)).getBase64Url());			
		properties.put("webauthn-" + userId, json.toString());
		properties.put("webauthn-" + keyId, json.toString());		
	}
  }  

  @Override
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
    Set<PublicKeyCredentialDescriptor> result = new HashSet<>();	
	Map<String, String> properties = getUserProperties(username);

	if (properties != null)
	{
		for (String key : properties.keySet())
		{
			if (key.startsWith("webauthn-"))
			{
				try {
					JSONObject json = new JSONObject(properties.get(key));
					ByteArray credentialId = ByteArray.fromBase64Url(json.getString("credentialId"));
					PublicKeyCredentialDescriptor descriptor = PublicKeyCredentialDescriptor.builder().id(credentialId).build();
					result.add(descriptor);	
				}
				catch (Exception e) {
					Log.warn( "getCredentialIdsForUsername", e );
				}				
			}
		}
	}	
    return result;	
  }

  @Override
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    long userId = BytesUtil.bytesToLong(userHandle.getBytes());	  

	try {	
		List<String> usernames = PropertyDAO.getUsernameByProperty("webauthn-" + userId, null);	
		
		if (usernames.size() > 0)
		{
			for (String username : usernames) {
				return Optional.of(username);
			}
		}	
	}
	catch (Exception e) {
		Log.warn( "getUsernameForUserHandle", e );
	}		
    return Optional.empty();
  }

  @Override
  public Optional<ByteArray> getUserHandleForUsername(String username) {
	Map<String, String> properties = getUserProperties(username);

	if (properties != null)
	{
		for (String key : properties.keySet())
		{
			if (key.startsWith("webauthn-"))
			{
				try {
					JSONObject json = new JSONObject(properties.get(key));
					Long id = json.getLong("userId");
					return Optional.of(new ByteArray(BytesUtil.longToBytes(id)));	
				}
				catch (Exception e) {
					Log.warn( "getCredentialIdsForUsername", e );
				}				
			}
		}
	}			  
    return Optional.empty();
  }


  @Override
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    long userId = BytesUtil.bytesToLong(userHandle.getBytes());	  
	String key = "webauthn-" + credentialId.getBase64Url();	
	
	try {	
		List<String> usernames = PropertyDAO.getUsernameByProperty(key, null);	
		
		if (usernames.size() > 0)
		{
			for (String username : usernames) {
				Map<String, String> properties = getUserProperties(username);

				if (properties != null)
				{
					JSONObject json = new JSONObject(properties.get(key));	
					Long userId2 = json.getLong("userId");
					
					if (userId2.equals(userId))
					{
						ByteArray id = new ByteArray(BytesUtil.longToBytes(userId2));
						ByteArray keyId = ByteArray.fromBase64Url(json.getString("credentialId"));
						ByteArray publicKeyCose = ByteArray.fromBase64Url(json.getString("publicKeyCose"));					
						
						return Optional.of(RegisteredCredential.builder()
						  .credentialId(keyId)
						  .userHandle(id)
						  .publicKeyCose(publicKeyCose)
						  .signatureCount(1).build());
					}
				}
			}
		}	
	}
	catch (Exception e) {
		Log.warn( "getUsernameForUserHandle", e );
	}		
    return Optional.empty();	    
  }

  @Override
  public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
    Set<RegisteredCredential> result = new HashSet<>();
	String key = "webauthn-" + credentialId.getBase64Url();	
	
	try {	
		List<String> usernames = PropertyDAO.getUsernameByProperty(key, null);	
		
		if (usernames.size() > 0)
		{
			for (String username : usernames) {
				Map<String, String> properties = getUserProperties(username);

				if (properties != null)
				{
					JSONObject json = new JSONObject(properties.get(key));	
					Long userId = json.getLong("userId");
					ByteArray id = new ByteArray(BytesUtil.longToBytes(userId));
					ByteArray keyId = ByteArray.fromBase64Url(json.getString("credentialId"));
					ByteArray publicKeyCose = ByteArray.fromBase64Url(json.getString("publicKeyCose"));					
					
					result.add(RegisteredCredential.builder()
					  .credentialId(keyId)
					  .userHandle(id)
					  .publicKeyCose(publicKeyCose)
					  .signatureCount(1).build());
				}
			}
		}	
	}
	catch (Exception e) {
		Log.warn( "getUsernameForUserHandle", e );
	}
	return result;
  }
  
  private Map<String, String> getUserProperties(String username)  {	  
	try {
		User user = userManager.getUser(username);
		return user.getProperties();
	}
	catch (Exception e) {
		Log.warn( "user not found " + username, e );
		return null;
	}	  
  }
}