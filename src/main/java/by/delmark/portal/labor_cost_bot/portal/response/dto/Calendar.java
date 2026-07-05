package by.delmark.portal.labor_cost_bot.portal.response.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Calendar {
    UUID fiscalYearId;
    String fiscalYearCode;
    List<Day> dayDtoList;
}
