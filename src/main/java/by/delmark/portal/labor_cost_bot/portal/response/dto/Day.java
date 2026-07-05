package by.delmark.portal.labor_cost_bot.portal.response.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Day {
    UUID dayExternalId;
    LocalDate dayDate;
    Integer dayOfWeekNumber;
    Boolean isFuture;
    Boolean isLaborCostCompleted;
    Boolean isPortalFactLaborCostAvailable;
    Boolean isWorking;
}
