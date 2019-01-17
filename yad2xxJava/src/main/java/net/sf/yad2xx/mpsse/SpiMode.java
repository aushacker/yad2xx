/*
 * Copyright 2016-2018 Stephen Davies
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

import static net.sf.yad2xx.mpsse.Command.*;

/**
 * SPI mode settings supported by the FTDI MPSSE. Only modes 0 and 2 are
 * available. In Mode 0 SCK idles low. In mode 2 SCK idles high.
 * <p>
 * Command fields record the individual MPSSE command to be issued when
 * performing an IN, OUT or INOUT operation on either byte or bit quantities.
 * <p>
 * Not all SPI devices have word sizes that are multiples of eight. Some
 * devices support 12 bits words. SPI operations cater for that possibility.
 * 
 * @author		Stephen Davies
 * @since		15 April 2016
 * @since		0.4
 */
public enum SpiMode {
	M0(false, MSB_FIRST_BYTE_IN_NEG_EDGE.getValue(), MSB_FIRST_BIT_IN_NEG_EDGE.getValue(), 
			  MSB_FIRST_BYTE_OUT_NEG_EDGE.getValue(), MSB_FIRST_BIT_OUT_NEG_EDGE.getValue(),
			  MSB_FIRST_BYTE_INOUT_NEGPOS_EDGE.getValue(), MSB_FIRST_BIT_INOUT_NEGPOS_EDGE.getValue()), 
	M2(true, MSB_FIRST_BYTE_IN_POS_EDGE.getValue(), MSB_FIRST_BIT_IN_POS_EDGE.getValue(),
			 MSB_FIRST_BYTE_OUT_POS_EDGE.getValue(), MSB_FIRST_BIT_OUT_POS_EDGE.getValue(),
			 MSB_FIRST_BYTE_INOUT_POSNEG_EDGE.getValue(), MSB_FIRST_BIT_INOUT_POSNEG_EDGE.getValue());
	
	private final boolean clockIdleHigh;
	private final byte bitInCommand;
	private final byte byteInCommand;
	private final byte bitOutCommand;
	private final byte byteOutCommand;
	private final byte bitInOutCommand;
	private final byte byteInOutCommand;
	
	private SpiMode(boolean clockIdleHigh,
			byte byteInCommand, byte bitInCommand, 
			byte byteOutCommand, byte bitOutCommand,
			byte byteInOutCommand, byte bitInOutCommand) {
		this.clockIdleHigh = clockIdleHigh;
		this.byteInCommand = byteInCommand;
		this.bitInCommand = bitInCommand;
		this.byteOutCommand = byteOutCommand;
		this.bitOutCommand = bitOutCommand;
		this.byteInOutCommand = byteInOutCommand;
		this.bitInOutCommand = bitInOutCommand;
	}
	
	public byte getBitInCommand() {
		return bitInCommand;
	}
	
	public byte getBitInOutCommand() {
		return bitInOutCommand;
	}
	
	public byte getByteInCommand() {
		return byteInCommand;
	}
	
	public byte getByteInOutCommand() {
		return byteInOutCommand;
	}
	
	public byte getBitOutCommand() {
		return bitOutCommand;
	}
	
	public byte getByteOutCommand() {
		return byteOutCommand;
	}
	
	public boolean isClockIdleHigh() {
		return clockIdleHigh;
	}
}
