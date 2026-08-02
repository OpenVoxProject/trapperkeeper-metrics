package com.puppetlabs.trapperkeeper.metrics;

import io.dropwizard.metrics5.Metric;
import io.dropwizard.metrics5.MetricFilter;
import io.dropwizard.metrics5.MetricName;

import java.util.Set;

// Takes in a whitelist of strings to match against
public class AllowedNamesMetricFilter implements MetricFilter{
    private final Set<String> allowedMetricNames;

    public AllowedNamesMetricFilter(Set<String> allowedMetricNames){
        this.allowedMetricNames = allowedMetricNames;
    }

    @Override
    public boolean matches(MetricName name, Metric metric) {
        if (allowedMetricNames.isEmpty()) {
            return true;
        } else return allowedMetricNames.contains(name.getKey());
    }
}
