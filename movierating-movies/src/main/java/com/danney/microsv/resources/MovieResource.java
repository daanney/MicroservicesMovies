package com.danney.microsv.resources;

import com.danney.microsv.models.Movie;
import com.danney.microsv.models.MovieSummary;
import com.danney.microsv.services.MovieInfo;
import com.netflix.discovery.converters.Auto;
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

    @Autowired
    private MovieInfo movieInfo;

    @GetMapping("/{movieId}")
    public Movie getMovie(@PathVariable("movieId") String movieId) {
        return movieInfo.getMovie(movieId);
    }
}
