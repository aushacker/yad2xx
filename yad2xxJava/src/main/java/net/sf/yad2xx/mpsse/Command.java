/*
 * Copyright 2018 Stephen Davies
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
package net.sf.yad2xx.mpsse;

/**
 * MPSSE Commands, taken from FTDI AN108.
 *
 * @author		Stephen Davies
 * @since		18 July 2018
 * @since		1.0
 */
public enum Command {

	MSB_FIRST_BYTE_OUT_POS_EDGE(0x10),
	MSB_FIRST_BYTE_OUT_NEG_EDGE(0x11),
	MSB_FIRST_BIT_OUT_POS_EDGE(0x12),
	MSB_FIRST_BIT_OUT_NEG_EDGE(0x13),
	MSB_FIRST_BYTE_IN_POS_EDGE(0x20),
	MSB_FIRST_BYTE_IN_NEG_EDGE(0x24),
	MSB_FIRST_BIT_IN_POS_EDGE(0x22),
	MSB_FIRST_BIT_IN_NEG_EDGE(0x26),
	MSB_FIRST_BYTE_INOUT_POSNEG_EDGE(0x31),
	MSB_FIRST_BYTE_INOUT_NEGPOS_EDGE(0x34),
	MSB_FIRST_BIT_INOUT_POSNEG_EDGE(0x33),
	MSB_FIRST_BIT_INOUT_NEGPOS_EDGE(0x36),
	
	LSB_FIRST_BYTE_OUT_POS_EDGE(0x18),
	LSB_FIRST_BYTE_OUT_NEG_EDGE(0x19),
	LSB_FIRST_BIT_OUT_POS_EDGE(0x1A),
	LSB_FIRST_BIT_OUT_NEG_EDGE(0x1B),
	LSB_FIRST_BYTE_IN_POS_EDGE(0x28),
	LSB_FIRST_BYTE_IN_NEG_EDGE(0x2C),
	LSB_FIRST_BIT_IN_POS_EDGE(0x2A),
	LSB_FIRST_BIT_IN_NEG_EDGE(0x2E),
	LSB_FIRST_BYTE_INOUT_POSNEG_EDGE(0x39),
	LSB_FIRST_BYTE_INOUT_NEGPOS_EDGE(0x3C),
	LSB_FIRST_BIT_INOUT_POSNEG_EDGE(0x3B),
	LSB_FIRST_BIT_INOUT_NEGPOS_EDGE(0x3E),
	
	TMS_BIT_OUT_POS_EDGE(0x4A),
	TMS_BIT_OUT_NEG_EDGE(0x4B),
	TMS_BIT_INOUT_POSPOS_EDGE(0x6A),
	TMS_BIT_INOUT_NEGPOS_EDGE(0x6B),
	TMS_BIT_INOUT_POSNEG_EDGE(0x6E),
	TMS_BIT_INOUT_NEGNEG_EDGE(0x6F),

	SET_DATA_BITS_LOW(0x80),
	SET_DATA_BITS_HIGH(0x82),
	READ_DATA_BITS_LOW(0x81),
	READ_DATA_BITS_HIGH(0x83),
	
	// This will make the I/Os only drive when the data is ‘0’ and tristate on 
	// the data being ‘1’ when the appropriate bit is set. Use this op-code when
	// configuring the MPSSE for I2C use.
	CONFIG_OPEN_DRAIN(0x9E),
	
	// force a buffer flush
	SEND_IMMEDIATE(0x87),
	
	ENABLE_LOOPBACK(0x84),
	DISABLE_LOOPBACK(0x85),
	
	SET_CLOCK_DIVISOR(0x86),
	
	DISABLE_DIVIDER(0x8A),
	ENABLE_DIVIDER(0x8B),

	ENABLE_3PHASE_CLK(0x8C),
	DISABLE_3PHASE_CLK(0x8D),
	
	ENABLE_ADAPTIVE_CLK(0x96),
	DISABLE_ADAPTIVE_CLK(0x97);
	
	private Command(int value) {
		this.value = (byte) value;
	}

    private final byte value;

    public byte getValue() {
    	return value;
    }
}
