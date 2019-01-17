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

/**
 * FTDI device types. Taken from the C enum FT_DEVICE in ftd2xx.h, version
 * 1.4.4.
 * <p>
 * Basically a bunch of hardcoded values to indicate the functions supported
 * by individual device types.
 * 
 * @author		Stephen Davies
 * @since		25 May 2012
 * @since		0.1
 */
public enum DeviceType {
    FT_DEVICE_232BM(0, false, false, false),
    FT_DEVICE_232AM(1, false, false, false),
    FT_DEVICE_100AX(2, false, false, false),
    FT_DEVICE_UNKNOWN(3, false, false, false),
	FT_DEVICE_2232C(4, true, false, false),
	FT_DEVICE_232R(5, false, false, false),
	FT_DEVICE_2232H(6, true, true, false),
	FT_DEVICE_4232H(7, true, true, false),
	FT_DEVICE_232H(8, true, true, true),
	FT_DEVICE_X_SERIES(9, false, false, false), // TODO - recheck the functions against the datasheet 
	FT_DEVICE_4222H_0(10, false, false, false), // TODO - recheck the functions against the datasheet
	FT_DEVICE_4222H_1_2(11, false, false, false), // TODO - recheck the functions against the datasheet
	FT_DEVICE_4222H_3(12, false, false, false), // TODO - recheck the functions against the datasheet
    FT_DEVICE_4222_PROG(13, false, false, false); // TODO - recheck the functions against the datasheet

	/**
	 * Matches C enum value.
	 */
    private final int type;
    
    /**
     * FT2232D, FT232H, FT2232H and FT4232H have a Multi Purpose 
     * Synchronous Serial Engine (MPSSE) mode.
     */
    private final boolean mpsseEnabled;
    
    /**
     * FT232H, FT2232H and FT4232H support additional MPSSE commands.
     */
    private final boolean mpsseEnhanced;
    
    /**
     * FT232H has open drain capable outputs.
     */
    private final boolean openDrainOutputs;
    
    private DeviceType(int type, boolean mpsseEnabled, boolean mpsseEnhanced, boolean openDrainOutputs) {
    	this.type = type;
    	this.mpsseEnabled = mpsseEnabled;
    	this.mpsseEnhanced = mpsseEnhanced;
    	this.openDrainOutputs = openDrainOutputs;
    }

    public boolean hasOpenDrainOutputs() {
    	return openDrainOutputs;
    }
    
    public boolean isMpsseEnabled() {
    	return mpsseEnabled;
    }

    public boolean isMpsseEnhanced() {
    	return mpsseEnhanced;
    }

    /**
     * Return C enum value.
     *
     * @return					FTDI defined type value as specified in
     * 							ftd2xx.h
     */
    public int getType() {
    	return type;
    }
}
