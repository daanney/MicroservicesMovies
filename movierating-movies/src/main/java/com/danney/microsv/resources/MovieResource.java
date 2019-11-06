package com.danney.microsv.resources;

import com.danney.microsv.models.Movie;
import com.danney.microsv.models.MovieSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@RestController
@RequestMapping("/movies")
public class MovieResource {

    @Value("rest.request.default_timeout")
    private long requestTimeout;

    @Value("${moviedb.api.url}")
    private String moviedbURL;

    @Value("${moviedb.api.key}")
    private String moviedbKey;

    @Autowired
    private WebClient.Builder wcBuilder;

    @GetMapping("/{movieId}")
    public Movie getMovie(@PathVariable("movieId") String movieId) {
        MovieSummary movie = wcBuilder.build().get()
                .uri(moviedbURL + "movie/" + movieId + "?api_key=" + moviedbKey)
                .retrieve().bodyToMono(MovieSummary.class)
                .timeout(Duration.ofMillis(requestTimeout))
                .block();
        return new Movie(movieId, movie.getTitle(), movie.getOverview());
    }
}
