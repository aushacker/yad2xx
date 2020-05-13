/*
 * Copyright 2012-2020 Stephen Davies
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
package net.sf.yad2xx.ft4222;

import net.sf.yad2xx.FTDIException;

/**
 * A Java Native Interface (JNI) wrapper that adapts the FTDI
 * LibFT4222 library to a more OO based approach. This Singleton type
 * provides a very thin Java layer over the top of the native C
 * language code and is central to the libraries operation.
 *
 * @author		Stephen Davies
 * @since		13 May 2020
 * @since		2.0
 */
public class FT4222Interface {

	/**
	 * Loads the native library on first class usage. Library location
	 * is JVM/platform dependent.
	 */
	static {
		System.loadLibrary("FT4222Interface");
	}

	/**
	 * This function builds a device information list
	 * (calls FT_CreateDeviceInfoList) and returns the number of D2XX devices
	 * connected to the system. The list contains information about both
	 * unopened and open devices.
	 * <p>
	 * Not sure how useful this really is in Java. Probably better off just
	 * using {@link #getDevices()} and using the returned array length.
	 * 
	 * @return					number of devices connected to the system
	 * @throws	FTDIException	FT_CreateDeviceInfoList returned a non-zero
	 *							status code
	 * @since	0.1
	 */
	public static native int getDeviceCount() throws FTDIException;

}
