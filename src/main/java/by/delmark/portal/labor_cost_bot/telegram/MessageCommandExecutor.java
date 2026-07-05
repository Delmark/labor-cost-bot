package by.delmark.portal.labor_cost_bot.telegram;

import by.delmark.portal.labor_cost_bot.telegram.dto.CollectedInfo;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
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
                    Данный бот предназначен для упрощения процесса выставления трудозатрат на корпоративном портале компании ООО "42".
                    Возможности бота:
                    
                    - Просмотр дней для которых необходимо выставить трудозатраты
                    - Возможность выставить типовые трудозатраты (по шаблону).
                    
                    Для получения общей информации по своим трудозатратам введите /info.
                    """;
            SendMessage messageRequest = new SendMessage(message.chat().id(), responseText);

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Информация по трудозатратам");
            button.setCallbackData("labor_cost_info");

            messageRequest.setReplyMarkup(new InlineKeyboardMarkup(button));
            bot.execute(messageRequest);
        };
    }

    private Consumer<Message> infoCommand() {
        return message -> {
            try {
                CollectedInfo info = portalDataAggregator.collectInfoMessage();
                SendMessage messageRequest = new SendMessage(message.chat().id(), info.message());
                bot.execute(messageRequest);
            } catch (Exception e) {
                log.error("Failed to collect labor cost info", e);
                bot.execute(new SendMessage(message.chat().id(),
                        "Не удалось получить данные с портала. Попробуйте позже."));
            }
        };
    }
}
