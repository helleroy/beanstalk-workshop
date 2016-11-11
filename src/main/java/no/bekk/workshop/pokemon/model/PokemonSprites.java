package no.bekk.workshop.pokemon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonSprites {

    private String frontDefault;

    public String getFrontDefault() {
        return frontDefault;
    }

    public PokemonSprites setFrontDefault(String frontDefault) {
        this.frontDefault = frontDefault;
        return this;
    }
}
