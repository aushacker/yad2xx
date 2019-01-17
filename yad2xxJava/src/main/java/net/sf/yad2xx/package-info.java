/*
 * Copyright 2012-2018 Stephen Davies
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

/**
 * Provides the types necessary to perform a Java language binding to the
 * FTDI supplied D2XX library.
 * <p>
 * Two classes are central to the package, FTDIInterface and Device.
 * FTDIInterface implements a Java Native Interface (JNI), nearly all the
 * methods are declared native. Actual implementations for these methods
 * are found in the file FTDIInterface.c. FTDIInterface is written in
 * functional, not OO, style. Few methods are intended to be called
 * directly.
 * <p>
 * The Device class permits operation of a single device such as an FT232
 * or access to an individual device channel, e.g. channel A, when a
 * multi-channel device like an FT2232H is attached. Device is written
 * in Object Oriented (OO) style.
 * <p>
 * All other types in the package are here to support Device and
 * FTDIInterface.
 * <p>
 * Generally, most operations are performed via instance methods on
 * Device. Client programs should look something like this:
 * <pre>
 * {@code
 * Device[] devices = FTDIInterface.getDevices();
 *
 * if (devices.length == 0) {
 *     // error, no devices attached
 * }
 *
 * Device dev = devices[0];
 * dev.open();
 *
 * // use the device
 *
 * dev.close();
 * }
 * </pre>
 *
 * @author		Stephen Davies
 * @since		20 May 2012
 * @since		0.1
 */
package net.sf.yad2xx;
