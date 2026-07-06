package by.delmark.portal.labor_cost_bot.portal;

import by.delmark.portal.labor_cost_bot.exceptions.ExpiredSessionException;
import by.delmark.portal.labor_cost_bot.portal.request.DayLaborCostRequest;
import by.delmark.portal.labor_cost_bot.portal.response.ProfileResponse;
import by.delmark.portal.labor_cost_bot.portal.response.ProjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortalClient {

    private final RestClient restClient;
    private final PortalSessionManager sessionManager;

    @Retryable(includes = ExpiredSessionException.class)
    @Cacheable(value = "profileInfo", key = "T(java.time.LocalDate).now()")
    public ProfileResponse getProfileBaseInfo() {
        return restClient.get()
                .uri("/portal/api/profile/base")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("JSESSIONID", sessionManager.getSessionId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandler())
                .body(ProfileResponse.class);
    }

    @Retryable(includes = ExpiredSessionException.class)
    public ProjectResponse getEmployeeProjects(UUID fiscalYearId, UUID employeeId) {
        return restClient.get()
                .uri("/portal/api/projects/fiscal-year/employee?fiscalYearExternalId={fiscalYearExternalId}&employeeExternalId={employeeExternalId}",
                        Map.of(
                            "fiscalYearExternalId", fiscalYearId,
                            "employeeExternalId", employeeId
                        )
                )
                .cookie("JSESSIONID", sessionManager.getSessionId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandler())
                .body(ProjectResponse.class);
    }

    @Retryable(includes = ExpiredSessionException.class)
    @CacheEvict(value = "profileInfo", allEntries = true)
    public void updateDayLaborCost(DayLaborCostRequest laborCostRequest) {
        restClient.post()
                .uri("/portal/api/labor-costs/day/")
                .body(laborCostRequest)
                .cookie("JSESSIONID", sessionManager.getSessionId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandler())
                .toBodilessEntity();
    }

    private RestClient.ResponseSpec.ErrorHandler errorHandler() {
        return (request, response) -> {
          if (HttpStatus.UNAUTHORIZED.equals(response.getStatusCode())) {
              log.info("Session expired, trying to relogin");
              sessionManager.login();
              throw new ExpiredSessionException("Session expired");
          }
        };
    }
}
