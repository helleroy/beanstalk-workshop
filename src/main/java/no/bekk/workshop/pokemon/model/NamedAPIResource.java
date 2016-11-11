package no.bekk.workshop.pokemon.model;

public class NamedAPIResource {

    private String name;
    private String url;

    public String getName() {
        return name;
    }

    public NamedAPIResource setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NamedAPIResource setUrl(String url) {
        this.url = url;
        return this;
    }
}
