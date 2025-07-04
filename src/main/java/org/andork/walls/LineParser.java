package org.andork.walls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.andork.segment.Segment;
import org.andork.segment.SegmentMatcher;
import org.andork.segment.SegmentParseException;

public class LineParser {
	protected Segment line;
	protected int index = 0;
	protected int expectedIndex = 0;
	protected Set<String> expectedItems = new LinkedHashSet<>();

	public LineParser() {
		line = new Segment("", null, 0, 0);
	}

	public LineParser(Segment line) {
		this.line = line;
	}

	@FunctionalInterface
	public static interface Production<R> {
		public R run() throws SegmentParseException;
	}

	@FunctionalInterface
	public static interface VoidProduction {
		public void run() throws SegmentParseException;
	}

	public void throwAllExpected(Production<?> production) throws SegmentParseException {
		try {
			production.run();
		}
		catch (SegmentParseExpectedException ex) {
			throwAllExpected(ex);
		}
	}

	public void throwAllExpected(VoidProduction production) throws SegmentParseException {
		try {
			production.run();
		}
		catch (SegmentParseExpectedException ex) {
			throwAllExpected(ex);
		}
	}

	@SafeVarargs
	public final <R> R oneOf(Production<? extends R>... productions) throws SegmentParseException {
		int start = index;
		for (Production<? extends R> production : productions) {
			try {
				return production.run();
			}
			catch (SegmentParseExpectedException ex) {
				if (index > start) {
					throwAllExpected(ex);
				}
				addExpected(ex);
			}
		}
		throwAllExpected();
		return null;
	}

	public void oneOf(VoidProduction... productions) throws SegmentParseException {
		int start = index;
		for (VoidProduction production : productions) {
			try {
				production.run();
				return;
			}
			catch (SegmentParseExpectedException ex) {
				if (index > start) {
					throwAllExpected(ex);
				}
				addExpected(ex);
			}
		}
		throwAllExpected();
	}

	@SafeVarargs
	public final <R> R oneOfWithLookahead(Production<? extends R>... productions) throws SegmentParseException {
		int start = index;
		for (Production<? extends R> production : productions) {
			try {
				return production.run();
			}
			catch (SegmentParseExpectedException ex) {
				addExpected(ex);
				index = start;
			}
		}
		throwAllExpected();
		return null;
	}

	public char oneOf(Set<Character> set) throws SegmentParseException {
		char c;
		if (index >= line.length() || !set.contains(c = line.charAt(index))) {
			List<String> expectedItems = new ArrayList<>();
			for (char exp : set) {
				expectedItems.add(String.valueOf(exp));
			}
			throw new SegmentParseExpectedException(line.charAtAsSegment(index), expectedItems);
		}
		index++;
		return c;
	}

	public <V> V oneOf(Map<Character, V> map) throws SegmentParseException {
		char c;
		if (index >= line.length() || !map.containsKey(c = line.charAt(index))) {
			List<String> expectedItems = new ArrayList<>();
			for (char exp : map.keySet()) {
				expectedItems.add(String.valueOf(exp));
			}
			throw new SegmentParseExpectedException(line.charAtAsSegment(index), expectedItems);
		}
		index++;
		return map.get(c);
	}

	public void oneOfWithLookahead(VoidProduction... productions) throws SegmentParseException {
		int start = index;
		for (VoidProduction production : productions) {
			try {
				production.run();
				return;
			}
			catch (SegmentParseExpectedException ex) {
				addExpected(ex);
				index = start;
			}
		}
		throwAllExpected();
	}

	public <V> V oneOf(Map<Character, V> map, V elseValue) throws SegmentParseException {
		try {
			return oneOf(map);
		}
		catch (SegmentParseExpectedException ex) {
			addExpected(ex);
			return elseValue;
		}
	}

	public <V> V oneOfLowercase(Pattern rx, Map<String, V> map) throws SegmentParseExpectedException {
		int start = index;
		Segment seg = expect(rx, map.keySet());
		String str = seg.toString().toLowerCase();
		if (!map.containsKey(str)) {
			index = start;
			throw new SegmentParseExpectedException(seg, map.keySet());
		}
		return map.get(str);
	}

	public <R> Optional<R> maybeWithLookahead(Production<R> production) throws SegmentParseException {
		int start = index;
		try {
			return Optional.of(production.run());
		}
		catch (SegmentParseExpectedException ex) {
			addExpected(ex);
			index = start;
			return Optional.empty();
		}
	}

	public Optional<Void> maybeWithLookahead(VoidProduction production) throws SegmentParseException {
		int start = index;
		try {
			production.run();
			return Optional.of(null);
		}
		catch (SegmentParseExpectedException ex) {
			addExpected(ex);
			index = start;
			return Optional.empty();
		}
	}

	public Optional<Void> maybe(VoidProduction production) throws SegmentParseException {
		int start = index;
		try {
			production.run();
			return Optional.of(null);
		}
		catch (SegmentParseExpectedException ex) {
			if (index > start) {
				throwAllExpected(ex);
			}
			addExpected(ex);
			return Optional.empty();
		}
	}

	public <R> Optional<R> maybe(Production<? extends R> production) throws SegmentParseException {
		int start = index;
		try {
			return Optional.of(production.run());
		}
		catch (SegmentParseExpectedException ex) {
			if (index > start) {
				throwAllExpected(ex);
			}
			addExpected(ex);
			return Optional.empty();
		}
	}

	public char expectChar(Predicate<Character> charPredicate, String... expectedItems)
		throws SegmentParseExpectedException {
		char c;
		if (index >= line.length() || !charPredicate.test(c = line.charAt(index))) {
			throw new SegmentParseExpectedException(line.charAtAsSegment(index), expectedItems);
		}
		index++;
		return c;
	}

	public void reset(Segment line) {
		this.line = line;
		index = 0;
		expectedIndex = 0;
		expectedItems.clear();
	}

	public boolean isAtEnd() {
		return index == line.length();
	}

	public void addExpected(SegmentParseExpectedException expected) {
		int index = Math.max(expected.getSegment().sourceIndex, 0) - Math.max(line.sourceIndex, 0);

		if (index > expectedIndex) {
			expectedItems.clear();
			expectedIndex = index;
		}
		if (index == expectedIndex) {
			expectedItems.addAll(expected.expectedItems());
		}
	}

	public SegmentParseExpectedException allExpected() {
		if (!expectedItems.isEmpty()) {
			return new SegmentParseExpectedException(line.charAtAsSegment(index), expectedItems);
		}
		return new SegmentParseExpectedException(line.charAtAsSegment(index), "<UNKNOWN>");
	}

	public void throwAllExpected() throws SegmentParseExpectedException {
		if (!expectedItems.isEmpty()) {
			throw new SegmentParseExpectedException(line.charAtAsSegment(expectedIndex), expectedItems);
		}
	}

	public void throwAllExpected(SegmentParseExpectedException finalEx) throws SegmentParseExpectedException {
		addExpected(finalEx);
		throwAllExpected();
	}

	public void clearExpected() {
		expectedItems.clear();
		expectedIndex = 0;
	}

	public Segment expect(char c) throws SegmentParseExpectedException {
		if (index < line.length()) {
			if (c == line.charAt(index)) {
				index++;
				return line.charAtAsSegment(index - 1);
			}
		}
		throw new SegmentParseExpectedException(line.charAtAsSegment(index), Character.toString(c));
	}

	public Segment expectIgnoreCase(char c) throws SegmentParseExpectedException {
		if (index < line.length()) {
			if (Character.toLowerCase(c) == Character.toLowerCase(line.charAt(index))) {
				index++;
				return line.charAtAsSegment(index - 1);
			}
		}
		throw new SegmentParseExpectedException(line.charAtAsSegment(index), Character.toString(c));
	}

	public Segment expect(String c) throws SegmentParseExpectedException {
		if (index + c.length() <= line.length()) {
			if (c.equals(line.substring(index, index + c.length()).toString())) {
				int start = index;
				index += c.length();
				return line.substring(start, index);
			}
		}
		throw new SegmentParseExpectedException(line.charAtAsSegment(index), c);
	}

	public Segment expectIgnoreCase(String c) throws SegmentParseExpectedException {
		if (index + c.length() <= line.length()) {
			if (c.equalsIgnoreCase(line.substring(index, index + c.length()).toString())) {
				int start = index;
				index += c.length();
				return line.substring(start, index);
			}
		}
		throw new SegmentParseExpectedException(line.charAtAsSegment(index), c);
	}

	public Segment expect(Pattern rx, Collection<String> expectedItems) throws SegmentParseExpectedException {
		SegmentMatcher matcher = new SegmentMatcher(line, rx);
		matcher.region(index, line.length());
		if (!matcher.find() || matcher.start() != index) {
			throw new SegmentParseExpectedException(line.charAtAsSegment(index), expectedItems);
		}
		index = matcher.end();
		return matcher.group(0);
	}

	public Segment expect(Pattern rx, String... expectedItems) throws SegmentParseExpectedException {
		SegmentMatcher matcher = new SegmentMatcher(line, rx);
		matcher.region(index, line.length());
		if (!matcher.find() || matcher.start() != index) {
			throw new SegmentParseExpectedException(line.charAtAsSegment(index), expectedItems);
		}
		index = matcher.end();
		return matcher.group(0);
	}

	protected static final Pattern whitespaceRx = Pattern.compile("\\s+");

	public Segment whitespace() throws SegmentParseExpectedException {
		return expect(whitespaceRx, "<WHITESPACE>");
	}

	protected static final Pattern whitespaceAndOrCommaRx = Pattern.compile("\\s*,\\s*|\\s+");

	public Segment whitespaceAndOrComma() throws SegmentParseExpectedException {
		return expect(whitespaceAndOrCommaRx, "<WHITESPACE/,>");
	}

	protected static final Pattern nonwhitespaceRx = Pattern.compile("\\S+");

	public Optional<Segment> maybeWhitespace() throws SegmentParseException {
		return maybe(this::whitespace);
	}

	public Optional<Segment> maybeWhitespaceAndOrComma() throws SegmentParseException {
		return maybe(this::whitespaceAndOrComma);
	}

	public Segment nonwhitespace() throws SegmentParseExpectedException {
		return expect(nonwhitespaceRx, "<NONWHITESPACE>");
	}

	protected static final Pattern unsignedIntLiteralRx = Pattern.compile("\\d+");

	public int unsignedIntLiteral() throws NumberFormatException, SegmentParseExpectedException {
		return Integer.valueOf(expect(unsignedIntLiteralRx, "<UNSIGNED_INT_LITERAL>").toString());
	}

	private static final Map<Character, Integer> intSignSignums = new HashMap<>();
	static {
		intSignSignums.put('-', -1);
		intSignSignums.put('+', 1);
	}

	public int intLiteral() throws SegmentParseException {
		Optional<Integer> signum = maybe(() -> oneOf(intSignSignums));
		return signum.isPresent() ? signum.get() * unsignedIntLiteral() : unsignedIntLiteral();
	}

	private static final Pattern unsignedDoubleLiteralRx = Pattern.compile("\\d+(\\.\\d*)?|\\.\\d+");

	public double unsignedDoubleLiteral() throws NumberFormatException, SegmentParseExpectedException {
		return Double.valueOf(expect(unsignedDoubleLiteralRx, "<UNSIGNED_DOUBLE_LITERAL>").toString());
	}

	protected static final Map<Character, Double> signSignums = new HashMap<>();
	static {
		signSignums.put('-', -1.0);
		signSignums.put('+', 1.0);
	}

	public double doubleLiteral() throws SegmentParseException {
		Optional<Double> signum = maybe(() -> oneOf(signSignums));
		return signum.isPresent() ? signum.get() * unsignedDoubleLiteral() : unsignedDoubleLiteral();
	}

	public void endOfLine() throws SegmentParseExpectedException {
		if (index != line.length()) {
			throw new SegmentParseExpectedException(line.charAtAsSegment(index), "<END_OF_LINE>");
		}
	}

	public Segment remaining() {
		Segment result = line.substring(index);
		index = line.length();
		return result;
	}

	public boolean maybeChar(char c) throws SegmentParseException {
		return maybe(() -> {
			expect(c);
			return null;
		}).isPresent();
	}
}
