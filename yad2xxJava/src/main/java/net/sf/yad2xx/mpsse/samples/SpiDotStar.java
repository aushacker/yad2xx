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
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;
import net.sf.yad2xx.mpsse.Spi;
import net.sf.yad2xx.mpsse.SpiMode;
import net.sf.yad2xx.samples.AbstractSample;

/**
 * Example program using an FTDI device in SPI mode to manipulate a single
 * DotStar LED. LED should flash red, green, blue then white.
 * <p>
 * Pinouts are:
 * <ul>
 * <li>xDBUS0 - CKI (SPI SCK)</li>
 * <li>xDBUS1 - SDI (SPI MOSI)</li>
 * </ul>
 *
 * @author		Stephen Davies
 * @since		26 June 2018
 * @since		1.0
 */
public class SpiDotStar extends AbstractSample {

	// SPI clock frequency in Hertz
	private static final int DESIRED_CLOCK = 500000;
	
	// DotStar frame size
	private static final int FRAME_SIZE = 32;
	
	// Delay in ms
	private static final int DELAY = 1;

	private Spi spi;

	public static void main(String[] args) {
		SpiDotStar dotStar = new SpiDotStar();
		
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
		displayUsage("net.sf.yad2xx.mpsse.samples.SpiDotStar [-h] [-p hex]");
	}
	
	private void run() {
		PrintStream out = System.out;

		try {
			out.println("DotStar LED Example");
			out.println("-------------------");
			printProlog(out);
			
			Device[] devices = FTDIInterface.getDevices();
			
			if (devices.length == 0) {
				out.println("*** No FTDI devices found. Possible VID/PID or driver problem. ***");
				return;
			}
			
			Device device = devices[0];
			Spi spi = new Spi(device, DESIRED_CLOCK, SpiMode.M0, true);
			spi.open();
			setSpi(spi);

			out.println("RED");
			writeLed(0x1f, 0xff, 0, 0);
			Thread.sleep(DELAY);

			out.println("GREEN");
			writeLed(0x1f, 0, 0xff, 0);
			Thread.sleep(DELAY);

			out.println("BLUE");
			writeLed(0x1f, 0, 0, 0xff);
			Thread.sleep(DELAY);

			out.println("WHITE");
			writeLed(0x1f, 0xff, 0xff, 0xff);
			Thread.sleep(DELAY);
			
			spi.close();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private void setSpi(Spi spi) {
		this.spi = spi;
	}
	
	private void writeLed(int intensity, int r, int g, int b) throws FTDIException {
		byte[] data = { 0, 0, 0, 0, (byte) (0xe0 | (intensity & 0x1f)), (byte) b, (byte) g, (byte) r };
		
		spi.transactWrite(2 * FRAME_SIZE, data);
	}
}
