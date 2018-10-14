package trabajogrado.broker.neuralnetwork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = {
        "http://localhost:4200"
})
public class NeuralNetworkController {

    private NeuralNetworkService neuralNetworkService;

    @Autowired
    public NeuralNetworkController(NeuralNetworkService neuralNetworkService) {
        this.neuralNetworkService = neuralNetworkService;
    }

    @PostMapping(value = "/neuralnetwork/clasificar_csv/{tipoClasificador}")
    public String clasificarCsv(
            @RequestBody MultipartFile zipFile,
            @RequestParam("cantidad_mensajes") int cantidadmensajes,
            @PathVariable("tipoClasificador") String tipoClasificador
    ) {
        return neuralNetworkService.clasificarCsv(zipFile, cantidadmensajes, tipoClasificador);
    }

    @PostMapping(value = "/neuralnetwork/clasificar_arff/{tipoClasificador}")
    public String clasificarArff(
            @RequestBody MultipartFile zipFile,
            @RequestParam("cantidad_mensajes") int cantidadMensajes,
            @RequestParam(value = "mostrar_tsv", required = false) boolean mostrarTsv,
            @PathVariable("tipoClasificador") String tipoClasificador
    ) {
        return neuralNetworkService.clasificarArff(zipFile, cantidadMensajes, mostrarTsv, tipoClasificador);
    }

    @PostMapping(value = "/neuralnetwork/clasificar_takeout/{tipoClasificador}")
    public String clasificarTakeout(
            @RequestBody MultipartFile zipFile,
            @RequestParam("cantidad_mensajes") int cantidadMensajes,
            @RequestParam(value = "mostrar_tsv", required = false) boolean mostrarTsv,
            @PathVariable("tipoClasificador") String tipoClasificador
    ) {
        return neuralNetworkService.clasificarTakeout(zipFile, cantidadMensajes, mostrarTsv, tipoClasificador);
    }

    @GetMapping(value = "/neuralnetwork/clasificar_lotr/{tipoClasificador}")
    public String clasificarLotr(
            @RequestParam("db_uri") String dbUri,
            @RequestParam("db_name") String dbName,
            @RequestParam("cantidad_chats") int cantidadChats,
            @RequestParam("cantidad_mensajes") int cantidadMensajes,
            @RequestParam(value = "mostrar_tsv", required = false) boolean mostrarTsv,
            @PathVariable("tipoClasificador") String tipoClasificador
    ) {
        return neuralNetworkService.clasificarLotr(dbUri, dbName, cantidadChats, cantidadMensajes, mostrarTsv, tipoClasificador);
    }
}
