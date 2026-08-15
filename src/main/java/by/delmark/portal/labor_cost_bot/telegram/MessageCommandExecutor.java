package by.delmark.portal.labor_cost_bot.telegram;

import by.delmark.portal.labor_cost_bot.telegram.callbacks.DayLaborCostCallbacks;
import by.delmark.portal.labor_cost_bot.telegram.dto.CollectedInfo;
import by.delmark.portal.labor_cost_bot.telegram.service.PortalDataAggregator;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.richmessages.InputRichMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.richmessages.SendRichMessage;
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
                    
                    - Просмотр дней для которых необходимо выставить трудозатраты
                    - Возможность выставить типовые трудозатраты (по шаблону).
                    
                    Для получения общей информации по своим трудозатратам введите /info.
                    """;
            long chatId = message.chat().id();
            SendMessage messageRequest = new SendMessage(chatId, responseText)
                    .replyMarkup(new InlineKeyboardMarkup(
                            new InlineKeyboardButton("Информация по трудозатратам").callbackData(DayLaborCostCallbacks.INFO)));
            bot.execute(messageRequest);
        };
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

    String markdown = """
            **bold text**
            __bold text__
            *italic text*
            _italic text_
            ~~strikethrough text~~
            `inline fixed-width code`
            ==marked text==
            ||spoiler||
            
            [inline URL](https://t.me/)
            [inline e-mail](mailto:user@example.com)
            [inline phone number](tel:+123456789)
            [inline mention of a user](tg://user?id=123456789)
            ![👍](tg://emoji?id=5368324170671202286)
            ![22:45 tomorrow](tg://time?unix=1647531900&format=wDT)
            $x^2 + y^2$
            \\#hashtag $USD +12345678901, card: 4242 4242 4242 4242, https://t.me t.me a@t.me /command @username
            all the text above was on the same line
            
            # Heading 1
            ## Heading 2
            ### Heading 3
            #### Heading 4
            ##### Heading 5
            ###### Heading 6
            
            Paragraph text
            
            ```python
              print('pre-formatted fixed-width code block written in the Python programming language')
            ```
            
            ---
            
            - unordered list item
            * unordered list item
            + unordered list item
            
            1. ordered list item
            2. ordered list item
            
            - [ ] task list item
            - [x] completed task list item
            
            >Block quotation started
            >
            >Block quotation continued on the next line
            >Block quotation continued on the same line
            >
            >The last line of the block quotation
            
            ![](https://telegram.org/example/photo.jpg)
            ![](https://telegram.org/example/video.mp4)
            ![](https://telegram.org/example/audio.mp3)
            ![](https://telegram.org/example/audio.ogg)
            ![](https://telegram.org/example/animation.gif)
            
            ![](https://telegram.org/example/photo.jpg "Photo caption")
            ![](https://telegram.org/example/video.mp4 "Video caption")
            ![](https://telegram.org/example/audio.mp3 "Audio caption")
            ![](https://telegram.org/example/audio.ogg "Voice note caption")
            ![](https://telegram.org/example/animation.gif "Animation caption")
            
            | Header 1 | Header 2 |
            |:---------|:--------:|
            | left     | center   |
            
            Text with a reference[^id1] and another one[^id2].
            
            [^id1]: Definition of the first footnote.
            [^id2]: Definition of the second footnote.
            
            $$E = mc^2$$
            
            ```math
            E = mc^2
            ```
            
            ## Example Nested Syntax Report for _Q1_
            Intro with <u>underlined text</u>, ==marked text==, and $x^2 + y^2$.
            **Bold _italic <u>underlined italic bold</u> italic_ bold**
            <u>In inline tags, nested **markdown** is parsed</u>
            >Quote with **bold text, ~~strikethrough, and <tg-spoiler>spoiler</tg-spoiler>~~**, plus [a link](https://t.me/).
            
            - List item with `code`, <sup>superscript</sup>, <sub>subscript</sub>, and a footnote[^note]
            - Another item with **bold <tg-spoiler><code>spoiler code</code></tg-spoiler>**
            - Another item with ~~strikethrough and <ins>inserted text</ins>~~
            
            | Metric | Value |
            |:-------|------:|
            | Speed  | **42** <sup>ms</sup> |
            | Status | <tg-spoiler>ready</tg-spoiler> |
            
            [^note]: Footnote with _italic text_ and <u>HTML underline</u>.
            
            ---
            
            # Details blocks can contain Markdown content:
            
            <details open><summary>Summary with **bold text**</summary>
            
            ### Details heading
            - List item with _italic text_
            - List item with <tg-spoiler>spoiler</tg-spoiler>
            
            </details>
            
            # Collages and slideshows can contain Markdown media blocks:
            
            <tg-collage>
            
            ![](https://telegram.org/example/photo.jpg)
            ![](https://telegram.org/example/video.mp4)
            
            </tg-collage>
            
            <tg-slideshow>
            
            ![](https://telegram.org/example/photo.jpg)
            ![](https://telegram.org/example/video.mp4)
            
            </tg-slideshow>
            
            """;

    public void sendInfo(long chatId) {
        CollectedInfo info = portalDataAggregator.collectInfoMessage();
        SendMessage request = new SendMessage(chatId, info.message());
        if (info.needToFill()) {
            request.replyMarkup(fillKeyboard());
        }
        bot.execute(request);
        bot.execute(
                new SendRichMessage(
                    chatId,
                    new InputRichMessage().markdown(markdown)
                )
        );
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
