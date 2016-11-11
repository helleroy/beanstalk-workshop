package no.bekk.workshop.pokemon.model;

public class Pokemon {

    private Integer id;
    private String name;
    private Integer height;
    private Integer weight;
    private PokemonSprites sprites;

    public Integer getId() {
        return id;
    }

    public Pokemon setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Pokemon setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public Pokemon setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public Pokemon setWeight(Integer weight) {
        this.weight = weight;
        return this;
    }

    public PokemonSprites getSprites() {
        return sprites;
    }

    public Pokemon setSprites(PokemonSprites sprites) {
        this.sprites = sprites;
        return this;
    }
}
