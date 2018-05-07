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

public enum SpinEntropy
{
    /**
     * <summary>
     *   Do not generate entropy as part of the spin value.
     * </summary>
     */
    None,

    /**
     * <summary>
     *   Generate entropy using 8 bits.
     * </summary>
     */
    One,

    /**
     * <summary>
     *   Generate entropy using 16 bits.
     * </summary>
     */
    Two,

    /**
     * <summary>
     *   Generate entropy using 24 bits.
     * </summary>
     */
    Three,

    /**
     * <summary>
     *   Generate entropy using 32 bits.
     * </summary>
     */
    Four
}