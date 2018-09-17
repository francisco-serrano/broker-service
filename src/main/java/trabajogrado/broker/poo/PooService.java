package trabajogrado.broker.poo;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import trabajogrado.broker.configuration.MicroservicesConfiguration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class PooService {

    private static final String CLASSIFY_ARFF_URL = "http://%s:%s/clasificar_arff";
    private static final String CLASSIFY_TAKEOUT_URL = "http://%s:%s/clasificar_takeout";
    private static final String CLASSIFY_LOTR_URL = "http://%s:%s/clasificar_lotr?db_uri=%s&db_name=%s&cantidad_chats=%s";

    private MicroservicesConfiguration configuration;

    @Autowired
    public PooService(MicroservicesConfiguration configuration) {
        this.configuration = configuration;
    }

    public String classifyArff(MultipartFile zipFile) {
        String url = String.format(CLASSIFY_ARFF_URL, configuration.getPooAddress(), configuration.getPooPort());

        return sendZipFileRequest(url, zipFile);
    }

    public String classifyTakeout(MultipartFile zipFile) {
        String url = String.format(CLASSIFY_TAKEOUT_URL, configuration.getPooAddress(), configuration.getPooPort());

        return sendZipFileRequest(url, zipFile);
    }

    public String classifyLotr(String dbUri, String dbName, int cantidadChats) {
        String url = String.format(CLASSIFY_LOTR_URL, configuration.getPooAddress(), configuration.getPooPort(), dbUri, dbName, cantidadChats);

        Unirest.setTimeouts(20000 * cantidadChats, 60000 * cantidadChats);

        try {
            return Unirest.get(url)
                    .asString().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("classifyLotr: Error en la comunicación con el servicio de resultados");
    }

    private String sendZipFileRequest(String url, MultipartFile zipFile) {
        // TODO: Parametrizar
        // Una hora de máximo para la conexión, dos para el socket
        Unirest.setTimeouts(3600000, 3600000 * 2);

        try {
            File zipFileTemp = new File("/tmp/file.zip");
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipFileTemp));
            bos.write(zipFile.getBytes());
            bos.close();

            String response = Unirest.post(url)
                    .field("zipFile", zipFileTemp)
                    .asString().getBody();

            if (zipFileTemp.delete())
                System.out.println("zipFileTemp eliminado");

            return response;
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("sendZipFileRequest: Error en la comunicación con el servicio de resultados");
    }

}
