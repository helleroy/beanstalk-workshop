package no.bekk.workshop.pokemon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EvolutionChain {

    private ChainLink chain;

    public ChainLink getChain() {
        return chain;
    }

    public EvolutionChain setChain(ChainLink chain) {
        this.chain = chain;
        return this;
    }
}
