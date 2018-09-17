package trabajogrado.broker.neuralnetwork;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trabajogrado.broker.configuration.MicroservicesConfiguration;

@Service
public class NeuralNetworkService {

    private static String URL_CLASIFICAR_LOTE = "http://%s:%s/clasificar_lote";

    private MicroservicesConfiguration configuration;

    @Autowired
    public NeuralNetworkService(MicroservicesConfiguration configuration) {
        this.configuration = configuration;
    }

    public String clasificarLote(String csvPlano) {
        String url = String.format(URL_CLASIFICAR_LOTE, configuration.getNnAddress(), configuration.getNnPort());

        try {
            return Unirest.post(url)
                    .body(csvPlano)
                    .asString().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("clasificarLote: Error en la comunicación con el microservicio");
    }
}
