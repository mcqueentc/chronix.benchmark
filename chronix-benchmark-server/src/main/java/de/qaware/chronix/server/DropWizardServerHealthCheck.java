package de.qaware.chronix.server;

import com.codahale.metrics.health.HealthCheck;

/**
 * Created by mcqueen666 on 15.06.16.
 */
public class DropWizardServerHealthCheck extends HealthCheck{
    @Override
    protected Result check() throws Exception {
        return HealthCheck.Result.healthy();
    }
}
