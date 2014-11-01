package org.ytrss;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

public class HTML {

	// Based on https://github.com/jhy/jsoup/blob/master/src/main/java/org/jsoup/examples/HtmlToPlainText.java
	private static class FormattingVisitor implements NodeVisitor {
		private static final int	maxWidth	= 80;
		private int					width		= 0;
		private final StringBuilder	accum		= new StringBuilder();	// holds the accumulated text

		// hit when the node is first seen
		@Override
		public void head(final Node node, final int depth) {
			final String name = node.nodeName();
			if (node instanceof TextNode) {
				append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
			}
			else if (name.equals("li")) {
				append("\n * ");
			}
			else if (name.equals("dt")) {
				append(" ");
			}
			else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr")) {
				append("\n");
			}
		}

		// hit when all of the node's children (if any) have been visited
		@Override
		public void tail(final Node node, final int depth) {
			final String name = node.nodeName();
			if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5")) {
				append("\n");
			}
			else if (name.equals("a")) {
				append(String.format(" <%s>", node.absUrl("href")));
			}
		}

		@Override
		public String toString() {
			return accum.toString();
		}

		// appends text to the string builder with a simple word wrap method
		private void append(final String text) {
			if (text.startsWith("\n")) {
				width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
			}
			if (text.equals(" ") && (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n"))) {
				return; // don't accumulate long runs of empty spaces
			}
			if (text.length() + width > maxWidth) { // won't fit, needs to wrap
				final String words[] = text.split("\\s+");
				for (int i = 0; i < words.length; i++) {
					String word = words[i];
					final boolean last = i == words.length - 1;
					if (!last) {
						word = word + " ";
					}
					if (word.length() + width > maxWidth) { // wrap and reset counter
						accum.append("\n").append(word);
						width = word.length();
					}
					else {
						accum.append(word);
						width += word.length();
					}
				}
			}
			else { // fits as is, without need to wrap text
				accum.append(text);
				width += text.length();
			}
		}
	}

	// Based on https://github.com/jhy/jsoup/blob/master/src/main/java/org/jsoup/examples/HtmlToPlainText.java
	public static String toFormattedPlainText(final String html) {
		final Document doc = Jsoup.parse(html);

		final FormattingVisitor formatter = new FormattingVisitor();
		final NodeTraversor traversor = new NodeTraversor(formatter);
		traversor.traverse(doc); // walk the DOM, and call .head() and .tail() for each node
		return formatter.toString();
	}

	private HTML() {
		// Static utility class, no instances allowed
	}

}
