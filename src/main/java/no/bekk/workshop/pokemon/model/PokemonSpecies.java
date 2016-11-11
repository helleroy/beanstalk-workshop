package no.bekk.workshop.pokemon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonSpecies {

    private String id;
    private NamedAPIResource evolutionChain;

    public String getId() {
        return id;
    }

    public PokemonSpecies setId(String id) {
        this.id = id;
        return this;
    }

    public NamedAPIResource getEvolutionChain() {
        return evolutionChain;
    }

    public PokemonSpecies setEvolutionChain(NamedAPIResource evolutionChain) {
        this.evolutionChain = evolutionChain;
        return this;
    }
}
