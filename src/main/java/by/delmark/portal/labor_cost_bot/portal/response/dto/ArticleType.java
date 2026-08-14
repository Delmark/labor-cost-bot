package by.delmark.portal.labor_cost_bot.portal.response.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleType {
    String id;
    String title;
}
