package es.e1sordo.worknote.configuration;

import es.e1sordo.worknote.logging.FeignLoggerWithoutExtraHeadersFactory;
import feign.Logger;
import feign.Retryer;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;

public class RetryableAndLoggedGatewaysConfiguration {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(5000L, 5000L, 3);
    }

    @Bean
    public FeignLoggerFactory loggerFactory() {
        return new FeignLoggerWithoutExtraHeadersFactory(
                logAllHeaders(), getPrintableHeaders(), getSecretHeaders()
        );
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    public boolean logAllHeaders() {
        return false;
    }

    @NonNull
    public List<String> getPrintableHeaders() {
        return Collections.emptyList();
    }

    @NonNull
    public List<String> getSecretHeaders() {
        return Collections.emptyList();
    }
}
