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
package net.sf.yad2xx.jtag.samples;

import java.io.PrintStream;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIInterface;
import net.sf.yad2xx.jtag.Jtag;
import net.sf.yad2xx.samples.AbstractSample;

/**
 * Sample program that uses JTAG to read the IDCODE and USERCODE
 * registers in a Xilinx XC9572XL CPLD.
 *
 * @author Stephen Davies
 * @since 28 April 2016
 * @since 0.4
 */
public class ReadIdCodeSample extends AbstractSample {

//	attribute INSTRUCTION_OPCODE of xc9572xl_vq44 : entity is
//	"BYPASS ( 11111111)," &
//	"CLAMP ( 11111010)," &
//	"ISPEX ( 11110000)," &
//	"EXTEST ( 00000000),"&
//	"FBLANK ( 11100101),"& 
//	"FBULK ( 11101101),"&
//	"FERASE ( 11101100),"&
//	"FPGM ( 11101010)," &
//	"FPGMI ( 11101011)," &
//	"FVFY ( 11101110)," &
//	"FVFYI ( 11101111)," &
//	"HIGHZ ( 11111100),"&
//	"IDCODE ( 11111110),"&
//	"INTEST ( 00000010),"&
//	"ISPEN ( 11101000)," &
//	"ISPENC ( 11101001)," &
//	"SAMPLE ( 00000001)," &
//	"USERCODE ( 11111101)"; 

	public static final int OPCODE_LENGTH = 8;
	public static final int IDREG_LENGTH = 32;
	
	public static final int OPCODE_BYPASS = 0xFF;
	public static final int OPCODE_IDCODE = 0xFE;
	public static final int OPCODE_USERCODE = 0xFD;
	
	public static void main(String[] args) {
		ReadIdCodeSample sample = new ReadIdCodeSample();
		
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
		displayUsage("net.sf.yad2xx.jtag.samples.ReadIdCodeSample [-h] [-p hex]");
	}
	
	private void run() {
		PrintStream out = System.out;
		
		try {
			
			out.println("Jtag Id Code Example");
			out.println("--------------------");
			printProlog(out);
			
			Device[] devices = FTDIInterface.getDevices();
			
			if (devices.length == 0) {
				out.println("*** No FTDI devices found. Possible VID/PID or driver problem. ***");
				return;
			}
			
			Device device = devices[0];
			Jtag jtag = new Jtag(device);
			jtag.open();
			
			jtag.scanIR(OPCODE_LENGTH, new byte[] { (byte) OPCODE_IDCODE });
			byte[] data = jtag.scanDR(IDREG_LENGTH);
			// output data
			
			//jtag.scanIR(OPCODE_LENGTH, new byte[] { (byte) OPCODE_USERCODE });
			//data = jtag.scanDR(IDREG_LENGTH);
			// output data
			
			jtag.close();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

}
