package no.bekk.workshop.pokemon.model;

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
