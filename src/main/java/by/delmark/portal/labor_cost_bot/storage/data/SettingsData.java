package by.delmark.portal.labor_cost_bot.storage.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SettingsData {
    private boolean isLaborCostNotificationEnabled;
    private boolean isNewArticlesNotificationEnabled;
}
