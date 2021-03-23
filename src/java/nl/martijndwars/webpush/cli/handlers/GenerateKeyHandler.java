package nl.martijndwars.webpush.cli.handlers;

import com.google.common.io.BaseEncoding;
import nl.martijndwars.webpush.Utils;
import nl.martijndwars.webpush.cli.commands.GenerateKeyCommand;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.*;
import java.security.*;

public class GenerateKeyHandler implements HandlerInterface {
    private GenerateKeyCommand generateKeyCommand;

    public GenerateKeyHandler(GenerateKeyCommand generateKeyCommand) {
        this.generateKeyCommand = generateKeyCommand;
    }

    @Override public void run() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        KeyPair keyPair = generateKeyPair();

        byte[] publicKey = Utils.savePublicKey((ECPublicKey) keyPair.getPublic());
        byte[] privateKey = Utils.savePrivateKey((ECPrivateKey) keyPair.getPrivate());

        if (generateKeyCommand.hasPublicKeyFile()) {
            writeKey(keyPair.getPublic(), new File(generateKeyCommand.getPublicKeyFile()));
        }

        System.out.println("PublicKey:");
        System.out.println(BaseEncoding.base64Url().encode(publicKey));

        System.out.println("PrivateKey:");
        System.out.println(BaseEncoding.base64Url().encode(privateKey));
    }

    /**
     * Generate an EC keypair on the prime256v1 curve.
     *
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    private KeyPair generateKeyPair() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("prime256v1");

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
        keyPairGenerator.initialize(parameterSpec);

        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Write the given key to the given file.
     *
     * @param key
     * @param file
     */
    private void writeKey(Key key, File file) throws IOException {
        file.createNewFile();

        try (PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            PemObject pemObject = new PemObject("Key", key.getEncoded());

            pemWriter.writeObject(pemObject);
        }
    }
}
