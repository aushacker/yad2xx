/*
 * Copyright 2012-2018 Stephen Davies
 * 
 * This file is part of yad2xx.
 * 
 * yad2xx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * yad2xx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with yad2xx.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.yad2xx;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Unit test FTStatus enumeration.
 *
 * @author		Stephen Davies
 * @since		July 9, 2018
 * @since		0.4
 */
public class FTStatusTest {

	@Test
	public void testByOrdinalLowest() {
		assertSame(FTStatus.FT_OK, FTStatus.byOrdinal(0));
	}

	/**
	 * Boundary test, enum values start at 0.
	 */
	@Test
	public void testByOrdinalLowerBoundary() {
		assertNull(FTStatus.byOrdinal(-1));
	}

	/**
	 * Boundary test, enum values end at 19 (for D2XX v1.4.4).
	 */
	@Test
	public void testByOrdinalUpperBoundary() {
		assertNull(FTStatus.byOrdinal(20));
	}
}
