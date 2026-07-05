package by.delmark.portal.labor_cost_bot.portal;

import by.delmark.portal.labor_cost_bot.configuration.PortalCredentials;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class PortalSessionManager {

    private static final String SESSION_CACHE = "session";
    private static final String SESSION_KEY = "jsessionid";

    private final RestClient restClient;
    private final PortalCredentials portalCredentials;
    private final CacheManager cacheManager;

    @PostConstruct
    public void init() {
        login();
    }

    public void login() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", portalCredentials.getUsername());
        form.add("password", portalCredentials.getPassword());

        RestClient.ResponseSpec spec = restClient.post()
                .uri("/login")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.ALL)
                .body(form)
                .retrieve();

        try {
            ResponseEntity<Void> response = spec.toBodilessEntity();
            List<String> setCookieHeaders = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (setCookieHeaders == null) {
                throw new IllegalStateException("No Set-Cookie header in login response");
            }
            String sessionHeader = setCookieHeaders.stream()
                    .filter(cookie -> cookie.startsWith("JSESSIONID"))
                    .findAny().orElseThrow(() -> new IllegalStateException("JSESSIONID not found"));

            String[] setCookies = sessionHeader.split(";");
            String sessionToken = null;
            for (String cookie : setCookies) {
                if (cookie.startsWith("JSESSIONID=")) {
                    sessionToken = cookie.split("=")[1];
                }
            }
            if (sessionToken == null) {
                throw new IllegalStateException("JSESSIONID not found");
            }
            sessionCache().put(SESSION_KEY, sessionToken);
        } catch (Exception e) {
            log.error("Failed to login", e);
            throw e;
        }
    }

    public String getSessionId() {
        String token = sessionCache().get(SESSION_KEY, String.class);
        if (token == null) {
            login();
            token = sessionCache().get(SESSION_KEY, String.class);
        }
        return token;
    }

    private Cache sessionCache() {
        return cacheManager.getCache(SESSION_CACHE);
    }
}
