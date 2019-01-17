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
import net.sf.yad2xx.FTDIException;

/**
 * Operates an FTDI device as an I2C port using the MPSSE feature. See FTDI
 * Application Notes AN113 and AN255 for details.
 * <p>
 * It appears that only the FT232H supports open drain outputs. According to
 * AN255:
 * <blockquote>
 * The FT2232H and FT4232H could also be used in place of the FT232H. Both the 
 * FT2232H and FT4232H have two MPSSE channels. However, please note that these
 * devices do not feature the open-drain option used here in the FT232H and so
 * the pins need to be manually tri-stated when not writing on the I2C bus.
 * This requires a small change to the routines used to generate the I2C
 * protocol.
 * </blockquote>
 * 
 * If using something other than an FT232H, series resistors for SCL and SDA
 * are recommended.
 * <p>
 * Pinouts are:
 * <ul>
 * <li>xDBUS0 - SCL</li>
 * <li>xDBUS1 - SDA</li>
 * <li>xDBUS2 - SDA</li>
 * </ul>
 * 
 * @author		Stephen Davies
 * @since		11 May 2016
 * @since		0.4
 */
public class I2C implements Closeable {

	// Common I2C bus clock settings
	public static final int ONE_HUNDRED_KHZ = 100000;
	public static final int FOUR_HUNDRED_KHZ = 400000;

	// bit positions corresponding to xDBUS0 and xDBUS1
	private static final int SCL_MASK = 1;
	private static final int SDA_MASK = 2;
	
	private Mpsse mpsse;
	private int clockRate;
	
	public I2C(Device device) {
		this(device, FOUR_HUNDRED_KHZ);
	}
	
	public I2C(Device device, int clockRate) {
		this.mpsse = new Mpsse(device);
		this.clockRate = clockRate;
	}

	/**
	 * Cleanly exit MPSSE mode and release the device.
	 */
	@Override
	public void close() {
		mpsse.close();
	}

	public void delay(long millis) {
		mpsse.delay(millis);
	}
	
	/**
	 * Execute buffered commands.
	 */
	public void execute() {
		mpsse.execute();
	}

	/**
	 * Sets I2C related pins (xD0/xD1/xD2) to their idle state.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public void idle() throws FTDIException {
		// allow SCL and SDA to be taken high by the tie high resistors
		byte lsbMask = (byte) (SCL_MASK & SDA_MASK);
		
		if (mpsse.hasOpenDrainOutputs()) { 
			// pins in open drain mode
			mpsse.setDataBitsLow(lsbMask, lsbMask);
		} else {
			// manually tri-state outputs
			mpsse.setDataBitsLow(lsbMask, (byte) 0);
		}
	}
	
	/**
	 * Initialise MPSSE specifically for I2C.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public void open() throws FTDIException {
		mpsse.open();
		mpsse.delay(50);
		
		mpsse.disableClockDivider();
		mpsse.disableAdaptiveClock();
		mpsse.enableThreePhaseClock();
		mpsse.execute();

		if (mpsse.hasOpenDrainOutputs()) {
			// configure SCL and SDA as open drain
			byte lsbMask = (byte) (SCL_MASK | SDA_MASK);
			mpsse.configureOpenDrainOutputs(lsbMask, (byte) 0);
		}
		idle();
		
		// Use of 3 phase clocking requires a clock 'fudge',
		// increase clock by 50% to compensate. 
		// See AN255 pg 7 for details.
		mpsse.configureClock((int) (clockRate * 1.5));
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
	 * Read a single byte and either ACK or NAK it.
	 * 
	 * @param	ack				ACK/NAK flag
	 * @return					data byte
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	protected byte read(boolean ack) throws FTDIException {
		
		// master holds SCL low, SDA is allowed to float
		if (mpsse.hasOpenDrainOutputs()) { 
			// pins in open drain mode
			mpsse.setDataBitsLow((byte) SDA_MASK, (byte) (SCL_MASK | SDA_MASK));
		} else {
			// manually tri-state outputs
			mpsse.setDataBitsLow((byte) SDA_MASK, (byte) SCL_MASK);
		}

		// read a single byte
		mpsse.enqueue(MSB_FIRST_BYTE_IN_POS_EDGE.getValue());
		mpsse.enqueue((byte) 0);
		mpsse.enqueue((byte) 0);

		// take control of SDA to allow the ACK to be transmitted
		mpsse.setDataBitsLow((byte) 0, (byte) (SCL_MASK | SDA_MASK));

		if (ack) {
			// output a single 0 bit (ACK)
			mpsse.enqueue(MSB_FIRST_BIT_OUT_NEG_EDGE.getValue());
			mpsse.enqueue((byte) 0);
			mpsse.enqueue((byte) 0);
		} else {
			// output a single 1 bit (NAK)
			mpsse.enqueue(MSB_FIRST_BIT_OUT_NEG_EDGE.getValue());
			mpsse.enqueue((byte) 0);
			mpsse.enqueue((byte) 0xFF);
		}
		// master holds SCL low, SDA is allowed to float
		if (mpsse.hasOpenDrainOutputs()) { 
			// pins in open drain mode
			mpsse.setDataBitsLow((byte) SDA_MASK, (byte) (SCL_MASK | SDA_MASK));
		} else {
			// manually tri-state outputs
			mpsse.setDataBitsLow((byte) SDA_MASK, (byte) SCL_MASK);
		}
		
		mpsse.enqueue(SEND_IMMEDIATE.getValue());
		mpsse.execute();
		
		byte[] data = mpsse.read(1);
		
		return data[0];
	}

	/**
	 * Read a byte with ACK.
	 *
	 * @return					data byte
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public byte readWithAck() throws FTDIException {
		return read(true);
	}

	/**
	 * Read a byte with NAK.
	 *
	 * @return					data byte
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public byte readWithNak() throws FTDIException {
		return read(false);
	}
	
	/**
	 * Sends a repeated start condition, the bus should already be owned 
	 * by the device, i.e. a previous start has been issued without a stop.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public void repeatedStart() throws FTDIException {

		// bring SDA high whilst keeping SCL low
		for (int i = 0; i < 4; i++) {
			if (mpsse.hasOpenDrainOutputs()) {
				mpsse.setDataBitsLow((byte) SDA_MASK, (byte) (SCL_MASK | SDA_MASK));
			} else {
				mpsse.setDataBitsLow((byte) SDA_MASK, (byte) SCL_MASK);
			}
		}
		
		// Now bring SCL high
		for (int i = 0; i < 4; i++) {
			if (mpsse.hasOpenDrainOutputs()) {
				mpsse.setDataBitsLow((byte) (SCL_MASK | SDA_MASK), (byte) (SCL_MASK | SDA_MASK));
			} else {
				mpsse.setDataBitsLow((byte) (SCL_MASK | SDA_MASK), (byte) 0);
			}
		}
		
		start();
	}
	
	/**
	 * Sets the Start condition on the I2C Lines.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public void start() throws FTDIException {
		
		// bring SDA low, keeping SCL high
		for (int i = 0; i < 4; i++) {
			if (mpsse.hasOpenDrainOutputs()) {
				mpsse.setDataBitsLow((byte) SCL_MASK, (byte) (SCL_MASK | SDA_MASK));
			} else {
				mpsse.setDataBitsLow((byte) SCL_MASK, (byte) SDA_MASK);
			}
		}
		
		// now bring SCL low
		for (int i = 0; i < 4; i++) {
			mpsse.setDataBitsLow((byte) 0, (byte) (SCL_MASK | SDA_MASK));
		}
	}

	/**
	 * Sets the Stop condition on the I2C Lines.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public void stop() throws FTDIException {

		// ensure SCL and SDA are low
		for (int i = 0; i < 4; i++) {
			mpsse.setDataBitsLow((byte) 0, (byte) (SCL_MASK | SDA_MASK));
		}
		
		// bring SCL high, keeping SDA low
		for (int i = 0; i < 4; i++) {
			if (mpsse.hasOpenDrainOutputs()) {
				mpsse.setDataBitsLow((byte) SCL_MASK, (byte) (SCL_MASK | SDA_MASK));
			} else {
				mpsse.setDataBitsLow((byte) SCL_MASK, (byte) SDA_MASK);
			}
		}
		
		// now bring SDA high
		for (int i = 0; i < 4; i++) {
			if (mpsse.hasOpenDrainOutputs()) {
				mpsse.setDataBitsLow((byte) (SCL_MASK | SDA_MASK), (byte) (SCL_MASK | SDA_MASK));
			} else {
				mpsse.setDataBitsLow((byte) (SCL_MASK | SDA_MASK), (byte) 0);
			}
		}
		mpsse.enqueue(SEND_IMMEDIATE.getValue());
		
		execute();
	}
	
	/**
	 * Read multiple bytes in a single device activation.
	 * 
	 * @param	address			i2c device address
	 * @param	count			number of reads to perform
	 * @return					received bytes or null if device activation
	 * 							failed
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public byte[] transactRead(int address, int count) throws FTDIException {
		byte[] result = null;
		
		start();
		
		if (writeAddress((byte) address, true)) {
			result = new byte[count];
			for (int i = 0; i < count; i++) {
				result[i] = readWithAck();
			}
		}
		
		stop();
		
		return result;
	}

	/**
	 * Write multiple bytes in a single device activation.
	 * 
	 * @param	address			i2c device address
	 * @param	data			bytes to write
	 * @return					number of bytes written
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public int transactWrite(int address, byte... data) throws FTDIException {
		int result = 0;
		
		start();
		
		if (writeAddress((byte) address, false)) {
			for ( ; result < data.length; result++) {
				write(data[result]);
			}
		}
		
		stop();
		
		return result;
	}
	
	/**
	 * Writes 1 byte, and checks if it returns an ACK or NACK by clocking in
	 * one bit. We clock one byte out to the I2C Slave. We then clock in one
	 * bit from the Slave which is the ACK/NAK bit. Put lines back to the idle
	 * state (idle between start and stop is clock low, data)
	 * 
	 * @param	value			data to write
	 * @return					true if the write was ACKed
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public boolean write(byte value) throws FTDIException {

		mpsse.setDataBitsLow((byte) 0, (byte) (SCL_MASK | SDA_MASK));

		// command: clock bytes out MSB first on clock falling edge
		mpsse.enqueue(MSB_FIRST_BYTE_OUT_NEG_EDGE.getValue());
		
		// 0 indicates a single byte to be sent
		mpsse.enqueue((byte) 0);
		mpsse.enqueue((byte) 0);
		mpsse.enqueue(value);
		
		// Put I2C line back to idle (during transfer) state... Clock line low, Data line high
		// AD0 (SCL) is output driven low
		// AD1 (DATA OUT) is output high (open drain) or set as input
		// AD2 (DATA IN) is input (therefore the output value specified is ignored)
		if (mpsse.hasOpenDrainOutputs()) {
			mpsse.setDataBitsLow((byte) SDA_MASK, (byte) (SCL_MASK | SDA_MASK));
		} else {
			mpsse.setDataBitsLow((byte) 0, (byte) SCL_MASK);
		}
		
		// read the ACK bit
		mpsse.enqueue(MSB_FIRST_BIT_IN_POS_EDGE.getValue());
		mpsse.enqueue((byte) 0);		// 0 for a single bit
		
		mpsse.enqueue(SEND_IMMEDIATE.getValue());
		mpsse.execute();

		byte[] buff = mpsse.read(1);

		return (buff[0] & 1) == 0;
	}

	/**
	 * Output the I2C device address.
	 *
	 * @param	address			i2c device address (7 bits)
	 * @param	read			true for read operations
	 * @return					true, if write was successful
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 */
	public boolean writeAddress(int address, boolean read) throws FTDIException {
		// I2C device addresses are normally 7 bits long
		byte value = (byte) (address << 1);

		// With I2C the 8th bit is used as a read/write flag
		if (read) {
			value |= 1;
		}
		
		return write(value);
	}
}
