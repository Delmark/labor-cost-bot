package by.delmark.portal.labor_cost_bot.telegram;

import by.delmark.portal.labor_cost_bot.portal.PortalClient;
import by.delmark.portal.labor_cost_bot.storage.FileStorage;
import by.delmark.portal.labor_cost_bot.storage.UserData;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramUpdatesProcessor {

    private final PortalClient portalClient;
    private final MessageCommandExecutor messageCommandExecutor;
    private final FileStorage fileStorage;
    private final TelegramBot bot;

    @Value("${telegram.bot.telegram-user-id}")
    private Long userId;

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(getListener());
    }

    private UpdatesListener getListener() {
        return updates -> {
            updates.forEach(update -> {
                try {
                    if (update.message() != null) {
                        handleMessage(update);
                    }
                    if (update.callbackQuery() != null) {
                        handleCallback(update);
                    }
                } catch (Exception e) {
                    log.error("Failed to process update {}", update.updateId(), e);
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        };
    }

    private void handleCallback(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        // игнорим других пользователей кроме юзера
        if (!Objects.equals(userId, callbackQuery.from().id())) {
            return;
        }

    }

    private void handleMessage(Update update) {
        Message message = update.message();
        // игнорим других пользователей кроме юзера
        if (!Objects.equals(userId, message.from().id())) {
            return;
        }
        ensureRegistration(message);
        messageCommandExecutor.handleCommand(message);
    }

    private void ensureRegistration(Message message) {
        Long chatId = message.chat().id();
        Long senderId = message.from().id();
        if (fileStorage.getUserData().isPresent()) {
            return;
        }
        fileStorage.updateUserData(new UserData(senderId, chatId));
    }
}
