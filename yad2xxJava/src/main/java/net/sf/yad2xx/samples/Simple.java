/*
 * Copyright 2015-2016 Stephen Davies
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
import net.sf.yad2xx.FTDIConstants;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;

/**
 * Based on the FTDI sample program Simple. Opens all attached devices, writes
 * some data then reads it back. Assumes the devices have a loopback connector
 * on them and they also have a serial number.
 * <p>
 * Uses default VID/PID values, override this with the -p option if required.
 *
 * @author		Stephen Davies
 * @since		6 July 2016
 * @since		0.3
 */
public class Simple extends AbstractSample {

	private byte[] buffer;
	
	public Simple() {
		buffer = new byte[10];
		
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte)('A' + i);
		}
	}
	
	private void displayUsage() {
		displayUsage("net.sf.yad2xx.samples.Simple [-h] [-p hex]");
	}
	
	public static void main(String[] args) {
		Simple simple = new Simple();
		
		try {
			if (simple.processOptions(args)) {
				simple.run();
			} else {
				simple.displayUsage();
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			simple.displayUsage();
		}
	}
	
	private void dumpBuffer(PrintStream out, byte[] buff) {
		for (int i = 0; i < buff.length; i++) {
			out.print((char) buff[i]);
		}
		out.println();
	}
	
	private void run() {
		PrintStream out = System.out;
		
		try {
			
			out.println("FTDI Simple Example");
			out.println("-------------------");
			printProlog(out);
			
			Device[] devices = FTDIInterface.getDevices();
			
			if (devices.length == 0) {
				out.println("*** No FTDI devices found. Possible VID/PID or driver problem. ***");
				return;
			}
			
			// List attached devices and their serial #'s
			for (int i = 0; i < devices.length; i++) {
				displayDevice(out, i, devices[i]);
			}
			
			out.println();
			
			// Transact individual devices
			for (int i = 0; i < devices.length; i++) {
				transact(out, devices[i]);
			}
			
			out.println("Test complete.");
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private void displayDevice(PrintStream out, int i, Device dev) throws FTDIException {
		out.print("Device ");
		out.print(i);
		out.print(", Serial Number - ");
		out.println(dev.getSerialNumber());
	}
	
	private void transact(PrintStream out, Device dev) throws FTDIException {
		if (dev.isOpen()) {
			out.println("Ignoring open device " + dev.getSerialNumber());
		} else {
			dev.open();
			out.println("Opened device " + dev.getSerialNumber());
			
			dev.setBaudRate(FTDIConstants.FT_BAUD_9600);
			dev.setTimeouts(2000, 2000);
			
			out.print("Sending buffer: ");
			dumpBuffer(out, buffer);
			
			int count = dev.write(buffer);
			if (count != buffer.length) {
				out.println("FT_WRITE only wrote this count: " + count);
			}
			
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) { }
			
			out.println(dev.getStatus());
			
			byte[] inBuff = new byte[buffer.length];
			
			count = dev.read(inBuff);
			
			if (count != buffer.length) {
				out.println("Expecting to receive: " + buffer.length + " bytes but got: " + count);
			} else {
				out.print("Received buffer: ");
				dumpBuffer(out, inBuff);
			}

			dev.close();
			out.println("Closed device " + dev.getSerialNumber());
			out.println();
		}
	}
}
