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
package net.sf.yad2xx.mpsse;

import static net.sf.yad2xx.mpsse.Command.*;

import java.io.Closeable;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIBitMode;
import net.sf.yad2xx.FTDIException;

/**
 * A wrapper for all MPSSE based protocols. Code here uses commands as
 * specified in FTDI Application Note AN108.
 * <p>
 * Application classes such as Spi, Jtag and I2C should wrap this object and
 * provide application specific method calls.
 * <p>
 * Inspiration for the way this type operates comes from the FTDI
 * application note AN114. Internally a buffer is used to queue commands
 * for the MPSSE (see {@link #enqueue(byte)}). The buffered commands are sent
 * for execution (see {@link #execute()}) as a block.
 * <p>
 * If the submitted command stream captures data the call to {@link #execute()}
 * should be followed by a call to {@link #read(int)}.
 * 
 * @author		Stephen Davies
 * @since		14 April 2016
 * @since		0.3
 */
public class Mpsse implements Closeable {

	private static final int TWELVE_MHZ = 12000000;
	private static final int SIXTY_MHZ = 60000000;
	
	public static final byte CMD_BOGUS = (byte) 0xAA;
	public static final byte BAD_COMMAND = (byte) 0xFA;

	// Largest single MPSSE data block is 65536 bytes, add a bit more for
	// one or more commands. I'm sure at some point this little hack will come
	// back to bite me.
	public static final int DEFAULT_BUFFER_SIZE = 70000;
	
	private Device device;
	
	private byte[] buffer;
	private int buffIdx;

	/**
	 * Wrapper an FTDI device for MPSSE operations. The default buffer
	 * size is used.
	 *
	 * @param	device						device to wrap
	 * @throws	IllegalArgumentException	if device has no MPSSE support
	 */
	public Mpsse(Device device) {
		this(device, DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * Wrapper an FTDI device for MPSSE operations.
	 *
	 * @param	device						device to wrap
	 * @param	buffLength					buffer length
	 * @throws	IllegalArgumentException	if device has no MPSSE support
	 */
	public Mpsse(Device device, int buffLength) {
		
		if (!device.getType().isMpsseEnabled()) {
			throw new IllegalArgumentException("Supplied device is not MPSSE capable.");
		}
		
		this.device = device;
		this.buffer = new byte[buffLength];
		this.buffIdx = 0;
	}
	
	/**
	 * Exit MPSSE mode and release the device.
	 */
	public void close() {
		try {
			// resetting the mode makes operation more reliable
			device.setBitMode((byte) 0, FTDIBitMode.FT_BITMODE_RESET);
			
			device.close();
		} catch (FTDIException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Configure port for MPSSE use. As per AN129 sample code.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	protected void configure() throws FTDIException {

		// reset USB device
		device.reset();
		delay(1);
		
		// purge any data still outstanding in USB device buffers
		int bytesRemaining = device.getQueueStatus();
		if (bytesRemaining > 0) {
			device.read(new byte[bytesRemaining]);
		}
			
		// Set USB transfer sizes to 64K
		device.setUSBParameters(65536, 65535);
			
		// disable event and error characters
		device.setChars((char) 0, false, (char) 0, false);
			
		// Set the read & write timeouts in milliseconds
		device.setTimeouts(3000, 3000);
			
		// Set the latency timer (default is 16ms)
		device.setLatencyTimer((byte) 1);
			
		// reset device mode (pinDirection, command)
		device.setBitMode((byte) 0, FTDIBitMode.FT_BITMODE_RESET);
		
		// enable MPSSE (pinDirection, command)
		device.setBitMode((byte) 0, FTDIBitMode.FT_BITMODE_MPSSE);
	}

	/**
	 * Makes clock configuration a bit easier by hiding some of the gory
	 * details of setting the clock divider. Clock will be set to the
	 * desired frequency or a value less than (never greater) depending
	 * on whether the desired setting can actually be achieved.
	 * 
	 * @param	desiredFrequency	merely a suggestion...
	 * @return						calculated 'best' divisor
	 */
	public int configureClock(int desiredFrequency) {
		
		boolean supportsHighFreq = device.getType().isMpsseEnhanced();
		int divisor = 0;
		
		if (supportsHighFreq && (desiredFrequency > TWELVE_MHZ)) {
			disableClockDivider();
			
			while (divisor < 0xffff) {
				int actualFrequency = SIXTY_MHZ / ((1 + divisor) * 2);
				
				if (desiredFrequency >= actualFrequency) {
					break;
				}
				
				divisor++;
			}
		} else {
			enableClockDivider();
			while (divisor < 0xffff) {
				int actualFrequency = TWELVE_MHZ / ((1 + divisor) * 2);
				
				if (desiredFrequency >= actualFrequency) {
					break;
				}
				
				divisor++;
			}
		}

		enqueue(SET_CLOCK_DIVISOR.getValue());
		enqueue((byte) (divisor & 0xff));
		enqueue((byte) ((divisor >> 8) & 0xff));
		
		return divisor;
	}
	
	/**
	 * The FT232H supports open drain outputs. Every 1 bit in lsbMask
	 * enables an open drain output for the corresponding xDBUS bit.
	 * Every 1 bit in msbMask does the same for the xCBUS.
	 * 
	 * @param	lsbMask			bit mask
	 * @param	msbMask			bit mask
	 */
	public void configureOpenDrainOutputs(byte lsbMask, byte msbMask) {
		enqueue(CONFIG_OPEN_DRAIN.getValue());
		enqueue(lsbMask);
		enqueue(msbMask);
	}
	
	/**
	 * Utility method to allow short delays.
	 * 
	 * @param	millis			delay time in ms
	 */
	public void delay(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException ignored) {
			// do nothing
		}
	}

	/**
	 * This will turn off adaptive clocking.
	 */
	public void disableAdaptiveClock() {
		if (device.getType().isMpsseEnhanced()) {
			enqueue(DISABLE_ADAPTIVE_CLK.getValue());
		}
	}
	
	/**
	 * This will turn off the divide by 5 from the 60 MHz clock.
	 */
	public void disableClockDivider() {
		if (device.getType().isMpsseEnhanced()) {
			enqueue(DISABLE_DIVIDER.getValue());
		}
	}
	
	/**
	 * This will disconnect the TDI output from the TDO input for loopback testing.
	 */
	public void disableLoopback() {
		enqueue(DISABLE_LOOPBACK.getValue());
	}

	/**
	 * This will give a 2 stage data shift which is the default state. So it
	 * will appear as Data setup for half clock period -&gt; Pulse clock for
	 * half clock period.
	 */
	public void disableThreePhaseClock() {
		if (device.getType().isMpsseEnhanced()) {
			enqueue(DISABLE_3PHASE_CLK.getValue());
		}
	}

	/**
	 * Adaptive clocking is required when using the JTAG interface on an ARM processor.
	 * This will cause the controller to wait for RTCK from the ARM processor which
	 * should be fed back into GPIOL3 (it is an input). After the TCK output has changed
	 * the controller waits until RTCK is sampled to be the same before it changes TCK
	 * again. It could be considered as an acknowledgement that the CLK signal was received.
	 */
	public void enableAdaptiveClock() {
		if (device.getType().isMpsseEnhanced()) {
			enqueue(ENABLE_ADAPTIVE_CLK.getValue());
		}
	}

	/**
	 * This will turn on the divide by 5 from the 60 MHz clock to give a
	 * 12MHz master clock for backward compatibility with FT2232D designs.
	 */
	public void enableClockDivider() {
		if (device.getType().isMpsseEnhanced()) {
			enqueue(ENABLE_DIVIDER.getValue());
		}
	}

	/**
	 * This will connect the TDI/DO output to the TDO/DI input for loopback testing.
	 */
	public void enableLoopback() {
		enqueue(ENABLE_LOOPBACK.getValue());
	}

	/**
	 * This will give a 3 stage data shift for the purposes of supporting
	 * interfaces such as I2C which need the data to be valid on both edges of
	 * the clk. So it will appear as Data setup for half clock period -&gt;
	 * pulse clock for half clock period -&gt; Data hold for half clock period.
	 */
	public void enableThreePhaseClock() {
		if (device.getType().isMpsseEnhanced()) {
			enqueue(ENABLE_3PHASE_CLK.getValue());
		}
	}

	/**
	 * Enqueue a single command or data value for later execution.
	 * 
	 * @param b single command or data byte
	 */
	public void enqueue(byte b) {
		buffer[buffIdx++] = b;
	}

	/**
	 * Enqueues the contents of the data array for later execution.
	 * 
	 * @param	data			to append to buffer
	 */
	public void enqueue(byte[] data) {
		enqueue(data, 0, data.length);
	}
	
	/**
	 * Enqueues the contents of the data array from the beginIndex.
	 * 
	 * @param data				to append to buffer
	 * @param beginIndex		of first byte to buffer
	 */
	public void enqueue(byte[] data, int beginIndex) {
		enqueue(data, beginIndex, data.length);
	}
	
	/**
	 * Enqueues a portion of an array. Includes data[beginIndex], excludes
	 * data[endIndex], (consistent with other Java API calls like 
	 * {@link java.lang.String#substring(int, int)}). Generally used to
	 * enqueue bulk data.
	 * 
	 * @param data				to append to buffer
	 * @param beginIndex		of first byte to buffer
	 * @param endIndex			of first byte to ignore
	 */
	public void enqueue(byte[] data, int beginIndex, int endIndex) {
		for (int i = 0; i < (endIndex - beginIndex); i++) {
			buffer[buffIdx++] = data[beginIndex + i];
		}
	}

	/**
	 * Sends the buffer contents to the MPSSE for execution.
	 */
	public void execute() {
		try {
			device.write(buffer, buffIdx);
			buffIdx = 0;
		}
		catch (FTDIException e) {
			throw new RuntimeException(e);
		}		
	}
	
	/**
	 * Allow client access to the device buffer status. Clients need to
	 * check if there is data in the buffer for synchronisation purposes.
	 * 
	 * @return					number of data bytes available to read
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public int getQueueStatus() throws FTDIException {
		return device.getQueueStatus();
	}

	/**
	 * All MPSSE implementations must be initialized before use.
	 */
	protected void initialise() {
		try {
			configure();
			delay(50);		// Allow configuration to take
			synchronise();
		} catch (FTDIException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasOpenDrainOutputs() {
		return device.getType().hasOpenDrainOutputs();
	}
	
	public void open() {
		try {
			device.open();
			initialise();
		} catch (FTDIException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] read(int byteCount) throws FTDIException {
		byte[] buffer = new byte[byteCount];
		device.read(buffer);
		
		return buffer;
	}
	
	public byte readDataBitsHigh() throws FTDIException {
		enqueue(READ_DATA_BITS_HIGH.getValue());
		execute();
		byte[] data = read(1);
		return data[0];
	}
	
	public byte readDataBitsLow() throws FTDIException {
		enqueue(READ_DATA_BITS_LOW.getValue());
		execute();
		byte[] data = read(1);
		return data[0];
	}
	
	/**
	 * This will setup the direction of the high 8 lines and force a value on
	 * the bits that are set as output. A 1 in the direction byte will make
	 * that bit an output.
	 * 
	 * @param	value			data to output
	 * @param	direction		pin control mask
	 */
	public void setDataBitsHigh(byte value, byte direction) {
		enqueue(SET_DATA_BITS_HIGH.getValue());
		enqueue(value);
		enqueue(direction);
	}
	
	/**
	 * This will setup the direction of the first 8 lines and force a value on
	 * the bits that are set as output. A 1 in the Direction byte will make that
	 * bit an output.
	 * 
	 * @param	value			data to output
	 * @param	direction		pin control mask
	 */
	public void setDataBitsLow(byte value, byte direction) {
		enqueue(SET_DATA_BITS_LOW.getValue());
		enqueue(value);
		enqueue(direction);
	}
	
	/**
	 * Synchronise the MPSSE by sending a bogus opcode (0xAA). The
	 * MPSSE will respond with a "Bad Command" (0xFA) followed by
	 * the bogus opcode itself.
	 * <p>
	 * Motivated by AN135 [5.3.1].
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	protected void synchronise() throws FTDIException {
		
		byte[] buffer = new byte[] { CMD_BOGUS };
		
		device.write(buffer, buffer.length);
	
		// Get the number of bytes in the device input buffer
		int inputCount = 0;
		do {
			inputCount = device.getQueueStatus();
		}
		while (inputCount == 0) ;
		
		// Check if Bad command and echo command receive
		buffer = new byte[inputCount];
		int responseSize = device.read(buffer);
		if ((responseSize != 2) || (buffer[0] != BAD_COMMAND) || (buffer[1] != CMD_BOGUS)) {
			throw new IllegalStateException("Unable to synchronise MPSSE");
		}
	}
}
