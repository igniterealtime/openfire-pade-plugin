package nl.martijndwars.webpush.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import nl.martijndwars.webpush.cli.commands.GenerateKeyCommand;
import nl.martijndwars.webpush.cli.commands.SendNotificationCommand;
import nl.martijndwars.webpush.cli.handlers.GenerateKeyHandler;
import nl.martijndwars.webpush.cli.handlers.SendNotificationHandler;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * Command-line interface
 */
public class Cli {
    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());

        GenerateKeyCommand generateKeyCommand = new GenerateKeyCommand();
        SendNotificationCommand sendNotificationCommand = new SendNotificationCommand();

        JCommander jCommander = JCommander.newBuilder()
            .addCommand("generate-key", generateKeyCommand)
            .addCommand("send-notification", sendNotificationCommand)
            .build();

        try {
            jCommander.parse(args);

            if (jCommander.getParsedCommand() != null) {
                switch (jCommander.getParsedCommand()) {
                    case "generate-key":
                        new GenerateKeyHandler(generateKeyCommand).run();
                        break;
                    case "send-notification":
                        new SendNotificationHandler(sendNotificationCommand).run();
                        break;
                }
            } else {
                jCommander.usage();
            }
        } catch (ParameterException e) {
            e.usage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
