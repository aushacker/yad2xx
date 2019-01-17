/*
 * Copyright 2012 Stephen Davies
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
import static net.sf.yad2xx.FTDIBitMode.FT_BITMODE_MPSSE;
import static net.sf.yad2xx.FTDIBitMode.FT_BITMODE_RESET;

import org.junit.Test;

/**
 * Unit test FTDIBitMode.
 * 
 * @since June 29, 2012
 * @author Stephen Davies
 */
public class FTDIBitModeTest {

	@Test
	public void testLookupPass() {
		assertSame(FT_BITMODE_RESET, FTDIBitMode.lookup((byte) 0));
		assertSame(FT_BITMODE_MPSSE, FTDIBitMode.lookup((byte) 0x02));
	}
	
	@Test
	public void testLookupFail() {
		assertNull(FTDIBitMode.lookup((byte) 3));
	}
	
}
