package trabajogrado.broker.neuralnetwork;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Joiner;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import trabajogrado.broker.configuration.MicroservicesConfiguration;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class NeuralNetworkService {

    private static String URL_CLASIFICAR_LOTE = "http://%s:%s/clasificar_csv/%s?cantidad_mensajes=%s&integrante=%s";

    private static String URL_GENERAR_CSV_ARFF = "http://%s:%s/obtener_arff_csv";
    private static String URL_GENERAR_CSV_TAKEOUT = "http://%s:%s/obtener_takeout_csv";
    private static String URL_GENERAR_CSV_LOTR = "http://%s:%s/obtener_lotr_csv?db_uri=%s&db_name=%s&cantidad_chats=%s";

    private static String URL_GENERAR_PERFILES_JSON = "http://%s:%s/generar_perfiles_json?comes_from_json=%s";
    private static String URL_GENERAR_PERFILES_TSV = "http://%s:%s/generar_perfiles_tsv";

    private MicroservicesConfiguration configuration;

    private String addressNN;
    private int portNN;
    private String addressPoo;
    private int portPoo;


    @Autowired
    public NeuralNetworkService(MicroservicesConfiguration configuration) {
        this.configuration = configuration;
        this.addressNN = configuration.dockerized() ? configuration.getNnAddressDocker() : configuration.getNnAddress();
        this.portNN = configuration.getNnPort();
        this.addressPoo = configuration.dockerized() ? configuration.getPooAddressDocker() : configuration.getPooAddress();
        this.portPoo = configuration.getPooPort();
    }

    // TODO: PROVISORIO -> Refactorización por duplicación de funcionalidad
    public String clasificarCsv(MultipartFile zipFile, int cantidadMensajes, String tipoClasificador, String integrante) {

        File zipFileTemp = new File("/tmp/file.zip");
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipFileTemp));
            bos.write(zipFile.getBytes());
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<File> fileList = obtainFilesWithinZip(zipFile);

        String csvGrande = "chatId,timestamp,integrante,mensaje\n" + fileList.stream()
                .map(csvFile -> {
                    try {
                        List<String[]> aux = new CSVReader(new FileReader(csvFile)).readAll();
                        aux.remove(0); // Borro el encabezado

                        return aux;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    throw new RuntimeException(String.format("Error al leer %s", csvFile.getName()));
                })
                .flatMap(Collection::stream)
                .map(fields -> Joiner.on(',').join(fields) + '\n')
                .collect(Collectors.joining());

        return classifyCsv(csvGrande, cantidadMensajes, tipoClasificador, integrante);
    }

    // TODO: PROVISORIO -> Refactorización por duplicación de funcionalidad
    private static List<File> obtainFilesWithinZip(MultipartFile zippedFile) {
        List<File> arffFileList = new ArrayList<>();

        File zippedTempFile = new File(String.format("temp_%s.zip", LocalDateTime.now().getNano()));

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zippedTempFile));
            bos.write(zippedFile.getBytes());
            bos.close();

            byte[] buffer = new byte[1024];

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zippedTempFile));

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File unzippedFile = new File(zipEntry.getName());
                FileOutputStream fos = new FileOutputStream(unzippedFile);

                int len;
                while ((len = zis.read(buffer)) > 0)
                    fos.write(buffer, 0, len);

                fos.close();
                zipEntry = zis.getNextEntry();

                arffFileList.add(unzippedFile);
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (zippedTempFile.delete())
            System.out.println(zippedTempFile.getName() + " deleted");

        return arffFileList;
    }

    public String clasificarArff(MultipartFile zipFile, int cantidadMensajes, boolean mostrarTsv, String tipoClasificador, String integrante) {
        String urlConversionCsv = String.format(URL_GENERAR_CSV_ARFF, addressPoo, portPoo);

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

        response = classifyCsv(response, cantidadMensajes, tipoClasificador, integrante);

        assert response != null;

        return generateProfiles(response, mostrarTsv);
    }

    public String clasificarTakeout(MultipartFile zipFile, int cantidadMensajes, boolean mostrarTsv, String tipoClasificador, String integrante) {
        String urlConversionCsv = String.format(URL_GENERAR_CSV_TAKEOUT, addressPoo, portPoo);

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

        response = classifyCsv(response, cantidadMensajes, tipoClasificador, integrante);

        assert response != null;

        return generateProfiles(response, mostrarTsv);
    }

    public String clasificarLotr(
            String dbUri, String dbName, int cantidadChats, int cantidadMensajes, boolean mostrarTsv, String tipoClasificador, String integrante
    ) {
        String urlConversionCsv = String.format(URL_GENERAR_CSV_LOTR, addressPoo, portPoo, dbUri, dbName, cantidadChats);

        Unirest.setTimeouts(120000 * cantidadChats, 240000 * cantidadChats);

        String response = null;

        try {
            response = Unirest.get(urlConversionCsv)
                    .asString().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        assert response != null;

        response = classifyCsv(response, cantidadMensajes, tipoClasificador, integrante);

        assert response != null;

        return generateProfiles(response, mostrarTsv);
    }

    private String classifyCsv(String csvContent, int cantidadMensajes, String tipoClasificador, String integrante) {
        String urlClasificacion = String.format(URL_CLASIFICAR_LOTE,
                addressNN, portNN, tipoClasificador, cantidadMensajes, integrante
        ).replace(' ', '+');

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
                String.format(URL_GENERAR_PERFILES_TSV, addressPoo, portPoo) :
                String.format(URL_GENERAR_PERFILES_JSON, addressPoo, portPoo, mostrarTsv);

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
