package org.andork.walls;

import java.util.Objects;

import org.andork.segment.SegmentParseException;

public class WallsMessage {
	public final String severity;
	public final String message;
	public final Object source;
	public final String context;
	public final int startLine;
	public final int startColumn;
	public final int endLine;
	public final int endColumn;

	public WallsMessage(String severity, String message, Object source, int startLine, int startColumn, int endLine,
			int endColumn, String context) {
		super();
		this.severity = severity;
		this.message = message;
		this.source = source;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
		this.context = context;
	}

	public WallsMessage(String severity, String message, Object source, int startLine, int startColumn, int endLine,
			int endColumn) {
		this(severity, message, source, startLine, startColumn, endLine, endColumn, "");
	}

	public WallsMessage(String severity, String message, Object source) {
		this(severity, message, source, -1, -1, -1, -1, "");
	}

	public WallsMessage(SegmentParseException ex) {
		this("error", ex.getLocalizedMessage(), Objects.toString(ex.getSegment().source),
				ex.getSegment().startLine, ex.getSegment().startCol,
				ex.getSegment().endLine, ex.getSegment().endCol, ex.getSegment().underlineInContext());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(severity).append(": ").append(message);
		if (context != null) {
			builder.append('\n').append(context);
		}
		if (source != null) {
			builder.append("\n(").append(source);
			if (startLine >= 0) {
				builder.append(", line ").append(startLine + 1);
				if (startColumn >= 0) {
					builder.append(", column ").append(startColumn + 1);
				}
			}
			builder.append(')');
		}

		return builder.toString();
	}
}
