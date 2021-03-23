package org.ifsoft.meet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "vapid")
public class PublicKey {

    private String publicKey;

    public PublicKey() {
    }

    public PublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }


    @XmlElement
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}