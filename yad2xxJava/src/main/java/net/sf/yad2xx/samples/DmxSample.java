/*
 * Copyright 2016-2018 Stephen Davies
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

import java.io.InputStream;
import java.io.PrintStream;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIConstants;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;

/**
 * Demonstrates an FTDI chip operating as a DMX-512 interface. This requires
 * an RS-485 transceiver be attached as outlined in the FTDI datasheet. Whilst
 * RS-485 allows half-duplex communication, the DMX protocol in normally
 * simplex.
 * <p>
 * Assumes a 4 channel RGB light is attached, channels are:
 * <ol>
 * <li>Intensity/Strobe</li>
 * <li>RED</li>
 * <li>GREEN</li>
 * <li>BLUE</li>
 * </ol>
 * <p>
 * Light should be full RED.
 * 
 * @author		Stephen Davies
 * @since		20 May 2016
 * @since		0.4
 */
public class DmxSample extends AbstractSample {

	private static final int DMX_BAUD_RATE = 250000;
	
	// Use 192 of possible 512 channels.
	// Plus 1 to allow for start code (0).
	private static final int CHANNEL_COUNT = 192 + 1;
	
	private boolean running;
	
	private byte[] channels;
	
	private Device device;
	
	public DmxSample() {
		running = true;
		channels = new byte[CHANNEL_COUNT];

		for (int i = 0; i < CHANNEL_COUNT; i++) {
			channels[i] = (byte) 0;
		}
		
		channels[1] = (byte) 0xff;	// Intensity/strobe channel
		channels[2] = (byte) 0xff;	// RED channel
	}
	
	private void displayUsage() {
		displayUsage("net.sf.yad2xx.samples.DmxSample [-h] [-p hex]");
	}
	
	public static void main(String[] args) {
		DmxSample dmx = new DmxSample();
		
		try {
			if (dmx.processOptions(args)) {
				dmx.run();
			} else {
				dmx.displayUsage();
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			dmx.displayUsage();
		}
	}
	
	private void run() {
		PrintStream out = System.out;
		
		try {
			
			out.println("DMX Sample");
			out.println("----------");
			printProlog(out);
			
			Device[] devices = FTDIInterface.getDevices();
			
			if (devices.length == 0) {
				out.println("*** No FTDI devices found. Possible VID/PID or driver problem. ***");
				return;
			}
			
			device = devices[0];
			
			device.open();
			device.reset();
			delay(10);
			
			device.setDataCharacteristics(FTDIConstants.FT_BITS_8,
					FTDIConstants.FT_STOP_BITS_2,
					FTDIConstants.FT_PARITY_NONE);
			device.setBaudRate(DMX_BAUD_RATE);
			delay(10);
			
			Thread background = new Thread() {
				public void run() {
					try {
						while (running) {
							sendDmxFrame();
							delay(1000);
						}
					}
					catch (Exception e) {
						running = false;
						e.printStackTrace();
					}
				}
			};
			
			background.start();
			
			InputStream in = System.in;
			
			while (running) {
				int c = in.read();
				if (c == -1) {
					running = false;
				} else {
					if (c == 'q') {
						running = false;
					} else if (c == '+') {
						//if ()
					}
				}
			}
			
			device.close();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	private void sendDmxFrame() throws FTDIException {
		device.setBreak(true);		// BREAK indicates start of frame
		device.setBreak(false);
		
		device.write(channels);		// START code plus 192 channel bytes
	}
}
