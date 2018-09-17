package trabajogrado.broker.results;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResultsController {

    private ResultsService resultsService;

    @Autowired
    public ResultsController(ResultsService resultsService) {
        this.resultsService = resultsService;
    }

    @GetMapping(value = "/results/{source}", produces = "application/json")
    public String getAllUsers(@PathVariable("source") String source) {
        return resultsService.getAllUsers(source);
    }

    @GetMapping(value = "/results/{source}/{user}", produces = "application/json")
    public String getUser(@PathVariable("source") String source, @PathVariable("user") String user) {
        return resultsService.getUser(source, user);
    }
}
