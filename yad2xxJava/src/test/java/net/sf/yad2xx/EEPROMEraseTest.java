/*
 * Copyright 2013 Stephen Davies
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
package net.sf.yad2xx;

/**
 * Erase a device EEPROM.
 *
 * @since 22/09/2013
 * @author Stephen Davies
 */
public class EEPROMEraseTest {

	private static final int PID = 0x84E0;
	
	public static void main(String[] args) throws FTDIException {

		FTDIInterface.setVidPid(0x0403, PID);
		
		if (FTDIInterface.getDevices().length > 0) {
			Device dev = FTDIInterface.getDevices()[0];
			
			try {
				dev.open();
				dev.eraseEE();
			}
			finally {
				dev.close();
			}
		}
	}

}
