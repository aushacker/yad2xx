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

import static net.sf.yad2xx.FTDIConstants.FT_FLAGS_HISPEED;
import static net.sf.yad2xx.FTDIConstants.FT_FLAGS_OPENED;

/**
 * Represents an individual FTDI device channel attached to the computers
 * USB ports.
 * <p>
 * Some FTDI devices, the FT2232H for example, support multiple
 * channels. Multi-channel devices will report one Device per channel.
 * When an FT2322H is connected the first Device is for the A channel,
 * the second Device is for the B channel.
 * <p>
 * Fields here are primarily sourced from the FT_GetDeviceInfoList call.
 * Differences to this are noted below.
 *
 * @author		Stephen Davies
 * @since		24 May 2012
 * @since		0.1
 */
public class Device {

	private int index;
	private int flags;
	private int type;
	private int id;
	private int locationId;

	/**
	 * Unique device identifier burned into the device. Value comes from
	 * either:
	 * <ol>
	 * <li>FT_GetDeviceInfoList (device is closed)</li>
	 * <li>FT_ListDevices (device is open)</li>
	 * </ol>
	 */
	private String serialNumber;

	/**
	 * Describes the device. Value comes from either:
	 * <ol>
	 * <li>FT_GetDeviceInfoList (device is closed)</li>
	 * <li>FT_ListDevices (device is open)</li>
	 * </ol>
	 */
    private String description;
    
    /**
     * Device handle used in C API. Leave this value alone. 
     */
	private long ftHandle;

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
	 */
	Device(int index, int flags, int type, int id, int locationId, String serialNumber, String description, long ftHandle) {
		this.index = index;
		this.flags = flags;
		this.type = type;
		this.id = id;
		this.locationId = locationId;
		this.serialNumber = serialNumber;
		this.description = description;
		this.ftHandle = ftHandle;
	}
	
	/**
	 * Close the device.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#close(Device)
	 * @since	0.1
	 */
	public void close() throws FTDIException {
		if (ftHandle != 0) {
			FTDIInterface.close(this);
		}
	}

	/**
	 * Send a reset command to the port.
	 * <p>
	 * Windows only.
	 * <p>
	 * This function is used to attempt to recover the port after a failure. It
	 * is not equivalent to an unplug-replug event.
	 *
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#cyclePort(long)
	 * @since	1.0
	 */
	public void cyclePort() throws FTDIException {
		FTDIInterface.cyclePort(ftHandle);
	}
	
	/**
	 * Erases the device EEPROM.
	 *
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#eraseEE(long)
	 * @since	0.2
	 */
	public void eraseEE() throws FTDIException {
		FTDIInterface.eraseEE(ftHandle);
	}
	
	/**
	 * Despite its name, GetBitMode samples the values of the data pins in 
	 * bit bang mode.
	 *
	 * @return					instantaneous value of the data bus
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#getBitMode(long)
	 * @since	0.2
	 */
	public byte getBitMode() throws FTDIException {
		return FTDIInterface.getBitMode(ftHandle);
	}

	/**
	 * Returns the Windows COM port associated with a device. Returns -1 if no
     * port is associated with the device.
     *
	 * @return					COM port number or -1
	 * @throws	FTDIException   D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#getComPortNumber(long)
	 * @since	1.0
	 */
	public long getComPortNumber() throws FTDIException {
		return FTDIInterface.getComPortNumber(ftHandle);
	}
	
	/**
	 * Return the device description.
	 *
	 * @return					device description 
	 * @since	0.1
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the D2XX driver version as Major.minor.build. NB. Device has to
	 * be opened before calling.
	 *
	 * @return					driver version string
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#getDriverVersionRaw(long)
	 * @since	0.3
	 */
	public String getDriverVersion() throws FTDIException {
		return FTDIInterface.formatVersion(FTDIInterface.getDriverVersionRaw(ftHandle));
	}

	/**
	 * Useful for debugging only.
	 * 
	 * @return					position within the array returned by
	 * 							{@link FTDIInterface#getDevices()}
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Get the current value of the latency timer.
	 * <p>
	 * In the FT8U232AM and FT8U245AM devices, the receive buffer timeout that
	 * is used to flush remaining data from the receive buffer was fixed at 16
	 * ms. In all other FTDI devices, this timeout is programmable and can be
	 * set at 1 ms intervals between 2ms and 255 ms. This allows the device to
	 * be better optimized for protocols requiring faster response times from
	 * short data packets.
	 * 
	 * @return					timeout value in ms
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#getLatencyTimer(long)
	 * @since	0.2
	 */
	public int getLatencyTimer() throws FTDIException {
		return (0xff & FTDIInterface.getLatencyTimer(ftHandle));
	}
	
	/**
	 * Gets the modem and line status from the device.
	 * 
	 * @return 					current modem and line status
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#getModemStatus(long)
	 * @since	0.3
	 */
	public ModemStatus getModemStatus() throws FTDIException {
		return new ModemStatus(FTDIInterface.getModemStatus(ftHandle));
	}

	/**
	 * Returns number of bytes in receive queue.
	 * 
	 * @return 					number of bytes in receive queue
	 * @throws	FTDIException 	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#getQueueStatus(long)
 	 * @since	0.1
	 */
	public int getQueueStatus() throws FTDIException {
		return FTDIInterface.getQueueStatus(ftHandle);
	}

	/**
	 * Return device serial number from FT_DEVICE_LIST_INFO_NODE.
	 *
	 * @return					device serial number
	 * @since	0.1
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Gets the device status including number of characters in the receive
	 * queue, number of characters in the transmit queue, and the current event
	 * status.
	 *
	 * @return 					a DeviceStatus containing status values
	 * @throws	FTDIException 	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#getStatus(long)
	 * @since	0.3
	 */
	public DeviceStatus getStatus() throws FTDIException {
		return FTDIInterface.getStatus(ftHandle);
	}
	
	/**
	 * Device type from FT_DEVICE_LIST_INFO_NODE. Mapped to Enum {@link 
	 * DeviceType}.
	 *
	 * @return					type
 	 * @since	0.1
	 */
	public DeviceType getType() {
		return DeviceType.values()[type];
	}

	/**
	 * Convenient way to test for USB HiSpeed capability.
	 *
	 * @return					USB high speed device
	 * @since	0.1
	 */
	public boolean isHighSpeed() {
		return (flags & FT_FLAGS_HISPEED) != 0;
	}

	/**
	 * Tests if device is open.
	 * <p>
	 * When using macOS there can be a conflict between the D2XX driver and the
	 * Apple supplied serial driver. A device being managed by the serial
	 * driver will be permanently open and cannot be accessed by the D2XX
	 * driver. You need to disable the Apple serial port driver first. 
	 *
	 * @return					device is open
	 * @since	0.1
	 */
	public boolean isOpen() {
		return (flags & FT_FLAGS_OPENED) != 0;
	}
	
	/**
	 * Begin a session with the device.
	 *  
	 * @throws	IllegalStateException	if device already open
	 * @throws	FTDIException			D2XX API call failed, see exception
	 * 									fields for details
	 * @see								FTDIInterface#open(Device)
	 * @since	0.1
	 */
	public void open() throws FTDIException {
		if (isOpen())
			throw new IllegalStateException("Device in use");
		FTDIInterface.open(this);
	}
	
	/**
	 * Purges receive and/or transmit buffers in the device.
	 *
	 * @param	mask			combination of FT_PURGE_RX and FT_PURGE_TX
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#purge(long, int)
	 * @since	1.0
	 */
	public void purge(int mask) throws FTDIException {
		FTDIInterface.purge(ftHandle, mask);
	}
	
	/**
	 * Reads data from device up to the size of the buffer. Note that this call
	 * will block if the requested number of bytes is not immediately 
	 * available. Call getQueueStatus to get the number of bytes actually
	 * available to avoid blocking. 
	 * 
	 * @param	buffer			bytes read from device. Buffer
	 * 							length determines maximum number of bytes read
	 * @return					number of bytes actually read
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#read(long, byte[], int)
	 * @since	0.1
	 */
	public int read(byte[] buffer) throws FTDIException {
		return FTDIInterface.read(ftHandle, buffer, buffer.length);
	}
	
	/**
	 * Read a 16-bit value from an EEPROM location.
	 * 
	 * @param	offset			EEPROM location to read from
	 * @return					WORD value read from the EEPROM
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#readEE(long, int)
	 * @since	0.2
	 */
	public int readEE(int offset) throws FTDIException {
		return FTDIInterface.readEE(ftHandle, offset);
	};
	
	/**
	 * Sends a reset command to the device.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#reset(long)
	 * @since	0.1
	 */
	public void reset() throws FTDIException {
		FTDIInterface.reset(ftHandle);
	}

	/**
	 * Send a reset command to the port.
	 * <p>
	 * Windows only.
	 * <p>
	 * This function is used to attempt to recover the port after a failure. It
	 * is not equivalent to an unplug-replug event.
	 *
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						Device#cyclePort()
	 * @see						FTDIInterface#resetPort(long)
	 * @since	1.0
	 */
	public void resetPort() throws FTDIException {
		FTDIInterface.resetPort(ftHandle);
	}
	
	/**
	 * Restarts the driver's IN task.
	 * <p>
	 * This function is used to restart the driver's IN task (read) after it
	 * has been stopped by a call to FT_StopInTask.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#restartInTask(long)
	 * @since	1.0
	 */
	public void restartInTask() throws FTDIException {
		FTDIInterface.restartInTask(ftHandle);
	}
	

	/**
	 * Sets the baud rate for the device.
	 * 
	 * @param	baudRate		baud rate
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setBaudRate(long, int)
	 * @since	0.1
	 */
	public void setBaudRate(int baudRate) throws FTDIException {
		FTDIInterface.setBaudRate(ftHandle, baudRate);
	}
	
	/**
	 * Enables different chip modes e.g. bit bang or MPSSE. Heavily dependent
	 * on the attached device capabilities.
	 *
	 * @param	pinDirection	sets up which bits are inputs and outputs.
	 * 							0 = input, 1 = output
	 * @param	bitMode			port mode
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setBitMode(long, byte, byte)
	 * @since	0.2
	 */
	public void setBitMode(byte pinDirection, FTDIBitMode bitMode) throws FTDIException {
		FTDIInterface.setBitMode(ftHandle, pinDirection, (byte)bitMode.getMode());
	}
	
	/**
	 * Sets or resets the device break condition. (Combines SetBreakOn and
	 * SetBreakOff).
	 *
	 * @param	breakCondition	true for on, false for off
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setBreakOff(long)
	 * @see						FTDIInterface#setBreakOn(long)
	 * @since	0.3
	 */
	public void setBreak(boolean breakCondition) throws FTDIException {
		if (breakCondition) {
			FTDIInterface.setBreakOn(ftHandle);
		} else {
			FTDIInterface.setBreakOff(ftHandle);
		}
	}
	
	/**
	 * Sets the special characters for the device.
	 *
	 * @param	event			event character
	 * @param	eventEnable		flag
	 * @param	error			error character
	 * @param	errorEnable		flag
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setChars(long, char, boolean,
	 * 								char, boolean)
	 * @since	0.2
	 */
	public void setChars(char event, boolean eventEnable, char error, boolean errorEnable) throws FTDIException {
		FTDIInterface.setChars(ftHandle, event, eventEnable, error, errorEnable);
	}
	
	/**
	 * Sets the data characteristics for the device.
	 * 
	 * @param	wordLength		number of bits per word, must be
	 * 							{@link FTDIConstants#FT_BITS_8} or
	 * 							{@link FTDIConstants#FT_BITS_7}
	 * @param	stopBits		number of stop bits, must be 
	 * 							{@link FTDIConstants#FT_STOP_BITS_1} or
	 * 							{@link FTDIConstants#FT_STOP_BITS_2}
	 * @param	parity			must be one of the parity values e.g.
	 * 							{@link FTDIConstants#FT_PARITY_NONE}
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setDataCharacteristics(long,
	 * 								byte, byte, byte)
	 * @since	0.3
	 */
	public void setDataCharacteristics(byte wordLength, byte stopBits, byte parity) throws FTDIException {
		FTDIInterface.setDataCharacteristics(ftHandle, wordLength, stopBits, parity);
	}
	
	/**
	 * Sets the maximum time in milliseconds that a USB request can remain
	 * outstanding.
	 * <p>
	 * The deadman timeout is referred to in application note AN232B-10
	 * Advanced Driver Options from the FTDI web site as the USB timeout. It is
	 * unlikely that this function will be required by most users.
	 * 
	 * @param	timeout			timeout value in milliseconds, default value is
	 * 							5000
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setDeadmanTimeout(long, long)
	 * @since	1.0
	 */
	public void setDeadmanTimeout(long timeout) throws FTDIException {
			FTDIInterface.setDeadmanTimeout(ftHandle, timeout);
	}
	
	/**
	 * Sets or clears the Data Terminal Ready (DTR) control signal.
	 * 
	 * @param	dtr				flag
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#clrDtr(long)
	 * @see						FTDIInterface#setDtr(long)
	 * @since	0.2
	 */
	public void setDtr(boolean dtr) throws FTDIException {
		if (dtr) {
			FTDIInterface.setDtr(ftHandle);
		} else {
			FTDIInterface.clrDtr(ftHandle);
		}
	}
	
	/**
	 * Sets the flow control for the device.
	 * 
	 * @param	flowControl		must be one of flow control values e.g.
	 *							{@link FTDIConstants#FT_FLOW_NONE}
	 * @param	xOn				character used to signal Xon. Only used if flow
	 * 							control is
	 * 							{@link FTDIConstants#FT_FLOW_XON_XOFF}
	 * @param	xOff			character used to signal Xoff. Only used if
	 * 							flow control is
	 * 							{@link FTDIConstants#FT_FLOW_XON_XOFF}
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setFlowControl(long, short, char,
	 * 								char)
	 * @since	0.3
	 */
	public void setFlowControl(short flowControl, char xOn, char xOff) throws FTDIException {
		FTDIInterface.setFlowControl(ftHandle, flowControl, xOn, xOff);
	}
	
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
	 * @param	timer			required value, in milliseconds, of latency
	 * 							timer. Valid range is 2 - 255
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setLatencyTimer(long, byte)
	 * @since	0.2
	 */
	public void setLatencyTimer(byte timer) throws FTDIException {
		FTDIInterface.setLatencyTimer(ftHandle, timer);
	}
	
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
	 * @param	count			required ResetPipeRetryCount
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setResetPipeRetryCount(long,
	 * 							long)
	 * @since	1.0
	 */
	public void setResetPipeRetryCount(long count) throws FTDIException {
		FTDIInterface.setResetPipeRetryCount(ftHandle, count);
	}
	
	/**
	 * Sets or clears the Request To Send (RTS) control signal.
	 * 
	 * @param	rts				flag
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#clrDtr(long)
	 * @see						FTDIInterface#setDtr(long)
	 * @since	0.2
	 */
	public void setRts(boolean rts) throws FTDIException {
		if (rts) {
			FTDIInterface.setRts(ftHandle);
		} else {
			FTDIInterface.clrRts(ftHandle);
		}
	}
	
	/**
	 * Sets the read and write timeouts for the device.
	 * 
	 * @param	readTimeout		read timeout in milliseconds.
	 * @param	writeTimeout	write timeout in milliseconds.
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setTimeouts(long, int, int)
	 * @since	0.1
	 */
	public void setTimeouts(int readTimeout, int writeTimeout) throws FTDIException {
		FTDIInterface.setTimeouts(ftHandle, readTimeout, writeTimeout);
	}
	
	/**
	 * Set the USB request transfer size.
	 * 
	 * @param	inTransferSize	transfer size for USB IN request
	 * @param	outTransferSize	transfer size for USB OUT request
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#setUSBParameters(long, int, int)
	 * @since	0.2
	 */
	public void setUSBParameters(int inTransferSize, int outTransferSize) throws FTDIException {
		FTDIInterface.setUSBParameters(ftHandle, inTransferSize, outTransferSize);
	}
	
	/**
	 * Stops the driver's IN task.
	 * <p>
	 * This function is used to put the driver's IN task (read) into a wait
	 * state. It can be used in situations where data is being received
	 * continuously, so that the device can be purged without more data being
	 * received. It is used together with FT_RestartInTask which sets the IN
	 * task running again.
	 * 
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#stopInTask(long)
	 * @since	1.0
	 */
	public void stopInTask() throws FTDIException {
		FTDIInterface.stopInTask(ftHandle);
	}

	/**
	 * Verbose debugging.
	 * 
	 * @since	0.1
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Dev ");
		result.append(index);
		result.append(":\n");
		result.append("\tFlags=");
		result.append(Integer.toHexString(flags));
		result.append(" (isOpen: ");
		result.append(isOpen());
		result.append(" isHighSpeed: ");
		result.append(isHighSpeed());
		result.append(")\n\tType=");
		result.append(Integer.toHexString(type));
		result.append(" (");
		result.append(getType());
		result.append(")\n\tID=");
		result.append(Integer.toHexString(id));
		result.append("\n\tlocID=");
		result.append(Integer.toHexString(locationId));
		result.append("\n\tSerialNumber=");
		result.append(serialNumber);
		result.append("\n\tDescription=");
		result.append(description);
		result.append("\n\tftHandle=");
		result.append(Long.toHexString(ftHandle));
		result.append('\n');
		return result.toString();
	}
	
	/**
	 * Writes a single byte to the device.
	 * 
	 * @param	data			value to write to device
	 * @return					number of bytes actually written
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#write(long, byte[], int)
	 * @since	0.3
	 */
	public int write(byte data) throws FTDIException {
		byte[] buffer = new byte[1];
		buffer[0] = data;
		return write(buffer, buffer.length);
	}
	
	/**
	 * Write data to the device.
	 * 
	 * @param	buffer			bytes to write to device
	 * @return					number of bytes actually written
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#write(long, byte[], int)
	 * @since	0.1
	 */
	public int write(byte[] buffer) throws FTDIException {
		return write(buffer, buffer.length);
	}
	
	/**
	 * Write data to the device.
	 * 
	 * @param	buffer			bytes to write to device
	 * @param	numBytesToWrite	like it says... allows buffer subset
	 * @return					number of bytes actually written
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#write(long, byte[], int)
	 * @since	0.1
	 */
	public int write(byte[] buffer, int numBytesToWrite) throws FTDIException {
		return FTDIInterface.write(ftHandle, buffer, numBytesToWrite);
	}

	/**
	 * Write a 16-bit value to an EEPROM location.
	 *
	 * @param	offset			EEPROM location to write to
	 * @param	value			WORD value to write to the EEPROM
	 * @throws	FTDIException	D2XX API call failed, see exception fields for
	 * 							details
	 * @see						FTDIInterface#writeEE(long, int, int)
	 * @since	0.2
	 */
	public void writeEE(int offset, int value) throws FTDIException {
		FTDIInterface.writeEE(ftHandle, offset, value);
	}

}
