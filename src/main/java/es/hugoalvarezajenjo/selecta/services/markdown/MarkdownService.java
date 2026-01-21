package es.hugoalvarezajenjo.selecta.services.markdown;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarkdownService {
    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService() {
        // Enable GitHub Flavored Markdown extensions (tables, strikethrough, etc.)
        List<Extension> extensions = List.of(TablesExtension.create());

        this.parser = Parser.builder()
                .extensions(extensions)
                .build();

        this.renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
    }

    /**
     * Converts markdown text to HTML.
     * Returns an empty string if the input is null or empty.
     * 
     * @param markdown The markdown text to convert
     * @return The HTML representation of the markdown
     */
    public String toHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }

        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
