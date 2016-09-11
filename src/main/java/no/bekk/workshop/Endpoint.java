package no.bekk.workshop;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class Endpoint {

    @RequestMapping(name = "/ping", path = "/")
    public String ping() {
        return "pong";
    }

    @RequestMapping(name = "/500", path = "/500")
    public String Error() {
        throw new RuntimeException();
    }

    @RequestMapping(name = "/hostname", path = "/hostname")
    public String hostname() {
        return getHostname();
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to resolve hostname", e);
        }
    }

    @RequestMapping(name = "/sleep", path = "/sleep")
    public String sleep(@RequestParam(defaultValue = "10", name = "seconds") int sleepSeconds) {
        try {
            Thread.sleep(sleepSeconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted sleep", e);
        }
        return "request took " + sleepSeconds + "s on " + getHostname();
    }

}
