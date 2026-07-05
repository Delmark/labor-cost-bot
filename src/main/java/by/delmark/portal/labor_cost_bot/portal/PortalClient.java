package by.delmark.portal.labor_cost_bot.portal;

import by.delmark.portal.labor_cost_bot.configuration.PortalCredentials;
import by.delmark.portal.labor_cost_bot.exceptions.ExpiredSessionException;
import by.delmark.portal.labor_cost_bot.portal.request.DayLaborCostRequest;
import by.delmark.portal.labor_cost_bot.portal.response.ProfileResponse;
import by.delmark.portal.labor_cost_bot.portal.response.ProjectResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortalClient {

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
                .uri("https://portal.answer-42.ru/login")
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

    private String sessionId() {
        String token = sessionCache().get(SESSION_KEY, String.class);
        if (token == null) {
            login();
            token = sessionCache().get(SESSION_KEY, String.class);
        }
        return token;
    }

    private Cache sessionCache() {
        Cache cache = cacheManager.getCache(SESSION_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Cache '" + SESSION_CACHE + "' is not configured");
        }
        return cache;
    }

    @Retryable(includes = ExpiredSessionException.class)
    @Cacheable(value = "profileInfo", key = "T(java.time.LocalDate).now()")
    public ProfileResponse getProfileBaseInfo() {
        return restClient.get()
                .uri("https://portal.answer-42.ru/portal/api/profile/base")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("JSESSIONID", sessionId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandler())
                .body(ProfileResponse.class);
    }

    @Retryable(includes = ExpiredSessionException.class)
    public ProjectResponse getEmployeeProjects(UUID fiscalYearId, UUID employeeId) {
        return restClient.get()
                .uri("https://portal.answer-42.ru/portal/api/projects/fiscal-year/employee?fiscalYearExternalId={fiscalYearExternalId}&employeeExternalId={employeeExternalId}",
                        Map.of(
                            "fiscalYearExternalId", fiscalYearId,
                            "employeeExternalId", employeeId
                        )
                )
                .cookie("JSESSIONID", sessionId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandler())
                .body(ProjectResponse.class);
    }

    @Retryable(includes = ExpiredSessionException.class)
    @CacheEvict(value = "profileInfo")
    public void updateDayLaborCost(DayLaborCostRequest laborCostRequest) {
        restClient.post()
                .uri("https://portal.answer-42.ru/portal/api/labor-costs/day/")
                .body(laborCostRequest)
                .cookie("JSESSIONID", sessionId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandler())
                .toBodilessEntity();
    }

    private RestClient.ResponseSpec.ErrorHandler errorHandler() {
        return (request, response) -> {
          if (HttpStatus.UNAUTHORIZED.equals(response.getStatusCode())) {
              log.info("Session expired, trying to relogin");
              login();
              throw new ExpiredSessionException("Session expired");
          }
        };
    }
}
