package by.delmark.portal.labor_cost_bot.utils;

import by.delmark.portal.labor_cost_bot.telegram.utils.HtmlToRichMessageConverter;
import by.delmark.portal.labor_cost_bot.telegram.utils.Portal2TelegramImgAdapter;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.stream.Stream;

class HtmlConversionTest {

    static class HtmlToMarkdownArgumentsProvider implements ArgumentsProvider {
        @Override
        public @NonNull Stream<? extends Arguments> provideArguments(
               @NonNull ParameterDeclarations parameters,
               @NonNull ExtensionContext context
        ) {
            return Stream.of(
                    // input, expected
                    Arguments.arguments("<p>Hello World!</p>", "Hello World!"),
                    Arguments.arguments("<p>Hello <em>World!</em></p>", "Hello _World!_"),
                    Arguments.arguments("<h1>Title 1</h><p>Hello <em>World!</em></p>", "# Title 1\n\nHello _World!_"),
                    Arguments.arguments("<img src=\"http://test\"></img>", "![](http://test)"),
                    Arguments.arguments("<a href=\"http://test\">", "[](http://test)"),
                    Arguments.arguments("<a href=\"http://test/\">test text</a>", "[test text](http://test/)"),
                    Arguments.arguments(
                            "<img src=\"https://example.com/image.png\"><p>Intro</p><p>Quote:</p><blockquote>Text</blockquote><p>After</p>",
                            "![](https://example.com/image.png)\n\nIntro\n\nQuote:\n\n> Text\n\nAfter"
                    ),
                    Arguments.arguments(
                            "<ol><li><p>First</p></li><li><p>Second</p></li></ol><ul><li><p>One</p></li><li><p>Two</p></li></ul>",
                            "1. First\n2. Second\n\n* One\n* Two"
                    )
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(HtmlToMarkdownArgumentsProvider.class)
    void basic_conversations(String inputHtml, String expectedMarkdown) {
        HtmlToRichMessageConverter converter = new HtmlToRichMessageConverter(new Portal2TelegramImgAdapter());
        String result = converter.convertHtmlToMarkdown(inputHtml);
        Assertions.assertThat(result).isEqualTo(expectedMarkdown);
    }
}
