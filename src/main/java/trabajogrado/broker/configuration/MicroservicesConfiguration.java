package trabajogrado.broker.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@PropertySource("classpath:microservices.properties")
public class MicroservicesConfiguration {

    @Value("${results.address}")
    private String resultsAddress;

    @Value("${results.address.docker}")
    private String resultsAddressDocker;

    @Value("${results.port}")
    private int resultsPort;

    @Value("${poo.address}")
    private String pooAddress;

    @Value("${poo.address.docker}")
    private String pooAddressDocker;

    @Value("${poo.port}")
    private int pooPort;

    @Value("${nn.address}")
    private String nnAddress;

    @Value("${nn.address.docker}")
    private String nnAddressDocker;

    @Value("${nn.port}")
    private int nnPort;

    @Value("${roles.address}")
    private String rolesAddress;

    @Value("${roles.address.docker}")
    private String rolesAddressDocker;

    @Value("${roles.port}")
    private int rolesPort;

    @Autowired
    private ApplicationArguments applicationArguments;

    public String getResultsAddress() {
        return resultsAddress;
    }

    public int getResultsPort() {
        return resultsPort;
    }

    public String getPooAddress() {
        return pooAddress;
    }

    public int getPooPort() {
        return pooPort;
    }

    public String getNnAddress() {
        return nnAddress;
    }

    public int getNnPort() {
        return nnPort;
    }

    public String getResultsAddressDocker() {
        return resultsAddressDocker;
    }

    public String getNnAddressDocker() {
        return nnAddressDocker;
    }

    public String getPooAddressDocker() {
        return pooAddressDocker;
    }

    public String getRolesAddress() {
        return rolesAddress;
    }

    public String getRolesAddressDocker() {
        return rolesAddressDocker;
    }

    public int getRolesPort() {
        return rolesPort;
    }

    public boolean dockerized() {
        if (applicationArguments.getSourceArgs().length == 0)
            return false;

        return applicationArguments.getSourceArgs()[0].equals("dockerized");
    }
}
