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
 * Enum values taken from libft4222.h. Originally FT4222_SPIClock.
 * 
 * @author Stephen Davies
 * @since May 2020
 * @since 2.1
 */
public enum SpiClock {
    CLK_NONE,
    CLK_DIV_2,      // 1/2   System Clock
    CLK_DIV_4,      // 1/4   System Clock
    CLK_DIV_8,      // 1/8   System Clock
    CLK_DIV_16,     // 1/16  System Clock
    CLK_DIV_32,     // 1/32  System Clock
    CLK_DIV_64,     // 1/64  System Clock
    CLK_DIV_128,    // 1/128 System Clock
    CLK_DIV_256,    // 1/256 System Clock
    CLK_DIV_512;    // 1/512 System Clock
}
