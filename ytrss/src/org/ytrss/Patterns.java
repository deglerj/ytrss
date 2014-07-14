package org.ytrss;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Patterns {

	public static String getMatchGroup(final Pattern pattern, final int group, final String string) {
		final Matcher matcher = pattern.matcher(string);

		if (matcher.find()) {
			return matcher.group(group);
		}
		else {
			throw new RuntimeException("Pattern \"" + pattern.pattern() + "\" does not match input:\n" + string);
		}
	}

}
