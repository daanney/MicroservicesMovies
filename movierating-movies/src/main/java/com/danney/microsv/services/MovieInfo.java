package com.danney.microsv.services;

import com.danney.microsv.models.Movie;
import com.danney.microsv.models.MovieSummary;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
public class MovieInfo {

    @Value("${rest.request.default_timeout}")
    private long requestTimeout;

    @Value("${moviedb.api.url}")
    private String moviedbURL;

    @Value("${moviedb.api.key}")
    private String moviedbKey;

    @Autowired
    private WebClient.Builder wcBuilder;

    @HystrixCommand(fallbackMethod = "getFallbackMovie")
    public Movie getMovie(@PathVariable("movieId") String movieId) {
        MovieSummary movie = wcBuilder.build().get()
            .uri(moviedbURL + "movie/" + movieId + "?api_key=" + moviedbKey)
            .retrieve().bodyToMono(MovieSummary.class)
            .timeout(Duration.ofMillis(requestTimeout))
            .block();
        return new Movie(movieId, movie.getTitle(), movie.getOverview());
    }

    public Movie getFallbackMovie(String movieId) {
        return new Movie(movieId, "Movie not found on MovieDB", "n/a");
    }
}
