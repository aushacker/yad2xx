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
 * FTDI defined constants. Copied from ftd2xx.h.
 * 
 * @author		Stephen Davies
 * @since		25 May 2012
 * @since		0.1
 */
public interface FTDIConstants {

	/**
	 * Standard FTDI USB vendor ID. 
	 */
	int FTDI_VID = 0x403;
	
	int FT_FLAGS_OPENED = 1;
	int FT_FLAGS_HISPEED = 2;

	//
	// Common Baud Rates
	//
	int FT_BAUD_300 = 300;
	int FT_BAUD_600 = 600;
	int FT_BAUD_1200 = 1200;
	int FT_BAUD_2400 = 2400;
	int FT_BAUD_4800 = 4800;
	int FT_BAUD_9600 = 9600;
	int FT_BAUD_14400 = 14400;
	int FT_BAUD_19200 = 19200;
	int FT_BAUD_38400 = 38400;
	int FT_BAUD_57600 = 57600;
	int FT_BAUD_115200 = 115200;
	int FT_BAUD_230400 = 230400;
	int FT_BAUD_460800 = 460800;
	int FT_BAUD_921600 = 921600;

	//
	// Word Lengths
	//
	byte FT_BITS_8 = 8;
	byte FT_BITS_7 = 7;

	//
	// Stop Bits
	//
	byte FT_STOP_BITS_1 = 0;
	byte FT_STOP_BITS_2 = 2;

	//
	// Parity
	//
	byte FT_PARITY_NONE = 0;
	byte FT_PARITY_ODD = 1;
	byte FT_PARITY_EVEN = 2;
	byte FT_PARITY_MARK = 3;
	byte FT_PARITY_SPACE = 4;
	
	//
	// Flow Control
	//

	short FT_FLOW_NONE = 0x0000;
	short FT_FLOW_RTS_CTS = 0x0100;
	short FT_FLOW_DTR_DSR = 0x0200;
	short FT_FLOW_XON_XOFF = 0x0400;
	
	//
	// Purge rx and tx buffers
	//
	int FT_PURGE_RX = 1;
	int FT_PURGE_TX = 2;
}
