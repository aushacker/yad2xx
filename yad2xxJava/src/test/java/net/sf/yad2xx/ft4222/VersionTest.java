/*
 * Copyright 2020 Stephen Davies
 *
 * This file is part of yad2xx.
 *
 * yad2xx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * yad2xx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with yad2xx. If not, see <https://www.gnu.org/licenses/>.
 */

package net.sf.yad2xx.ft4222;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test Version.
 *
 * @author Stephen Davies
 * @since May 2020
 * @since 2.1
 */
public class VersionTest {

    /**
     * Object under test.
     */
    private Version version;

    @Test
    public void testRevisionA() {
        version = new Version(Version.CHIP_VERSION_A, 0);

        assertEquals("A", version.getRevision());
    }

    @Test
    public void testRevisionB() {
        version = new Version(Version.CHIP_VERSION_B, 0);

        assertEquals("B", version.getRevision());
    }

    @Test
    public void testRevisionC() {
        version = new Version(Version.CHIP_VERSION_C, 0);

        assertEquals("C", version.getRevision());
    }

    @Test
    public void testRevisionD() {
        version = new Version(Version.CHIP_VERSION_D, 0);

        assertEquals("D", version.getRevision());
    }

    @Test
    public void testToString() {
        version = new Version(Version.CHIP_VERSION_D, 0x01040000);

        assertEquals("Version(D,1.4)", version.toString());
    }
}
