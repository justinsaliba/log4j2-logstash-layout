package com.vlkan.log4j2.logstash.layout.resolver;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.LogEvent;

import java.io.IOException;

class ExceptionResolver implements EventResolver {

    private static final ExceptionInternalResolverFactory INTERNAL_RESOLVER_FACTORY =
            new ExceptionInternalResolverFactory() {

                @Override
                EventResolver createClassNameResolver() {
                    return new EventResolver() {
                        @Override
                        public void resolve(LogEvent logEvent, JsonGenerator jsonGenerator) throws IOException {
                            Throwable exception = logEvent.getThrown();
                            if (exception == null) {
                                jsonGenerator.writeNull();
                            } else {
                                String exceptionClassName = exception.getClass().getCanonicalName();
                                jsonGenerator.writeString(exceptionClassName);
                            }
                        }
                    };
                }

                @Override
                EventResolver createMessageResolver(final EventResolverContext context) {
                    return new EventResolver() {
                        @Override
                        public void resolve(LogEvent logEvent, JsonGenerator jsonGenerator) throws IOException {
                            Throwable exception = logEvent.getThrown();
                            if (exception != null) {
                                String exceptionMessage = exception.getMessage();
                                boolean exceptionMessageExcluded = context.isEmptyPropertyExclusionEnabled() && StringUtils.isEmpty(exceptionMessage);
                                if (!exceptionMessageExcluded) {
                                    jsonGenerator.writeString(exceptionMessage);
                                    return;
                                }
                            }
                            jsonGenerator.writeNull();
                        }
                    };
                }

                @Override
                EventResolver createStackTraceTextResolver(final EventResolverContext context) {
                    return new EventResolver() {
                        @Override
                        public void resolve(LogEvent logEvent, JsonGenerator jsonGenerator) throws IOException {
                            Throwable exception = logEvent.getThrown();
                            if (exception != null) {
                                StackTraceTextResolver.getInstance().resolve(exception, jsonGenerator);
                            }
                        }
                    };
                }

                @Override
                EventResolver createStackTraceObjectResolver(final EventResolverContext context) {
                    return new EventResolver() {
                        @Override
                        public void resolve(LogEvent logEvent, JsonGenerator jsonGenerator) throws IOException {
                            Throwable exception = logEvent.getThrown();
                            if (exception != null) {
                                context.getStackTraceObjectResolver().resolve(exception, jsonGenerator);
                            }
                        }
                    };
                }

            };

    private final EventResolver internalResolver;

    ExceptionResolver(EventResolverContext context, String key) {
        this.internalResolver = INTERNAL_RESOLVER_FACTORY.createInternalResolver(context, key);
    }

    static String getName() {
        return "exception";
    }

    @Override
    public void resolve(LogEvent logEvent, JsonGenerator jsonGenerator) throws IOException {
        internalResolver.resolve(logEvent, jsonGenerator);
    }

}
