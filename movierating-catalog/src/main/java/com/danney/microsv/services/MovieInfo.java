package com.danney.microsv.services;

import com.danney.microsv.models.CatalogItem;
import com.danney.microsv.models.Movie;
import com.danney.microsv.models.Rating;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class MovieInfo {

    @Value("${rest.request.default_timeout}")
    private long requestTimeout;

    @Value("${rest.service.movies.url}")
    private String movieUrl;

    @Autowired
    private WebClient.Builder wcBuilder;

    private Map<String, Movie> moviesCache = new HashMap<>();

    @HystrixCommand(
        fallbackMethod = "getFallbackCatalogItem",
        // BULKHEAD PATTERN
        // separate thread pool for different operations/calls
        threadPoolKey = "movieInfoThreadPool",
        threadPoolProperties = {
            // Concurrent open threads possible
            @HystrixProperty(name="coreSize", value="20"),
            // Size of "waiting" pool, no consumption of Threads
            @HystrixProperty(name="maxQueueSize", value="20")
        }
    )
    public CatalogItem getCatalogItem(Rating rating) {
        Movie movie = moviesCache.get(rating.getMovieId());

        if(null == movie) {
            // old way - RestTemplate
            // Movie m = restTmpl.getForObject(movieUrl + rating.getMovieId(), Movie.class);

            // "New" (reactive) way using webclient builder
            movie = wcBuilder.build().get()
                .uri(movieUrl + rating.getMovieId())
                .retrieve().bodyToMono(Movie.class)
                .timeout(Duration.ofMillis(requestTimeout))
                .block(); // block is removing the reactive part
            moviesCache.put(rating.getMovieId(), movie);
        }

        return new CatalogItem(movie, rating);
    }

    public CatalogItem getFallbackCatalogItem(Rating rating) {
        Movie movie = new Movie("0", "Movie not found", "n/a");
        return new CatalogItem(movie, rating);
    }
}
