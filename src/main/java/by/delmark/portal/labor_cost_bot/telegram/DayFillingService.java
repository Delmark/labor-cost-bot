package by.delmark.portal.labor_cost_bot.telegram;

import by.delmark.portal.labor_cost_bot.portal.PortalClient;
import by.delmark.portal.labor_cost_bot.portal.request.DayLaborCostRequest;
import by.delmark.portal.labor_cost_bot.portal.request.dto.ProjectValue;
import by.delmark.portal.labor_cost_bot.portal.response.dto.Day;
import by.delmark.portal.labor_cost_bot.portal.response.dto.FavoriteProject;
import by.delmark.portal.labor_cost_bot.telegram.dto.AggregatedPortalData;
import by.delmark.portal.labor_cost_bot.telegram.dto.FillSession;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DayFillingService {

    private static final int[] PRESETS = {0, 25, 50, 100};
    private static final Locale RU = Locale.of("ru");

    private final TelegramBot bot;
    private final PortalDataAggregator portalDataAggregator;
    private final PortalClient portalClient;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final Map<Long, FillSession> sessions = new ConcurrentHashMap<>();

    public String enter(Long chatId, Integer messageId) {
        AggregatedPortalData data = portalDataAggregator.getAggregatedPortalData();
        List<Day> days = data.getDaysToFill().stream()
                .sorted(Comparator.comparing(Day::getDayDate))
                .collect(Collectors.toList());
        if (days.isEmpty()) {
            return "Нет дней для заполнения";
        }
        if (data.getFavoriteProjects().isEmpty()) {
            return "Нет доступных проектов для заполнения";
        }

        FillSession session = new FillSession(days, data.getFavoriteProjects(), data.getEmployeeId());
        sessions.put(chatId, session);
        renderDay(chatId, messageId, session);
        return null;
    }

    public String setPercent(Long chatId, Integer messageId, String data) {
        FillSession session = sessions.get(chatId);
        if (session == null) {
            return "Сессия устарела, откройте информацию заново";
        }

        String[] parts = data.split(":");
        int projectIndex = Integer.parseInt(parts[1]);
        int percent = Integer.parseInt(parts[2]);

        FavoriteProject project = session.getProjects().get(projectIndex);
        int current = session.currentPercent(project.getExternalId());
        if (current == percent) {
            return null;
        }

        int othersTotal = session.currentTotal() - current;
        if (othersTotal + percent > 100) {
            return "Суммарно нельзя больше 100%%, осталось %d%%".formatted(100 - othersTotal);
        }

        session.setCurrentValue(project.getExternalId(), percent);
        renderDay(chatId, messageId, session);
        return null;
    }

    public String navigate(Long chatId, Integer messageId, int direction) {
        FillSession session = sessions.get(chatId);
        if (session == null) {
            return "Сессия устарела, откройте информацию заново";
        }

        int target = session.getIndex() + direction;
        if (target < 0 || target >= session.getDays().size()) {
            return direction < 0 ? "Это первый день" : "Это последний день";
        }

        // сохраняем текущий день только когда уходим на соседний/выходим из меню
        String toast = saveIfComplete(session);
        session.moveTo(target);
        renderDay(chatId, messageId, session);
        return toast;
    }

    public String leave(Long chatId) {
        FillSession session = sessions.remove(chatId);
        if (session == null) {
            return null;
        }
        return saveIfComplete(session);
    }

    private String saveIfComplete(FillSession session) {
        int total = session.currentTotal();
        if (total == 100) {
            saveDay(session);
            return "День сохранён ✅";
        }
        if (total > 0) {
            return "День не сохранён: нужно ровно 100%%, сейчас %d%%".formatted(total);
        }
        return null;
    }

    private void saveDay(FillSession session) {
        Day day = session.currentDay();
        List<ProjectValue> projectValues = session.currentValues().entrySet().stream()
                .map(entry -> {
                    ProjectValue value = new ProjectValue();
                    value.setProjectExternalId(entry.getKey());
                    value.setPercentValue(entry.getValue());
                    return value;
                })
                .collect(Collectors.toList());

        DayLaborCostRequest request = new DayLaborCostRequest();
        request.setDayExternalId(day.getDayExternalId());
        request.setEmployeeExternalId(session.getEmployeeId());
        request.setProjectValueList(projectValues);

        log.info("Saving day {} with {} project(s)", day.getDayExternalId(), projectValues.size());
        portalClient.updateDayLaborCost(request);
    }

    private void renderDay(Long chatId, Integer messageId, FillSession session) {
        EditMessageText edit = new EditMessageText(chatId, messageId, buildText(session))
                .replyMarkup(buildKeyboard(session));
        bot.execute(edit);
    }

    private String buildText(FillSession session) {
        Day day = session.currentDay();
        String weekday = DayOfWeek.of(day.getDayOfWeekNumber()).getDisplayName(TextStyle.FULL_STANDALONE, RU);
        int total = session.currentTotal();
        StringJoiner projectNames = new StringJoiner("\n");
        session.getProjects().forEach(project -> projectNames.add(project.getShortName()));
        return """
                📅 %s, %s
                День %d из %d

                Распределите 100%% между проектами.
                Проставлено: %d%%   (осталось %d%%)
                
                Список проектов:
                %s"""
                .formatted(
                        weekday,
                        dateFormat.format(day.getDayDate()),
                        session.getIndex() + 1,
                        session.getDays().size(),
                        total,
                        100 - total,
                        projectNames.toString()
                );
    }

    private InlineKeyboardMarkup buildKeyboard(FillSession session) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<FavoriteProject> projects = session.getProjects();

        for (int i = 0; i < projects.size(); i++) {
            FavoriteProject project = projects.get(i);
            int current = session.currentPercent(project.getExternalId());

            InlineKeyboardButton[] row = new InlineKeyboardButton[PRESETS.length + 1];
            row[0] = new InlineKeyboardButton(project.getShortName() + " - " + current + "%")
                    .callbackData(Callbacks.NOOP);
            for (int j = 0; j < PRESETS.length; j++) {
                int preset = PRESETS[j];
                String label = current == preset ? "[" + preset + "]" : String.valueOf(preset);
                row[j + 1] = new InlineKeyboardButton(label)
                        .callbackData(Callbacks.SET_PREFIX + i + ":" + preset);
            }
            keyboard.addRow(row);
        }

        keyboard.addRow(
                new InlineKeyboardButton("<-").callbackData(Callbacks.NAV_PREV),
                new InlineKeyboardButton("Выйти").callbackData(Callbacks.EXIT),
                new InlineKeyboardButton("->").callbackData(Callbacks.NAV_NEXT)
        );
        return keyboard;
    }
}
