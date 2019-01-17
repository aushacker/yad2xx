/*
 * Copyright 2014 Stephen Davies
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

import java.io.PrintStream;
import static net.sf.yad2xx.FTDIConstants.*;

/**
 * Send and receive characters to the serial port. Connect tx to rx to get loopback.
 * 
 * @since Dec 8, 2014
 * @author Stephen Davies
 */
public class SerialEcho {

	public static void main(String[] args) {
		try {
			PrintStream out = System.out;
		
			out.println("Serial Echo");
			out.println("-----------");
			out.println("D2XX Library version: " + FTDIInterface.getLibraryVersion());
			out.println();
			
			out.println("---------");
			if (FTDIInterface.getDevices().length > 0) {
				Device dev = FTDIInterface.getDevices()[0];
				if (!dev.isOpen()) {
					out.println("Opening device 0");
					dev.open();
					out.println("Setting baud");
					dev.setBaudRate(FT_BAUD_19200);
					out.println("Setting 8,N,1");
					dev.setDataCharacteristics(FT_BITS_8, FT_STOP_BITS_1, FT_PARITY_NONE);
					out.println("Setting flow control");
					dev.setFlowControl(FT_FLOW_NONE, (char) 0, (char) 0);
					out.println(dev.getModemStatus());
					out.println("Sending data");
					byte[] data = { (byte)0x61, (byte)0x62 };
					for (int i = 0; i < 100; i++) {
						dev.write(data);
						try { Thread.sleep(100); }
						catch (InterruptedException ie) {}

						byte[] input = new byte[2];
						out.println("Reading bytes: " + dev.read(input));
						out.println(input[0] + " " + input[1]);
					}
					out.println(dev);
					out.println("Closing device 0");
					dev.close();
					out.println(dev);
				} else {
					out.println("Unable to open device");
				}
			}
		} catch (FTDIException e) {
			e.printStackTrace();
			System.err.println("Function: " + e.getFunction());
			System.err.println("Status: " + e.getStatus());
		}
	}

}
