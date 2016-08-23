package no.bekk.workshop;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Endpoint {

    @RequestMapping(name = "/ping")
    public String ping() {
        return "pong";
    }
}
