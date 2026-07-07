package by.delmark.portal.labor_cost_bot.telegram;

import by.delmark.portal.labor_cost_bot.storage.FileStorage;
import by.delmark.portal.labor_cost_bot.storage.UserData;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.message.MaybeInaccessibleMessage;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
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

    private final MessageCommandExecutor messageCommandExecutor;
    private final DayFillingService dayFillingService;
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

        String toast;
        try {
            toast = dispatchCallback(callbackQuery);
        } catch (Exception e) {
            log.error("Failed to handle callback {}", callbackQuery.data(), e);
            toast = "Произошла ошибка, попробуйте позже";
        }

        AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.id());
        if (toast != null) {
            answer.text(toast);
        }
        bot.execute(answer);
    }

    private String dispatchCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.data();
        MaybeInaccessibleMessage message = callbackQuery.maybeInaccessibleMessage();
        if (data == null || message == null) {
            return null;
        }
        Long chatId = message.chat().id();
        Integer messageId = message.messageId();

        if (Callbacks.INFO.equals(data)) {
            messageCommandExecutor.sendInfo(chatId);
            return null;
        }
        if (Callbacks.FILL.equals(data)) {
            return dayFillingService.enter(chatId, messageId);
        }
        if (data.startsWith(Callbacks.SET_PREFIX)) {
            return dayFillingService.setPercent(chatId, messageId, data);
        }
        if (Callbacks.NAV_PREV.equals(data)) {
            return dayFillingService.navigate(chatId, messageId, -1);
        }
        if (Callbacks.NAV_NEXT.equals(data)) {
            return dayFillingService.navigate(chatId, messageId, 1);
        }
        if (Callbacks.EXIT.equals(data)) {
            String toast = dayFillingService.leave(chatId);
            messageCommandExecutor.editToInfo(chatId, messageId);
            return toast;
        }
        return null;
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
