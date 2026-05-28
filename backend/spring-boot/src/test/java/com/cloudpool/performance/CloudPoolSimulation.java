package com.cloudpool.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class CloudPoolSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .authorizationHeader("Bearer ${token}");

    ScenarioBuilder scn = scenario("CloudPool Load Test")
        .exec(
            http("Get Files")
                .get("/api/files")
                .check(status().is(200))
        )
        .pause(1)
        .exec(
            http("Get Buckets")
                .get("/api/files/buckets")
                .check(status().is(200))
        )
        .pause(1)
        .exec(
            http("Get Storage Quota")
                .get("/api/files/quota")
                .check(status().is(200))
        );

    {
        setUp(
            scn.injectOpen(
                rampUsers(50).during(30),
                constantUsersPerSec(10).during(60)
            )
        ).protocols(httpProtocol)
            .assertions(
                global().responseTime().max().lt(5000),
                global().successfulRequests().percent().gt(99.0)
            );
    }
}
