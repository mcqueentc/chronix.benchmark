package de.qaware.chronix.server;

import de.qaware.chronix.server.benchmark.collector.StatsCollectorResource;
import de.qaware.chronix.server.benchmark.configurator.BenchmarkConfiguratorResource;
import de.qaware.chronix.server.benchmark.queryrunner.QueryRunnerResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * Created by mcqueen666 on 15.06.16.
 */
public class DropWizardServerApplication extends Application<DropWizardServerConfiguration>{

    public static void main(String[] args) throws Exception {
        new DropWizardServerApplication().run(args);
    }

    @Override
    public String getName() {
        return "chronix-benchmark";
    }

    @Override
    public void initialize(Bootstrap<DropWizardServerConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(DropWizardServerConfiguration configuration, Environment environment) {
        //Register BenchmarkConfiguratorResource
        final BenchmarkConfiguratorResource configuratorResource = new BenchmarkConfiguratorResource();
        environment.jersey().register(configuratorResource);

        //Register QueryRunnerResource
        final QueryRunnerResource queryRunnerResourcerunner = new QueryRunnerResource();
        environment.jersey().register(queryRunnerResourcerunner);

        //Register StatsCollectorResource
        final StatsCollectorResource statsCollectorResource = new StatsCollectorResource();
        environment.jersey().register(statsCollectorResource);

        //Register HealthCheck
        final DropWizardServerHealthCheck healthCheck = new DropWizardServerHealthCheck();
        environment.healthChecks().register("template", healthCheck);

        environment.jersey().register(MultiPartFeature.class);
    }
}
