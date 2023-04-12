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
        rootKeystorePassword = dotenv.get("ROOT_KEYSTORE_PASSWORD");
        otherKeystorePassword = dotenv.get("OTHER_KEYSTORE_PASSWORD");
        otherKeystorePath = dotenv.get("OTHER_KEYSTORE_PATH");
        rootKeystorePath = dotenv.get("ROOT_KEYSTORE_PATH");
    }
}
