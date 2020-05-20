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

import net.sf.yad2xx.ft4222.ClockRate;

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
     * Software reset for device.
     * <p>
     * This function is used to attempt to recover system after a failure.
     * It is a software reset for the device.
     *
     * @throws FTDIException
     *             API call failed, see exception fields for details. More
     *             information can be found in AN_329.
     * @see FTDIInterface#chipReset(long)
     * @since 2.1
     */
    public void chipReset() throws FTDIException {
        FTDIInterface.chipReset(getHandle());
    }

    /**
     * Get the current system clock rate.
     *
     * @return the current clock rate
     * @throws FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @throws IllegalStateException
     *             Device must be opened before calling this method.
     * @see FTDIInterface#getClock(long)
     * @since 2.1
     */
    public ClockRate getClock() throws FTDIException {
        if (!isOpen())
            throw new IllegalStateException("Device not open");
        return ClockRate.byOrdinal(FTDIInterface.getClock(getHandle()));
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
     * @param buffer
     *            
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
     * Set the system clock rate. The FT4222H supports 4 clock rates: 80MHz,
     * 60MHz, 48MHz, or 24MHz. By default, the FT4222H runs at 60MHz clock
     * rate.
     *
     * @param rate
     * @throws FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @see FTDIInterface#setClock(long, long)
     * @since 2.1
     */
    public void setClock(ClockRate rate) throws FTDIException {
        FTDIInterface.setClock(getHandle(), rate.ordinal());
    }

    /**
     * Enable or disable, suspend out, which will emit a signal when FT4222H
     * enters suspend mode. Please note that the suspend-out pin is not
     * available under mode 2. By default, suspend-out function is on.
     * <p>
     * When suspend-out function is on, suspend-out pin emits signal according
     * to suspend-out polarity. The default value of suspend-out polarity is
     * active high. It means suspend-out pin output low in normal mode and
     * output high in suspend mode. Suspend-out polarity only can be adjusted
     * by FT_PROG.
     *
     * @param enable
     * @throws FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @see FTDIInterface#setSuspendOut(long, boolean)
     * @since 2.1
     */
    public void setSuspendOut(boolean enable) throws FTDIException {
        FTDIInterface.setSuspendOut(getHandle(), enable);
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
