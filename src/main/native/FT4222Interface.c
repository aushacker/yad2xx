/*
 * Copyright 2012-2020 Stephen Davies
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
 * FT4222Interface.c is a Java Native Interface (JNI) binding to allow Java
 * program to access the C language functions provided by the FTDI LibFT4222
 * library.
 *
 * Stephen Davies
 * May 2020
 */
#include <stdio.h>
#include <stdlib.h>

#if defined (_WIN32)
#include <windows.h>
#endif

#include "net_sf_yad2xx_ft4222_FT4222Interface.h"
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
 * Initialize the FT4222H as an I2C master with the requested I2C speed.
 *
 * Class:     net_sf_yad2xx_ft4222_FT4222Interface
 * Method:    i2cMasterInit
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_ft4222_FT4222Interface_i2cMasterInit
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
 * Release allocated resources.
 *
 * Class:     net_sf_yad2xx_ft4222_FT4222Interface
 * Method:    unInitialize
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_yad2xx_ft4222_FT4222Interface_unInitialize
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

