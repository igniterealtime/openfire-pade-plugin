package nl.martijndwars.webpush.cli.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Generate a VAPID keypair")
public class GenerateKeyCommand {

    @Parameter(names = "--publicKeyFile", description = "File to write keypair to.")
    private String publicKeyFile;

    public Boolean hasPublicKeyFile() {
        return publicKeyFile != null;
    }

    public String getPublicKeyFile() {
        return publicKeyFile;
    }

}
