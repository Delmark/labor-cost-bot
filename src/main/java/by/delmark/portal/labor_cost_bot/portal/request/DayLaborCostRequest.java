package by.delmark.portal.labor_cost_bot.portal.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DayLaborCostRequest {
    String dayExternalId;
    UUID employeeExternalId;

}
