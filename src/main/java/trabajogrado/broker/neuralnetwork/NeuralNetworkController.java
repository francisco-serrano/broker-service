package trabajogrado.broker.neuralnetwork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class NeuralNetworkController {

    private NeuralNetworkService neuralNetworkService;

    @Autowired
    public NeuralNetworkController(NeuralNetworkService neuralNetworkService) {
        this.neuralNetworkService = neuralNetworkService;
    }

    @PostMapping(value = "/neuralnetwork/clasificar_arff")
    public String clasificarArff(
            @RequestBody MultipartFile zipFile,
            @RequestParam("cantidad_mensajes") int cantidadMensajes
    ) {
        return neuralNetworkService.clasificarArff(zipFile, cantidadMensajes);
    }

    @PostMapping(value = "/neuralnetwork/clasificar_takeout")
    public String clasificarTakeout(
            @RequestBody MultipartFile zipFile,
            @RequestParam("cantidad_mensajes") int cantidadMensajes
    ) {
        return neuralNetworkService.clasificarTakeout(zipFile, cantidadMensajes);
    }

    @GetMapping(value = "/neuralnetwork/clasificar_lotr")
    public String clasificarLotr(
            @RequestParam("db_uri") String dbUri,
            @RequestParam("db_name") String dbName,
            @RequestParam("cantidad_chats") int cantidadChats,
            @RequestParam("cantidad_mensajes") int cantidadMensajes
    ) {
        return neuralNetworkService.clasificarLotr(dbUri, dbName, cantidadChats, cantidadMensajes);
    }
}
