package no.bekk.workshop.pokemon;

import no.bekk.workshop.pokemon.domain.Evolutions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/pokemon")
public class PokeAPI {

    private final PokeService service;

    @Autowired
    public PokeAPI(PokeService service) {
        this.service = service;
    }

    @RequestMapping(value = "/{name}", produces = APPLICATION_JSON_VALUE)
    public Evolutions pokemonEvolutions(@PathVariable("name") String name) throws IOException {
        return service.pokemonEvolutions(name);
    }
}
