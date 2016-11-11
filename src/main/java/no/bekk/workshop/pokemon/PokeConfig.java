package no.bekk.workshop.pokemon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;

@Configuration
public class PokeConfig {

    @Bean
    public PokeService pokeService() {
        OkHttpClient okHttpClient = new OkHttpClient()
                .setCache(new Cache(new File("cache"), 256000000L));

        ObjectMapper jsonMapper = new ObjectMapper()
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(SNAKE_CASE);

        return new PokeService(okHttpClient, jsonMapper);
    }
}
