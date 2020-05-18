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

package net.sf.yad2xx;

/**
 * High level API for communicating with FTDI FT4222 devices.
 *
 * @author Stephen Davies
 * @since 13 May 2020
 * @since 2.1
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
     * @param serialNumber             burned in device serial number
     * @param description              driver description
     * @param ftHandle
     *
     * @since 2.1
     */
    FT4222Device(int index, int flags, int type, int id, int locationId, String serialNumber, String description, long ftHandle) {
        super(index, flags, type, id, locationId, serialNumber, description, ftHandle);
    }

    /**
     * Initialize the FT4222H as an I2C master with the requested I2C speed.
     *
     * @param kbps
     *            The speed of I2C transmission. It ranges from 60K bps to 3400K
     *            bps. By specified speed, the initialization function helps to
     *            setup the bus speed with the corresponding mode. This
     *            parameter is used to configure the FT4222H to be either SM,
     *            FB, FM+ or HS mode
     * @throws FTDIException
     *             API call failed, see exception fields for details. More
     *             information can be found in AN_329.
     * @see FTDIInterface#i2cMasterInit(long, int)
     * @since 2.1
     */
    public void i2cMasterInit(int kbps) throws FTDIException {
        FTDIInterface.i2cMasterInit(getHandle(), kbps);
    }

    /**
     * Read data from the specified I2C slave device with START and STOP
     * conditions.
     *
     * @param slaveAddress
     *            address of the target i2c slave
     * @param bytesToRead
     *            max number of bytes to read from the device
     * @return sizeTransferred
     * @throws FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @see FTDIInterface#i2cMasterRead(long, int, int)
     * @since 2.1
     */
    public int i2cMasterRead(int slaveAddress, byte[] buffer, int bytesToRead) throws FTDIException {
        return FTDIInterface.i2cMasterRead(getHandle(), slaveAddress, buffer, bytesToRead);
    }

    /**
     * Write data to the specified I2C slave device with START and STOP
     * conditions.
     *
     * @param slaveAddress
     *            address of the target i2c slave
     * @param buffer
     *            data to be written to the device. Array length implies
     *            number of bytes to write
     * @return number of bytes actually transferred (sizeTransferred).
     * @throws FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @see FTDIInterface#i2cMasterWrite(long, int, byte[], int)
     * @since 2.1
     */
    public int i2cMasterWrite(int slaveAddress, byte[] buffer) throws FTDIException {
        return FTDIInterface.i2cMasterWrite(getHandle(), slaveAddress, buffer,
                                            buffer.length);
    }

    /**
     * Release allocated resources. Should be called before calling close().
     *
     * @throws FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @see FTDIInterface#unInitialize(long)
     * @since 2.1
     */
    public void unInitialize() throws FTDIException {
        FTDIInterface.unInitialize(getHandle());
    }
}
