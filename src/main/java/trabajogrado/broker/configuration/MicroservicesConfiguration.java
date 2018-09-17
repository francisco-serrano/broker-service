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

    public String getResultsAddress() {
        return resultsAddress;
    }

    public int getResultsPort() {
        return resultsPort;
    }
}
