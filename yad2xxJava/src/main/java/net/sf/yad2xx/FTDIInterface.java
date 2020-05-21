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
package net.sf.yad2xx;

import net.sf.yad2xx.ft4222.Version;

/**
 * A Java Native Interface (JNI) wrapper that adapts the FTDI
 * D2XX library to a more OO based approach. This Singleton type
 * provides a very thin Java layer over the top of the native C
 * language code and is central to the libraries operation.
 * <p>
 * Start with FTDIInterface.getDevices() and work from there.
 * <p>
 * Some functions are intended to be callable directly, some are 
 * intended to be called indirectly i.e. via a 
 * {@link net.sf.yad2xx.Device}. In general,
 * directly callable functions are public. Methods intended to be called
 * indirectly (i.e. via Device) have default (package-private) visibility.
 * <p>
 * D2XX functions that are device independent, i.e. those that do not have
 * an FT_HANDLE argument, are implemented publically and should be invoked
 * statically via FTDIInterface.
 * <p>
 * D2XX functions that are device dependent, i.e. those that have an FT_HANDLE
 * argument, are declared here with package-private visibility. These methods
 * should be invoked using instance methods on the Device class.
 *
 * @author		Stephen Davies
 * @since		20 May 2012
 * @since		0.1
 */
public class FTDIInterface {

	/**
	 * Loads the native library on first class usage. Library location
	 * is JVM/platform dependent.
	 */
	static {
		System.loadLibrary("FTDIInterface");
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

	/**
	 * Returns an array of Devices which describe attached D2XX devices.
	 * Combines calls to FT_CreateDeviceInfoList and FT_GetDeviceInfoList.
	 * <p>
	 * Copies values returned from FT_GetDeviceInfoList into individual Device
	 * objects.
	 * 
	 * @return 					array of Devices describing attached D2XX
	 * 							devices
	 * @throws	FTDIException	FT_CreateDeviceInfoList returned a non-zero
	 * 							status code
	 * @since	0.1
	 */
	public static native Device[] getDevices() throws FTDIException;

	/**
	 * Returns the D2XX library version as M.m.p. A prettier way of calling
	 * FT_GetLibraryVersion.
	 *
	 * @return					library version string
	 * @throws	FTDIException	FT_GetLibraryVersion returned a non-zero status
	 * 							code
	 * @since	0.1
	 */
	public static String getLibraryVersion() throws FTDIException {
		return formatVersion(getLibraryVersionInt());
	}

	/**
	 * FT_GetLibraryVersion in its raw format.
	 * 
	 * @return					the D2XX DLL version number
	 * @throws	FTDIException	FT_GetLibraryVersion returned a non-zero status
	 *							code
	 * @since	0.1
	 */
	public static native int getLibraryVersionInt() throws FTDIException;

	/**
	 * A command to retrieve the current VID and PID combination from within
	 * the internal device list table. Returns a two integer array, the first
	 * element is the VID, the second the PID.
	 * 
	 * @return					VID and PID
	 * @throws	FTDIException	FT_GetVIDPID returned a non-zero status code
	 * @since	0.1
	 */
	public static int[] getVidPid() throws FTDIException {
		long raw = getVidPidRaw();
		int[] result = new int[2];
		result[0] = ((int)raw >> 16) & 0xFFFF;
		result[1] = ((int)raw) & 0xFFFF;
		return result;
	}

	/**
	 * Windows only. No-op on Linux and OS X.
	 * <p>
	 * This function can be of use when trying to recover devices
	 * programatically.
	 * <p>
	 * Calling rescan is equivalent to clicking the "Scan for hardware changes"
	 * button in the Device Manager. Only USB hardware is checked for new
	 * devices. All USB devices are scanned, not just FTDI devices.
	 * 
	 * @throws	FTDIException	FT_Rescan returned a non-zero status code
	 * @since	1.0
	 */
	public static native void rescan() throws FTDIException;
	
	/**
	 * Windows only. No-op on Linux and OS X.
	 * <p>
	 * Forces a reload of the driver for devices with a specific VID and PID
	 * combination.
	 * <p>
	 * Calling reload forces the operating system to unload and reload the
	 * driver for the specified device IDs. If the VID and PID parameters are
	 * null, the drivers for USB root hubs will be reloaded, causing all USB
	 * devices connected to reload their drivers. Please note that this
	 * function will not work correctly on 64-bit Windows when called from a
	 * 32-bit application.
	 * 
	 * @param	vid				vendor id (16 bits)
	 * @param	pid				product id (16 bits)
	 * @throws	FTDIException	FT_Reload returned a non-zero status code
	 * @since	1.0
	 */
	public static native void reload(int vid, int pid) throws FTDIException;

	/**
	 * A command to include a custom VID and PID combination within the
	 * internal device list table. This will allow the driver to load for the
	 * specified VID and PID combination.
	 * <p>
	 * Use this call on Linux and OS X when the target device has a non-FTDI
	 * VID/PID programed. Must be called before calling {@link #getDevices()}.
	 * <p>
	 * On Windows this performs a no-op.
	 * 
	 * @param	vid				vendor id (16 bits)
	 * @param	pid				product id (16 bits)
	 * @throws	FTDIException	FT_SetVIDPID returned a non-zero status code
	 * @since	0.1
	 */
	public static native void setVidPid(int vid, int pid) throws FTDIException;

    /**
     * Software reset for device. (FT4222 only)
     * <p>
     * This function is used to attempt to recover system after a failure.
     * It is a software reset for the device.
     *
     * @param   ftHandle        FT4222 device handle
     * @throws  FTDIException   FT4222_ChipReset returned a non-zero status code
     * @see                     FT4222Device#chipReset()
     * @since   2.1
     */
    static native void chipReset(long ftHandle) throws FTDIException;

    /**
     * Close the opened device. Calls FT_Close. ftHandle and flags will be 
     * reset at completion.
     *
     * @param   device          device to close
     * @throws  FTDIException   FT_Close returned a non-zero status code
     * @see                     Device#close()
     * @since   0.1
     */
    static native void close(Device device) throws FTDIException;

	/**
	 * Clears the Data Terminal Ready (DTR) control signal.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException   FT_ClrDtr returned a non-zero status code
	 * @see						Device#setDtr(boolean)
	 * @since	0.2
	 */
	static native void clrDtr(long ftHandle) throws FTDIException;
	
	/**
	 * Clears the Request To Send (RTS) control signal.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException   FT_ClrRts returned a non-zero status code
	 * @see						Device#setRts(boolean)
	 * @since	0.2
	 */
	static native void clrRts(long ftHandle) throws FTDIException;
	
	/**
	 * Sends a cycle command to the USB port.
	 * <p>
	 * Windows only. No-op on Linux and OS X.
	 * <p>
	 * The effect of this function is the same as disconnecting then
	 * reconnecting the device from USB.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException	FT_CyclePort returned a non-zero status code
	 * @see						Device#cyclePort()
	 * @since	1.0
	 */
	static native void cyclePort(long ftHandle) throws FTDIException;
	
	/**
	 * Erases the device EEPROM.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException   FT_EraseEE returned a non-zero status code
	 * @see						Device#eraseEE()
	 * @since	0.2
	 */
	static native void eraseEE(long ftHandle) throws FTDIException;

	/**
	 * Common formatting for driver and DLL version strings. Converts binary
	 * to more human readable M.m.p String format.
	 * 
	 * @param	version			binary driver/library version number
	 * @return					human readable M.m.p
	 * @since	0.1
	 */
	static String formatVersion(int version) {
		int major = (version & 0xff0000) >> 16;
		int minor = (version & 0xff00) >> 8;
		int patch = (version & 0xff);
		
		return "" + major + "." + minor + "." + patch;
	}

	/**
	 * Gets the instantaneous value of the data bus.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @return					current data bus value
	 * @throws	FTDIException   FT_GetBitMode returned a non-zero status code
	 * @see						Device#getBitMode()
	 * @since	0.2
	 */
	static native byte getBitMode(long ftHandle) throws FTDIException;
	
    /**
     * Get the current system clock rate (FT4222 only).
     *
     * @param   ftHandle        FT4222 device handle
     * @throws  FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @see FT4222Device#getClock()
     * @since 2.1
     */
    static native int getClock(long ftHandle) throws FTDIException;

	/**
	 * Returns the Windows COM port associated with a device. Returns -1 if no
     * port is associated with the device.
     *
	 * @param	ftHandle		D2XX device handle
	 * @return					COM port number or -1
	 * @throws	FTDIException   FT_GetComPortNumber returned a non-zero status
	 * 							code
	 * @see						Device#getComPortNumber()
	 * @since	1.0
	 */
	static native long getComPortNumber(long ftHandle) throws FTDIException;
	
	/**
	 * FT_GetDriverVersion in its raw format.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @return					the D2XX driver version number
	 * @throws	FTDIException	FT_GetDriverVersion returned a non-zero status
	 * 							code
	 * @see						Device#getDriverVersion()
	 * @since	0.3
	 */
	static native int getDriverVersionRaw(long ftHandle) throws FTDIException;

	/**
	 * Get the current value of the latency timer. In the FT8U232AM and
	 * FT8U245AM devices, the receive buffer timeout that is used to flush
	 * remaining data from the receive buffer was fixed at 16 ms. In all other
	 * FTDI devices, this timeout is programmable and can be set at 1 ms
	 * intervals between 2ms and  255 ms. This allows the device to be better
	 * optimized for protocols requiring faster response times from short data
	 * packets.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @return					timeout value in ms
	 * @throws	FTDIException	FT_GetLatencyTimer returned a non-zero status
	 * 							code
	 * @see						Device#getLatencyTimer()
	 * @since	0.2
	 */
	static native byte getLatencyTimer(long ftHandle) throws FTDIException;
	
    /**
     * This function returns the maximum packet size in a transaction (FT4222 only).
     *
     * @param ftHandle
     *            FT4222 device handle
     * @return maximum packet size
     * @throws FTDIException
     *             API call failed, see exception fields for details. More
     *             information can be found in AN_329.
     * @throws IllegalStateException
     *             Device must be opened before calling this method.
     * @see FT4222Device#getMaxTransferSize()
     * @since 2.1
     */
    static native int getMaxTransferSize(long ftHandle) throws FTDIException;

	/**
	 * Gets the modem and line status from the device.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @return					raw D2XX API modem and line status value
	 * @throws	FTDIException	FT_GetModemStatus returned a non-zero status
	 * 							code
	 * @see						Device#getModemStatus()
	 * @since	0.3
	 */
	static native int getModemStatus(long ftHandle) throws FTDIException;

	/**
	 * Returns the number of bytes available in the receive queue.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @return					number of bytes available in the receive queue
	 * @throws	FTDIException	FT_GetQueueStatus returned a non-zero status
	 * 							code
	 * @see						Device#getQueueStatus()
	 * @since	0.1
	 */
	static native int getQueueStatus(long ftHandle) throws FTDIException;
	
    /**
     * Gets the device status including number of characters in the receive
     * queue, number of characters in the transmit queue, and the current event
     * status.
     *
     * @param   ftHandle        D2XX device handle
     * @return                  device status
     * @throws  FTDIException   FT_GetStatus returned a non-zero status code
     * @see                     DeviceStatus
     * @see                     Device#getStatus()
     * @since   0.3
     */
    static native DeviceStatus getStatus(long ftHandle) throws FTDIException;
    
    /**
     * Get the versions of FT4222H and LibFT4222 (FT4222 only).
     *
     * @param ftHandle
     *            FT4222 device handle
     * @return device status
     * @throws  FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @see Version
     * @see FT422Device#getVersion()
     * @since 2.1
     */
    static native Version getVersion(long ftHandle) throws FTDIException;

	/**
	 * A command to retrieve the current VID and PID combination from within
	 * the internal device list table. Java prevents returning multiple values
	 * so VID and PID are encoded in the lower 32 bits of the return value.
	 * <p>
	 * Clients should prefer {@link #getVidPid()}.
	 * 
	 * @return					VID and PID
	 * @throws	FTDIException	FT_GetVIDPID returned a non-zero status code
	 * @since	0.1
	 */
	static native long getVidPidRaw() throws FTDIException;

	/**
	 * Initialize the FT4222H as an I2C master with the requested I2C speed.
	 *
	 * @param   ftHandle        FT4222 device handle
	 * @param   kbps            the speed of I2C transmission
	 * @throws  FTDIException   FT4222_I2CMaster_Init returned a non-zero
	 *                          status code
	 * @see                     FTDIDevice#i2cMasterInit(int)
	 * @since   2.1
	 */
	static native void i2cMasterInit(long ftHandle, int kbps) throws FTDIException;

    /**
     * Read data from the specified I2C slave device with START and STOP
     * conditions.
     *
     * @param   ftHandle        FT4222 device handle
     * @param   slaveAddress    address of the target i2c slave
     * @param   buffer          where to store the results
     * @param   bytesToRead     max number of bytes to read (may be less than
     *                          bufferLength)
     * @return                  number of bytes actually read
     * @throws  FTDIException   API call failed, see exception fields for
     *                          details
     * @see                     FT4222Device#i2cMasterRead(int, int)
     * @since   2.1
     */
    static native int i2cMasterRead(long ftHandle, int slaveAddress, byte[] buffer, int bytesToRead) throws FTDIException;

    /**
     * Write data to the specified I2C slave device with START and STOP
     * conditions.
     *
     * @param   ftHandle        FT4222 device handle
     * @param   slaveAddress    address of the target i2c slave
     * @param   buffer          data to be written to the device. Array length
     *                          implies number of bytes to write
     * @param   bytesToWrite    number of bytes to output
     * @return                  number of bytes actually transferred
     *                          (sizeTransferred).
     * @throws  FTDIException   API call failed, see exception fields for
     *                          details
     * @see                     FT4222Device#i2cMasterWrite(int, byte[])
     * @since   2.1
     */
    static native int i2cMasterWrite(long ftHandle, int slaveAddress, byte[] buffer, int bytesToWrite) throws FTDIException;

	/**
	 * Opens the device. D2XX handle is recorded in the device.
	 * 
	 * @param	device			device to open
	 * @throws	FTDIException	FT_Open returned a non-zero status code
	 * @see						Device#open()
	 * @since	0.1
	 */
	static native void open(Device device) throws FTDIException;
	
	/**
	 * Purges receive and/or transmit buffers in the device.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @param	mask			combination of FT_PURGE_RX and FT_PURGE_TX
	 * @throws	FTDIException	C API call failed, see exception fields for
	 * 							details
	 * @see						Device#purge(int)
	 * @since	1.0
	 */
	static native void purge(long ftHandle, int mask) throws FTDIException;
	
	/**
	 * Reads data from device up to the size of the buffer. This call will
	 * block if the requested number of bytes is not immediately available.
	 * Call {@link #getQueueStatus()} to get the number of bytes actually
	 * available to avoid blocking. 
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @param	buffer          where to store the results
	 * @param	bufferLength	number of bytes to read (may be less than
	 * 							bufferLength)
	 * @return					number of bytes actually read
	 * @throws	FTDIException	FT_Read returned a non-zero status code
	 * @see						Device#read(byte[])
	 * @since	0.1
	 */
	static native int read(long ftHandle, byte[] buffer, int bufferLength) throws FTDIException;
	
	/**
	 * Reads a 16-bit value from an EEPROM location.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @param	wordOffset		EEPROM location to read from
	 * @return					WORD value read from the EEPROM
	 * @throws	FTDIException	FT_ReadEE returned a non-zero status code
	 * @see						Device#readEE(int)
	 * @since	0.2
	 */
	static native int readEE(long ftHandle, int wordOffset) throws FTDIException;
	
	/**
	 * Send a reset command to the device.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException	FT_ResetDevice returned a non-zero status code
	 * @see						Device#reset()
	 * @since	0.1
	 */
	static native void reset(long ftHandle) throws FTDIException;
	
	/**
	 * Send a reset command to the port.
	 * <p>
	 * Windows only. No-op on Linux and OS X. 
	 * <p>
	 * This function is used to attempt to recover the port after a failure. It
	 * is not equivalent to an unplug-replug event.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException	FT_ResetPort returned a non-zero status code
	 * @see						Device#cyclePort()
	 * @see						Device#resetPort()
	 * @since	1.0
	 */
	static native void resetPort(long ftHandle) throws FTDIException;
	
	/**
	 * Restarts the driver's IN task.
	 * <p>
	 * This function is used to restart the driver's IN task (read) after it
	 * has been stopped by a call to FT_StopInTask.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException	FT_RestartInTask returned a non-zero status
	 * 							code
	 * @see						Device#restartInTask()
	 * @since	1.0
	 */
	static native void restartInTask(long ftHandle) throws FTDIException;

	/**
	 * Sets the baud rate for the device.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @param	baudRate		desired baud rate
	 * @throws	FTDIException	FT_SetBaudRate returned a non-zero status code
	 * @see						Device#setBaudRate()
	 * @since	0.1
	 */
	static native void setBaudRate(long ftHandle, int baudRate) throws FTDIException;
	
	/**
	 * Enables different chip modes e.g. bit bang or MPSSE. Heavily dependent
	 * on the attached device capabilities.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @param	pinDirection	which bits are inputs and outputs (1 = output)
	 * @param	mode			{@link FTDIBitMode}
	 * @throws	FTDIException	FT_SetBitMode returned a non-zero status code
	 * @see						Device#setBitMode(byte, FTDIBitMode)				
	 * @since	0.2
	 */
	static native void setBitMode(long ftHandle, byte pinDirection, byte mode) throws FTDIException;
	
	/**
	 * Resets the BREAK condition for the device.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException	FT_SetBreakOff returned a non-zero status code
	 * @see						Device#setBreak(boolean)
	 * @since	0.3
	 */
	static native void setBreakOff(long ftHandle) throws FTDIException;
	
	/**
	 * Sets the BREAK condition for the device.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException	FT_SetBreakOn returned a non-zero status code
	 * @see						Device#setBreak(boolean)
	 * @since	0.3
	 */
	static native void setBreakOn(long ftHandle) throws FTDIException;
	
	/**
	 * Sets the special characters for the device.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @param	event			event character
	 * @param	eventEnable		enable event character
	 * @param	error			error character
	 * @param	errorEnable		enable error character
	 * @throws	FTDIException	FT_SetChars returned a non-zero status code
	 * @see						Device#setChars(char, boolean, char, boolean)
	 * @since	0.2
	 */
	static native void setChars(long ftHandle, char event, boolean eventEnable, char error, boolean errorEnable) throws FTDIException;
	
    /**
     * Set the system clock rate (FT4222 only).
     *
     * @param   ftHandle        D2XX device handle
     * @param   rate            clock rate
     * @throws  FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @see FT4222Device#setClock(ClockRate)
     * @since 2.1
     */
    static native void setClock(long ftHandle, long rate) throws FTDIException;

	/**
	 * Sets the data characteristics for the device.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @param	wordLength		number of bits per word, must be
	 * 							{@link FTDIConstants#FT_BITS_8} or
	 * 							{@link FTDIConstants#FT_BITS_7}
	 * @param	stopBits		number of stop bits, must be 
	 * 							{@link FTDIConstants#FT_STOP_BITS_1} or
	 * 							{@link FTDIConstants#FT_STOP_BITS_2}
	 * @param	parity			must be one of the parity values e.g.
	 * 							{@link FTDIConstants#FT_PARITY_NONE}
	 * @throws	FTDIException	FT_SetDataCharacteristics returned a non-zero
	 * 							status code
	 * @see						Device#setDataCharacteristics(byte, byte, byte)
	 * @since	0.3
	 */
	static native void setDataCharacteristics(long ftHandle, byte wordLength, byte stopBits, byte parity) throws FTDIException;

	/**
	 * Sets the maximum time in milliseconds that a USB request can remain
	 * outstanding.
	 * <p>
	 * The deadman timeout is referred to in application note AN232B-10
	 * Advanced Driver Options from the FTDI web site as the USB timeout. It is
	 * unlikely that this function will be required by most users.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @param	timeout			timeout value in milliseconds, default value is
	 * 							5000
	 * @throws	FTDIException	FT_SetDeadmanTimeout returned a non-zero status
	 * 							code
	 * @see						Device#setDeadmanTimeout(long)
	 * @since	1.0
	 */
	static native void setDeadmanTimeout(long ftHandle, long timeout) throws FTDIException;
	
	/**
	 * Sets the Data Terminal Ready (DTR) control signal.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException	FT_SetDtr returned a non-zero status code
	 * @see						Device#setDtr(boolean)
	 * @since	0.2
	 */
	static native void setDtr(long ftHandle) throws FTDIException;
	
	/**
	 * Sets the flow control for the device.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @param	flowControl		must be one of flow control values e.g.
	 *							{@link FTDIConstants#FT_FLOW_NONE}
	 * @param	xOn				character used to signal Xon. Only used if flow
	 * 							control is
	 * 							{@link FTDIConstants#FT_FLOW_XON_XOFF}
	 * @param	xOff			character used to signal Xoff. Only used if flow
	 * 							control is
	 * 							{@link FTDIConstants#FT_FLOW_XON_XOFF}
	 * @throws	FTDIException	FT_SetFlowControl returned a non-zero status
	 * 							code
	 * @see						Device#setFlowControl(short, char, char)
	 * @since	0.3
	 */
	static native void setFlowControl(long ftHandle, short flowControl, char xOn, char xOff) throws FTDIException;
	
    /**
     * Set trigger condition for the pin wakeup/interrupt (FT4222 only).
     *
     * @param ftHandle
     *            D2XX device handle
     * @param trigger
     *            trigger condition
     * @throws FTDIException
     *             API call failed, see exception fields for details. More
     *             information can be found in AN_329.
     * @see FT4222Device#setInterruptTrigger(GpioTrigger)
     * @since 2.1
     */
    static native void setInterruptTrigger(long ftHandle, int trigger) throws FTDIException;

    /**
     * Set the latency timer value.
     * <p>
     * In the FT8U232AM and FT8U245AM devices, the receive buffer timeout that
     * is used to flush remaining data from the receive buffer was fixed at 16
     * ms. In all other FTDI devices, this timeout is programmable and can be
     * set at 1 ms intervals between 2ms and 255 ms. This allows the device to
     * be better optimized for protocols requiring faster response times from
     * short data packets.
     * 
     * @param   ftHandle        D2XX device handle
     * @param   timer           required value, in milliseconds, of latency
     *                          timer. Valid range is 2 - 255
     * @throws  FTDIException   FT_SetLatencyTimer returned a non-zero status
     *                          code
     * @see                     Device#setLatencyTimer(byte)
     * @since                   0.2
     */
    static native void setLatencyTimer(long ftHandle, byte timer) throws FTDIException;
    
	/**
	 * Sets the ResetPipeRetryCount value.
	 * <p>
	 * ResetPipeRetryCount controls the maximum number of times that the driver
	 * tries to reset a pipe on which an error has occurred.
	 * ResetPipeRequestRetryCount defaults to 50. It may be necessary to
	 * increase this value in noisy environments where a lot of USB errors
	 * occur.
	 * <p>
	 * Windows and Windows CE only. Linux and macOS perform a no-op.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @param	count			required ResetPipeRetryCount
	 * @throws	FTDIException	FT_SetResetPipeRetryCount returned a non-zero
	 * 							status code
	 * @see						Device#setResetPipeRetryCount(long)
	 * @since	1.0
	 */
	static native void setResetPipeRetryCount(long ftHandle, long count) throws FTDIException;
	
	/**
	 * Sets the Request To Send (RTS) control signal.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException	FT_SetRts returned a non-zero status code
	 * @see						Device#setRts(boolean)
	 * @since	0.2
	 */
	static native void setRts(long ftHandle) throws FTDIException;
	
    /**
     * Enable or disable, suspend out, which will emit a signal when FT4222H
     * enters suspend mode (FT4222 only).
     *
     * @param   ftHandle        D2XX device handle
     * @param   enable
     * @throws  FTDIException
     *             API call failed, see exception fields for details.
     *             More information can be found in AN_329.
     * @see FT4222Device#setSuspendOut(boolean)
     * @since 2.1
     */
    static native void setSuspendOut(long ftHandle, boolean enable) throws FTDIException;

	/**
	 * Sets the read and write timeouts for the device.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @param	readTimeout		read timeout in milliseconds
	 * @param	writeTimeout	write timeout in milliseconds
	 * @throws	FTDIException	FT_SetTimeouts returned a non-zero status code
	 * @see						Device#setTimeouts(int, int)
	 * @since	0.1
	 */
	static native void setTimeouts(long ftHandle, int readTimeout, int writeTimeout) throws FTDIException;
	
	/**
	 * Set the USB request transfer size.
	 * <p>
	 * This function can be used to change the transfer sizes from the default 
	 * transfer size of 4096 bytes to better suit the application requirements.
	 * Transfer sizes must be set to a multiple of 64 bytes between 64 bytes
	 * and 64k bytes. When FT_SetUSBParameters is called, the change comes into
	 * effect immediately and any data that was held in the driver at the time
	 * of the change is lost. Note that, at present, only dwInTransferSize is
	 * supported.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @param	inTransferSize 	transfer size for USB IN request
	 * @param	outTransferSize	transfer size for USB OUT request
	 * @throws	FTDIException	FT_SetUSBParameters returned a non-zero status
	 * 							code
	 * @see						Device#setUSBParameters(int, int)
	 * @since	0.2
	 */
	static native void setUSBParameters(long ftHandle, int inTransferSize, int outTransferSize) throws FTDIException;

    /**
     * Enable or disable wakeup/interrupt (FT4222 only).
     *
     * @param ftHandle
     *            D2XX device handle
     * @param enable
     * @throws FTDIException
     *            API call failed, see exception fields for details. More
     *            information can be found in AN_329.
     * @see FT4222Device#setWakeUpInterrupt(boolean)
     * @since 2.1
     */
    static native void setWakeUpInterrupt(long ftHandle, boolean enable) throws FTDIException;

	/**
	 * Stops the driver's IN task.
	 * <p>
	 * This function is used to put the driver's IN task (read) into a wait
	 * state. It can be used in situations where data is being received
	 * continuously, so that the device can be purged without more data being
	 * received. It is used together with FT_RestartInTask which sets the IN
	 * task running again.
	 * 
	 * @param	ftHandle		D2XX device handle
	 * @throws	FTDIException	FT_StopInTask returned a non-zero status code
	 * @see						Device#stopInTask()
	 * @since	1.0
	 */
	static native void stopInTask(long ftHandle) throws FTDIException;

	/**
	 * Release allocated resources.
	 *  
	 * @param   ftHandle                FT4222 device handle
	 * @throws  FTDIException           FT4222_UnInitialize returned a non-zero status code
	 * @see                             FTDIDevice#unInitialize()
	 * @since   2.1
	 */
	static native void unInitialize(long ftHandle) throws FTDIException;

	/**
	 * Write data to the device.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @param	buffer			bytes to write to device
	 * @param	numBytesToWrite number of bytes to transfer
	 * @return					number of bytes actually written
	 * @throws	FTDIException	FT_Write returned a non-zero status code
	 * @see						Device#write(byte)
	 * @see						Device#write(byte[])
	 * @see						Device#write(byte[], int)
	 * @since	0.1
	 */
	static native int write(long ftHandle, byte[] buffer, int numBytesToWrite) throws FTDIException;

	/**
	 * Write a 16-bit value to an EEPROM location.
	 *
	 * @param	ftHandle		D2XX device handle
	 * @param	wordOffset		EEPROM location to write to
	 * @param	value			WORD value to write to the EEPROM
	 * @throws	FTDIException	FT_WriteEE returned a non-zero status code
	 * @since	0.2
	 */
	static native void writeEE(long ftHandle, int wordOffset, int value) throws FTDIException;

}
