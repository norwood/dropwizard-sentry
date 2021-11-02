package org.dhatim.dropwizard.sentry;

import io.sentry.SentryOptions;

/**
 * Custom Sentry Configurator for advanced usages.
 */
public interface SentryConfigurator {

    /**
     * Configure Sentry using the provided options.
     *
     * @param options Sentry options configured using the other parameters.
     *               You can override parameters or setup advanced options,
     *               such as <a href="https://docs.sentry.io/platforms/java/configuration/filtering/">filtering</a>.
     */
    void configure(SentryOptions options);
}
