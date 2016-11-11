package no.bekk.workshop.pokemon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChainLink {

    private NamedAPIResource species;
    private List<ChainLink> evolvesTo;

    public NamedAPIResource getSpecies() {
        return species;
    }

    public ChainLink setSpecies(NamedAPIResource species) {
        this.species = species;
        return this;
    }

    public List<ChainLink> getEvolvesTo() {
        return evolvesTo;
    }

    public ChainLink setEvolvesTo(List<ChainLink> evolvesTo) {
        this.evolvesTo = evolvesTo;
        return this;
    }
}