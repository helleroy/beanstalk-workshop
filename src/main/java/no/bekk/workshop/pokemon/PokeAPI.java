package no.bekk.workshop.pokemon;

import no.bekk.workshop.pokemon.domain.Evolutions;
import no.bekk.workshop.pokemon.domain.PokemonInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/pokemon")
public class PokeAPI {

    private final PokeService service;

    @Autowired
    public PokeAPI(PokeService service) {
        this.service = service;
    }

    @RequestMapping(value = "/**", produces = APPLICATION_JSON_VALUE)
    public List<PokemonInfo> pokemonList(@RequestParam(value = "offset", required = false, defaultValue = "0") int offset) throws IOException {
        return service.allPokemon(offset);
    }

    @RequestMapping(value = "/{name}", produces = APPLICATION_JSON_VALUE)
    public Evolutions pokemonEvolutions(@PathVariable("name") String name) throws IOException {
        return service.pokemonEvolutions(name);
    }
}
