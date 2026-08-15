package by.delmark.portal.labor_cost_bot.utils;

import by.delmark.portal.labor_cost_bot.telegram.utils.HtmlToRichMessageConverter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class HtmlConversionTest {

    @Test
    void paragraph_conversion() {
        String html = "<p>Hello World!</p>";
        String expected = "\nHello World!";
        String result = HtmlToRichMessageConverter.convertHtmlToMarkdown(html);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void italic_in_paragraph_conversion() {
        String html = "<p>Hello <em>World!</em></p>";
        String expected = "\nHello _World!_";
        String result = HtmlToRichMessageConverter.convertHtmlToMarkdown(html);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void simple_header_with_paragraph_conversion() {
        String html = "<h1>Title 1</h><p>Hello <em>World!</em></p>";
        String expected = "\n# Title 1\nHello _World!_";
        String result = HtmlToRichMessageConverter.convertHtmlToMarkdown(html);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void image_conversion() {
        String html = "<img src=\"http://test\"></img>";
        String expected = "![](http://test)";
        String result = HtmlToRichMessageConverter.convertHtmlToMarkdown(html);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void anchor_link() {
        String html = "<a href=\"http://test\">";
        String expected = "[](http://test)";
        String result = HtmlToRichMessageConverter.convertHtmlToMarkdown(html);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void anchor_with_text() {
        String html = "<a href=\"http://test/\">test text</a>";
        String expected = "[test text](http://test/)";
        String result = HtmlToRichMessageConverter.convertHtmlToMarkdown(html);
        Assertions.assertThat(result).isEqualTo(expected);
    }
}
