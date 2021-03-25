package org.ifsoft.webauthn;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRegistrationStorage implements CredentialRepository {

  private Logger logger = LoggerFactory.getLogger(UserRegistrationStorage.class);

  @Override
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
    Set<PublicKeyCredentialDescriptor> result = new HashSet<>();
    return result;	
  }

  @Override
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    return Optional.empty();
  }

  @Override
  public Optional<ByteArray> getUserHandleForUsername(String username) {
    return Optional.empty();
  }


  @Override
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    return Optional.empty();	  
  }

  @Override
  public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
    Set<RegisteredCredential> result = new HashSet<>();
    return result;
  }
}