package by.delmark.portal.labor_cost_bot.portal.response;

import by.delmark.portal.labor_cost_bot.portal.response.dto.ArticleMainTag;
import by.delmark.portal.labor_cost_bot.portal.response.dto.ArticleType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleFeedResponse {
    UUID externalId;
    ArticleType type;
    String name;
    String summary;
    Boolean isDraft;
    Boolean isSurvey;
    ArticleMainTag mainTagDto;
    OffsetDateTime createdDate;
    OffsetDateTime lastModifiedDate;
}
