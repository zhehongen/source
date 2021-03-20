/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.core.metrics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.core.config.ConfigurationService;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Support code for use of metrics.
 * 
 * @since 3.3.0
 */
public final class MetricsSupport {

    /**
     * Private constructor.
     */
    private MetricsSupport() {
        
    }
    
    /**
     * Get the metric registry installed into the runtime.
     * 
     * @return default registry
     */
    @Nullable public static MetricRegistry getMetricRegistry() {
        return ConfigurationService.get(MetricRegistry.class);
    }
    
    /**
     * Register a metric instance under the given name.
     * 
     * <p>
     * Any existing instance registered under the given name will be replaced. 
     * The {@link MetricRegistry} on which to operate will be obtained via {@link #getMetricRegistry()}.
     * </p>
     * 
     * @param name the name under which to register the metric
     * @param metric the metric instance to register
     * @return the metric instance which was registered
     * 
     * @param <T> the type of metric being registered
     */
    @Nullable public static <T extends Metric> T register(@Nonnull final String name, @Nonnull final T metric) {
        return register(name, metric, true, null);
    }
            
    /**
     * Register a metric instance under the given name.
     * 
     * <p>
     * The {@link MetricRegistry} on which to operate will be obtained via {@link #getMetricRegistry()}.
     * </p>
     * 
     * @param name the name under which to register the metric
     * @param metric the metric instance to register
     * @param replaceExisting whether or not to replace the existing metric registered under that name
     * @return the metric instance which was registered
     * 
     * @param <T> the type of metric being registered
     */
    @Nullable public static <T extends Metric> T register(@Nonnull final String name, @Nonnull final T metric, 
            final boolean replaceExisting) {
        return register(name, metric, replaceExisting, null);
    }
    
    /**
     * Register a metric instance under the given name.
     * 
     * @param name the name under which to register the metric
     * @param metric the metric instance to register
     * @param replaceExisting whether or not to replace the existing metric registered under that name
     * @param registry the metric registry on which to operate. 
     *          If null, will be obtained via {@link #getMetricRegistry()}.
     * @return the metric instance which was registered
     * 
     * @param <T> the type of metric being registered
     */
    @Nullable public static <T extends Metric> T register(@Nonnull final String name, @Nonnull final T metric, 
            final boolean replaceExisting, @Nullable final MetricRegistry registry) { 
        
        Constraint.isNotNull(name, "Metric name was null");
        Constraint.isNotNull(metric, "Metric was null");
        
        MetricRegistry metricRegistry = registry;
        if (metricRegistry == null) {
            metricRegistry = getMetricRegistry();
        }
        if (metricRegistry == null) {
            return null;
        }
        
        synchronized (metricRegistry) {
            try {
                if (replaceExisting) {
                    metricRegistry.remove(name);
                }
                return metricRegistry.register(name, metric);
            } catch (final IllegalArgumentException e) {
                // Catch this and try again, just in case something not using this synchronized
                // method added since we removed above.
                if (replaceExisting) {
                    metricRegistry.remove(name);
                    return metricRegistry.register(name, metric);
                } else {
                    throw e;
                }
            }
        }
        
    }
    
    /**
     * Remove a metric instance registered under the given name.
     * 
     * @param name the name under which to deregister the metric
     *          
     * @return whether or not the metric was actually removed
     */
    public static boolean remove(@Nonnull final String name) {
        return remove(name, null, null);
    }
    
    /**
     * Remove a metric instance registered under the given name.
     * 
     * <p>
     * If a non-null metric instance is supplied, the metric instance registered under the given name will only
     * be removed if it is the same instance as supplied, as determined by 
     * {@link #isMetricInstanceRegisteredUnderName(String, Metric, MetricRegistry)}
     * </p>
     * 
     * <p>
     * The {@link MetricRegistry} on which to operate will be obtained via {@link #getMetricRegistry()}.
     * </p>
     * 
     * @param name the name under which to deregister the metric
     * @param metric the metric instance to remove
     *          
     * @return whether or not the metric was actually removed
     */
    public static boolean remove(@Nonnull final String name, @Nullable final Metric metric) {
        return remove(name, metric, null);
    }
    
    /**
     * Remove a metric instance registered under the given name.
     * 
     * <p>
     * If a non-null metric instance is supplied, the metric instance registered under the given name will only
     * be removed if it is the same instance as supplied, as determined by 
     * {@link #isMetricInstanceRegisteredUnderName(String, Metric, MetricRegistry)}
     * </p>
     * 
     * <p>
     * The {@link MetricRegistry} on which to operate will be obtained via {@link #getMetricRegistry()}.
     * </p>
     * 
     * @param name the name under which to deregister the metric
     * @param metric the metric instance to remove
     * @param registry the metric registry on which to operate. 
     *          If null, will be obtained via {@link #getMetricRegistry()}.
     *          
     * @return whether or not the metric was actually removed
     */
    public static boolean remove(@Nonnull final String name, @Nullable final Metric metric, 
            @Nullable final MetricRegistry registry) {
        
        Constraint.isNotNull(name, "Metric name was null");
        
        MetricRegistry metricRegistry = registry;
        if (metricRegistry == null) {
            metricRegistry = getMetricRegistry();
        }
        if (metricRegistry == null) {
            return false;
        }
        
        synchronized (metricRegistry) {
            if (metric != null && !isMetricInstanceRegisteredUnderName(name, metric, metricRegistry)) {
                return false;
            }
            return metricRegistry.remove(name);
        }
    }
    
    /**
     * Determine whether the given metric instance is registered under the given name.
     * 
     * @param name the name under which to deregister the metric
     * @param metric the metric instance to remove
     * @param registry the metric registry on which to operate. 
     * 
     * @return true if the given metric instance is registered under the given name, false if not
     */
    public static boolean isMetricInstanceRegisteredUnderName(@Nonnull final String name, @Nonnull final Metric metric,
            @Nonnull final MetricRegistry registry) {
        
        Constraint.isNotNull(registry, "MetricRegistry was null");
        Constraint.isNotNull(name, "Metric name was null");
        Constraint.isNotNull(metric, "Metric was null");
        
        final Metric registeredMetric = registry.getMetrics().get(name);
        return metric == registeredMetric;
    }
    
    /**
     * Start the specified timer.
     * 
     * @param timer the timer to start, may be null
     * 
     * @return the timer context, or null if the input timer was null
     */
    @Nullable public static Context startTimer(@Nullable final Timer timer) {
        if (timer != null) {
            return timer.time();
        } else {
            return null;
        }
    }
    
    /**
     * Stop the timer represented by the specified timer context instance.
     * 
     * @param context the timer context to stop, may be null
     * 
     * @return the elapsed time in nanoseconds, or null if the input context was null
     */
    @Nullable public static Long stopTimer(@Nullable final Context context) {
        if (context != null) {
            return context.stop();
        } else {
            return null;
        }
    }
}