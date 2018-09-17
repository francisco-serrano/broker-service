package trabajogrado.broker.poo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PooController {

    private PooService pooService;

    @Autowired
    public PooController(PooService pooService) {
        this.pooService = pooService;
    }

    @PostMapping(value = "/clasificar_arff", produces = "application/json")
    public String classifyArff(@RequestBody MultipartFile zipFile) {
        return pooService.classifyArff(zipFile);
    }

    @PostMapping(value = "/clasificar_takeout", produces = "application/json")
    public String classifyTakeout(@RequestBody MultipartFile zipFile) {
        return pooService.classifyTakeout(zipFile);
    }

    @GetMapping(value = "/clasificar_lotr", produces = "application/json")
    public String classifyLotr(
            @RequestParam("db_uri") String dbUri,
            @RequestParam("db_name") String dbName,
            @RequestParam("cantidad_chats") int cantidadChats
    ) {
        return pooService.classifyLotr(dbUri, dbName, cantidadChats);
    }
}
