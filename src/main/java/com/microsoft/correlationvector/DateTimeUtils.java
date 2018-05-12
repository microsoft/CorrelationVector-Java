/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 *
 * @author  Ayushi Batra
 * @version 1.0
 * @since   05-06-2018
 */
package com.microsoft.correlationvector;


public class DateTimeUtils {

    private static final int TicksInMilliseconds = 10000;

    public static long getTicksSinceEpoch() {
        return System.currentTimeMillis() * TicksInMilliseconds;
    }
}
