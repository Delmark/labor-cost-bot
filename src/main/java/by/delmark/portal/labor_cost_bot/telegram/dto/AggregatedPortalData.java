package by.delmark.portal.labor_cost_bot.telegram.dto;

import by.delmark.portal.labor_cost_bot.portal.response.dto.Day;
import by.delmark.portal.labor_cost_bot.portal.response.dto.FavoriteProject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedPortalData {
    List<Day> daysToFill;
    LaborRank rank;
    List<FavoriteProject> favoriteProjects;
    UUID employeeId;
}
