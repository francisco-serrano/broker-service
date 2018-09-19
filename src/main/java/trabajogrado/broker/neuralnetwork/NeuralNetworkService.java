package trabajogrado.broker.neuralnetwork;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import trabajogrado.broker.configuration.MicroservicesConfiguration;

import java.io.*;

@Service
public class NeuralNetworkService {

    private static String URL_CLASIFICAR_LOTE = "http://%s:%s/clasificar_csv?cantidad_mensajes=%s";
    private static String URL_GENERAR_CSV_ARFF = "http://%s:%s/obtener_arff_csv";
    private static String URL_GENERAR_CSV_TAKEOUT = "http://%s:%s/obtener_takeout_csv";

    private MicroservicesConfiguration configuration;

    @Autowired
    public NeuralNetworkService(MicroservicesConfiguration configuration) {
        this.configuration = configuration;
    }

    public String clasificarArff(MultipartFile zipFile, int cantidadMensajes) {

        String urlConversionFormato = String.format(URL_GENERAR_CSV_ARFF, configuration.getPooAddress(), configuration.getPooPort());

        String response = null;

        try {
            File zipFileTemp = new File("/tmp/file.zip");
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipFileTemp));
            bos.write(zipFile.getBytes());
            bos.close();

            response = Unirest.post(urlConversionFormato)
                    .field("zipFile", zipFileTemp)
                    .asString().getBody();

            if (zipFileTemp.delete())
                System.out.println("zipFileTemp eliminadooooo");

        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }

        assert response != null;

        return classifyCsv(response, cantidadMensajes);
    }

    public String clasificarTakeout(MultipartFile zipFile, int cantidadMensajes) {
        String url = String.format(URL_GENERAR_CSV_TAKEOUT, configuration.getPooAddress(), configuration.getPooPort());

        String response = null;

        try {
            File zipFileTemp = new File("/tmp/file.zip");
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipFileTemp));
            bos.write(zipFile.getBytes());
            bos.close();

            response = Unirest.post(url)
                    .field("zipFile", zipFileTemp)
                    .asString().getBody();

            if (zipFileTemp.delete())
                System.out.println("zipFileTemp eliminadooo");
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }

        assert response != null;

        return classifyCsv(response, cantidadMensajes);
    }

    private String classifyCsv(String csvContent, int cantidadMensajes) {
        String urlClasificacion = String.format(URL_CLASIFICAR_LOTE, configuration.getNnAddress(), configuration.getNnPort(), cantidadMensajes);

        Unirest.setTimeouts(1000 * cantidadMensajes, 2000 * cantidadMensajes);

        try {
            File csvFileTemp = new File("/tmp/file.csv");
            FileWriter writer = new FileWriter(csvFileTemp);
            writer.write(csvContent);
            writer.close();

            csvContent = Unirest.post(urlClasificacion)
                    .field("csv_file", csvFileTemp)
                    .asString().getBody();

            if (csvFileTemp.delete())
                System.out.println("csvFileTemp eliminadooooo");

            return csvContent;
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("classifyCsv: Error en la comunicación con el microservicio");
    }
}
