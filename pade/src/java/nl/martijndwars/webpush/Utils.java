package nl.martijndwars.webpush;

import com.google.common.io.BaseEncoding;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class Utils {
    /**
     * Get the uncompressed encoding of the public key point. The resulting array
     * should be 65 bytes length and start with 0x04 followed by the x and y
     * coordinates (32 bytes each).
     *
     * @param publicKey
     * @return
     */
    public static byte[] savePublicKey(ECPublicKey publicKey) {
        return publicKey.getQ().getEncoded(false);
    }

    public static byte[] savePrivateKey(ECPrivateKey privateKey) {
        return privateKey.getD().toByteArray();
    }

    /**
     * Base64-decode a string. Works for both url-safe and non-url-safe
     * encodings.
     *
     * @param base64Encoded
     * @return
     */
    public static byte[] base64Decode(String base64Encoded) {
        if (base64Encoded.contains("+") || base64Encoded.contains("/")) {
            return BaseEncoding.base64().decode(base64Encoded);
        } else {
            return BaseEncoding.base64Url().decode(base64Encoded);
        }
    }

    /**
     * Load the public key from a URL-safe base64 encoded string. Takes into
     * account the different encodings, including point compression.
     *
     * @param encodedPublicKey
     */
    public static PublicKey loadPublicKey(String encodedPublicKey) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedPublicKey = base64Decode(encodedPublicKey);

        KeyFactory kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);

        // prime256v1 is NIST P-256
        ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
        ECPoint point = ecSpec.getCurve().decodePoint(decodedPublicKey);
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);

        return kf.generatePublic(pubSpec);
    }

    /**
     * Load the private key from a URL-safe base64 encoded string
     *
     * @param encodedPrivateKey
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey loadPrivateKey(String encodedPrivateKey) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedPrivateKey = base64Decode(encodedPrivateKey);

        // prime256v1 is NIST P-256
        ECParameterSpec params = ECNamedCurveTable.getParameterSpec("prime256v1");
        ECPrivateKeySpec prvkey = new ECPrivateKeySpec(new BigInteger(decodedPrivateKey), params);
        KeyFactory kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);

        return kf.generatePrivate(prvkey);
    }
}
