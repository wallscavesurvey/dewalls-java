package org.andork.walls.wpj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.andork.segment.Segment;
import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.UnitizedDouble;
import org.andork.walls.LineParser;
import org.andork.walls.SegmentParseExpectedException;
import org.andork.walls.WallsMessage;

public class WallsProjectParser extends LineParser {
	WallsProjectEntry currentEntry;
	WallsProjectBook projectRoot;
	List<WallsMessage> messages = new ArrayList<>();

	public WallsProjectBook result() {
		return projectRoot;
	}

	WallsProjectBook currentBook() {
		if (currentEntry == null) {
			return null;
		}
		if (currentEntry instanceof WallsProjectBook) {
			return (WallsProjectBook) currentEntry;
		}
		return currentEntry.parent;
	}

	public void parseLine(String line) throws SegmentParseException {
		parseLine(new Segment(line, null, 0, 0));
	}

	public void parseLine(Segment line) throws SegmentParseException {
		reset(line);

		maybeWhitespace();

		if (projectRoot == null) {
			oneOf(() -> {
				bookLine();
			}, () -> {
				commentLine();
			}, () -> {
				emptyLine();
			});
		}
		else if (currentEntry == null) {
			oneOf(() -> {
				commentLine();
			}, () -> {
				emptyLine();
			});
		}
		else {
			oneOf(
				this::bookLine,
				this::endbookLine,
				this::surveyLine,
				this::nameLine,
				this::pathLine,
				this::refLine,
				this::optionsLine,
				this::statusLine,
				this::commentLine,
				this::emptyLine);
		}
	}

	void emptyLine() throws SegmentParseException {
		maybeWhitespace();
		endOfLine();
	}

	void bookLine() throws SegmentParseExpectedException {
		expectIgnoreCase(".BOOK");
		whitespace();
		String title = remaining().toString();
		WallsProjectBook parent = currentBook();
		WallsProjectBook newEntry = new WallsProjectBook(parent, title);
		if (parent != null) {
			parent.children.add(newEntry);
		}
		else {
			projectRoot = newEntry;
		}
		currentEntry = newEntry;
	}

	void endbookLine() throws SegmentParseException {
		expectIgnoreCase(".ENDBOOK");
		maybeWhitespace();
		endOfLine();
		currentEntry = currentBook().parent;
	}

	void nameLine() throws SegmentParseExpectedException {
		expectIgnoreCase(".NAME");
		whitespace();
		currentEntry.name = remaining();
	}

	void pathLine() throws SegmentParseExpectedException {
		expectIgnoreCase(".PATH");
		whitespace();
		currentEntry.path = Paths.get(remaining().toString().replaceAll("\\\\", File.separator));
	}

	void surveyLine() throws SegmentParseExpectedException {
		expectIgnoreCase(".SURVEY");
		whitespace();
		String title = remaining().toString();
		WallsProjectBook book = currentBook();
		WallsProjectEntry newEntry = new WallsProjectEntry(book, title);
		book.children.add(newEntry);
		currentEntry = newEntry;
	}

	void statusLine() throws NumberFormatException, SegmentParseExpectedException {
		expectIgnoreCase(".STATUS");
		whitespace();
		int startIndex = index;
		currentEntry.status = unsignedIntLiteral();
		currentEntry.statusSegment = line.substring(startIndex, index);
	}

	void optionsLine() throws SegmentParseExpectedException {
		expectIgnoreCase(".OPTIONS");
		whitespace();
		currentEntry.options = remaining();
	}

	void commentLine() throws SegmentParseExpectedException {
		expect(";");
		remaining();
	}

	void refLine() throws SegmentParseException {
		expectIgnoreCase(".REF");
		whitespace();
		GeoReference ref = new GeoReference();
		ref.northing = new UnitizedDouble<>(doubleLiteral(), Length.meters);
		whitespace();
		ref.easting = new UnitizedDouble<>(doubleLiteral(), Length.meters);
		whitespace();
		ref.zone = intLiteral();
		whitespace();
		ref.gridConvergence = new UnitizedDouble<>(doubleLiteral(), Angle.degrees);
		whitespace();
		ref.elevation = new UnitizedDouble<>(doubleLiteral(), Length.meters);
		whitespace();
		ref.flags = (byte) intLiteral();
		whitespace();

		double degrees, minutes, seconds;

		degrees = intLiteral();
		whitespace();
		minutes = unsignedIntLiteral();
		whitespace();
		seconds = unsignedDoubleLiteral();
		whitespace();
		ref.latitude =
			new UnitizedDouble<>(degrees + ((minutes + seconds / 60.0) / 60.0) * Math.signum(degrees), Angle.degrees);

		degrees = intLiteral();
		whitespace();
		minutes = unsignedIntLiteral();
		whitespace();
		seconds = unsignedDoubleLiteral();
		whitespace();
		ref.longitude =
			new UnitizedDouble<>(degrees + ((minutes + seconds / 60.0) / 60.0) * Math.signum(degrees), Angle.degrees);

		ref.wallsDatumIndex = unsignedIntLiteral();
		whitespace();
		expect('"');
		ref.datumName = expect(notQuote, "<DATUM_NAME>").toString();
		expect('"');
		maybeWhitespace();
		endOfLine();
		currentEntry.reference = ref;
	}

	private static final Pattern notQuote = Pattern.compile("[^\"]+");

	public List<WallsMessage> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	public WallsProjectBook parseFile(String fileName) throws IOException {
		try (FileInputStream in = new FileInputStream(fileName)) {
			return parseFile(in, fileName);
		}
	}

	public WallsProjectBook parseFile(InputStream in, String fileName) {
		messages.clear();

		int lineNumber = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			String line;
			while ((line = reader.readLine()) != null && (projectRoot == null || currentEntry != null)) {
				line = line.trim();

				try {
					parseLine(new Segment(line, fileName, lineNumber, 0));
				}
				catch (SegmentParseException ex) {
					messages.add(new WallsMessage(ex));
					return null;
				}

				lineNumber++;
			}

			// not enough .ENDBOOKs at end?
			if (currentEntry != null) {
				messages
					.add(
						new WallsMessage(
							"error",
							"unexpected end of file: " + fileName,
							new Segment("", fileName, lineNumber, 0)));
				return null;
			}

			if (projectRoot != null) {
				projectRoot.path = Paths.get(fileName).toAbsolutePath().getParent().normalize();
			}

			return projectRoot;
		}
		catch (IOException ex) {
			messages
				.add(
					new WallsMessage(
						"error",
						"failed to read from file: " + fileName,
						new Segment("", fileName, lineNumber, 0)));
			return null;
		}
	}

}
