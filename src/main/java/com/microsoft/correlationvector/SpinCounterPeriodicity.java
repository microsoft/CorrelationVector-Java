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

public enum SpinCounterPeriodicity
{
    /**
     * <summary>
     *   Do not store a counter as part of the spin value.
     * </summary>
     */
    None,

    /**
     * <summary>
     *   The short periodicity stores the counter using 16 bits.
     * </summary>
     */
    Short,

    /**
     * <summary>
     *   The medium periodicity stores the counter using 24 bits.
     * </summary>
     */
    Medium,

    /**
     * <summary>
     *  The long periodicity stores the counter using 32 bits.
     * </summary>
     */
    Long
}