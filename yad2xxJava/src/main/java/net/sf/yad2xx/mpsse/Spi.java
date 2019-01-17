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

import java.io.Closeable;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIException;

/**
 * Operates an FTDI device as an SPI port using the MPSSE feature.
 * Due to limitations in the MPSSE engine, only SPI modes 0 and 2 are
 * supported (see AN114, pg 3).
 * <p>
 * Pinouts are:
 * <ul>
 * <li>xDBUS0 - SCK</li>
 * <li>xDBUS1 - MOSI</li>
 * <li>xDBUS2 - MISO</li>
 * <li>xDBUS3 - CS</li>
 * </ul>
 * <p>
 * Clock line (SCK) can be configured to idle low (Mode 0) or
 * idle high (Mode 2). CS (Chip Select) signal can be configured
 * for active low or active high, see {@link #selectActiveHigh}
 * property.
 * 
 * @author		Stephen Davies
 * @since		15 April 2016
 * @since		0.4
 */
public class Spi implements Closeable {
	
	public static final int SPI_SCK_MASK = 1;
	public static final int SPI_MOSI_MASK = 2;
	public static final int SPI_MISO_MASK = 4;
	public static final int SPI_CS_MASK = 8;
	
	// SCK, MOSI and CS are outputs (pin = 1)
	private static final byte SPI_OUTPUT_PIN_MASK =
			(byte) (SPI_CS_MASK | SPI_MOSI_MASK | SPI_SCK_MASK);
	
	private static final int DEFAULT_CLOCK_RATE = 100000;
	
	private Mpsse mpsse;
	
	private SpiMode mode;
	
	/**
	 * Chip select (CS) polarity.
	 */
	private boolean selectActiveHigh;
	
	/**
	 * Clock (SCK) frequency (hertz).
	 */
	private int clockRate;
	
	/**
	 * Wrapper an FTDI device for SPI operations. The default clock rate,
	 * SPI mode 0 and an active low chip select are used.
	 *
	 * @param	device						device to wrap
	 * @throws	IllegalArgumentException	if device has no MPSSE support
	 */
	public Spi(Device device) {
		this(device, DEFAULT_CLOCK_RATE, SpiMode.M0, false);
	}

	/**
	 * Wrapper an FTDI device for SPI operations.
	 *
	 * @param	device						device to wrap
	 * @param	clockRate					clock frequency in hertz
	 * @param	mode						SPI mode 0 or mode 2
	 * @param	selectActiveHigh			chip select polarity
	 * @throws	IllegalArgumentException	if device has no MPSSE support
	 */
	public Spi(Device device, int clockRate, SpiMode mode, boolean selectActiveHigh) {
		this.mpsse = new Mpsse(device);
		this.clockRate = clockRate;
		this.mode = mode;
		this.selectActiveHigh = selectActiveHigh;
	}
	
	/**
	 * Activate the chip select line. Level is determined by the
	 * selectActiveHigh property.
	 */
	public void assertSelect() {
		
		// each command takes 0.2us, allow 1us
		for (int i = 0; i < 5; i++) {
			mpsse.setDataBitsLow((byte) (selectActiveHigh ? 8 : 0), SPI_OUTPUT_PIN_MASK);
		}		
	}
	
	/**
	 * Deactivate the chip select line. Level is determined by the
	 * selectActiveHigh property.
	 */
	public void clearSelect() {
		
		// each command takes 0.2us, allow 1us
		for (int i = 0; i < 5; i++) {
			mpsse.setDataBitsLow((byte) (selectActiveHigh ? 0 : 8), SPI_OUTPUT_PIN_MASK);
		}
	}

	/**
	 * Cleanly exit MPSSE mode and release the device.
	 */
	@Override
	public void close() {
		mpsse.close();
	}

	/**
	 * Execute buffered commands.
	 */
	public void execute() {
		mpsse.execute();
	}
	
	/**
	 * Initialise MPSSE specifically for SPI.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public void open() throws FTDIException {
		mpsse.open();
		mpsse.delay(50);
		
		mpsse.disableClockDivider();
		mpsse.disableAdaptiveClock();
		mpsse.disableThreePhaseClock();
		mpsse.execute();
		
		clearSelect();
		execute();
		
		mpsse.configureClock(clockRate);
		mpsse.execute();
		mpsse.delay(20);
		
		mpsse.disableLoopback();
		mpsse.execute();
		mpsse.delay(30);
		
		if (mpsse.getQueueStatus() != 0) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Read a number of bits.
	 *
	 * TODO - more detail, bit order etc.
	 *
	 * @param	bitCount		number of bits to read
	 * @return					input bits
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public byte[] readBits(int bitCount) throws FTDIException {

		int byteCount = bitCount / 8;	// complete bytes to receive
		int extraBits = bitCount % 8;	// leftover bits to receive
		
		if (byteCount > 0) {
			int count = byteCount - 1;						// FTDI oddity, 0 length means 1 byte
			
			mpsse.enqueue(mode.getByteInCommand());
			mpsse.enqueue((byte) (count & 0xff));			// LengthL
			mpsse.enqueue((byte) ((count >> 8) & 0xff));	// LengthH
		}
		
		if (extraBits > 0) {
			int count = extraBits - 1;						// FTDI oddity, 0 length means 1 bit
			
			mpsse.enqueue(mode.getBitInCommand());
			mpsse.enqueue((byte) count);					// Length
		}
		
		execute();
		
		if (extraBits > 0) {
			byteCount++;
		}
		
		return mpsse.read(byteCount);
	}
	
	/**
	 * Read and write bits simultaneously.
	 *
	 * TODO - more detail, bit order etc.
	 *
	 * @param	bitCount		number of bits to read/write
	 * @param	data			bits to write
	 * @return					bits read
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public byte[] readWriteBits(int bitCount, byte[] data) throws FTDIException {

		int byteCount = bitCount / 8;	// complete bytes to transmit
		int extraBits = bitCount % 8;	// leftover bits to transmit
		
		if (byteCount > 0) {
			int count = byteCount - 1;						// FTDI oddity, 0 length means 1 byte
			
			mpsse.enqueue(mode.getByteInOutCommand());
			mpsse.enqueue((byte) (count & 0xff));			// LengthL
			mpsse.enqueue((byte) ((count >> 8) & 0xff));	// LengthH
			mpsse.enqueue(data, 0, byteCount);				// Byte1 .. ByteN
		}

		if (extraBits > 0) {
			int count = extraBits - 1;						// FTDI oddity, 0 length means 1 bit
			
			mpsse.enqueue(mode.getBitInOutCommand());
			mpsse.enqueue((byte) count);					// Length
			mpsse.enqueue(data[byteCount]);					// Byte1
		}

		execute();
		
		if (extraBits > 0) {
			byteCount++;
		}
		
		return mpsse.read(byteCount);
	}
	
	/**
	 * Read instantaneous value on the devices xCBUS pins.
	 * 
	 * @return					instantaneous value on the xCBUS pins
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public byte readDataBitsHigh() throws FTDIException {
		return mpsse.readDataBitsHigh();
	}
	
	/**
	 * Read instantaneous value on the devices xDBUS pins.
	 * 
	 * @return					instantaneous value on the xDBUS pins
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public byte readDataBitsLow() throws FTDIException {
		return mpsse.readDataBitsLow();
	}
	
	/**
	 * Set the direction of the high 8 lines (xCBUS) and forces a value onto
	 * the bits that are set as outputs. A 1 in the direction byte will make
	 * that bit as an output.
	 * 
	 * @param	value			data to output
	 * @param	direction		pin control mask
	 */
	public void setDataBitsHigh(byte value, byte direction) {
		mpsse.setDataBitsHigh(value, direction);
	}
	
	/**
	 * Set the direction of the low 8 lines (xDBUS) and forces a value onto
	 * the bits that are set as outputs. A 1 in the direction byte will make
	 * that bit as an output.
	 * 
	 * @param	value			data to output
	 * @param	direction		pin control mask
	 */
	public void setDataBitsLow(byte value, byte direction) {
		mpsse.setDataBitsLow(value, direction);
	}
	
	/**
	 * Performs a complete SPI read/write cycle. A convenient way to
	 * read/write byte multiple values.
	 * 
	 * @param	data			byte(s) to write
	 * @return					byte(s) read from target
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public byte[] transactReadWrite(byte... data) throws FTDIException {
		return transactReadWrite(8 * data.length, data);
	}

	/**
	 * Performs a complete SPI read/write cycle. Use when number of bits is
	 * not a multiple of 8.
	 *
	 * @param	bitCount		number of bits to read/write
	 * @param	data			bytes to write
	 * @return					bytes read from target
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public byte[] transactReadWrite(int bitCount, byte[] data) throws FTDIException {
		assertSelect();
		byte[] result = readWriteBits(bitCount, data);
		clearSelect();

		return result;
	}

	/**
	 * Performs a complete SPI write cycle. If SPI target provides any data it
	 * is ignored. A convenient way to write byte multiple values.
	 *
	 * @param	data			bytes to write
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public void transactWrite(byte[] data) throws FTDIException {
		transactWrite(8 * data.length, data);
	}

	/**
	 * Performs a complete SPI write cycle. If SPI target provides any data it
	 * is ignored.  Use when number of bits is not a multiple of 8.
	 *
	 * TODO - clarify bit order (endianness) and how last byte is formatted
	 *
	 * @param	bitCount		number of bits to write
	 * @param	data			bits to write
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public void transactWrite(int bitCount, byte[] data) throws FTDIException {
		assertSelect();
		writeBits(bitCount, data);
		clearSelect();
		execute();
	}

	/**
	 * Low level SPI write. Client should call assertSelect prior to calling.
	 * After calling this method the client should call {@link #execute() } and
	 * {@link #clearSelect()}.
	 *  
	 * @param	bitCount		number of bits to write
	 * @param	data			bits to write
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public void writeBits(int bitCount, byte[] data) throws FTDIException {
		
		int byteCount = bitCount / 8;	// complete bytes to transmit
		int extraBits = bitCount % 8;	// leftover bits to transmit
		
		if (byteCount > 0) {
			int count = byteCount - 1;						// FTDI oddity, 0 length means 1 byte
			
			mpsse.enqueue(mode.getByteOutCommand());
			mpsse.enqueue((byte) (count & 0xff));			// LengthL
			mpsse.enqueue((byte) ((count >> 8) & 0xff));	// LengthH
			mpsse.enqueue(data, 0, byteCount);				// Byte1 .. ByteN
		}

		if (extraBits > 0) {
			int count = extraBits - 1;						// FTDI oddity, 0 length means 1 bit
			
			mpsse.enqueue(mode.getBitOutCommand());
			mpsse.enqueue((byte) count);					// Length
			mpsse.enqueue(data[byteCount]);					// Byte1
		}
	}
}
