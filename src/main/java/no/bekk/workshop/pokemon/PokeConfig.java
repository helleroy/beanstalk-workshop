package no.bekk.workshop.pokemon;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
public class PokeConfig {

    @Bean
    public PokeService pokeService() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(new Cache(new File("cache"), 256000000L))
                .connectionPool(new ConnectionPool(20, 30, SECONDS))
                .build();

        ObjectMapper jsonMapper = new ObjectMapper()
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(SNAKE_CASE);

        return new PokeService(okHttpClient, jsonMapper);
    }
}
