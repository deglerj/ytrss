package org.ytrss;

import java.util.concurrent.TimeUnit;

public class Dates {

	// Based on: http://stackoverflow.com/questions/3859288/how-to-calculate-time-ago-in-java
	public static String formatAsPrettyString(long duration, final TimeUnit max, final TimeUnit min) {
		if (duration == 0) {
			return "now";
		}

		final StringBuilder res = new StringBuilder();

		TimeUnit current = max;

		while (duration > 0) {
			final long temp = current.convert(duration, TimeUnit.MILLISECONDS);

			if (temp > 0) {
				duration -= current.toMillis(temp);
				res.append(temp).append(" ").append(current.name().toLowerCase());
				if (temp < 2) {
					res.deleteCharAt(res.length() - 1);
				}
				res.append(", ");
			}

			if (current == min) {
				break;
			}

			current = TimeUnit.values()[current.ordinal() - 1];
		}

		// clean up our formatting....

		// we never got a hit, the time is lower than we care about
		if (res.lastIndexOf(", ") < 0) {
			return "0 " + min.name().toLowerCase();
		}

		// yank trailing ", "
		res.deleteCharAt(res.length() - 2);

		// convert last ", " to " and"
		final int i = res.lastIndexOf(", ");
		if (i > 0) {
			res.deleteCharAt(i);
			res.insert(i, " and");
		}

		return res.toString();
	}

	private Dates() {
		// Static utility class, no instances allowed
	}

}
