/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 */
package com.microsoft.correlationvector;

/**
 * DateTimeUtilties that helps with CV's Spin operation.
 */
public class DateTimeUtils {

    private static final int TICKS_IN_MILLISECONDS = 10000;

    /**
     * Gets the number of ticks since epoch time.
     *
     * @return number of ticks since epoch.
     */
    public static long getTicksSinceEpoch() {
        return System.currentTimeMillis() * TICKS_IN_MILLISECONDS;
    }
}
