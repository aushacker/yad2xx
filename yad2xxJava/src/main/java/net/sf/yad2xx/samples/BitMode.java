/*
 * Copyright 2015-2018 Stephen Davies
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
package net.sf.yad2xx.samples;

import java.io.PrintStream;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIBitMode;
import net.sf.yad2xx.FTDIConstants;
import net.sf.yad2xx.FTDIInterface;

/**
 * Based on the FTDI sample program BitMode. Tests bit bang mode.
 *
 * @author		Stephen Davies
 * @since		0.3
 */
public class BitMode extends AbstractSample {

	public BitMode() {
	}

	private void displayUsage() {
		displayUsage("net.sf.yad2xx.samples.BitMode [-h] [-p hex]");
	}
	
	public static void main(String[] args) {
		BitMode bm = new BitMode();
		
		try {
			if (bm.processOptions(args)) {
				bm.run();
			} else {
				bm.displayUsage();
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			bm.displayUsage();
		}
	}
	
	private void run() {
		PrintStream out = System.out;
		int baudRate = FTDIConstants.FT_BAUD_9600;
		
		try {
			
			out.println("BitMode Example");
			out.println("---------------");
			out.println();
			
			Device[] devices = FTDIInterface.getDevices();
			
			if (devices.length == 0) {
				out.println("*** No FTDI devices found. Possible VID/PID or driver problem. ***");
				return;
			}
			
			Device dev = devices[0];
			dev.open();
			
			/* 
			 * Enable bit-bang mode, where 8 UART pins (RX, TX, RTS etc.) become
			 * general-purpose I/O pins.
			 */
			out.println("Selecting asynchronous bit-bang mode.");
			dev.setBitMode((byte)0xFF, FTDIBitMode.FT_BITMODE_ASYNC_BITBANG);	// all pins as outputs
			
			/* 
			 * In bit-bang mode, setting the baud rate gives a clock rate
			 * 16 times higher, e.g. baud = 9600 gives 153600 bytes per second.
			 */
			out.print("Setting clock rate to ");
			out.println(baudRate * 16);
			dev.setBaudRate(baudRate);
			
			/*
			 * Use FT_Write to set values of output pins.  Here we set
			 * them to alternate low and high (0xAA == 10101010).
			 */
			byte outputData = (byte)0xAA;
			dev.write(outputData);

			/* Despite its name, GetBitMode samples the values of the data pins. */
			byte pinStatus = dev.getBitMode();

			if (pinStatus != outputData) {
				out.print("Failure: pin data is 0x");
				out.print(Integer.toHexString(pinStatus & 0xFF));
				out.print(" but expected 0x");
				out.println(Integer.toHexString(outputData & 0xFF)); 
			}

			out.print("Success: pin data is ");
			out.print(Integer.toHexString(pinStatus & 0xFF));
			out.println(", as expected.");
			
			/* Return chip to default (UART) mode. */
			dev.setBitMode((byte)0, FTDIBitMode.FT_BITMODE_RESET);
			dev.close();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
