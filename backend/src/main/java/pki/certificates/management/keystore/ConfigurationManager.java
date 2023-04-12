package pki.certificates.management.keystore;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ConfigurationManager {

    private Dotenv dotenv;
    private String rootKeystorePassword;
    private String otherKeystorePassword;
    private String otherKeystorePath;
    private String rootKeystorePath;

    public ConfigurationManager() {
        dotenv = Dotenv.load();
        rootKeystorePassword = "password";
        otherKeystorePassword = "password";
        otherKeystorePath = "src/main/resources/static/other-keystore.jks";
        rootKeystorePath = "src/main/resources/static/root-keystore.jks";
    }
}
