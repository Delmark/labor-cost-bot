package by.delmark.portal.labor_cost_bot.telegram.service;

import by.delmark.portal.labor_cost_bot.portal.PortalClient;
import by.delmark.portal.labor_cost_bot.portal.request.ArticleFeedRequest;
import by.delmark.portal.labor_cost_bot.portal.response.ArticleFeedResponse;
import by.delmark.portal.labor_cost_bot.portal.response.ArticleResponse;
import by.delmark.portal.labor_cost_bot.storage.CacheStorage;
import by.delmark.portal.labor_cost_bot.telegram.callbacks.ArticleCallbacks;
import by.delmark.portal.labor_cost_bot.telegram.utils.HtmlToRichMessageConverter;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.richmessages.InputRichMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.richmessages.SendRichMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleFeedService {

    private final TelegramBot bot;
    private final PortalClient portalClient;
    private final CacheStorage cacheStorage;

    private static final String EXT_ID_CACHE = "news";
    private static final int LENGTH_LIMIT = 32768;

    // TODO: в будущем добавить возможность настроить зону
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter articleCreatedAtFormatter = DateTimeFormatter.ofPattern("MM.dd HH:mm:ss");
    private final HtmlToRichMessageConverter htmlToRichMessageConverter;

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

            int articleIdHash = cacheStorage.putExternalIdIfAbsent(EXT_ID_CACHE, article.getExternalId());
            String callbackData = ArticleCallbacks.FULL_ARTICLE + articleIdHash;
            responseKeyboard.addRow(
                    new InlineKeyboardButton(article.getName()).callbackData(callbackData)
            );
        }
        responseKeyboard.addRow(
                new InlineKeyboardButton("<-").callbackData(ArticleCallbacks.ARTICLE_FEED + (page - 1)),
                new InlineKeyboardButton("->").callbackData(ArticleCallbacks.ARTICLE_FEED + (page + 1))
        );

        String message = "~~~ Страница %d ~~~ \n\n\n %s".formatted(page + 1, responseArticles);

        EditMessageText editMessageRequest = new EditMessageText(chatId, messageId, message)
                .replyMarkup(responseKeyboard);
        bot.execute(editMessageRequest);

        return null;
    }

    public String showFullArticle(Long chatId, Integer messageId, String data) {
        int articleIdHash = Integer.parseInt(data.split(":")[1]);
        UUID articleExternalId = cacheStorage.getExternalId(EXT_ID_CACHE, articleIdHash);
        if (articleExternalId == null) {
            return "Не удалось получить информацию об статье";
        }
        ArticleResponse article = portalClient.getArticle(articleExternalId);
        String richMessageConvertedText = htmlToRichMessageConverter.convertHtmlToMarkdown(article.getHtml());
        if (richMessageConvertedText.length() > LENGTH_LIMIT) {
            chunkAndSendMultipleMessages(richMessageConvertedText, chatId);
        } else {
            SendRichMessage messageReq = new SendRichMessage(chatId, new InputRichMessage().markdown(richMessageConvertedText));
            BaseResponse response = bot.execute(messageReq);
            log.debug("{} {}", response.errorCode(), response.description());
        }
        return null;
    }

    // todo: разбивать надо не по тексту, а по блокам
    private void chunkAndSendMultipleMessages(String fullMessage, Long chatId) {
        String[] chunkedRichMessage = new String[fullMessage.length() / LENGTH_LIMIT];
        for (int i = 0; i < chunkedRichMessage.length; i++) {
            int chunkStart = i * LENGTH_LIMIT;
            int chunkEnd = Math.min(LENGTH_LIMIT * i, fullMessage.length() - 1);
            String chunk = fullMessage.substring(chunkStart, chunkEnd);
            SendRichMessage messageReq = new SendRichMessage(chatId, new InputRichMessage().markdown(chunk));
            bot.execute(messageReq);
        }
    }
}
