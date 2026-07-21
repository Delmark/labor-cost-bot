package by.delmark.portal.labor_cost_bot.portal.response;

import by.delmark.portal.labor_cost_bot.portal.response.dto.Calendar;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileResponse {
    UUID externalId;
    String fullName;
    Integer emptyLaborCostDays;
}
