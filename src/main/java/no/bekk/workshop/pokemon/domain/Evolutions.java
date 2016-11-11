package no.bekk.workshop.pokemon.domain;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Evolutions {

    private final List<PokemonInfo> pokemon;

    public Evolutions(List<PokemonInfo> pokemon) {
        this.pokemon = pokemon
                .stream()
                .sorted((pokemon1, pokemon2) -> pokemon1.getId().compareTo(pokemon2.getId()))
                .collect(toList());
    }

    public List<PokemonInfo> getPokemon() {
        return pokemon;
    }
}
