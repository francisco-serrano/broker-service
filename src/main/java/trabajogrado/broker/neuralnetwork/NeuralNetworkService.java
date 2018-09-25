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
    private static String URL_GENERAR_CSV_LOTR = "http://%s:%s/obtener_lotr_csv?db_uri=%s&db_name=%s&cantidad_chats=%s";

    private static String URL_GENERAR_PERFILES_JSON = "http://%s:%s/generar_perfiles_json?comes_from_json=%s";
    private static String URL_GENERAR_PERFILES_TSV = "http://%s:%s/generar_perfiles_tsv";

    private MicroservicesConfiguration configuration;

    @Autowired
    public NeuralNetworkService(MicroservicesConfiguration configuration) {
        this.configuration = configuration;
    }

    public String clasificarArff(MultipartFile zipFile, int cantidadMensajes, boolean mostrarTsv) {
        String urlConversionCsv = String.format(URL_GENERAR_CSV_ARFF,
                configuration.getPooAddress(),
                configuration.getPooPort()
        );

        String response = null;

        try {
            File zipFileTemp = new File("/tmp/file.zip");
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipFileTemp));
            bos.write(zipFile.getBytes());
            bos.close();

            response = Unirest.post(urlConversionCsv)
                    .field("zipFile", zipFileTemp)
                    .asString().getBody();

            if (zipFileTemp.delete())
                System.out.println("zipFileTemp eliminado");

        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }

        assert response != null;

        response = classifyCsv(response, cantidadMensajes);

        assert response != null;

        return generateProfiles(response, mostrarTsv);
    }

    public String clasificarTakeout(MultipartFile zipFile, int cantidadMensajes, boolean mostrarTsv) {
        String urlConversionCsv = String.format(URL_GENERAR_CSV_TAKEOUT,
                configuration.getPooAddress(),
                configuration.getPooPort()
        );

        String response = null;

        try {
            File zipFileTemp = new File("/tmp/file.zip");
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipFileTemp));
            bos.write(zipFile.getBytes());
            bos.close();

            response = Unirest.post(urlConversionCsv)
                    .field("zipFile", zipFileTemp)
                    .asString().getBody();

            if (zipFileTemp.delete())
                System.out.println("zipFileTemp eliminadooo");
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }

        assert response != null;

        response = classifyCsv(response, cantidadMensajes);

        assert response != null;

        return generateProfiles(response, mostrarTsv);
    }

    public String clasificarLotr(String dbUri, String dbName, int cantidadChats, int cantidadMensajes, boolean mostrarTsv) {
        String urlConversionCsv = String.format(URL_GENERAR_CSV_LOTR,
                configuration.getPooAddress(),
                configuration.getPooPort(),
                dbUri,
                dbName,
                cantidadChats
        );

        Unirest.setTimeouts(120000 * cantidadChats, 240000 * cantidadChats);

        String response = null;

        try {
            response = Unirest.get(urlConversionCsv)
                    .asString().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        assert response != null;

        response = classifyCsv(response, cantidadMensajes);

        assert response != null;

        return generateProfiles(response, mostrarTsv);
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
                System.out.println("csvFileTemp eliminado");

            return csvContent;
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("classifyCsv: Error en la comunicación con el microservicio");
    }

    private String generateProfiles(String classifiedCsv, boolean mostrarTsv) {
        String urlGeneracionPerfil = mostrarTsv ?
                String.format(
                        URL_GENERAR_PERFILES_TSV,
                        configuration.getPooAddress(),
                        configuration.getPooPort()
                ) :
                String.format(
                        URL_GENERAR_PERFILES_JSON,
                        configuration.getPooAddress(),
                        configuration.getPooPort(),
                        mostrarTsv
                );

        try {
            File csvFileTempPerfiles = new File("/tmp/file.csv");
            FileOutputStream fos = new FileOutputStream(csvFileTempPerfiles);
            fos.write(classifiedCsv.getBytes());

            classifiedCsv = Unirest.post(urlGeneracionPerfil)
                    .field("csvFile", csvFileTempPerfiles)
                    .asString().getBody();

            if (csvFileTempPerfiles.delete())
                System.out.println("csvFileTempPerfiles eliminado");

            return classifiedCsv;
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("clasificarArff: Error en la generación de perfiles");
    }
}
