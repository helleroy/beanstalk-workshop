package no.bekk.workshop;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class Endpoint {

    @RequestMapping(name = "/ping")
    public String ping() {
        return "pong";
    }


    @RequestMapping(name = "/hostname", path = "/hostname")
    public String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to resolve hostname", e);
        }
    }

}
