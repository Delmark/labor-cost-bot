package by.delmark.portal.labor_cost_bot.telegram.utils;

import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class HtmlToRichMessageConverter {

    public static final Map<String, HtmlTagMappingRegistry> htmlTagRegistry =
            Arrays.stream(HtmlTagMappingRegistry.values())
                    .collect(Collectors.toMap(
                            HtmlTagMappingRegistry::getHtmlTag,
                            Function.identity()
                    ));

    public String convertHtmlToMarkdown(String html) {
        Document document = Jsoup.parse(html);
        Elements allElements = document.getAllElements();

        StringBuilder markdown = new StringBuilder();

        allElements.iterator()
                .forEachRemaining(element ->
                        markdown.append(convertTagToMarkdown(element))
                );

        return markdown.toString();
    }

    public String convertTagToMarkdown(Element element) {
        String tag = element.tagName();
        // если мы не знаем что это за тэг, поставим параграф по умолчанию
        if (!htmlTagRegistry.containsKey(tag)) {
            tag = HtmlTagMappingRegistry.PARAGRAPH.getHtmlTag();
        }
        HtmlTagMappingRegistry tagFromRegistry = htmlTagRegistry.get(tag);
        Function<Element, String> toMarkdownConverter = tagFromRegistry.getMarkdownConverter();
        return toMarkdownConverter.apply(element);
    }
}
