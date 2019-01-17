/*
 * Copyright 2018 Stephen Davies
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
import net.sf.yad2xx.FTDIInterface;
import net.sf.yad2xx.mpsse.Spi;
import net.sf.yad2xx.mpsse.SpiMode;
import net.sf.yad2xx.samples.AbstractSample;

/**
 * A sample program that tests bi-directional SPI port operation.
 * Connect MOSI and MISO (i.e. loopback) before running.
 * <p>
 * Pinouts are:
 * <ul>
 * <li>xDBUS0 - SK (SCK)</li>
 * <li>xDBUS1 - DO (MOSI)</li>
 * <li>xDBUS2 - DI (MISO)</li>
 * <li>xDBUS3 - CS (CS)</li>
 * </ul>
 *
 * @author		Stephen Davies
 * @since		6 July 2018
 * @since		1.0
 */
public class SpiBiDirectional extends AbstractSample {

	// SPI clock frequency in Hertz
	private static final int DESIRED_CLOCK = 500000;
	
	public static void main(String[] args) {
		SpiBiDirectional dotStar = new SpiBiDirectional();
		
		try {
			if (dotStar.processOptions(args)) {
				dotStar.run();
			} else {
				dotStar.displayUsage();
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			dotStar.displayUsage();
		}
	}
	
	private void displayUsage() {
		displayUsage("net.sf.yad2xx.mpsse.samples.SpiBiDirectional [-h] [-p hex]");
	}
	
	private void run() {
		PrintStream out = System.out;

		try {
			out.println("SPI BiDirectional Port Example");
			out.println("------------------------------");
			printProlog(out);
			
			Device[] devices = FTDIInterface.getDevices();
			
			if (devices.length == 0) {
				out.println("*** No FTDI devices found. Possible VID/PID or driver problem. ***");
				return;
			}
			
			Device device = devices[0];
			Spi spi = new Spi(device, DESIRED_CLOCK, SpiMode.M0, false);
			spi.open();

			byte[] result = spi.transactReadWrite((byte) 0xA5);

			result = spi.transactReadWrite(new byte[10000]);

			out.print("Result: ");
			out.println(Integer.toHexString(result[0]));

			spi.close();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
