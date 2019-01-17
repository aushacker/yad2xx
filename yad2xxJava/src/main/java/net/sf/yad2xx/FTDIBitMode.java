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

/**
 * FTDI bit mode settings. Copied from ftd2xx.h.
 *
 * @author		Stephen Davies
 * @since		27 May 2012
 * @since		0.1
 */
public enum FTDIBitMode {

	FT_BITMODE_RESET(0x00),
	FT_BITMODE_ASYNC_BITBANG(0x01),
	FT_BITMODE_MPSSE(0x02),
	FT_BITMODE_SYNC_BITBANG(0x04),
	FT_BITMODE_MCU_HOST(0x08),
	FT_BITMODE_FAST_SERIAL(0x10),
	FT_BITMODE_CBUS_BITBANG(0x20),
	FT_BITMODE_SYNC_FIFO(0x40);

	private FTDIBitMode(int mode) {
		this.mode = (byte) mode;
	}
	
	private byte mode;
	
	public byte getMode() {
		return mode;
	}
	
	/**
	 * Locate an FTDIBitMode given its id. Returns null if not found.
	 *
	 * @param	id				bit pattern used by FTDI device
	 * @return					matching Enum value or null if not found
	 */
	public static FTDIBitMode lookup(byte id) {
		FTDIBitMode result = null;
		
		switch (id) {
		case 0x00:
			result = FT_BITMODE_RESET;
			break;
		case 0x01:
			result = FT_BITMODE_ASYNC_BITBANG;
			break;
		case 0x02:
			result = FT_BITMODE_MPSSE;
			break;
		case 0x04:
			result = FT_BITMODE_SYNC_BITBANG;
			break;
		case 0x08:
			result = FT_BITMODE_MCU_HOST;
			break;
		case 0x10:
			result = FT_BITMODE_FAST_SERIAL;
			break;
		case 0x20:
			result = FT_BITMODE_CBUS_BITBANG;
			break;
		case 0x40:
			result = FT_BITMODE_SYNC_FIFO;
			break;
		}
		return result;
	}
}
