package no.bekk.workshop.pokemon.model;

import java.util.List;

public class NamedAPIResourceList {

    private int count;
    private List<NamedAPIResource> results;
    private String previous;
    private String next;

    public int getCount() {
        return count;
    }

    public NamedAPIResourceList setCount(int count) {
        this.count = count;
        return this;
    }

    public List<NamedAPIResource> getResults() {
        return results;
    }

    public NamedAPIResourceList setResults(List<NamedAPIResource> results) {
        this.results = results;
        return this;
    }

    public String getPrevious() {
        return previous;
    }

    public NamedAPIResourceList setPrevious(String previous) {
        this.previous = previous;
        return this;
    }

    public String getNext() {
        return next;
    }

    public NamedAPIResourceList setNext(String next) {
        this.next = next;
        return this;
    }
}
