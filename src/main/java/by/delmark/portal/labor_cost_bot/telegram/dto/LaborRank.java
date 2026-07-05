package by.delmark.portal.labor_cost_bot.telegram.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LaborRank {
    EXEMPLARY("Примерный"),
    NORMAL("Обычный"),
    LAZY("Ленивый"),
    SALARYLESS("У кого-то не будет зарплаты");

    private final String alias;

    public static LaborRank getLaborRank(Integer uncompletedDays) {
        if (uncompletedDays == 0) {
            return EXEMPLARY;
        } else if (uncompletedDays >= 1 && uncompletedDays < 4) {
            return NORMAL;
        } else if (uncompletedDays >= 4 && uncompletedDays < 15) {
            return LAZY;
        } else {
            return SALARYLESS;
        }
    }
}
