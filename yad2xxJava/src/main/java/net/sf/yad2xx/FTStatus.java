/*
 * Copyright 2015-2018 Stephen Davies
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
 * Values for FT_STATUS (DWORD), returned by most D2XX function calls.
 * Taken from ftd2xx.h, version 1.4.4.
 *  
 * @author		Stephen Davies
 * @since		15 June 2015
 * @since		0.3
 */
public enum FTStatus {
	
	FT_OK(0),
	FT_INVALID_HANDLE(1),
	FT_DEVICE_NOT_FOUND(2),
	FT_DEVICE_NOT_OPENED(3),
	FT_IO_ERROR(4),
	FT_INSUFFICIENT_RESOURCES(5),
	FT_INVALID_PARAMETER(6),
	FT_INVALID_BAUD_RATE(7),
	FT_DEVICE_NOT_OPENED_FOR_ERASE(8),
	FT_DEVICE_NOT_OPENED_FOR_WRITE(9),
	FT_FAILED_TO_WRITE_DEVICE(10),
	FT_EEPROM_READ_FAILED(11),
	FT_EEPROM_WRITE_FAILED(12),
	FT_EEPROM_ERASE_FAILED(13),
	FT_EEPROM_NOT_PRESENT(14),
	FT_EEPROM_NOT_PROGRAMMED(15),
	FT_INVALID_ARGS(16),
	FT_NOT_SUPPORTED(17),
	FT_OTHER_ERROR(18),
	FT_DEVICE_LIST_NOT_READY(19);

	/**
	 * Integer value used by the D2XX API (C language enums number their
	 * elements starting at 0 and increasing by 1).
	 */
	private final int ftdiOrdinal;
	
	private static final FTStatus[] VALUES = {
		FT_OK, FT_INVALID_HANDLE, FT_DEVICE_NOT_FOUND, FT_DEVICE_NOT_OPENED, FT_IO_ERROR,
		FT_INSUFFICIENT_RESOURCES, FT_INVALID_PARAMETER, FT_INVALID_BAUD_RATE,
		FT_DEVICE_NOT_OPENED_FOR_ERASE,	FT_DEVICE_NOT_OPENED_FOR_WRITE,
		FT_FAILED_TO_WRITE_DEVICE, FT_EEPROM_READ_FAILED,
		FT_EEPROM_WRITE_FAILED, FT_EEPROM_ERASE_FAILED,	FT_EEPROM_NOT_PRESENT,
		FT_EEPROM_NOT_PROGRAMMED, FT_INVALID_ARGS, FT_NOT_SUPPORTED,
		FT_OTHER_ERROR, FT_DEVICE_LIST_NOT_READY
	};
	
	private FTStatus(int ordinal) {
		this.ftdiOrdinal = ordinal;
	}

	/**
	 * Convert API value to its Java enum equivalent.
	 *
	 * @param	ftdiOrdinal		D2XX status value
	 * @return					matching Java enum value
	 */
	public static FTStatus byOrdinal(int ftdiOrdinal) {
		if (ftdiOrdinal >= 0 && ftdiOrdinal < VALUES.length) {
			return VALUES[ftdiOrdinal];
		} else {
			return null;
		}
	}

	/**
	 * Access D2XX API status code value.
	 *
	 * @return					D2XX value
	 */
	public int getFtdiOrdinal() {
		return ftdiOrdinal;
	}
}
