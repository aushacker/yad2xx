/*
 * Copyright 2016 Stephen Davies
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
package net.sf.yad2xx.mpsse.samples;

import java.io.PrintStream;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;
import net.sf.yad2xx.mpsse.I2C;
import net.sf.yad2xx.samples.AbstractSample;

/**
 * Example program using an FTDI FT2232H device in MPSSE mode to operate
 * an I2C I/O Expander (PCF8575).
 * 
 * @author Stephen Davies
 * @since 11 May 2016
 * @since 0.4
 */
public class I2CSample extends AbstractSample {

	// PCF8575 address (for Sparkfun breakout)
	private static final int ADDRESS = 0x20;
	
	protected I2C i2c;
	
	public static void main(String[] args) {
		I2CSample sample = new I2CSample();
		
		try {
			if (sample.processOptions(args)) {
				sample.run();
			} else {
				sample.displayUsage();
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			sample.displayUsage();
		}
	}
	
	private void displayUsage() {
		displayUsage("net.sf.yad2xx.mpsse.samples.I2CSample [-h] [-p hex]");
	}
	
	public void run() {
		PrintStream out = System.out;
		
		try {
			
			out.println("I2C I/O Expander Example");
			out.println("------------------------");
			printProlog(out);
			
			Device[] devices = FTDIInterface.getDevices();
			
			if (devices.length == 0) {
				out.println("*** No FTDI devices found. Possible VID/PID or driver problem. ***");
				return;
			}
			
			Device device = devices[0];
			i2c = new I2C(device, I2C.ONE_HUNDRED_KHZ);
			i2c.open();
			
			//scanI2CBus(0, 127);
			
//			for (int i = 0; i < 1000; i++) {
//				int shift = i % 8;
//				
//				i2c.start();
//				i2c.writeAddress(ADDRESS, false);
//				i2c.write((byte) (1 << shift));		// low port
//				i2c.write((byte) 0);				// high port
//				i2c.stop();
//				
//				i2c.delay(1);
//			}

			byte[] data = new byte[2];
			i2c.delay(100);
			
			i2c.start();
			
			i2c.writeAddress(ADDRESS, false);
			i2c.write((byte) 1);
			i2c.write((byte) 0x55);

			i2c.repeatedStart();

			i2c.writeAddress(ADDRESS,  true);
			data[0] = i2c.readWithAck();
			data[1] = i2c.readWithAck();

			i2c.stop();

			i2c.delay(100);
			i2c.close();
			
			System.out.println(data[0]);
			System.out.println(data[1]);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public void scanI2CBus(int startAddress, int endAddress) throws FTDIException {
		for (int i=startAddress; i < endAddress; i++ ) {
			i2c.start();
			
			if (i2c.writeAddress((byte)i, true)) {
				System.out.println(i + ": found");
			} else { 
				System.out.println(i + ": -");
			}
			
			i2c.stop();
			i2c.delay(1);
		}
	}
}
