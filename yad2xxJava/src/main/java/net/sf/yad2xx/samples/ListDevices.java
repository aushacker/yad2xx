/*
 * Copyright 2015 Stephen Davies
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
import net.sf.yad2xx.FTDIInterface;

/**
 * Lists the details of all attached FTDI devices reachable by the D2XX driver.
 * It can be handy to step through the run() method in the Java debugger.
 *
 * @author		Stephen Davies
 * @since		6 July 2016
 * @since		0.3
 */
public class ListDevices extends AbstractSample {

	private void displayUsage() {
		displayUsage("net.sf.yad2xx.samples.ListDevices [-h] [-p hex]");
	}
	
	public static void main(String[] args) {
		ListDevices ld = new ListDevices();
		
		try {
			if (ld.processOptions(args)) {
				ld.run();
			} else {
				ld.displayUsage();
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			ld.displayUsage();
		}
	}

	/**
	 * Determine how many FTDI devices are attached and list their details.
	 */
	private void run() {
		PrintStream out = System.out;
		
		try {
			
			out.println("FTDI Device List");
			out.println("----------------");
			out.println();
			
			// Work out attached FTDI USB devices
			Device[] devices = FTDIInterface.getDevices();
			
			if (devices.length == 0) {
				out.println("*** No FTDI devices found. Possible VID/PID or driver problem. ***");
				return;
			}
			
			out.format("Got %d devices\n", devices.length);

			// Print details for each attached Device
			for (int i = 0; i < devices.length; i++) {
				out.println(devices[i]);
			}
			
		}
		catch (Throwable e) {
			e.printStackTrace(System.err);
		}
	}
}
