package by.delmark.portal.labor_cost_bot.portal.response;

import by.delmark.portal.labor_cost_bot.portal.response.dto.Type;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleTagResponse {
    UUID externalId;
    String name;
    String description;
    Boolean isReadyForDeletion;
    LocalDate lastModifiedDate;
    Type typeDto;
}
