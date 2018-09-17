package trabajogrado.broker.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@PropertySource("classpath:microservices.properties")
public class MicroservicesConfiguration {

    @Value("${results.address}")
    private String resultsAddress;

    @Value("${results.port}")
    private int resultsPort;

    @Value("${poo.address}")
    private String pooAddress;

    @Value("${poo.port}")
    private int pooPort;

    @Value("${nn.address}")
    private String nnAddress;

    @Value("${nn.port}")
    private int nnPort;

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
}
