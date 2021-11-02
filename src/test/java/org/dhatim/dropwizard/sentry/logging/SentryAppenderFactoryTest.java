package org.dhatim.dropwizard.sentry.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.filter.ThresholdLevelFilterFactory;
import io.dropwizard.logging.layout.DropwizardLayoutFactory;
import io.sentry.logback.SentryAppender;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class SentryAppenderFactoryTest {

    private final LoggerContext context = new LoggerContext();
    private final DropwizardLayoutFactory layoutFactory = new DropwizardLayoutFactory();
    private final ThresholdLevelFilterFactory levelFilterFactory = new ThresholdLevelFilterFactory();
    private final AsyncLoggingEventAppenderFactory asyncAppenderFactory = new AsyncLoggingEventAppenderFactory();

    @Test
    public void hasValidDefaults() throws IOException, ConfigurationException {
        final SentryAppenderFactory factory = new SentryAppenderFactory();

        assertNull("default dsn is unset", factory.dsn);
        assertNull("default environment is unset", factory.environment);
        assertNull("default release is unset", factory.release);
        assertNull("default serverName is unset", factory.serverName);
    }

    @Test(expected = NullPointerException.class)
    public void buildSentryAppenderShouldFailWithNullContext() {
        new SentryAppenderFactory().build(null, "", null, levelFilterFactory, asyncAppenderFactory);
    }

    @Test
    public void buildSentryAppenderShouldWorkWithValidConfiguration() {
        SentryAppenderFactory factory = new SentryAppenderFactory();
        factory.dsn = "https://user:pass@app.sentry.io/id";

        Appender<ILoggingEvent> appender = factory.build(context, "", layoutFactory, levelFilterFactory, asyncAppenderFactory);

        assertThat(appender, instanceOf(SentryAppender.class));
    }

}
