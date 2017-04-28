package org.andork.walls;

import org.andork.walls.LineParser.Production;
import org.andork.walls.LineParser.VoidProduction;
import org.junit.Assert;

public class LineParserAssertions {
	public static void assertThrows(Production<?> p) {
		try {
			p.run();
			Assert.fail("expected production to throw");
		} catch (Exception ex) {
			// ignore
		}
	}
	
	public static void assertThrows(VoidProduction p) {
		try {
			p.run();
			Assert.fail("expected production to throw");
		} catch (Exception ex) {
			// ignore
		}
	}
}
