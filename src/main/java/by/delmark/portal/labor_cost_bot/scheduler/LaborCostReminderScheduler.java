package by.delmark.portal.labor_cost_bot.scheduler;

import by.delmark.portal.labor_cost_bot.storage.FileStorage;
import by.delmark.portal.labor_cost_bot.storage.UserData;
import by.delmark.portal.labor_cost_bot.telegram.Callbacks;
import by.delmark.portal.labor_cost_bot.telegram.PortalDataAggregator;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class LaborCostReminderScheduler {

    private final TelegramBot bot;
    private final FileStorage fileStorage;
    private final PortalDataAggregator portalDataAggregator;

    @Scheduled(cron = "${reminder.cron:0 0 18 * * *}", zone = "${reminder.zone:Europe/Minsk}")
    public void remindUncompletedDays() {
        Optional<UserData> userData = fileStorage.getUserData();
        if (userData.isEmpty()) {
            return;
        }

        try {
            int uncompleted = portalDataAggregator.getAggregatedPortalData().getDaysToFill().size();
            if (uncompleted == 0) {
                return;
            }

            long chatId = userData.get().getChatId();
            SendMessage reminder = new SendMessage(chatId, buildReminder(uncompleted))
                    .replyMarkup(new InlineKeyboardMarkup(
                            new InlineKeyboardButton("Проставить дни").callbackData(Callbacks.FILL)));
            bot.execute(reminder);
        } catch (Exception e) {
            log.error("Не удалось отправить ежедневное напоминание", e);
        }
    }

    private String buildReminder(int uncompleted) {
        return "🔔 Напоминание: нужно заполнить трудозатраты за %d %s."
                .formatted(uncompleted, pluralizeDays(uncompleted));
    }

    private String pluralizeDays(int count) {
        int mod10 = count % 10;
        int mod100 = count % 100;
        if (mod10 == 1 && mod100 != 11) {
            return "день";
        }
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) {
            return "дня";
        }
        return "дней";
    }
}
