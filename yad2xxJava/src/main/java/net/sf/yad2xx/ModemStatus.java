/*
 * Copyright 2014-2018 Stephen Davies
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

/**
 * Convenience class to simplify the handling of calls to GetModemStatus.
 *
 * @author		Stephen Davies
 * @since		14 December 2014
 * @since		0.3
 */
public class ModemStatus {

	// bit masks defined by FTDI
	private static final byte BI_MASK = 0x10;			// Break Interrupt
	private static final byte CTS_MASK = 0x10;			// Clear to Send
	private static final byte DCD_MASK = (byte) 0x80;	// Data Carrier Detect
	private static final byte DSR_MASK = 0x20;			// Data Set Ready
	private static final byte FE_MASK = 0x08;			// Framing Error
	private static final byte OE_MASK = 0x02;			// Overrun error
	private static final byte PE_MASK = 0x04;			// Parity Error
	private static final byte RI_MASK = 0x40;			// Ring Indicator
	
	private byte modemStatus;
	private byte lineStatus;
	
	public ModemStatus(int status) {
		this.modemStatus = (byte) (status & 0xFF);
		this.lineStatus = (byte) ((status >> 8) & 0xFF);
	}
	
	public boolean hasBreakInterrupt() {
		return (lineStatus & BI_MASK) != 0;
	}
	
	public boolean hasCTS() {
		return (modemStatus & CTS_MASK) != 0; 
	}
	
	public boolean hasDCD() {
		return (modemStatus & DCD_MASK) != 0; 
	}
	
	public boolean hasDSR() {
		return (modemStatus & DSR_MASK) != 0; 
	}

	public boolean hasFramingError() {
		return (lineStatus & FE_MASK) != 0;
	}
	
	public boolean hasOverrunError() {
		return (lineStatus & OE_MASK) != 0;
	}
	
	public boolean hasParityError() {
		return (lineStatus & PE_MASK) != 0;
	}
	
	public boolean hasRI() {
		return (modemStatus & RI_MASK) != 0; 
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("ModemStatus(modem: ");
		result.append(Integer.toHexString(modemStatus));
		result.append(", line: ");
		result.append(Integer.toHexString(lineStatus));
		result.append(")");
		return result.toString();
	}
	
}
