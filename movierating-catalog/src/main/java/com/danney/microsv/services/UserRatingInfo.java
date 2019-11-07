package com.danney.microsv.services;

import com.danney.microsv.models.Rating;
import com.danney.microsv.models.UserRating;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class UserRatingInfo {

    @Value("${rest.request.default_timeout}")
    private long requestTimeout;

    @Value("${rest.service.ratings.url}")
    private String ratingsUrl;

    @Autowired
    private RestTemplate restTmpl;

    @HystrixCommand(
        fallbackMethod = "getFallbackUserRating",
        // BULKHEAD PATTERN
        // separate thread pool for different operations/calls
        threadPoolKey = "ratingsInfoThreadPool",
        threadPoolProperties = {
            // Concurrent open threads possible
            @HystrixProperty(name="coreSize", value="20"),
            // Size of "waiting" pool, no consumption of Threads
            @HystrixProperty(name="maxQueueSize", value="20")
        }
    )
    public UserRating getUserRating(String userId) {
        return restTmpl.getForObject(ratingsUrl + "users/" + userId, UserRating.class);
    }

    public UserRating getFallbackUserRating(String userId) {
        return new UserRating(Arrays.asList(new Rating("0", 0)));
    }
}
