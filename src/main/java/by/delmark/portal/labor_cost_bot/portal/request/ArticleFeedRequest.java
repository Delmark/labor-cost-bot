package by.delmark.portal.labor_cost_bot.portal.request;

import by.delmark.portal.labor_cost_bot.portal.request.dto.SortBy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleFeedRequest {
    @Builder.Default
    List<UUID> authorExternalIdList = new ArrayList<>();

    Integer pageNumber;

    @Builder.Default
    Integer pageSize = 5;

    @Builder.Default
    SortBy sort = SortBy.CREATED_DESC;

    @Builder.Default
    List<UUID> tagExternalIdList = new ArrayList<>();

    @Builder.Default
    List<String> typeList = List.of("NEWS", "FLOOD");
}


