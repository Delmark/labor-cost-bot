package by.delmark.portal.labor_cost_bot.telegram.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum HtmlTagMappingRegistry {
    BOLD("strong", (element) -> {
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        return "**" + content + "**";
    }),

    BREAK("br", (_) -> "\n"),

    ITALIC("em", (element) -> {
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        return "_" + content + "_";
    }),

    LINK("a", (element) -> {
        StringBuilder content = new StringBuilder();
        String href = element.attr("href");
        writeInnerContent(content, element);
        return "[" + content + "](" + href + ")";
    }),

    UNDERSCORE("u", (element) -> {
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        return "<u>" + content + "</u>"; // почти ничего не поменялось
    }),

    ORDERED_LIST("ol", (element) -> {
        AtomicInteger order = new AtomicInteger();
        StringBuilder content = new StringBuilder();

        List<Node> childNodes = element.childNodes();
        childNodes.forEach(childNode -> {
            switch (childNode) {
                case Element innerListEl when element.tagName().equals("li") -> {
                    String elementOrder = order.incrementAndGet() + ". ";
                    StringBuilder innerLiContent = new StringBuilder();
                    writeInnerContent(innerLiContent, innerListEl);
                    content.append(elementOrder).append(innerLiContent).append("\n");
                }
                case Element innerElement -> content.append(
                        HtmlToRichMessageConverter
                                .convertTagToMarkdown(innerElement)
                );
                case TextNode textNode -> content.append(textNode.text());
                default -> {}
            }
        });

        return content.toString();
    }),

    UNORDERED_LIST("ul", (element) -> {
        StringBuilder content = new StringBuilder();

        List<Node> childNodes = element.childNodes();
        childNodes.forEach(childNode -> {
            switch (childNode) {
                case Element innerListEl when element.tagName().equals("li") -> {
                    StringBuilder innerLiContent = new StringBuilder();
                    writeInnerContent(innerLiContent, innerListEl);
                    content.append("*").append(innerLiContent).append("\n");
                }
                case Element innerElement -> content.append(
                        HtmlToRichMessageConverter
                                .convertTagToMarkdown(innerElement)
                );
                case TextNode textNode -> content.append(textNode.text());
                default -> {}
            }
        });

        return content.toString();
    }),

    STRIKETHROUGH("s", (element) -> {
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        return "~~" + content + "~~";
    }),

    QUOTE("blockquote", (element) -> {
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        return content
                .toString()
                .lines()
                .map(s -> (s.isBlank()) ? ">" : "> " + s)
                .collect(Collectors.joining("\n"));
    }),

    IMAGE("img", (element) -> {
        String imageSource = element.attr("src");
        return "![](" + imageSource + ")";
    }),

    HEADING("h[1-6]", (element) -> {
        int headerLevel = Character.digit(element.tag().name().charAt(1), 10);
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        String headerFill = Strings.repeat("#", headerLevel);
        return headerFill + " " + content;
    }),

    PARAGRAPH("p", (element) -> {
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        return content + "\n\n";
    }),;

    private final String htmlTag;
    private final Function<Element, String> markdownConverter;

    private static void writeInnerContent(StringBuilder content, Element element) {
        List<Node> childNodes = element.childNodes();
        childNodes.forEach(childNode -> {
            switch (childNode) {
                case Element innerElement -> content.append(HtmlToRichMessageConverter.convertTagToMarkdown(innerElement));
                case TextNode textNode -> content.append(textNode.text());
                default -> {}
            }
        });
    }
}
