/*
 * Copyright 2020 Stephen Davies
 *
 * This file is part of yad2xx.
 *
 * yad2xx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * yad2xx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with yad2xx. If not, see <https://www.gnu.org/licenses/>.
 */

package net.sf.yad2xx.ft4222;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIException;

/**
 * @author Stephen Davies
 * @since 13 May 2020
 * @since 2.0
 */
public class FT4222Device extends Device {

	/**
	 * Constructor intended for internal library use only. Use 
	 * {@link net.sf.yad2xx.FTDInterface#getDevices()}.
	 *  
	 * @param index
	 * @param flags
	 * @param type
	 * @param id
	 * @param locationId
	 * @param serialNumber		burned in device serial number
	 * @param description		driver description
	 * @param ftHandle
	 *
	 * @since 2.0
	 */
	FT4222Device(int index, int flags, int type, int id, int locationId, String serialNumber, String description, long ftHandle) {
		super(index, flags, type, id, locationId, serialNumber, description, ftHandle);
	}

	public void i2cMasterInit(int kbps) throws FTDIException {
		// TODO
	}

	public byte[] i2cMasterRead(int slaveAddress) throws FTDIException {
		// TODO
		return null;
	}

	public int i2cMasterWrite(int slaveAddress, byte[] data) throws FTDIException {
		// TODO
		return 0;
	}

    public void unInitialize() throws FTDIException {
    	// TODO
    }
}
