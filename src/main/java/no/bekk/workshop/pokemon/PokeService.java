package no.bekk.workshop.pokemon;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.bekk.workshop.pokemon.domain.Evolutions;
import no.bekk.workshop.pokemon.domain.PokemonInfo;
import no.bekk.workshop.pokemon.model.*;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

public class PokeService {

    private static final String POKEMON_ALL_URL = "http://pokeapi.co/api/v2/pokemon/";
    private static final String POKEMON_URL = "http://pokeapi.co/api/v2/pokemon/%s/";
    private static final String POKEMON_SPECIES_URL = "http://pokeapi.co/api/v2/pokemon-species/%s/";

    private final OkHttpClient okHttpClient;
    private final ObjectMapper jsonMapper;

    public PokeService(OkHttpClient okHttpClient, ObjectMapper jsonMapper) {
        this.okHttpClient = okHttpClient;
        this.jsonMapper = jsonMapper;
    }

    public List<PokemonInfo> allPokemon(int offset) {
        return callApi(POKEMON_ALL_URL + "?offset=" + offset)
                .thenApply(response -> toType(response, NamedAPIResourceList.class))
                .thenApply(NamedAPIResourceList::getResults)
                .thenApply(results -> results
                        .parallelStream()
                        .map(NamedAPIResource::getUrl)
                        .map(url -> callApi(url)
                                .thenApply(response -> toType(response, Pokemon.class))
                                .join())
                        .map(PokemonInfo::fromModel)
                        .collect(toList()))
                .join();
    }

    public Evolutions pokemonEvolutions(String name) {
        return getSpecies(name)
                .thenCompose(this::getEvolutionChain)
                .thenCompose(this::getPokemonInChain)
                .thenApply(pokemon -> pokemon
                        .stream()
                        .map(PokemonInfo::fromModel)
                        .collect(toList()))
                .thenApply(Evolutions::new)
                .join();
    }

    private CompletableFuture<PokemonSpecies> getSpecies(String name) {
        return callApi(String.format(POKEMON_SPECIES_URL, name))
                .thenApply(response -> toType(response, PokemonSpecies.class));
    }

    private CompletableFuture<EvolutionChain> getEvolutionChain(PokemonSpecies species) {
        return callApi(species.getEvolutionChain().getUrl())
                .thenApply(response -> toType(response, EvolutionChain.class));
    }

    private CompletableFuture<List<Pokemon>> getPokemonInChain(EvolutionChain evolutionChain) {
        List<CompletableFuture<Pokemon>> futures = requestPokemonInChain(evolutionChain.getChain(), new ArrayList<>());

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures
                        .stream()
                        .map(CompletableFuture::join)
                        .collect(toList()));
    }

    private List<CompletableFuture<Pokemon>> requestPokemonInChain(ChainLink chainLink, List<CompletableFuture<Pokemon>> futures) {
        for (ChainLink evolvesTo : chainLink.getEvolvesTo()) {
            requestPokemonInChain(evolvesTo, futures);
        }

        futures.add(
                callApi(String.format(POKEMON_URL, chainLink.getSpecies().getName()))
                        .thenApply(response -> toType(response, Pokemon.class)));

        return futures;
    }

    private CompletableFuture<Response> callApi(String url) {
        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return okHttpClient.newCall(
                                new Request.Builder()
                                        .cacheControl(new CacheControl.Builder()
                                                .maxStale(Integer.MAX_VALUE, SECONDS)
                                                .build())
                                        .get()
                                        .url(url)
                                        .build())
                                .execute();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to call api", e);
                    }
                })
                .thenApply(response -> {
                    int code = response.code();
                    switch (code) {
                        case 200:
                            return response;
                        case 404:
                            throw new RuntimeException("Could not find the requested pokemon");
                        default:
                            throw new RuntimeException("Got error from PokeAPI [" + code + "]");
                    }
                });
    }

    private <T> T toType(Response response, Class<T> type) {
        try (ResponseBody responseBody = response.body()) {
            String string = responseBody.string();
            return jsonMapper.readValue(string, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to map response to type [" + type.getCanonicalName() + "]", e);
        }
    }
}
