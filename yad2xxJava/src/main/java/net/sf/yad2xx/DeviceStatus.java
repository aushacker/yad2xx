/*
 * Copyright 2012-18 Stephen Davies
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
 * Immutable result from the D2XX FT_GetStatus function.
 *
 * @author		Stephen Davies
 * @since		28 Dec 2014
 * @since		0.3
 */
public class DeviceStatus {
	
	// Implementation note. D2XX API is using the DWORD type for these values,
	// a 32 bit unsigned int. Using Java long guarantees no sign issues.
	
	/**
	 * Number of bytes in receive queue.
	 */
	private final long txBytes;
	
	/**
	 * Number of bytes in transmit queue.
	 */
	private final long rxBytes;

	/**
	 * Current event status.
	 */
	private final long eventStatus;

	/**
	 * Create device status snapshot.
	 *
	 * @param	rxBytes			number of bytes in receive queue
	 * @param	txBytes         number of bytes in transmit queue
	 * @param	eventStatus		current event status
	 */
	public DeviceStatus(long rxBytes, long txBytes, long eventStatus) {
		this.rxBytes = rxBytes;
		this.txBytes = txBytes;
		this.eventStatus = eventStatus;
	}

	public long getEventStatus() {
		return eventStatus;
	}

	public long getRxBytes() {
		return rxBytes;
	}
	
	public long getTxBytes() {
		return txBytes;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("DeviceStatus(rx: ");
		result.append(rxBytes);
		result.append(", tx: ");
		result.append(txBytes);
		result.append(", eventStatus: ");
		result.append(Long.toString(eventStatus, 16));
		result.append(")");
		return result.toString();
	}
}