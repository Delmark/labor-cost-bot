package by.delmark.portal.labor_cost_bot.portal.response.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleTag {
    UUID externalId;
    String name;
    Boolean isReadyForDeletion;
    OffsetDateTime lastModifiedDate;
}
