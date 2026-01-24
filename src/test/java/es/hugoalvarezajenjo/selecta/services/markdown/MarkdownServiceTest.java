package es.hugoalvarezajenjo.selecta.services.markdown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarkdownServiceTest {

    private MarkdownService markdownService;

    @BeforeEach
    void setUp() {
        markdownService = new MarkdownService();
    }

    @Test
    void toHtml_shouldReturnEmptyString_whenInputIsNull() {
        assertEquals("", markdownService.toHtml(null));
    }

    @Test
    void toHtml_shouldReturnEmptyString_whenInputIsEmpty() {
        assertEquals("", markdownService.toHtml(""));
    }

    @Test
    void toHtml_shouldReturnEmptyString_whenInputIsBlank() {
        assertEquals("", markdownService.toHtml("   "));
    }

    @Test
    void toHtml_shouldConvertSimpleMarkdownToHtml() {
        String markdown = "# Title\n\nThis is **bold** and this is [a link](https://example.com).";
        String html = markdownService.toHtml(markdown);

        assertTrue(html.contains("<h1>Title</h1>"));
        assertTrue(html.contains("<strong>bold</strong>"));
        assertTrue(html.contains("<a href=\"https://example.com\">a link</a>"));
    }

    @Test
    void toHtml_shouldSupportTables_usingGfmExtension() {
        String markdown = "| Header 1 | Header 2 |\n| --- | --- |\n| Cell 1 | Cell 2 |";
        String html = markdownService.toHtml(markdown);

        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<thead>"));
        assertTrue(html.contains("<th>Header 1</th>"));
        assertTrue(html.contains("<td>Cell 1</td>"));
    }

    @Test
    void toHtml_shouldHandleComplexMarkdown() {
        String markdown = "## Subtitle\n\n* Item 1\n* Item 2\n\n> Blockquote\n\n`code block`";
        String html = markdownService.toHtml(markdown);

        assertTrue(html.contains("<h2>Subtitle</h2>"));
        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<li>Item 1</li>"));
        assertTrue(html.contains("<blockquote>"));
        assertTrue(html.contains("<code>code block</code>"));
    }
}
