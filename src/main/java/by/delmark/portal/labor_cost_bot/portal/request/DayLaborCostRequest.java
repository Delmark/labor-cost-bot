package by.delmark.portal.labor_cost_bot.portal.request;

import by.delmark.portal.labor_cost_bot.portal.request.dto.ProjectValue;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DayLaborCostRequest {
    UUID dayExternalId;
    UUID employeeExternalId;
    List<ProjectValue> projectValues;
}
