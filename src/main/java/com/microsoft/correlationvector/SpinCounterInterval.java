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

public enum SpinCounterInterval
{
    /**
     * <summary>
     *   The coarse interval drops the 24 least significant bits in DateTime.Ticks
     *   resulting in a counter that increments every 1.67 seconds.
     * </summary>
     */
    Coarse,

    /**
     * <summary>
     *   The fine interval drops the 16 least significant bits in DateTime.Ticks
     *   resulting in a counter that increments every 6.5 milliseconds.
     * </summary>
     */
    Fine
}