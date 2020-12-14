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
 * Enum values taken from libft4222.h.
 *
 * @author Stephen Davies
 * @since May 2020
 * @since 2.1
 */
public enum ClockRate {
    SYS_CLK_60,
    SYS_CLK_24,
    SYS_CLK_48,
    SYS_CLK_80;
    
    private static final ClockRate[] VALUES = {
        SYS_CLK_60, SYS_CLK_24, SYS_CLK_48, SYS_CLK_80
    };
    
    /**
     * Convert API value to its Java enum equivalent.
     *
     * @param ordinal
     *            value returned by C api
     * @return matching Java enum value
     */
    public static ClockRate byOrdinal(int ordinal) {
        return VALUES[ordinal];
    }
    
}
