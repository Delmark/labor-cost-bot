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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static by.delmark.portal.labor_cost_bot.telegram.utils.HtmlToRichMessageConverter.convertTagToMarkdown;

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
        return "<u>" + content + "</u>";
    }),

    ORDERED_LIST("ol", (element) -> {
        AtomicInteger order = new AtomicInteger();
        StringBuilder content = new StringBuilder();

        List<Node> childNodes = element.childNodes();
        childNodes.forEach(childNode -> writeListItemContent(content, childNode, () -> order.incrementAndGet() + ". "));

        return asBlock(content.toString());
    }),

    UNORDERED_LIST("ul", (element) -> {
        StringBuilder content = new StringBuilder();

        List<Node> childNodes = element.childNodes();
        childNodes.forEach(childNode -> writeListItemContent(content, childNode, () -> "* "));

        return asBlock(content.toString());
    }),

    STRIKETHROUGH("s", (element) -> {
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        return "~~" + content + "~~";
    }),

    QUOTE("blockquote", (element) -> {
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        String quote = content.toString().strip();
        return asBlock(
                quote.lines()
                .map(s -> (s.isBlank()) ? ">" : "> " + s)
                .collect(Collectors.joining("\n"))
        );
    }),

    IMAGE("img", (element) -> {
        String imageSource = element.attr("src").strip();
        return imageSource.isEmpty() ? "" : asBlock("![](" + imageSource + ")");
    }),

    HEADING("h[1-6]", (element) -> {
        int headerLevel = Character.digit(element.tag().name().charAt(1), 10);
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        String headerFill = Strings.repeat("#", headerLevel);
        return asBlock(headerFill + " " + content);
    }),

    PARAGRAPH("p", (element) -> {
        StringBuilder content = new StringBuilder();
        writeInnerContent(content, element);
        return asBlock(content.toString());
    }),;

    private final String htmlTag;
    private final Function<Element, String> markdownConverter;

    private static void writeInnerContent(StringBuilder content, Element element) {
        List<Node> childNodes = element.childNodes();
        childNodes.forEach(childNode -> {
            switch (childNode) {
                case Element innerElement -> content.append(convertTagToMarkdown(innerElement));
                case TextNode textNode -> content.append(textNode.text());
                default -> {}
            }
        });
    }

    private static void writeListItemContent(StringBuilder content, Node itemNode, Supplier<String> markerSupplier) {
        switch (itemNode) {
            case Element innerListEl when innerListEl.tagName().equals("li") -> {
                String elementMarker = markerSupplier.get();
                StringBuilder innerLiContent = new StringBuilder();
                writeListItemInnerContent(innerLiContent, innerListEl);
                content.append(elementMarker).append(innerLiContent.toString().strip()).append("\n");
            }
            case Element innerElement -> content.append(convertTagToMarkdown(innerElement));
            case TextNode textNode -> content.append(textNode.text());
            default -> {}
        }
    }

    private static void writeListItemInnerContent(StringBuilder content, Element listItem) {
        List<Node> childNodes = listItem.childNodes();
        childNodes.forEach(childNode -> {
            switch (childNode) {
                case Element innerElement -> {
                    String convertedElement = convertTagToMarkdown(innerElement);
                    if (!convertedElement.isBlank()) {
                        if (innerElement.tagName().equals("p") && !content.isEmpty()) {
                            content.append("\n");
                        }
                        content.append(convertedElement.strip());
                    }
                }
                case TextNode textNode -> content.append(textNode.text());
                default -> {}
            }
        });
    }

    private static String asBlock(String content) {
        String normalizedContent = content.strip();
        return normalizedContent.isEmpty() ? "" : "\n\n" + normalizedContent + "\n\n";
    }
}
