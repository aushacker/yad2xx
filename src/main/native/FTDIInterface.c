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

/*
 * FTDIInterface.c is a Java Native Interface (JNI) binding to allow Java
 * program to access the C language functions provided by the FTDI D2XX
 * library.
 *
 * Stephen Davies
 * May 2012
 */
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

#if defined (_WIN32)
#include <windows.h>
#endif

#include "net_sf_yad2xx_FTDIInterface.h"
#include "ftd2xx.h"
#include "libft4222.h"

/*
 * Utility method to make it easier to handle failures.
 *
 * Creates an FTDIException, sets the status and function name.
 */
void ThrowFTDIException(JNIEnv * env, const jint ftStatus, const char * functionName) {

    // Lookup exception class
    jclass exceptionCls = (*env)->FindClass(env, "net/sf/yad2xx/FTDIException");
    if (exceptionCls == NULL) {
        return;  // Exception thrown
    }

    // Lookup FTStatus class
    jclass statusCls = (*env)->FindClass(env, "net/sf/yad2xx/FTStatus");
    if (statusCls == NULL) {
        return;  // Exception thrown
    }

    // Get the FTStatus matching the ftStatus value
    jmethodID byOrdId = (*env)->GetStaticMethodID(env, statusCls, "byOrdinal", "(I)Lnet/sf/yad2xx/FTStatus;");
    if (byOrdId == NULL) {
        return;  // Exception thrown
    }

    jobject status = (*env)->CallStaticObjectMethod(env, statusCls, byOrdId, ftStatus);
    if (status == NULL) {
        return;
    }
    
	// Get the constructor for FTDIException(FTDIStatus, String)
	jmethodID cid = (*env)->GetMethodID(env, exceptionCls, "<init>", "(Lnet/sf/yad2xx/FTStatus;Ljava/lang/String;)V");
	if (cid == NULL) {
		return;  // Exception thrown
	}

	// Convert C string to Java
	jstring jFuncName = (*env)->NewStringUTF(env, functionName);
	if (jFuncName == NULL) {
		return; // Exception thrown
	}

	// Create and throw the exception
	jthrowable theException = (*env)->NewObject(env, exceptionCls, cid, status, jFuncName);
	if (theException != NULL) {
		(*env)->Throw(env, theException);
	}

    (*env)->DeleteLocalRef(env, status);
    (*env)->DeleteLocalRef(env, statusCls);
    (*env)->DeleteLocalRef(env, exceptionCls);
	(*env)->DeleteLocalRef(env, jFuncName);
	(*env)->DeleteLocalRef(env, theException);
}


/*
 * Close an open device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    close
 * Signature: (Lnet/sf/yad2xx/Device;)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_close
  (JNIEnv * env, jclass clsIFace, jobject device)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	// Get Device.class
	jclass deviceCls = (*env)->GetObjectClass(env, device);
	if (deviceCls == NULL) {
		return; // Exception thrown
	}

	// get device field ftHandle
	jfieldID handleID = (*env)->GetFieldID(env, deviceCls, "ftHandle", "J");
	if (handleID == NULL) {
		return; // Exception thrown
	}
	ftHandle = (FT_HANDLE) (*env)->GetLongField(env, device, handleID);

	ftStatus = FT_Close(ftHandle);

	if (ftStatus == FT_OK) {

		// update device flags
		jint flags;
		jfieldID flagsID = (*env)->GetFieldID(env, deviceCls, "flags", "I");
		if (flagsID == NULL) {
			return; // Exception thrown
		}
		flags = (*env)->GetIntField(env, device, flagsID);
		flags &= ~(FT_FLAGS_OPENED);
		(*env)->SetIntField(env, device, flagsID, flags);

		// update device handle
		(*env)->SetLongField(env, device, handleID, 0);

	} else {
		ThrowFTDIException(env, ftStatus, "FT_Close");
	}
}


/*
 * This function clears the Data Terminal Ready (DTR) control signal.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    clrDtr
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_clrDtr
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_ClrDtr(ftHandle);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_ClrDtr");
		return;
	}
}


/*
 * This function clears the Request To Send (RTS) control signal.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    clrRts
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_clrRts
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_ClrRts(ftHandle);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_ClrRts");
		return;
	}
}


/*
 * Sends a cycle command to the USB port.
 *
 * Windows only.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    cyclePort
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_cyclePort
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
#if defined (_WIN32)

	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_CyclePort(ftHandle);

	if (ftStatus != FT_OK) {
		ThrowFTDIException(env, ftStatus, "FT_CyclePort");
	}

#else

	// Function is not defined on Linux or OS X platforms, no-op instead.
	return;

#endif
}


/*
 * Erases the device EEPROM.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    eraseEE
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_eraseEE
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_EraseEE(ftHandle);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_EraseEE");
		return;
	}
}


/*
 * Returns the instantaneous value of the data bus.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getBitMode
 * Signature: (J)B
 */
JNIEXPORT jbyte JNICALL Java_net_sf_yad2xx_FTDIInterface_getBitMode
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	UCHAR     BitMode;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_GetBitMode(ftHandle, &BitMode);

	if (ftStatus == FT_OK) {
		return (jbyte)BitMode;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_GetBitMode");
		return 0;
	}
}


/*
 * Returns the Windows COM port associated with a device. Returns -1 if no
 * port is associated with the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getComPortNumber
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_yad2xx_FTDIInterface_getComPortNumber
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	LONG      lPortNumber;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_GetComPortNumber(ftHandle, &lPortNumber);

	if (ftStatus == FT_OK) {
		return (jlong)lPortNumber;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_GetComPortNumber");
		return 0;
	}
}


/*
 * Returns the number of D2XX devices attached.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getDeviceCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_getDeviceCount
  (JNIEnv * env, jclass clsIFace)
{
	FT_STATUS ftStatus;
	DWORD dwNumDevs;

	ftStatus = FT_CreateDeviceInfoList(&dwNumDevs);
	if (ftStatus == FT_OK) {
		return dwNumDevs;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_CreateDeviceInfoList");
		return 0;
	}
}


/**
 * Combines FT_CreateDeviceInfoList and FT_GetDeviceInfoList.
 *
 * Copies values into individual Device objects.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getDevices
 * Signature: ()[Lnet/sf/yad2xx/Device;
 */
JNIEXPORT jobjectArray JNICALL Java_net_sf_yad2xx_FTDIInterface_getDevices
  (JNIEnv * env, jclass clsIFace)
{
	FT_STATUS ftStatus;
	DWORD dwNumDevs;
    char * serialNumber;
    char * description;
    char sBuff[64];
    char dBuff[64];
    
	// How many devices are attached?
	ftStatus = FT_CreateDeviceInfoList(&dwNumDevs);
	if (ftStatus != FT_OK) {
		ThrowFTDIException(env, ftStatus, "FT_CreateDeviceInfoList");
		return NULL;
	}

	// Lookup Device.class
	jclass deviceCls = (*env)->FindClass(env, "net/sf/yad2xx/Device");
	if (deviceCls == NULL) {
		return NULL;  // Exception thrown
	}

	// Lookup FT4222Device.class
	jclass ft4222deviceCls = (*env)->FindClass(env, "net/sf/yad2xx/FT4222Device");
	if (ft4222deviceCls == NULL) {
		return NULL;  // Exception thrown
	}

	// Allocate an array to hold the correct number of attached Devices
	jobjectArray devices = (*env)->NewObjectArray(env, dwNumDevs, deviceCls, NULL);
	if (devices == NULL) {
		return NULL;  // OutOfMemoryError thrown
	}

	if (dwNumDevs > 0) {
		// allocate storage for list based on numDevs
		FT_DEVICE_LIST_INFO_NODE * devInfo = (FT_DEVICE_LIST_INFO_NODE*) malloc(sizeof(FT_DEVICE_LIST_INFO_NODE) * dwNumDevs);

		// get the device information list
		ftStatus = FT_GetDeviceInfoList(devInfo, &dwNumDevs);
		if (ftStatus == FT_OK) {

			// Get the constructor for Device(int,int,int,int,int,String,String,long)
			jmethodID cid = (*env)->GetMethodID(env, deviceCls, "<init>", "(IIIIILjava/lang/String;Ljava/lang/String;J)V");
			if (cid == NULL) {
				return NULL;  // Exception thrown
			}

			// Get the constructor for FT4222Device(int,int,int,int,int,String,String,long)
			jmethodID ft4222cid = (*env)->GetMethodID(env, ft4222deviceCls, "<init>", "(IIIIILjava/lang/String;Ljava/lang/String;J)V");
			if (ft4222cid == NULL) {
				return NULL;  // Exception thrown
			}

			int64_t i;
			for (i = 0LL; i < dwNumDevs; i++) {
			
                if (devInfo[i].Flags & FT_FLAGS_OPENED) {
                    // Open devices have data missing in the DevInfoList structure,
                    // need to get these details from FT_ListDevices.
                    //
                    // NB. The first argument to FT_ListDevices is interpreted as either
                    // a pointer or an integer depending on the FLAGS used.
                    //
                    ftStatus = FT_ListDevices((PVOID)i, sBuff, FT_LIST_BY_INDEX | FT_OPEN_BY_SERIAL_NUMBER);
                    serialNumber = sBuff;
                    if (ftStatus != FT_OK) {
                        sBuff[0] = 0;
                    }
                    ftStatus = FT_ListDevices((PVOID)i, dBuff, FT_LIST_BY_INDEX | FT_OPEN_BY_DESCRIPTION);
                    description = dBuff;
                    if (ftStatus != FT_OK) {
                        dBuff[0] = 0;
                    }
                } else {
                	// closed device
                    serialNumber = devInfo[i].SerialNumber;
                    description = devInfo[i].Description;
                }
                
				// Convert C strings to Java
				jstring jSerial = (*env)->NewStringUTF(env, serialNumber);
				if (jSerial == NULL) {
					return NULL; // Exception thrown
				}
				jstring jDesc = (*env)->NewStringUTF(env, description);
				if (jDesc == NULL) {
					return NULL; // Exception thrown
				}

				// Construct either a Device or FT4222Device
				jobject device = NULL;
				if (devInfo[i].Type >= FT_DEVICE_4222H_0 && devInfo[i].Type <= FT_DEVICE_4222_PROG) {
					// new FT4222Device
                    device = (*env)->NewObject(env, ft4222deviceCls, ft4222cid, i, devInfo[i].Flags, devInfo[i].Type, devInfo[i].ID,
                                               devInfo[i].LocId, jSerial, jDesc, devInfo[i].ftHandle);
				} else {
					// new Device
                    device = (*env)->NewObject(env, deviceCls, cid, i, devInfo[i].Flags, devInfo[i].Type, devInfo[i].ID,
                                               devInfo[i].LocId, jSerial, jDesc, devInfo[i].ftHandle);
				}
				if (device == NULL) {
					return NULL; // Exception thrown
				}

				// insert into result array
				(*env)->SetObjectArrayElement(env, devices, i, device);

				(*env)->DeleteLocalRef(env, jSerial);
				(*env)->DeleteLocalRef(env, jDesc);
				(*env)->DeleteLocalRef(env, device);
			}

		}

		free(devInfo);
	}

	return devices;
}


/*
 * Returns the D2XX driver version number.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getDriverVersionRaw
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_getDriverVersionRaw
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	DWORD dwVersion;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_GetDriverVersion(ftHandle, &dwVersion);

	if (ftStatus == FT_OK) {
		return dwVersion;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_GetDriverVersion");
		return 0;
	}
}


/*
 * Get the current value of the latency timer.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getLatencyTimer
 * Signature: (J)B
 */
JNIEXPORT jbyte JNICALL Java_net_sf_yad2xx_FTDIInterface_getLatencyTimer
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	UCHAR LatencyTimer;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_GetLatencyTimer(ftHandle, &LatencyTimer);

	if (ftStatus == FT_OK) {
		return (jbyte)LatencyTimer;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_GetLatencyTimer");
		return 0;
	}
}


/*
 * Returns the D2XX DLL version number.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getLibraryVersionInt
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_getLibraryVersionInt
  (JNIEnv * env, jclass clsIFace)
{
	FT_STATUS ftStatus;
	DWORD dwVersion;

	ftStatus = FT_GetLibraryVersion(&dwVersion);
	if (ftStatus == FT_OK) {
		return dwVersion;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_GetLibraryVersion");
		return 0;
	}
}


/*
 * Gets the modem status and the line status from the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getModemStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_getModemStatus
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	DWORD     dwModemStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_GetModemStatus(ftHandle, &dwModemStatus);

	if (ftStatus == FT_OK) {
		return dwModemStatus;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_GetModemStatus");
		return 0;
	}
}


/*
 * Gets the number of bytes in the receive queue.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getQueueStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_getQueueStatus
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	DWORD     dwNumBytes;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_GetQueueStatus(ftHandle, &dwNumBytes);

	if (ftStatus == FT_OK) {
		return dwNumBytes;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_GetQueueStatus");
		return 0;
	}
}


/*
 * Gets the device status including number of characters in the receive queue,
 * number of characters in the transmit queue, and the current event status.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getStatus
 * Signature: (J)I
 */
JNIEXPORT jobject JNICALL Java_net_sf_yad2xx_FTDIInterface_getStatus
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	DWORD     rxCount, txCount, eventStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_GetStatus(ftHandle, &rxCount, &txCount, &eventStatus);

	if (ftStatus != FT_OK) {
		ThrowFTDIException(env, ftStatus, "FT_GetStatus");
		return 0;
	}

    // Lookup DeviceStatus class
    jclass statusCls = (*env)->FindClass(env, "net/sf/yad2xx/DeviceStatus");
    if (statusCls == NULL) {
        return 0;  // Exception thrown
    }

	// Get the constructor for DeviceStatus(long, long, long)
	jmethodID cid = (*env)->GetMethodID(env, statusCls, "<init>", "(JJJ)V");
	if (cid == NULL) {
		return 0;  // Exception thrown
	}

	jobject result = (*env)->NewObject(env, statusCls, cid, rxCount, txCount, eventStatus);

	(*env)->DeleteLocalRef(env, statusCls);

	return result;
}


/*
 * A command to retrieve the current VID and PID combination from within the
 * internal device list table. Java prevents returning multiple values so
 * VID and PID are encoded in the lower 32 bits of the return value.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    getVidPidRaw
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_net_sf_yad2xx_FTDIInterface_getVidPidRaw
  (JNIEnv * env, jclass clsIFace)
{
	FT_STATUS	ftStatus;
	DWORD		vid, pid;
	jlong		result = 0;

//
// As of D2XX driver v2.12.24 function FT_GetVIDPID is not available on
// the Windows platform.
//
#ifndef WIN32
	ftStatus = FT_GetVIDPID(&vid, &pid);
	if (ftStatus == FT_OK) {
		result = pid;
		result &= 0xFFFF;	// prevent sign extension
		result |= (vid << 16);
		result &= 0xFFFFFFFF; // ditto
		return result;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_GetVIDPID");
		return 0;
	}
#else
    return 0;
#endif

}


/*
 * Open the device and return a handle which will be used for subsequent accesses.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    open
 * Signature: (Lnet/sf/yad2xx/Device;)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_open
  (JNIEnv * env, jclass clsIFace, jobject device)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	DWORD dwDeviceIndex;

	jclass deviceCls = (*env)->GetObjectClass(env, device);
	if (deviceCls == NULL) {
		return; // Exception thrown
	}

	// get device index
	jfieldID indexID = (*env)->GetFieldID(env, deviceCls, "index", "I");
	if (indexID == NULL) {
		return; // Exception thrown
	}
	dwDeviceIndex = (*env)->GetIntField(env, device, indexID);

	ftStatus = FT_Open(dwDeviceIndex, &ftHandle);

	if (ftStatus == FT_OK) {

		// update device flags
		jint flags;
		jfieldID flagsID = (*env)->GetFieldID(env, deviceCls, "flags", "I");
		if (flagsID == NULL) {
			return; // Exception thrown
		}
		flags = (*env)->GetIntField(env, device, flagsID);
		flags |= FT_FLAGS_OPENED;
		(*env)->SetIntField(env, device, flagsID, flags);

		// update device handle
		jfieldID handleID = (*env)->GetFieldID(env, deviceCls, "ftHandle", "J");
		if (handleID == NULL) {
			return; // Exception thrown
		}
		(*env)->SetLongField(env, device, handleID, (jlong) ftHandle);

	} else {
		ThrowFTDIException(env, ftStatus, "FT_Open");
	}
}


/*
 * Purges receive and/or transmit buffers in the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    purge
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_purge
  (JNIEnv * env, jclass clsIFace, jlong handle, jint mask)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_Purge(ftHandle, (DWORD) mask);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_Purge");
		return;
	}
}


/*
 * Read data from the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    read
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_read
  (JNIEnv * env, jclass clsIFace, jlong handle, jbyteArray buffer, jint buffLength)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	DWORD     dwNumBytesToRead;
	DWORD     dwNumBytesRead;
	jbyte     inBuff[buffLength];

	ftHandle = (FT_HANDLE) handle;
	dwNumBytesToRead = buffLength;

	ftStatus = FT_Read(ftHandle, inBuff, dwNumBytesToRead, &dwNumBytesRead);

	if (ftStatus == FT_OK) {
		(*env)->SetByteArrayRegion(env, buffer, 0, (jsize) dwNumBytesRead, inBuff);
		return dwNumBytesRead;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_Read");
		return 0;
	}

}


/*
 * Read EEPROM data.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    readEE
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_readEE
  (JNIEnv * env, jclass clsIFace, jlong handle, jint offset)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	DWORD     dwWordOffset;
	WORD      wValue;

	ftHandle = (FT_HANDLE) handle;
	dwWordOffset = offset;

    ftStatus = FT_ReadEE(ftHandle, dwWordOffset, &wValue);

	if (ftStatus == FT_OK) {
		return wValue;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_ReadEE");
		return 0;
	}
}


/*
 * Windows only.
 *
 * This function can be of use when trying to recover devices programatically.
 *
 * Calling FT_Rescan is equivalent to clicking the "Scan for hardware changes"
 * button in the Device Manager. Only USB hardware is checked for new devices.
 * All USB devices are scanned, not just FTDI devices.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    rescan
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_rescan
  (JNIEnv * env, jclass clsIFace)
{
#if defined (_WIN32)

	FT_STATUS ftStatus;

	ftStatus = FT_Rescan();

	if (ftStatus != FT_OK) {
		ThrowFTDIException(env, ftStatus, "FT_Rescan");
	}

#else

	// Function is not defined on Linux or OS X platforms, no-op instead.
	return;

#endif
}


/*
 * Windows only.
 *
 * Forces a reload of the driver for devices with a specific VID and PID
 * combination.
 *
 * Calling FT_Reload forces the operating system to unload and reload the
 * driver for the specified device IDs. If the VID and PID parameters are null,
 * the drivers for USB root hubs will be reloaded, causing all USB devices
 * connected to reload their drivers. Please note that this function will not
 * work correctly on 64-bit Windows when called from a 32-bit application.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    reload
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_reload
  (JNIEnv * env, jclass clsIFace, jint vid, jint pid)
{
#if defined (_WIN32)

	FT_STATUS ftStatus;

	ftStatus = FT_Reload((WORD) vid, (WORD) pid);

	if (ftStatus != FT_OK) {
		ThrowFTDIException(env, ftStatus, "FT_Reload");
	}

#else

	// Function is not defined on Linux or OS X platforms, no-op instead.
	return;

#endif
}


/*
 * Sends a reset command to the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    reset
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_reset
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_ResetDevice(ftHandle);

	if (ftStatus != FT_OK) {
		ThrowFTDIException(env, ftStatus, "FT_ResetDevice");
	}
}


/*
 * Sends a reset command to the port.
 *
 * Windows only.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    resetPort
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_resetPort
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
#if defined (_WIN32)

	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_ResetPort(ftHandle);

	if (ftStatus != FT_OK) {
		ThrowFTDIException(env, ftStatus, "FT_ResetPort");
	}

#else

	// Function is not defined on Linux or OS X platforms, no-op instead.
	return;

#endif
}


/*
 * Restarts the driver's IN task.
 *
 * This function is used to restart the driver's IN task (read) after it has
 * been stopped by a call to FT_StopInTask.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    restartInTask
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_restartInTask
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_RestartInTask(ftHandle);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_RestartInTask");
		return;
	}
}


/*
 * Sets the baud rate for the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setBaudRate
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setBaudRate
  (JNIEnv * env, jclass clsIFace, jlong handle, jint baudRate)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetBaudRate(ftHandle, (DWORD) baudRate);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetBaudRate");
		return;
	}
}


/*
 * Enables different chip modes.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setBitMode
 * Signature: (JBB)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setBitMode
  (JNIEnv * env, jclass clsIFace, jlong handle, jbyte mask, jbyte mode)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetBitMode(ftHandle, mask, mode);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetBitMode");
		return;
	}
}


/*
 * Resets the BREAK condition of the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setBreakOff
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setBreakOff
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetBreakOff(ftHandle);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetBreakOff");
		return;
	}
}


/*
 * Sets the BREAK condition of the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setBreakOn
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setBreakOn
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetBreakOn(ftHandle);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetBreakOn");
		return;
	}
}


/*
 * Set special characters for the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setChars
 * Signature: (JCZCZ)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setChars
  (JNIEnv * env, jclass clsIFace, jlong handle, jchar event, jboolean eventEnable, jchar error, jboolean errorEnable)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetChars(ftHandle,
			event,
			eventEnable ? 1 : 0,
			error,
			errorEnable ? 1 : 0);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetChars");
		return;
	}
}


/*
 * Set data characteristics for the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setDataCharacteristics
 * Signature: (JBBB)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setDataCharacteristics
  (JNIEnv * env, jclass clsIFace, jlong handle, jbyte wordLength, jbyte stopBits, jbyte parity)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetDataCharacteristics(ftHandle,
			(UCHAR) wordLength,
			(UCHAR) stopBits,
			(UCHAR) parity);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetDataCharacteristics");
		return;
	}
}


/*
 * Sets the maximum time in milliseconds that a USB request can remain
 * outstanding.
 *
 * The deadman timeout is referred to in application note AN232B-10 Advanced
 * Driver Options from the FTDI web site as the USB timeout. It is unlikely
 * that this function will be required by most users.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setDeadmanTimeout
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setDeadmanTimeout
  (JNIEnv * env, jclass clsIFace, jlong handle, jlong timeout)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetDeadmanTimeout(ftHandle, (DWORD) timeout);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetDeadmanTimeout");
		return;
	}
}


/*
 * Sets the Data Terminal Ready (DTR) control signal.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setDtr
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setDtr
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetDtr(ftHandle);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetDtr");
		return;
	}
}


/*
 * Sets flow control for the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setFlowControl
 * Signature: (JSCC)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setFlowControl
  (JNIEnv * env, jclass clsIFace, jlong handle, jshort flowControl, jchar xOn, jchar xOff)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetFlowControl(ftHandle,
			(USHORT) flowControl,
			(UCHAR) xOn,
			(UCHAR) xOff);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetFlowControl");
		return;
	}
}


/*
 * Set the latency timer value.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setLatencyTimer
 * Signature: (JB)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setLatencyTimer
  (JNIEnv * env, jclass clsIFace, jlong handle, jbyte timer)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetLatencyTimer(ftHandle, (UCHAR) timer);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetLatencyTimer");
		return;
	}
}


/*
 * Sets the ResetPipeRetryCount value.
 *
 * ResetPipeRetryCount controls the maximum number of times that the driver
 * tries to reset a pipe on which an error has occurred.
 * ResetPipeRequestRetryCount defaults to 50. It may be necessary to increase
 * this value in noisy environments where a lot of USB errors occur.
 *
 * Windows and Windows CE only.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setResetPipeRetryCount
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setResetPipeRetryCount
  (JNIEnv * env, jclass clsIFace, jlong handle, jlong count)
{
#if defined (_WIN32)

	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetResetPipeRetryCount(ftHandle, (DWORD) count);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetResetPipeRetryCount");
		return;
	}

#else

	// Function is not defined on Linux or OS X platforms, no-op instead.
	return;

#endif
}


/*
 * Sets the Request To Send (RTS) control signal.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setRts
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setRts
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetRts(ftHandle);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetRts");
		return;
	}
}


/*
 * This function sets the read and write timeouts for the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setTimeouts
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setTimeouts
  (JNIEnv * env, jclass clsIFace, jlong handle, jint readTimeout, jint writeTimeout)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetTimeouts(ftHandle, (DWORD) readTimeout, (DWORD) writeTimeout);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetTimeouts");
		return;
	}
}


/*
 * Set the USB request transfer size.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setUSBParameters
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setUSBParameters
  (JNIEnv * env, jclass clsIFace, jlong handle, jint inTransferSize, jint outTransferSize)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_SetUSBParameters(ftHandle, (DWORD) inTransferSize, (DWORD) outTransferSize);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetUSBParameters");
		return;
	}
}


/*
 * A command to include a custom VID and PID combination within the internal device list table.
 * This will allow the driver to load for the specified VID and PID combination.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    setVidPid
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_setVidPid
  (JNIEnv * env, jclass clsIFace, jint vid, jint pid)
{
#if defined (_WIN32)

	// Function is not defined on Windows platform, no-op instead.

	return;

#else

	FT_STATUS ftStatus;

	ftStatus = FT_SetVIDPID(vid, pid);
	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_SetVIDPID");
		return;
	}

#endif
}


/*
 * Stops the driver's IN task.
 *
 * This function is used to put the driver's IN task (read) into a wait state.
 * It can be used in situations where data is being received continuously, so
 * that the device can be purged without more data being received. It is used
 * together with FT_RestartInTask which sets the IN task running again.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    stopInTask
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_stopInTask
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT_StopInTask(ftHandle);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_StopInTask");
		return;
	}
}


/*
 * Write data to the device.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    write
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_write
  (JNIEnv * env, jclass clsIFace, jlong handle, jbyteArray buffer, jint buffLength)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;
	DWORD     dwByteCount;
	DWORD     dwBytesWritten;
	jbyte     writeBuffer[buffLength];

	dwByteCount = (DWORD) buffLength;
	ftHandle = (FT_HANDLE) handle;
	(*env)->GetByteArrayRegion(env, buffer, 0, dwByteCount, writeBuffer);

	ftStatus = FT_Write(ftHandle, writeBuffer, (DWORD) buffLength, &dwBytesWritten);

	if (ftStatus == FT_OK) {
		return (jint) dwBytesWritten;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_Write");
		return 0;
	}

}


/*
 * Write a value to an EEPROM location.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    writeEE
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_writeEE
  (JNIEnv * env, jclass clsIFace, jlong handle, jint offset, jint value)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;

	ftStatus = FT_WriteEE(ftHandle, offset, value);

	if (ftStatus == FT_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT_Write");
		return;
	}

}

//
// *************** LibFT4222 Functions ***************************************
//

/*
 * Perform chip software reset.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    chipReset
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_chipReset
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT4222_ChipReset(ftHandle);

	if (ftStatus == FT4222_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT4222_ChipReset");
		return;
	}
}


/*
 * Initialize the FT4222H as an I2C master with the requested I2C speed.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    i2cMasterInit
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_i2cMasterInit
  (JNIEnv * env, jclass clsIFace, jlong handle, jint kbps)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT4222_I2CMaster_Init(ftHandle, kbps);

	if (ftStatus == FT4222_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT4222_I2CMaster_Init");
		return;
	}
}


/*
 * Read data from the specified I2C slave device with START and STOP
 * conditions.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    i2cMasterRead
 * Signature: (JI[BI)I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_i2cMasterRead
  (JNIEnv * env, jclass clsIFace, jlong handle, jint slaveAddress, jbyteArray buffer, jint bytesToRead)
{
    FT_HANDLE ftHandle;
    FT_STATUS ftStatus;
    uint16_t  sizeTransferred;
	jbyte     readBuffer[bytesToRead];

    ftHandle = (FT_HANDLE) handle;
    ftStatus = FT4222_I2CMaster_Read(ftHandle, slaveAddress, (uint8_t *) readBuffer, bytesToRead, &sizeTransferred);

    if (ftStatus == FT4222_OK) {
		(*env)->SetByteArrayRegion(env, buffer, 0, (jsize) sizeTransferred, readBuffer);
        return sizeTransferred;
    } else {
        ThrowFTDIException(env, ftStatus, "FT4222_I2CMaster_Read");
        return 0;
	}
}


/*
 * Write data to the specified I2C slave device with START and STOP
 * conditions.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    i2cMasterWrite
 * Signature: (JI[BI)I
 */
JNIEXPORT jint JNICALL Java_net_sf_yad2xx_FTDIInterface_i2cMasterWrite
  (JNIEnv * env, jclass clsIFace, jlong handle, jint slaveAddress, jbyteArray buffer, jint bytesToWrite)
{
    FT_HANDLE ftHandle;
    FT_STATUS ftStatus;
    uint16_t  sizeTransferred;
	jbyte     writeBuffer[bytesToWrite];

	(*env)->GetByteArrayRegion(env, buffer, 0, bytesToWrite, writeBuffer);

    ftHandle = (FT_HANDLE) handle;
    ftStatus = FT4222_I2CMaster_Write(ftHandle, slaveAddress, (uint8_t *) writeBuffer, bytesToWrite, &sizeTransferred);

    if (ftStatus == FT4222_OK) {
        return sizeTransferred;
    } else {
        ThrowFTDIException(env, ftStatus, "FT4222_I2CMaster_Write");
        return 0;
	}
}


/*
 * Release allocated resources.
 *
 * Class:     net_sf_yad2xx_FTDIInterface
 * Method:    unInitialize
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_FTDIInterface_unInitialize
  (JNIEnv * env, jclass clsIFace, jlong handle)
{
	FT_HANDLE ftHandle;
	FT_STATUS ftStatus;

	ftHandle = (FT_HANDLE) handle;
	ftStatus = FT4222_UnInitialize(ftHandle);

	if (ftStatus == FT4222_OK) {
		return;
	} else {
		ThrowFTDIException(env, ftStatus, "FT4222_UnInitialize");
		return;
	}
}

