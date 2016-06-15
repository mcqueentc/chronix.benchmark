package de.qaware.chronix.server.benchmark.collector;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Created by mcqueen666 on 15.06.16.
 */
@Path("/collector")
@Produces(MediaType.APPLICATION_JSON)
public class StatsCollectorResource {
    // JUST FOR TESTING
    @GET
    @Path("test")
    @Timed
    public String test() {
        return "Hello from StatsCollector Resource!";
    }
}
