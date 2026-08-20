package by.delmark.portal.labor_cost_bot.telegram.service;

import by.delmark.portal.labor_cost_bot.portal.PortalClient;
import by.delmark.portal.labor_cost_bot.portal.request.ArticleFeedRequest;
import by.delmark.portal.labor_cost_bot.portal.response.ArticleFeedResponse;
import by.delmark.portal.labor_cost_bot.telegram.callbacks.ArticleCallbacks;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class ArticleFeedService {

    private final TelegramBot bot;
    private final PortalClient portalClient;

    // TODO: в будущем добавить возможность настроить зону
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter articleCreatedAtFormatter = DateTimeFormatter.ofPattern("MM.dd HH:mm:ss");

    public String showArticleFeed(Long chatId, Integer messageId, String data) {
        int page = Integer.parseInt(data.split(":")[1]);
        if (page < 0) {
            return "Больше новостей нет";
        }

        List<ArticleFeedResponse> articles = portalClient
                .getArticleFeed(
                        ArticleFeedRequest.builder()
                                .pageNumber(page)
                                .build()
                );
        if (CollectionUtils.isEmpty(articles)) {
            if (page == 0) {
                return "Не удалось найти новости";
            }
            return "Больше новостей нет";
        }

        InlineKeyboardMarkup responseKeyboard = new InlineKeyboardMarkup();
        StringJoiner responseArticles = new StringJoiner("\n\n");
        for (ArticleFeedResponse article : articles) {
            String formattedCreatedAt = article.getCreatedDate()
                    .atZoneSameInstant(DEFAULT_ZONE).format(articleCreatedAtFormatter);

            String titleWithDate = formattedCreatedAt + " ~~ " + article.getName();
            String articleInfo = titleWithDate + "\n" + article.getSummary();
            responseArticles.add(articleInfo);

            // todo: как смапить id из сервиса в наш?
            String internalArticleId = article.getExternalId().toString();
            String callbackData = ArticleCallbacks.FULL_ARTICLE + internalArticleId;
            responseKeyboard.addRow(
                    new InlineKeyboardButton(article.getName(), callbackData)
            );
        }
        responseKeyboard.addRow(
                new InlineKeyboardButton("<-", ArticleCallbacks.ARTICLE_FEED + (page - 1)),
                new InlineKeyboardButton("Выход", "exit placeholder"),
                new InlineKeyboardButton("->", ArticleCallbacks.ARTICLE_FEED + (page + 1))
        );

        String message = "~~~ Страница %d ~~~ \n\n\n %s".formatted(page + 1, responseArticles);

        EditMessageText editMessageRequest = new EditMessageText(chatId, messageId, message)
                .replyMarkup(responseKeyboard);
        bot.execute(editMessageRequest);

        return null;
    }



}
