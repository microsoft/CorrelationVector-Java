/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 */
package com.microsoft.correlationvector;

/**
 * The number of least significant bits to drop in DateTime.Ticks when computing
 * the counter for CV's Spin operation.
 */
public enum SpinCounterInterval {

    /**
     * The coarse interval drops the 24 least significant bits in DateTime.Ticks
     * resulting in a counter that increments every 1.67 seconds.
     */
    Coarse(24),
    /**
     * The fine interval drops the 16 least significant bits in DateTime.Ticks
     * resulting in a counter that increments every 6.5 milliseconds.
     */
    Fine(16);

    /**
     * The number of least significant bits to drop.
     */
    private final int ticksBitsToDrop;

    /**
     * SpinCounterInterval Constructor.
     *
     * @param tickBitsToDrop
     *            Number of least significant bits to drop.
     */
    private SpinCounterInterval(int tickBitsToDrop) {
        this.ticksBitsToDrop = tickBitsToDrop;
    }

    /**
     * Gets the number of least significant bits to drop.
     *
     * @return ticks bits to drop.
     */
    public int getTicksBitsToDrop() {
        return this.ticksBitsToDrop;
    }
}