package by.delmark.portal.labor_cost_bot.portal.response;

import by.delmark.portal.labor_cost_bot.portal.response.dto.ArticleTag;
import by.delmark.portal.labor_cost_bot.portal.response.dto.Type;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleResponse {
    UUID externalId;
    String name;
    String summary;
    ArticleTag mainTagDto;
    Type type;
    OffsetDateTime createdDate;
    OffsetDateTime lastModifiedDate;
    String html;
}
