package trabajogrado.broker.results;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import trabajogrado.broker.configuration.MicroservicesConfiguration;

import java.io.*;

@Service
public class ResultsService {

    // http://<address>:<port>/<nombre_tabla>
    private static final String GET_ALL_USERS_URL = "http://%s:%s/%s";

    // http://<address>:<port>/<nombre_tabla>/<nombre_usuario>
    private static final String GET_USER_URL = "http://%s:%s/%s/%s";

    private static final String ADD_CONV_URL = "http://%s:%s/{tablename}";

    private static final String GET_TABLES_URL = "http://%s:%s/tables";

    private MicroservicesConfiguration configuration;
    private String address;
    private int port;

    @Autowired
    public ResultsService(MicroservicesConfiguration configuration) {
        this.configuration = configuration;
        this.address = configuration.dockerized() ? configuration.getResultsAddressDocker() : configuration.getResultsAddress();
        this.port = configuration.getResultsPort();
    }

    public String getTables() {
        String url = String.format(GET_TABLES_URL, address, port);

        try {
            return Unirest.get(url).asString().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("getTables: Error en la comunicación con el servicio de resultados");
    }

    public String getAllUsers(String table) {
//        String url = String.format(GET_ALL_USERS_URL,
//                configuration.getResultsAddress(),
//                configuration.getResultsPort(),
//                table
//        );

        String url = String.format(GET_ALL_USERS_URL, address, port, table);

        try {
            return Unirest.get(url).asString().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("getAllUsers: Error en la comunicación con el servicio de resultados");
    }

    public String getUser(String table, String user) {
//        String url = String.format(GET_USER_URL,
//                configuration.getResultsAddress(),
//                configuration.getResultsPort(),
//                table,
//                user
//        );
        String url = String.format(GET_USER_URL, address, port, table, user);

        try {
            return Unirest.get(url).asString().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("getUser: Error en la comunicación con el servicio de resultados");
    }

    public String addConversation(MultipartFile csvFile, String tablename) {
//        String url = String.format(ADD_CONV_URL,
//                configuration.getResultsAddress(),
//                configuration.getResultsPort()
//        );

        String url = String.format(ADD_CONV_URL, address, port);

        try {
            File csvTemp = new File("temp.csv");
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(csvTemp));
            bos.write(csvFile.getBytes());
            bos.close();

            String response = Unirest.post(url)
                    .routeParam("tablename", tablename)
                    .field("csv_file", csvTemp)
                    .asString().getBody();

            if (csvTemp.delete())
                System.out.println("csvTemp eliminado");

            return response;
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("addConversation: Error en la comunicación con el servicio de resultados");
    }
}
