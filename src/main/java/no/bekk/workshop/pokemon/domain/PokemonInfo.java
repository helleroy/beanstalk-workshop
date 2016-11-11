package no.bekk.workshop.pokemon.domain;

import no.bekk.workshop.pokemon.model.Pokemon;

public class PokemonInfo {

    private Integer id;
    private String name;
    private Integer height;
    private Integer weight;
    private String imageUrl;

    public static PokemonInfo fromModel(Pokemon model) {
        return new PokemonInfo()
                .setId(model.getId())
                .setName(model.getName())
                .setHeight(model.getHeight())
                .setWeight(model.getWeight())
                .setImageUrl(model.getSprites().getFrontDefault());
    }

    public Integer getId() {
        return id;
    }

    public PokemonInfo setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PokemonInfo setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public PokemonInfo setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public PokemonInfo setWeight(Integer weight) {
        this.weight = weight;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public PokemonInfo setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }


}
