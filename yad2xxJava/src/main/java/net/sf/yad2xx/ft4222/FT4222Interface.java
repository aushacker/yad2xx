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
 * FT4222 library to a more OO based approach. This Singleton type
 * provides a very thin Java layer over the top of the native C
 * language code and is central to the libraries operation.
 *
 * @author      Stephen Davies
 * @since       13 May 2020
 * @since       2.1
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
	 * Initialize the FT4222H as an I2C master with the requested I2C speed.
	 *  
	 * @param   ftHandle                FT4222 device handle
	 * @throws  FTDIException           FT4222_I2CMaster_Init returned a non-zero status code
	 * @see                             FT4222Device#i2cMasterInit(int)
	 * @since   2.1
	 */
	native static void i2cMasterInit(long ftHandle, int kbps) throws FTDIException;

	/**
	 * Release allocated resources.
	 *  
	 * @param   ftHandle                FT4222 device handle
	 * @throws  FTDIException           FT4222_UnInitialize returned a non-zero status code
	 * @see                             FT4222Device#unInitialize()
	 * @since   2.1
	 */
	native static void unInitialize(long ftHandle) throws FTDIException;
}
