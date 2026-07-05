package by.delmark.portal.labor_cost_bot.telegram;

import by.delmark.portal.labor_cost_bot.portal.PortalClient;
import by.delmark.portal.labor_cost_bot.portal.response.ProfileResponse;
import by.delmark.portal.labor_cost_bot.portal.response.dto.Calendar;
import by.delmark.portal.labor_cost_bot.portal.response.dto.Day;
import by.delmark.portal.labor_cost_bot.portal.response.dto.FavoriteProject;
import by.delmark.portal.labor_cost_bot.telegram.dto.AggregatedPortalData;
import by.delmark.portal.labor_cost_bot.telegram.dto.CollectedInfo;
import by.delmark.portal.labor_cost_bot.telegram.dto.LaborRank;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalDataAggregator {

    private final PortalClient portalClient;
    private final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public AggregatedPortalData getAggregatedPortalData() {
        ProfileResponse profileResponse = portalClient.getProfileBaseInfo();

        String currentYear = yearFormatter.format(LocalDate.now());

        Calendar currentCalendar = profileResponse.getCalendarDtoList().stream()
                .filter(calendar -> calendar.getFiscalYearCode().equals(currentYear))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No calendar for fiscal year " + currentYear));

        List<Day> uncompletedDays = currentCalendar.getDayDtoList().stream()
                .filter(day -> !day.getIsFuture() && day.getIsWorking() && !day.getIsLaborCostCompleted())
                .collect(Collectors.toList());

        UUID fiscalYearId = currentCalendar.getFiscalYearId();
        UUID employeeId = profileResponse.getExternalId();

        List<FavoriteProject> favoriteProjects = portalClient
                .getEmployeeProjects(fiscalYearId, employeeId)
                .getFavoriteProjectList()
                .stream()
                .filter(project -> project.getStatus().equalsIgnoreCase("активен"))
                .collect(Collectors.toList());

        return AggregatedPortalData.builder()
                .daysToFill(uncompletedDays)
                .rank(LaborRank.getLaborRank(uncompletedDays.size()))
                .favoriteProjects(favoriteProjects)
                .build();
    }

    public CollectedInfo collectInfoMessage() {
        AggregatedPortalData data = getAggregatedPortalData();
        StringBuilder responseText = new StringBuilder();
        responseText.append("Текущая информация по трудозатратам:");

        List<Day> daysToFill = data.getDaysToFill();
        if (daysToFill.isEmpty()) {
            responseText.append(
                    "У вас нет незаполненный дней! Идеально!.\n\nВаш ранг: %s"
                            .formatted(data.getRank().getAlias())
            );
            return new CollectedInfo(responseText.toString(), false);
        }

        int uncompleted = daysToFill.size();
        responseText.append("В данный момент незаполненно %d дней".formatted(uncompleted));
        if (uncompleted < 5) {
            responseText.append("\nНеобходимо заполнить ТРЗ на следующие дни:");
            daysToFill.forEach(day -> responseText
                    .append("\n")
                    .append(DayOfWeek.of(day.getDayOfWeekNumber())
                            .getDisplayName(
                                    TextStyle.FULL_STANDALONE,
                                    Locale.of("ru")
                            )
                    )
                    .append(" ")
                    .append(dateFormat.format(day.getDayDate())));
        } else {
            List<String> uncompletedPeriods = generateDatePeriods(daysToFill);
            responseText.append("\nНеобходимо заполнить ТРЗ на следующие периоды:");
            StringJoiner joiner = new StringJoiner("\n");
            uncompletedPeriods.forEach(joiner::add);
            responseText.append(joiner);
        }

        List<FavoriteProject> projects = data.getFavoriteProjects();
        if (!CollectionUtils.isEmpty(projects)) {
            responseText.append("\n\nПроекты над которыми можно заполнить информацию из этого бота:");
            StringJoiner joiner = new StringJoiner("\n");
            projects.forEach(project -> joiner.add(project.getShortName()));
            responseText.append(joiner);
        }

        responseText.append("\n\nВаш ранг: %s".formatted(data.getRank().getAlias()));
        return new CollectedInfo(responseText.toString(), true);
    }

    private List<String> generateDatePeriods(List<Day> days) {
        List<String> dates = new ArrayList<>();
        List<LocalDate> sortedDates = days.stream()
                .map(Day::getDayDate)
                .sorted()
                .collect(Collectors.toList());

        LocalDate periodStart = sortedDates.getFirst();
        LocalDate prevDay = sortedDates.getFirst();

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate currentDay = sortedDates.get(i);
            if (!currentDay.minusDays(1).equals(prevDay)) {
                dates.add(dateFormat.format(periodStart) + " - " + dateFormat.format(prevDay));
                periodStart = currentDay;
            }
            prevDay = currentDay;
        }
        dates.add(dateFormat.format(periodStart) + " - " + dateFormat.format(prevDay));

        return dates;
    }
}
