package org.andork.walls;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.andork.segment.Segment;
import org.andork.segment.SegmentParseException;

public class SegmentParseExpectedException extends SegmentParseException {
	private static final long serialVersionUID = -5243227002158898703L;

	private final Set<String> expectedItems;

	public SegmentParseExpectedException(Segment segment, String... expectedItems) {
		this(segment, Arrays.asList(expectedItems));
	}

	public SegmentParseExpectedException(Segment segment, Collection<String> expectedItems) {
		super(createMessage(segment, expectedItems), segment);
		this.expectedItems = Collections.unmodifiableSet(new LinkedHashSet<>(expectedItems));
	}

	public Set<String> expectedItems() {
		return expectedItems;
	}

	private static String createMessage(Segment segment, Collection<String> expectedItems) {
		StringBuilder message = new StringBuilder("error: Expected ");
		if (expectedItems.size() == 1) {
			message.append('"').append(expectedItems.iterator().next()).append("\"\n");
		} else {
			message.append("one of:");
			for (String s : expectedItems) {
				message.append("\n  ").append(s);
			}
		}
		return message.toString();
	}
}
