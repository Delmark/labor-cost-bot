package by.delmark.portal.labor_cost_bot.telegram.dto;

import by.delmark.portal.labor_cost_bot.portal.response.dto.Day;
import by.delmark.portal.labor_cost_bot.portal.response.dto.FavoriteProject;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class FillSession {

    private final List<Day> days;
    private final List<FavoriteProject> projects;
    private final UUID employeeId;

    // <dayExternalId <-> (projectExternalId - процент)>
    private final Map<UUID, Map<UUID, Integer>> valuesByDay = new HashMap<>();

    private int index = 0;

    public FillSession(List<Day> days, List<FavoriteProject> projects, UUID employeeId) {
        this.days = days;
        this.projects = projects;
        this.employeeId = employeeId;
    }

    public Day currentDay() {
        return days.get(index);
    }

    public Map<UUID, Integer> currentValues() {
        return valuesByDay.computeIfAbsent(currentDay().getDayExternalId(), key -> new HashMap<>());
    }

    public int currentTotal() {
        return currentValues().values().stream().mapToInt(Integer::intValue).sum();
    }

    public int currentPercent(UUID projectId) {
        return currentValues().getOrDefault(projectId, 0);
    }

    public void setCurrentValue(UUID projectId, int percent) {
        Map<UUID, Integer> values = currentValues();
        if (percent <= 0) {
            values.remove(projectId);
        } else {
            values.put(projectId, percent);
        }
    }

    public boolean moveTo(int newIndex) {
        if (newIndex < 0 || newIndex >= days.size()) {
            return false;
        }
        this.index = newIndex;
        return true;
    }
}
