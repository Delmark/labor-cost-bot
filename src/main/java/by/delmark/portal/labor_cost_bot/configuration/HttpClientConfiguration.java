package by.delmark.portal.labor_cost_bot.configuration;

import lombok.Getter;
import lombok.Setter;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "client.configuration")
public class HttpClientConfiguration {

    @Setter @Getter
    private Duration connectionTimeout;

    @Setter @Getter
    private String userAgent;

    @Bean
    public RestClient restClient() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectionRequestTimeout(connectionTimeout);
        requestFactory.setReadTimeout(connectionTimeout);

        HttpClient httpClient = HttpClientBuilder.create()
                .setUserAgent(userAgent)
                .build();
        requestFactory.setHttpClient(httpClient);

        return RestClient.builder().requestFactory(requestFactory).build();
    }
}
