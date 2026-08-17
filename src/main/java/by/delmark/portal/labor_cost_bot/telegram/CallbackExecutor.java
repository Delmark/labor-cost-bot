package by.delmark.portal.labor_cost_bot.telegram;

import by.delmark.portal.labor_cost_bot.telegram.callbacks.ArticleCallbacks;
import by.delmark.portal.labor_cost_bot.telegram.callbacks.DayLaborCostCallbacks;
import by.delmark.portal.labor_cost_bot.telegram.callbacks.SystemCallbacks;
import by.delmark.portal.labor_cost_bot.telegram.service.ArticleFeedService;
import by.delmark.portal.labor_cost_bot.telegram.service.LaborCostFillingService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.message.MaybeInaccessibleMessage;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackExecutor {

    private final TelegramBot bot;
    private final MessageCommandExecutor messageCommandExecutor;
    private final LaborCostFillingService laborCostFillingService;
    private final ArticleFeedService articleFeedService;

    public void handleCallback(CallbackQuery callbackQuery) {
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
        // переделать в стратегию?
        String cbPrefix = data.split("-")[0];
        switch (cbPrefix) {
            case DayLaborCostCallbacks.PREFIX -> {
                return handleLCCallback(data, message);
            }
            case ArticleCallbacks.PREFIX -> {
                return handleArticleCallback(data, message);
            }
            case SystemCallbacks.PREFIX -> {
                return handleSystemCallback(data, message);
            }
            default -> {
                return null;
            }
        }

    }


    private String handleLCCallback(String data, MaybeInaccessibleMessage message) {
        Long chatId = message.chat().id();
        Integer messageId = message.messageId();
        if (DayLaborCostCallbacks.INFO.equals(data)) {
            messageCommandExecutor.sendInfo(chatId);
            return null;
        }
        if (DayLaborCostCallbacks.FILL.equals(data)) {
            return laborCostFillingService.enter(chatId, messageId);
        }
        if (data.startsWith(DayLaborCostCallbacks.SET_PREFIX)) {
            return laborCostFillingService.setPercent(chatId, messageId, data);
        }
        if (DayLaborCostCallbacks.NAV_PREV.equals(data)) {
            return laborCostFillingService.navigate(chatId, messageId, -1);
        }
        if (DayLaborCostCallbacks.NAV_NEXT.equals(data)) {
            return laborCostFillingService.navigate(chatId, messageId, 1);
        }
        if (DayLaborCostCallbacks.EXIT.equals(data)) {
            String toast = laborCostFillingService.leave(chatId);
            messageCommandExecutor.editToInfo(chatId, messageId);
            return toast;
        }
        return null;
    }

    private String handleArticleCallback(String data, MaybeInaccessibleMessage message) {
        return null;
    }

    private String handleSystemCallback(String data, MaybeInaccessibleMessage message) {
        return null;
    }
}
