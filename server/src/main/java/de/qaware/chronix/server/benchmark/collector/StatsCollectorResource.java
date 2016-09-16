package de.qaware.chronix.server.benchmark.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;
import de.qaware.chronix.server.benchmark.configurator.BenchmarkConfiguratorResource;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcqueen666 on 15.06.16.
 */
@Path("/collector")
@Produces(MediaType.APPLICATION_JSON)
public class StatsCollectorResource {

    private final StatsCollector statsCollector = StatsCollector.getInstance();
    private final Logger logger = LoggerFactory.getLogger(StatsCollectorResource.class);

    // JUST FOR TESTING
    @GET
    @Path("test")
    @Timed
    public String test() {
        return "Hello from StatsCollector Resource!";
    }


    @GET
    @Path("benchmarkrecords")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBenchmarkRecords(){
        List<BenchmarkRecord> benchmarkRecordList = statsCollector.getBenchmarkRecords();
        logger.info("Size of benchmarkRecordList: {}", benchmarkRecordList.size());
        GenericEntity<List<BenchmarkRecord>> genericEntity = new GenericEntity<List<BenchmarkRecord>>(new ArrayList<>(benchmarkRecordList)){};

        return Response.ok().entity(genericEntity).build();

    }


}
