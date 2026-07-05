package by.delmark.portal.labor_cost_bot.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("portal.credentials")
@Component
@Data
public class PortalCredentials {
    private String username;
    private String password;
}
