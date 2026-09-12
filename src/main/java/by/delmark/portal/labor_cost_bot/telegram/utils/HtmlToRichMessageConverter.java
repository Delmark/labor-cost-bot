package by.delmark.portal.labor_cost_bot.telegram.utils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlToRichMessageConverter {

    public static final Map<String, HtmlTagMappingRegistry> htmlMappingRegistry =
            Arrays.stream(HtmlTagMappingRegistry.values())
                    .collect(Collectors.toMap(
                            HtmlTagMappingRegistry::getHtmlTag,
                            Function.identity()
                    ));

    private final Portal2TelegramImgAdapter imgAdapter;

    public String convertHtmlToMarkdown(String html) {
        if (!StringUtils.hasText(html)) {
            return "";
        }
        Document document = Jsoup.parse(html);
        Elements allElements = document.body().children();

        StringBuilder markdown = new StringBuilder();

        allElements.forEach(element -> {
//            if (element.tagName().equals("img")) {
//                String src = element.attr("src");
//                element.attr("src", imgAdapter.adaptImageLinkForTG(src));
//            }
            String convertedElement = convertTagToMarkdown(element);
            if (StringUtils.hasText(convertedElement)) {
                if (!markdown.isEmpty()) {
                    markdown.append("\n\n");
                }
                markdown.append(convertedElement.strip());
            }
        });
        log.debug("{}", markdown);
        return markdown.toString();
    }

    public static String convertTagToMarkdown(Element element) {
        String tag = element.tagName();
        HtmlTagMappingRegistry tagFromRegistry;
        if (htmlMappingRegistry.containsKey(tag)) {
            tagFromRegistry = htmlMappingRegistry.get(tag);
        } else if (tag.matches(HtmlTagMappingRegistry.HEADING.getHtmlTag())) {
            // у заголовков N-ого уровня я поставил тэг регуляркой
            tagFromRegistry = HtmlTagMappingRegistry.HEADING;
        } else {
            // если мы не знаем что это за тэг, поставим параграф по умолчанию
            tagFromRegistry = HtmlTagMappingRegistry.PARAGRAPH;
        }
        Function<Element, String> toMarkdownConverter = tagFromRegistry.getMarkdownConverter();
        return toMarkdownConverter.apply(element);
    }
}
