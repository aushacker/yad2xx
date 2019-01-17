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
package net.sf.yad2xx.jtag;

import static net.sf.yad2xx.mpsse.Command.*;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.mpsse.Mpsse;

import java.io.Closeable;

/**
 * A layer over an MPSSE capable FTDI device that contains knowledge
 * of the IEEE 1149.1 JTAG protocol. Uses the GoF wrapper pattern.
 * <p>
 * Pinouts are:
 * <ul>
 * <li>xDBUS0 - TCK</li>
 * <li>xDBUS1 - TDI</li>
 * <li>xDBUS2 - TDO</li>
 * <li>xDBUS3 - TMS</li>
 * </ul>
 * 
 * @author	Stephen Davies
 * @since	14 April 2016
 * @since	0.4
 */
public class Jtag implements Closeable {

	public static final int JTAG_TCK_MASK = 1;
	public static final int JTAG_TDI_MASK = 2;
	public static final int JTAG_TDO_MASK = 4;
	public static final int JTAG_TMS_MASK = 8;
	
	public static final byte JTAG_OUTPUT_PIN_MASK =
			(byte) (JTAG_TMS_MASK | JTAG_TDI_MASK | JTAG_TCK_MASK);
	
	private Mpsse mpsse;
	
	private TapState currentState;
	private TapState endIr;
	private TapState endDr;
	
	/**
	 * Layer over an existing FTDI device.
	 * 
	 * @param	device			device to wrapper
	 */
	public Jtag(Device device) {
		this.mpsse = new Mpsse(device);
		this.endDr =  TapState.IDLE;
		this.endIr =  TapState.IDLE;
	}
	
	/**
	 * Drives the TAP state machine through count cycles with value being
	 * applied to the TMS pin, LSB first.
	 * 
	 * @param	count			number of TCK cycles
	 * @param	value			to be shifted out TMS
	 */
	public void alterTms(int count, int value) {
		mpsse.enqueue(TMS_BIT_OUT_NEG_EDGE.getValue());
		// Number of clock pulses = count + 1 
		mpsse.enqueue((byte) (count - 1));
		// Data is shifted LSB first
		mpsse.enqueue((byte) value);

		mpsse.execute();
	}

	/**
	 * Testing complete. Cleanup resources.
	 */
	@Override
	public void close() {
		mpsse.close();
	}
	
	/**
	 * Initialise MPSSE specifically for JTAG.
	 * 
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public void open() throws FTDIException {
		mpsse.open();
		
		mpsse.disableClockDivider();
		mpsse.disableAdaptiveClock();
		mpsse.disableThreePhaseClock();
		mpsse.execute();
		
		// Set initial states of the MPSSE interface - low byte, both pin directions and output values
		// Pin name Signal Direction Config Initial State Config
		// ADBUS0 TCK 		output	1		low				0
		// ADBUS1 TDI		output	1 		low				0
		// ADBUS2 TDO		input	0						0
		// ADBUS3 TMS		output	1		high			1
		// ADBUS4 GPIOL0	input	0						0
		// ADBUS5 GPIOL1	input	0						0
		// ADBUS6 GPIOL2	input	0						0
		// ADBUS7 GPIOL3	input	0						0
		mpsse.setDataBitsLow((byte) JTAG_TMS_MASK, JTAG_OUTPUT_PIN_MASK);

		// Set initial states of the MPSSE interface - high byte, both pin directions and output values
		// Pin name Signal Direction Config Initial State Config
		// ACBUS0 GPIOH0	input	0						0
		// ACBUS1 GPIOH1	input	0						0
		// ACBUS2 GPIOH2	input	0						0
		// ACBUS3 GPIOH3	input	0						0
		// ACBUS4 GPIOH4	input	0						0
		// ACBUS5 GPIOH5	input	0						0
		// ACBUS6 GPIOH6	input	0						0
		// ACBUS7 GPIOH7	input	0						0
		mpsse.setDataBitsHigh((byte) 0, (byte) 0);
		
		mpsse.configureClock(100000);
		mpsse.execute();
		mpsse.delay(20);
		
		mpsse.disableLoopback();
		mpsse.execute();
		mpsse.delay(30);
		
		reset();
		
		if (mpsse.getQueueStatus() != 0) {
			throw new IllegalStateException();
		}
	}

	public void reset() {
		alterTms(5, 0x1F);
		currentState = TapState.RESET;
	}
	
	public byte[] scanDR(int bitCount) throws FTDIException {
		int byteCount = bitCount / 8;
		if (bitCount % 8 != 0) {
			byteCount++;
		}
		byte[] dummy =  new byte[byteCount];
		return scanDR(bitCount, dummy);
	}
	
	public byte[] scanDR(int bitCount, byte[] outData) throws FTDIException {
		
		transitionTo(TapState.DRSHIFT);
		
		byte[] result = shiftBits(bitCount, outData);
		currentState = TapState.DREXIT1;
		
		transitionTo(endDr);
		
		return result;
	}
	
	public byte[] scanIR(int bitCount, byte[] outData) throws FTDIException {
		
		transitionTo(TapState.IRSHIFT);

		byte[] result = shiftBits(bitCount, outData);
		currentState = TapState.IREXIT1;
		
		transitionTo(endIr);
		
		return result;
	}

	public byte[] shiftBits(int bitCount, byte[] outData) throws FTDIException {
		int byteCount = bitCount / 8;
		int extraBits = bitCount % 8;
		
		// if whole # of bytes then fragment the last byte
		// so that last bit TMS navigation works.
		if (extraBits == 0) {
			byteCount--;
			extraBits = 8;
		}
		
		
		if (byteCount > 0) {
			int count = byteCount - 1;						// FTDI uses 0 length to indicate a 1
			mpsse.enqueue(LSB_FIRST_BYTE_INOUT_POSNEG_EDGE.getValue());
			mpsse.enqueue((byte) (count & 0xff));			// LSB
			mpsse.enqueue((byte) ((count >> 8) & 0xff));	// MSB
			mpsse.enqueue(outData, 0, byteCount);
		}
		
		if (extraBits > 1) {
			int count = extraBits - 2; 						// 0 indicates a single bit
			mpsse.enqueue(LSB_FIRST_BIT_INOUT_POSNEG_EDGE.getValue());
			mpsse.enqueue((byte) count);
			mpsse.enqueue(outData[outData.length - 1]);
		}
		
		// now shift last bit, moving TAP to EXIT-? as we do
		mpsse.enqueue(TMS_BIT_INOUT_POSNEG_EDGE.getValue());
		mpsse.enqueue((byte) 0);					// one bit only
		// need to combine TMS bit with last data bit,
		// data bit needs to be bit 7, TMS bit is 0
		int value = outData[outData.length - 1];
		value <<= (8 - extraBits);					// move last bit to bit 7
		value &= 0x80;								// only bit 7 survives
		value |= 1;									// TMS bit is 1 to exit SHIFT state
		mpsse.enqueue((byte) value);
		mpsse.execute();
		
		if (extraBits > 0) {
			byteCount++;
		}
		
		byte[] result = mpsse.read(byteCount + 1);
		System.out.println(mpsse.getQueueStatus());
		
		return result;
	}
	
	public void transitionTo(TapState to) {
		int[] transitions = currentState.svfPathTo(to);
		byte result = 0;
		
		for (int i = transitions.length - 1; i >= 0; i--) {
			result <<= 1;
			if (transitions[i] == 1) {
				result |= 1;
			}
		}
		
		alterTms(transitions.length, result);
		currentState = to;
	}
}
