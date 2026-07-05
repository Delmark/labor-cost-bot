package by.delmark.portal.labor_cost_bot.portal.response;

import by.delmark.portal.labor_cost_bot.portal.response.dto.FavoriteProject;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
public class ProjectResponse {
    UUID employeeExternalId;
    UUID fiscalYearExternalId;
    List<FavoriteProject> favoriteProjectList;
}
