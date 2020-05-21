/*
 * Copyright 2015-2020 Stephen Davies
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
 * <p>
 * Extended by version 2.1 to allow for additional status codes used by
 * LibFT4222.
 *
 * @author Stephen Davies
 * @since 15 June 2015
 * @since 0.3
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
    FT_DEVICE_LIST_NOT_READY(19),

    // FT_STATUS extensions from LibFT4222

    FT4222_DEVICE_NOT_SUPPORTED(1000),
    FT4222_CLK_NOT_SUPPORTED(1001),
    FT4222_VENDER_CMD_NOT_SUPPORTED(1002),
    FT4222_IS_NOT_SPI_MODE(1003),
    FT4222_IS_NOT_I2C_MODE(1004),
    FT4222_IS_NOT_SPI_SINGLE_MODE(1005),
    FT4222_IS_NOT_SPI_MULTI_MODE(1006),
    FT4222_WRONG_I2C_ADDR(1007),
    FT4222_INVAILD_FUNCTION(1008),
    FT4222_INVALID_POINTER(1009),
    FT4222_EXCEEDED_MAX_TRANSFER_SIZE(1010),
    FT4222_FAILED_TO_READ_DEVICE(1011),
    FT4222_I2C_NOT_SUPPORTED_IN_THIS_MODE(1012),
    FT4222_GPIO_NOT_SUPPORTED_IN_THIS_MODE(1013),
    FT4222_GPIO_EXCEEDED_MAX_PORTNUM(1014),
    FT4222_GPIO_WRITE_NOT_SUPPORTED(1015),
    FT4222_GPIO_PULLUP_INVALID_IN_INPUTMODE(1016),
    FT4222_GPIO_PULLDOWN_INVALID_IN_INPUTMODE(1017),
    FT4222_GPIO_OPENDRAIN_INVALID_IN_OUTPUTMODE(1018),
    FT4222_INTERRUPT_NOT_SUPPORTED(1019),
    FT4222_GPIO_INPUT_NOT_SUPPORTED(1020),
    FT4222_EVENT_NOT_SUPPORTED(1021),
    FT4222_FUN_NOT_SUPPORT(1022);

    /**
     * Integer value used by the D2XX API (C language enums number their
     * elements starting at 0 and increasing by 1).
     */
    private final int ftdiOrdinal;

    /**
     * Values shared by D2XX and FT4222 library functions.
     */
    private static final FTStatus[] VALUES = {
        FT_OK, FT_INVALID_HANDLE, FT_DEVICE_NOT_FOUND, FT_DEVICE_NOT_OPENED,
        FT_IO_ERROR, FT_INSUFFICIENT_RESOURCES, FT_INVALID_PARAMETER,
        FT_INVALID_BAUD_RATE, FT_DEVICE_NOT_OPENED_FOR_ERASE,
        FT_DEVICE_NOT_OPENED_FOR_WRITE, FT_FAILED_TO_WRITE_DEVICE,
        FT_EEPROM_READ_FAILED, FT_EEPROM_WRITE_FAILED, FT_EEPROM_ERASE_FAILED,
        FT_EEPROM_NOT_PRESENT, FT_EEPROM_NOT_PROGRAMMED, FT_INVALID_ARGS,
        FT_NOT_SUPPORTED, FT_OTHER_ERROR, FT_DEVICE_LIST_NOT_READY
    };

    /**
     * Values unique to FT4222 library functions.
     */
    private static final FTStatus[] FT4222_VALUES = {
        FT4222_DEVICE_NOT_SUPPORTED, FT4222_CLK_NOT_SUPPORTED,
        FT4222_VENDER_CMD_NOT_SUPPORTED, FT4222_IS_NOT_SPI_MODE,
        FT4222_IS_NOT_I2C_MODE, FT4222_IS_NOT_SPI_SINGLE_MODE,
        FT4222_IS_NOT_SPI_MULTI_MODE, FT4222_WRONG_I2C_ADDR,
        FT4222_INVAILD_FUNCTION, FT4222_INVALID_POINTER,
        FT4222_EXCEEDED_MAX_TRANSFER_SIZE, FT4222_FAILED_TO_READ_DEVICE,
        FT4222_I2C_NOT_SUPPORTED_IN_THIS_MODE,
        FT4222_GPIO_NOT_SUPPORTED_IN_THIS_MODE,
        FT4222_GPIO_EXCEEDED_MAX_PORTNUM, FT4222_GPIO_WRITE_NOT_SUPPORTED,
        FT4222_GPIO_PULLUP_INVALID_IN_INPUTMODE,
        FT4222_GPIO_PULLDOWN_INVALID_IN_INPUTMODE,
        FT4222_GPIO_OPENDRAIN_INVALID_IN_OUTPUTMODE,
        FT4222_INTERRUPT_NOT_SUPPORTED, FT4222_GPIO_INPUT_NOT_SUPPORTED,
        FT4222_EVENT_NOT_SUPPORTED, FT4222_FUN_NOT_SUPPORT
    };

    FTStatus(int ordinal) {
        this.ftdiOrdinal = ordinal;
    }

    /**
     * Convert API value to its Java enum equivalent.
     *
     * @param ftdiOrdinal
     *            D2XX status value
     * @return matching Java enum value
     */
    public static FTStatus byOrdinal(int ftdiOrdinal) {
        if (ftdiOrdinal >= 0 && ftdiOrdinal < VALUES.length) {
            return VALUES[ftdiOrdinal];
        } else if (ftdiOrdinal >= 1000 && ftdiOrdinal < (1000 + FT4222_VALUES.length)) {
            return FT4222_VALUES[ftdiOrdinal - 1000];
        } else {
            return null;
        }
    }

    /**
     * Access D2XX API status code value.
     *
     * @return D2XX value
     */
    public int getFtdiOrdinal() {
        return ftdiOrdinal;
    }
}
