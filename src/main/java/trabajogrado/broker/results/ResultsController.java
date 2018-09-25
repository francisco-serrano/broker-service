package trabajogrado.broker.results;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = {
        "http://localhost:4200"
})
public class ResultsController {

    private ResultsService resultsService;

    @Autowired
    public ResultsController(ResultsService resultsService) {
        this.resultsService = resultsService;
    }

    @GetMapping(value = "/results/tables", produces = "application/json")
    public String getTables() {
        return resultsService.getTables();
    }

    @GetMapping(value = "/results/{source}", produces = "application/json")
    public String getAllUsers(@PathVariable("source") String source) {
        return resultsService.getAllUsers(source);
    }

    @GetMapping(value = "/results/{source}/{user}", produces = "application/json")
    public String getUser(@PathVariable("source") String source, @PathVariable("user") String user) {
        if (user.equals("undefined"))
            return getAllUsers(source);

        return resultsService.getUser(source, user);
    }

    @PostMapping(value = "/results/{tablename}", produces = "application/json")
    public String addConversation(@RequestBody MultipartFile csvFile, @PathVariable("tablename") String tablename) {
        return resultsService.addConversation(csvFile, tablename);
    }
}
