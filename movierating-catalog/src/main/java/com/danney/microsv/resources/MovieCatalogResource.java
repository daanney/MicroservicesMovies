package com.danney.microsv.resources;

import com.danney.microsv.models.Catalog;
import com.danney.microsv.models.CatalogItem;
import com.danney.microsv.models.Movie;
import com.danney.microsv.models.UserRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {

    @Value("rest.service.movies.url")
    private String movieUrl;

    @Value("rest.service.ratings.url")
    private String ratingsUrl;

    @Value("rest.request.default_timeout")
    private long requestTimeout;

    @Autowired
    private RestTemplate restTmpl;

    @Autowired
    private WebClient.Builder wcBuilder;

    @GetMapping("/{userId}")
    public Catalog getCatalog(@PathVariable("userId") String userId) {

        // Rest call: the "old" way using RestTemplate
        UserRating ratings = restTmpl.getForObject(ratingsUrl + "users/" + userId, UserRating.class);


        List<CatalogItem> items = ratings.getRatings().stream().map(rating -> {
            // Get the Movie of rating

            // old way - RestTemplate
            // Movie m = restTmpl.getForObject(movieUrl + rating.getMovieId(), Movie.class);

            // "New" (reactive) way using webclient builder
            Movie movie = wcBuilder.build().get()
                    .uri(movieUrl + rating.getMovieId())
                    .retrieve().bodyToMono(Movie.class)
                    .timeout(Duration.ofMillis(requestTimeout))
                    .block(); // block is removing the reactive part

            return new CatalogItem(movie, rating);
        }).collect(Collectors.toList());

        return new Catalog(items);
    }
}
