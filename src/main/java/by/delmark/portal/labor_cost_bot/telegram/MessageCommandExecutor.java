package by.delmark.portal.labor_cost_bot.telegram;

import by.delmark.portal.labor_cost_bot.telegram.callbacks.ArticleCallbacks;
import by.delmark.portal.labor_cost_bot.telegram.callbacks.DayLaborCostCallbacks;
import by.delmark.portal.labor_cost_bot.telegram.callbacks.SystemCallbacks;
import by.delmark.portal.labor_cost_bot.telegram.dto.CollectedInfo;
import by.delmark.portal.labor_cost_bot.telegram.service.PortalDataAggregator;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageCommandExecutor {

    private final TelegramBot bot;
    private final PortalDataAggregator portalDataAggregator;

    private final Map<String, Consumer<Message>> commandsMap = Map.of(
            "/help",  helpCommand(),
            "/start", helpCommand(),
            "/info",  infoCommand()
    );

    public void handleCommand(Message message) {
        String text = message.text();
        if (text == null || text.isBlank()) {
            return;
        }
        String messageCommand = text.split(" ")[0];
        Consumer<Message> command = commandsMap.get(messageCommand);
        if (command != null) {
            command.accept(message);
        }
    }

    private Consumer<Message> helpCommand() {
        return message -> {
            String responseText = """
                    Данный бот предназначен для упрощения процесса выставления трудозатрат на корпоративном портале.
                    
                    Возможности бота:
                    
                    Трудозатраты
                    - Просмотр дней для которых необходимо выставить трудозатраты
                    - Возможность выставить типовые трудозатраты (по шаблону).
                    - Включить напоминание про необходимость заполнения ТРЗ.
                    
                    Стена
                    - Просмотр стены с портала, включая просмотр новостей.
                    - Включить автоматическое уведомления о новостях на портале.
                    
                    Команда получения общей информации по своим трудозатратам: /info
                    Команда просмотра стены: /feed
                    Команда настроек: /settings
                    """;
            long chatId = message.chat().id();

            SendMessage messageRequest = new SendMessage(chatId, responseText)
                    .replyMarkup(buildHelpKeyboard());
            bot.execute(messageRequest);
        };
    }

    private InlineKeyboardMarkup buildHelpKeyboard() {
        return new InlineKeyboardMarkup()
                .addRow(new InlineKeyboardButton("Информация по трудозатратам").callbackData(DayLaborCostCallbacks.INFO))
                .addRow(new InlineKeyboardButton("Просмотр стены").callbackData(ArticleCallbacks.ARTICLE_FEED + 0))
                .addRow(new InlineKeyboardButton("Настройки").callbackData(SystemCallbacks.SETTINGS));
    }

    private Consumer<Message> infoCommand() {
        return message -> {
            long chatId = message.chat().id();
            try {
                sendInfo(chatId);
            } catch (Exception e) {
                log.error("Failed to collect labor cost info", e);
                bot.execute(new SendMessage(chatId,
                        "Не удалось получить данные с портала. Попробуйте позже."));
            }
        };
    }

    public void sendInfo(long chatId) {
        CollectedInfo info = portalDataAggregator.collectInfoMessage();
        SendMessage request = new SendMessage(chatId, info.message());
        if (info.needToFill()) {
            request.replyMarkup(fillKeyboard());
        }
        bot.execute(request);
    }

    public void editToInfo(long chatId, Integer messageId) {
        CollectedInfo info = portalDataAggregator.collectInfoMessage();
        EditMessageText request = new EditMessageText(chatId, messageId, info.message());
        if (info.needToFill()) {
            request.replyMarkup(fillKeyboard());
        }
        bot.execute(request);
    }

    private InlineKeyboardMarkup fillKeyboard() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("Проставить дни").callbackData(DayLaborCostCallbacks.FILL));
    }
}
