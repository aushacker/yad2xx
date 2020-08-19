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
import net.sf.yad2xx.ft4222.GpioTrigger;
import net.sf.yad2xx.ft4222.Version;

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
     * {@link net.sf.yad2xx.FTDIInterface#getDevices()}.
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
     * This function returns the maximum packet size in a transaction. It will
     * be affected by different bus speeds, chip modes, and functions. The
     * maximum transfer size is maximum size in writing path.
     *
     * @return maximum packet size
     * @throws FTDIException
     *             API call failed, see exception fields for details. More
     *             information can be found in AN_329.
     * @throws IllegalStateException
     *             Device must be opened before calling this method.
     *             Device must be initialized before calling this method.
     * @see FTDIInterface#getMaxTransferSize(long)
     * @since 2.1
     */
    public int getMaxTransferSize() throws FTDIException {
        if (!isOpen())
            throw new IllegalStateException("Device not open");

        try {
            return FTDIInterface.getMaxTransferSize(getHandle());
        }
        catch (FTDIException e) {
            if (e.getStatus() == FTStatus.FT_DEVICE_NOT_OPENED) {
                // One of the init methods needs to be called before getting transfer size
                // e.g. i2cMasterInit
                throw new IllegalStateException("FT4222 device not initialized", e);
            } else {
                throw e;
            }
        }
    }

    /**
     * Get the versions of FT4222H and LibFT4222.
     *
     * @throws FTDIException
     *             API call failed, see exception fields for details. More
     *             information can be found in AN_329.
     * @throws IllegalStateException
     *             Device must be opened before calling this method.
     * @see FTDIInterface#getVersion(long)
     * @since 2.1
     */
    public Version getVersion() throws FTDIException {
        if (!isOpen())
            throw new IllegalStateException("Device not open");
        return FTDIInterface.getVersion(getHandle());
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

    //TODO added by Peter
    /**
     * Function added for personal research reads out the message and
     * prints it out
     * @param buffer
     *              address of the target i2c slave
     * @param length
     *              max number of bytes to read from the device
     * @return
     * @throws FTDIException
     * @since 2.2
     */
    public int i2cMasterReadAndPrint(byte[] buffer, int length) throws FTDIException
    {
        int slaveAddr = 0x28;
        i2cMasterRead(slaveAddr, buffer, length);

        System.out.printf("I2C master read data from the slave(%#x)... \n", slaveAddr);
        System.out.print("  slave data: ");
        for (int i = 0; i < length; ++i) {
            System.out.printf("%#x, ", buffer[i]);
        }
        System.out.println();
        return 0;
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
     * @since 2.2
     */
    public int i2cMasterWrite(int slaveAddress, byte[] buffer) throws FTDIException {
        return FTDIInterface.i2cMasterWrite(getHandle(), slaveAddress, buffer,
                buffer.length);
    }

    //TODO added by Peter
    /**
     * Functions added for personal research, writes and
     * prints out the size of written message
     * @param bytes
     * @throws FTDIException
     * @since 2.1
     */
    public void i2cMasterWriteAndPrint(byte[] bytes) throws FTDIException
    {
        int slaveAddr = 0x28;
        int sizeTransferred = 0;

        System.out.printf("I2C master write data to the slave(%#x)... \n", slaveAddr);
        sizeTransferred = i2cMasterWrite(slaveAddr, bytes);
        System.out.printf("bytes written: %d\n", sizeTransferred);
    }

    /**
     * Set the system clock rate. The FT4222H supports 4 clock rates: 80MHz, 60MHz,
     * 48MHz, or 24MHz. By default, the FT4222H runs at 60MHz clock rate.
     *
     * @param rate
     *            FT4222 system clock rate
     * @throws FTDIException
     *            API call failed, see exception fields for details. More
     *            information can be found in AN_329.
     * @see FTDIInterface#setClock(long, long)
     * @since 2.1
     */
    public void setClock(ClockRate rate) throws FTDIException {
        FTDIInterface.setClock(getHandle(), rate.ordinal());
    }

    //TODO added by Peter
    /**
     * Initialize the GPIO interface of the FT4222H.
     * Please note the GPIO interface is available on the 2nd
     * USB interface in mode 0 or on the 4th USB interface in mode 1.
     *
     * @throws FTDIException
     *            API call failed, see exception fields for details. More
     *            information can be found in AN_329.
     * @since 2.2
     */
    public void gpioInit() throws FTDIException {
        FTDIInterface.gpioInit(getHandle());
    }

    //TODO added by Peter
    /**
     * Get the size of trigger event queue.
     *
     * Prerequisite:
     * FT4222_GPIO_Init
     *
     * @throws FTDIException
     *            API call failed, see exception fields for details. More
     *            information can be found in AN_329.
     * @since 2.2
     */
    public int gpioGetTriggerStatus() throws FTDIException{
        return FTDIInterface.gpioGetTriggerStatus(getHandle());
    }

    /**
     * Set trigger condition for the pin wakeup/interrupt. By default, the trigger
     * condition is GPIO_TRIGGER_RISING.
     * <p>
     * This function configures trigger condition for wakeup/interrupt.
     * <p>
     * When GPIO3 acts as wakeup pin. It means that ft4222H device has the
     * capability to wake up the host. Only GPIO_TRIGGER_RISING and
     * GPIO_TRIGGER_FALLING are valid when GPIO3 act as a wakeup pin. It is not
     * necessary to call FT4222_GPIO_Init to set up wake-up function.
     * <p>
     * When GPIO3 acts as interrupt pin. All trigger condition can be set. The
     * result of trigger status can be inquired by FT4222_GPIO_ReadTriggerQueue or
     * FT4222_GPIO_Read. This is because the trigger status is provided by the GPIO
     * pipe. Therefore it is necessary to call FT4222_GPIO_Init to set up interrupt
     * function.
     * <p>
     * For GPIO triggering conditions, GPIO_TRIGGER_LEVEL_HIGH and
     * GPIO_TRIGGER_LEVEL_LOW, that can be configured when GPIO3 behaves as an
     * interrupt pin, when the system enters suspend mode, these two configurations
     * will act as GPIO_TRIGGER_RISING and GPIO_FALLING respectively.
     *
     * @param trigger
     *            trigger condition
     * @throws FTDIException
     *            API call failed, see exception fields for details. More
     *            information can be found in AN_329.
     * @throws IllegalStateException
     *            Device must be opened before calling this method.
     * @see FTDIInterface#setInterruptTrigger(long, int)
     */
    public void setInterruptTrigger(GpioTrigger trigger) throws FTDIException {
        if (!isOpen())
            throw new IllegalStateException("Device not open");
        FTDIInterface.setInterruptTrigger(getHandle(), trigger.getValue());
    }

    /**
     * Enable or disable, suspend out, which will emit a signal when FT4222H enters
     * suspend mode. Please note that the suspend-out pin is not available under
     * mode 2. By default, suspend-out function is on.
     * <p>
     * When suspend-out function is on, suspend-out pin emits signal according to
     * suspend-out polarity. The default value of suspend-out polarity is active
     * high. It means suspend-out pin output low in normal mode and output high in
     * suspend mode. Suspend-out polarity only can be adjusted by FT_PROG.
     *
     * @param enable
     *            TRUE to enable suspend out and configure GPIO2 as an output pin
     *            for emitting a signal when suspended. FALSE to switch back to
     *            GPIO2.
     * @throws FTDIException
     *            API call failed, see exception fields for details. More
     *            information can be found in AN_329.
     * @throws IllegalStateException
     *            Device must be opened before calling this method.
     * @see FTDIInterface#setSuspendOut(long, boolean)
     * @since 2.1
     */
    public void setSuspendOut(boolean enable) throws FTDIException {
        if (!isOpen())
            throw new IllegalStateException("Device not open");
        FTDIInterface.setSuspendOut(getHandle(), enable);
    }

    /**
     * Enable or disable wakeup/interrupt. By default, wake-up/interrupt function is
     * on.
     * <p>
     * When Wake up/Interrupt function is on, GPIO3 pin acts as an input pin for
     * wakeup/interrupt.
     * <p>
     * While system is in normal mode, GPIO3 acts as an interrupt pin. While system
     * is in suspend mode, GPIO3 acts as a wakeup pin.
     *
     * @param enable
     *            TRUE to configure GPIO3 as an input pin for wakeup/interrupt.
     *            FALSE to switch back to GPIO3.
     * @throws FTDIException
     *            API call failed, see exception fields for details. More
     *            information can be found in AN_329.
     * @throws IllegalStateException
     *            Device must be opened before calling this method.
     * @see FTDIInterface#setWakeUpInterrupt(long, boolean)
     * @since 2.1
     */
    public void setWakeUpInterrupt(boolean enable) throws FTDIException {
        if (!isOpen())
            throw new IllegalStateException("Device not open");
        FTDIInterface.setWakeUpInterrupt(getHandle(), enable);
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
