package org.andork.walls.lst;

import org.andork.segment.Segment;
import org.junit.Assert;
import org.junit.Test;

public class StationPositionTests {
	@Test
	public void testGetNameWithPrefix() {
		StationPosition pos = new StationPosition();
		Assert.assertNull(pos.getNameWithPrefix());
		pos.name = new Segment("blah", null, 0, 0);
		Assert.assertEquals("blah", pos.getNameWithPrefix());
		pos.prefix = new Segment("A:B", null, 0, 0);
		Assert.assertEquals("A:B:blah", pos.getNameWithPrefix());
	}
}
