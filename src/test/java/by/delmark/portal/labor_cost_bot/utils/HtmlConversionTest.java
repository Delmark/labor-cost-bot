package by.delmark.portal.labor_cost_bot.utils;

import by.delmark.portal.labor_cost_bot.telegram.utils.HtmlToRichMessageConverter;
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
                    Arguments.arguments("<p>Hello World!</p>", "\nHello World!"),
                    Arguments.arguments("<p>Hello <em>World!</em></p>", "\nHello _World!_"),
                    Arguments.arguments("<h1>Title 1</h><p>Hello <em>World!</em></p>", "\n# Title 1\nHello _World!_"),
                    Arguments.arguments("<img src=\"http://test\"></img>", "![](http://test)"),
                    Arguments.arguments("<a href=\"http://test\">", "[](http://test)"),
                    Arguments.arguments("<a href=\"http://test/\">test text</a>", "[test text](http://test/)")
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(HtmlToMarkdownArgumentsProvider.class)
    void basic_conversations(String inputHtml, String expectedMarkdown) {
        String result = HtmlToRichMessageConverter.convertHtmlToMarkdown(inputHtml);
        Assertions.assertThat(result).isEqualTo(expectedMarkdown);
    }
}
