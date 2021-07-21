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

/**
 * Immutable implementation of FT4222.h type FT4222_Version.
 *
 * @author Stephen Davies
 * @since May 2020
 * @since 2.1
 */
public final class Version {

    public static final int CHIP_VERSION_A = 0x42220100;
    public static final int CHIP_VERSION_B = 0x42220200;
    public static final int CHIP_VERSION_C = 0x42220300;
    public static final int CHIP_VERSION_D = 0x42220400;

    /**
     * Chip version.
     */
    private final int chipVersion;

    /**
     * Dll version.
     */
    private final int dllVersion;

    public Version(int chipVersion, int dllVersion) {
        this.chipVersion = chipVersion;
        this.dllVersion = dllVersion;
    }

    /**
     * @return chip version
     */
    public int getChipVersion() {
        return chipVersion;
    }

    /**
     * @return dll version
     */
    public int getDllVersion() {
        return dllVersion;
    }

    /**
     * Human readable dll version.
     *
     * @return DLL version
     */
    public String getLibraryVersion() {
        // dllVersion format 0x01040000
        int major = (dllVersion >> 24) & 0xff;
        int minor = (dllVersion >> 16) & 0xff;
        
        return "" + major + "." + minor;
    }

    /**
     * Human readable revision code.
     *
     * @return revision code
     */
    public String getRevision() {
        switch (chipVersion) {
        case CHIP_VERSION_A:
            return "A";
        case CHIP_VERSION_B:
            return "B";
        case CHIP_VERSION_C:
            return "C";
        case CHIP_VERSION_D:
            return "D";
        default:
            return null;
        }
    }

    /**
     * Assist debugging.
     *
     * @return concise, human readable representation
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Version(");
        result.append(getRevision());
        result.append(",");
        result.append(getLibraryVersion());
        result.append(")");

        return result.toString();
    }
}
