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
package net.sf.yad2xx.mpsse.samples;

import java.io.PrintStream;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;
import net.sf.yad2xx.mpsse.Spi;
import net.sf.yad2xx.mpsse.SpiMode;
import net.sf.yad2xx.samples.AbstractSample;

/**
 * Example program using an FTDI device in SPI mode to manipulate the contents of
 * a CAT93C46 EEPROM. The EEPROM is configured in 16 bit mode (ORG = 1).
 * 
 * @author		Stephen Davies
 * @since		15 April 2016
 * @since		0.4
 */
public class SpiEEPROMSample extends AbstractSample {

	private static final int DESIRED_CLOCK = 500000;
	
	private static final int MEM_SIZE_BITS = 1024;
	
	private static final String HEX = "0123456789ABCDEF";
	
	//
	// CAT93C46 opcodes
	//
	// 3 bit opcodes
	private static final int OP_READ = 6;
	private static final int OP_ERASE = 7;
	private static final int OP_WRITE = 5;
	// 5 bit opcodes
	private static final int OP_WRITE_ENABLE = 0x13;
	private static final int OP_WRITE_DISABLE = 0x10;
	private static final int OP_ERASE_ALL = 0x12;
	//private static final int OP_WRITE_ALL = 0x11;

	private Spi spi;
	private WordSize wordSize;
	
	public static void main(String[] args) {
		SpiEEPROMSample dumper = new SpiEEPROMSample(WordSize.W16);
		
		try {
			if (dumper.processOptions(args)) {
				dumper.run();
			} else {
				dumper.displayUsage();
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			dumper.displayUsage();
		}
	}
	
	private SpiEEPROMSample(WordSize wordSize) {
		this.wordSize = wordSize;
	}
	
	private void displayUsage() {
		displayUsage("net.sf.yad2xx.mpsse.samples.SpiEEPROMSample [-h] [-p hex]");
	}
	
	private void dumpMemory(PrintStream out) throws FTDIException {
		int wordCount = MEM_SIZE_BITS / wordSize.getBits();
		int readsPerLine = (wordSize == WordSize.W8) ? 16 : 8;
		
		for (int base = 0; base < wordCount; base += readsPerLine) {
			printHex(out, (byte) base);
			out.print(":");
			
			for (int offset = 0; offset < readsPerLine; offset++) {
				int val = read(base + offset);
				
				out.print(' ');
				
				if (offset == (readsPerLine / 2)) {
					out.print("- ");
				}
				
				printHex(out, (byte) val);
				
				if (wordSize == WordSize.W16) {
					out.print(' ');
					printHex(out, (byte) (val >> 8));
				}
			}
			
			out.println();
		}
	}

	private byte[] encodeShortOperation(int opcode, int address, Integer data) {
		byte[] result = null;
		
		if (data == null) {
			result = new byte[2];
			result[0] = (byte) (opcode << 5);
			if (wordSize == WordSize.W8) {
				result[0] |= (byte) ((address >> 2) & 0x1f);
				result[1] = (byte) ((address << 6) & 0xff);
			} else {
				result[0] |= (byte) ((address >> 1) & 0x1f);
				result[1] = (byte) ((address << 7) & 0xff);
			}
		} else {
		}
		
		return result;
	}
	
	/**
	 * Write enable/disable and erase/write all are long operations. They
	 * have 4-5 don't care values in their least significant bits. The
	 * result will be a 9 or 10 bit field encoded into 2 bytes. All of the
	 * first byte is used, only bits 6-7 of the second byte are used.
	 */
	private byte[] encodeLongOperation(int opcode) {
		byte[] result = new byte[2];
		
		result[0] = (byte) (opcode << 3);	// MSB
		result[1] = 0;						// LSB
		
		return result;
	}
	
	/**
	 * Erase the given address (i.e. set all 16 bits to 0xFFFF).
	 * 
	 * @param	address			address to erase
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public void erase(int address) throws FTDIException {
		spi.transactWrite(wordSize.getCommandLength(), encodeShortOperation(OP_ERASE, address, null));		
	}

	/**
	 * Erase the entire chip.
	 * 
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public void eraseAll() throws FTDIException {
		spi.transactWrite(wordSize.getCommandLength(), encodeLongOperation(OP_ERASE_ALL));		
	}

	/**
	 * Output a single byte in HEX.
	 * 
	 * @param	out				console
	 * @param	b				value
	 */
	private void printHex(PrintStream out, byte b) {
		out.print(HEX.charAt((b >> 4) & 0xf));
		out.print(HEX.charAt(b & 0xf));
	}

	/**
	 * Read a single byte or word from the specified address.
	 * 
	 * @param	address			address
	 * @return					target byte or word
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public int read(int address) throws FTDIException {
		spi.assertSelect();
		spi.writeBits(wordSize.getCommandLength(), encodeShortOperation(OP_READ, address, null));
		
		byte[] data = spi.readBits(wordSize.getBits());
		
		spi.clearSelect();
		spi.execute();
		
		int result = 0;
		
		if (wordSize == WordSize.W8) {
			result = data[0];
		} else {
			result = (data[0] << 8) | data[1];
		}
		
		return result;
	}
	
	private void run() {
		PrintStream out = System.out;
		
		try {
			
			out.println("Dump EEPROM Example");
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

			out.println("Initial memory contents\n");
			
			writeDisable();
			dumpMemory(out);

			out.println("\nAttempting to write (write disabled)\n");
			write(0, 0x100);
			dumpMemory(out);
			
			out.println("\nWriting to locations 0 and 1 (write enabled)\n");
			writeEnable();
			write(0, 0x100);
			waitWhileBusy();
			write(1, 0x302);
			waitWhileBusy();
			dumpMemory(out);
			
			out.println("\nErasing location 1\n");
			erase(1);
			waitWhileBusy();
			dumpMemory(out);
			
			out.println("\nErasing all\n");
			eraseAll();
			waitWhileBusy();
			dumpMemory(out);
			
			spi.close();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private void setSpi(Spi spi) {
		this.spi = spi;
	}

	/**
	 * The CAT93C46 self-times its write operations. Asserting CS will
	 * enable the device, it will pull MISO low until the write is complete.
	 * 
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public void waitWhileBusy() throws FTDIException {
		spi.assertSelect();
		spi.execute();
		
		// wait for MISO to go high
		while ( (spi.readDataBitsLow() & Spi.SPI_MISO_MASK) == 0) {
		};
		
		spi.clearSelect();
		spi.execute();
	}

	/**
	 * Write a value to a single location.
	 * 
	 * @param	address			address
	 * @param	value			value to write
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public void write(int address, int value) throws FTDIException {
		spi.assertSelect();
		spi.writeBits(wordSize.getCommandLength(), encodeShortOperation(OP_WRITE, address, null));
		if (wordSize == WordSize.W8) {
			spi.writeBits(8, new byte[] { (byte) (value & 0xff)});
		} else {
			spi.writeBits(16, new byte[] { (byte) ((value >> 8) & 0xff), (byte) (value & 0xff) });
		}
		
		spi.clearSelect();
		spi.execute();
	}
	
	/**
	 * Set EEPROM to ignore write requests.
	 *
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public void writeDisable() throws FTDIException {
		spi.transactWrite(wordSize.getCommandLength(), encodeLongOperation(OP_WRITE_DISABLE));	
	}
	
	/**
	 * Set EEPROM to allow write requests.
	 *
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 */
	public void writeEnable() throws FTDIException {
		spi.transactWrite(wordSize.getCommandLength(), encodeLongOperation(OP_WRITE_ENABLE));	
	}
	
	/**
	 * The CAT93C46 can be organized into 8 or 16 bit words. This setting effects 
	 * the SPI command word lengths. 
	 */
	private enum WordSize {
		
		W8(8, 10), W16(16, 9);
		
		/**
		 * Memory id organized into 8 ot 16 bit words.
		 */
		private final int bits;

		/** 
		 * Command word lengths vary depending on the CAT93C46 organization. 
		 * With an 8 bit word commands are 10 bits long, for 16 bit word commands 
		 * are 9 bits long.
		 */
		private final int commandLength;
		
		private WordSize(int bits, int commandLength) {
			this.bits = bits;
			this.commandLength = commandLength;
		}
		
		public int getBits() {
			return bits;
		}
		
		public int getCommandLength() {
			return commandLength;
		}
	}
}
