package de.qaware.chronix.server.benchmark.collector;

import com.codahale.metrics.annotation.Timed;
import de.qaware.chronix.common.QueryUtil.BenchmarkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
        StatsCollector statsCollector = StatsCollector.getInstance();
        List<BenchmarkRecord> benchmarkRecordList = statsCollector.getBenchmarkRecords();
        logger.info("Size of benchmarkRecordList: {}", benchmarkRecordList.size());
        GenericEntity<List<BenchmarkRecord>> genericEntity = new GenericEntity<List<BenchmarkRecord>>(new ArrayList<>(benchmarkRecordList)){};

        return Response.ok().entity(genericEntity).build();

    }

    @GET
    @Path("delete/benchmarkrecords")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteBenchmarkRecords(){
        StatsCollector statsCollector = StatsCollector.getInstance();
        if(statsCollector.deleteBenchmarkRecords()){
            return Response.ok().entity("Benchmark record file on server deleted.").build();
        }
        return Response.serverError().entity("Could not delete benchmark record file on server.").build();
    }

}
