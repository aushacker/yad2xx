/*
 * Copyright 2012 Stephen Davies
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

/**
 * Exercise the JNI interface by listing all attached FTDI devices and their properties.
 * 
 * @since Jun 20, 2012
 * @author Stephen Davies
 */
public class FTDIInterfaceTest {

	public static void main(String[] args) {
		try {
			PrintStream out = System.out;
		
			out.println("FTDI Test");
			out.println("---------");
			out.println("D2XX Library version: " + FTDIInterface.getLibraryVersion());
			out.println();
			
			out.println("Standard device count: " + FTDIInterface.getDeviceCount());
			out.println("Standard FTDI devices:");
			listDevices(out);
			
			//
			// Include FTDI devices with non-factory VID/PID settings.
			// If your FTDI device(s) have been reconfigured to use a custom VID/PID you should add them here.
			//
			//out.println("Setting custom VID/PID\n");
			//FTDIInterface.setVidPid(0x0403, 0x84e0);
			
			//out.println("Total device count: " + FTDIInterface.getDeviceCount());
			//out.println("All FTDI devices:");
			//listDevices(out);

			out.println("---------");
			if (FTDIInterface.getDevices().length > 0) {
				Device dev = FTDIInterface.getDevices()[0];
				if (!dev.isOpen()) {
					out.println("Opening device 0");
					dev.open();
					out.println("Driver: " + dev.getDriverVersion());
					out.println();
					out.println(dev);
					out.println("Closing device 0");
					dev.close();
					out.println(dev);
				}
			}
		} catch (FTDIException e) {
			e.printStackTrace();
			System.err.println("Function: " + e.getFunction());
			System.err.println("Status: " + e.getStatus());
		}
	}

	private static void listDevices(PrintStream out) throws FTDIException {
		Device[] devices = FTDIInterface.getDevices();
		for (int i = 0; i < devices.length; i++) {
			Device dev = devices[i];
			out.println(dev);
		}
	}
}
