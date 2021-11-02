package org.dhatim.dropwizard.sentry.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.sentry.SentryOptions;
import io.sentry.logback.SentryAppender;
import org.dhatim.dropwizard.sentry.SentryConfigurator;
import org.dhatim.dropwizard.sentry.filters.DroppingSentryLoggingFilter;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonTypeName("sentry")
public class SentryAppenderFactory extends AbstractAppenderFactory<ILoggingEvent> {

    private static final String APPENDER_NAME = "dropwizard-sentry";

    @NotNull
    @JsonProperty
    public String dsn = null;

    @JsonProperty
    public Optional<String> environment = Optional.empty();

    @JsonProperty
    public Optional<Map<String, String>> tags = Optional.empty();

    @JsonProperty
    public Optional<String> release = Optional.empty();

    @JsonProperty
    public Optional<String> serverName = Optional.empty();

    @JsonProperty
    public Optional<List<String>> inAppIncludes = Optional.empty();

    @JsonProperty
    public Optional<List<String>> inAppExcludes = Optional.empty();

    @JsonProperty
    public Optional<String> configurator = Optional.empty();

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context,
                                         String applicationName,
                                         LayoutFactory<ILoggingEvent> layoutFactory,
                                         LevelFilterFactory<ILoggingEvent> levelFilterFactory,
                                         AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {
        checkNotNull(context);
        SentryOptions options = new SentryOptions();
        options.setDsn(this.dsn);
        environment.ifPresent(options::setEnvironment);
        tags.ifPresent(tags -> tags.forEach(options::setTag));
        release.ifPresent(options::setRelease);
        serverName.ifPresent(options::setServerName);
        inAppIncludes.ifPresent(inAppIncludes -> inAppIncludes.forEach(options::addInAppInclude));
        inAppExcludes.ifPresent(inAppExcludes -> inAppExcludes.forEach(options::addInAppExclude));
        configurator.ifPresent(configurator -> {
            try {
                Class<?> klass = Class.forName(configurator);
                if (!SentryConfigurator.class.isAssignableFrom(klass)) {
                    throw new IllegalArgumentException("configurator class " + configurator + " does not implement " + SentryConfigurator.class.getName());
                }
                SentryConfigurator sentryConfigurator = ((Class<SentryConfigurator>) klass).getDeclaredConstructor().newInstance();
                sentryConfigurator.configure(options);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("configurator class " + configurator + " not found", e);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("configurator class " + configurator + " does not define a default constructor", e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException("cannot invoke default constructor on configurator class " + configurator, e);
            }
        });

        SentryAppender appender = new SentryAppender();

        appender.setOptions(options);
        appender.setName(APPENDER_NAME);
        appender.setContext(context);

        appender.addFilter(levelFilterFactory.build(threshold));
        getFilterFactories().forEach(f -> appender.addFilter(f.build()));
        appender.start();

        final Filter<ILoggingEvent> filter = new DroppingSentryLoggingFilter();
        filter.start();
        appender.addFilter(filter);

        return appender;
    }

}
