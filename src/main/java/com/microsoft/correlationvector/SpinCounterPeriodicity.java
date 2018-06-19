/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 */
package com.microsoft.correlationvector;

/**
 * Counter for CV's Spin operation.
 */
public enum SpinCounterPeriodicity {

    /**
     * Do not store a counter as part of the spin value.
     */
    None(0),
    /**
     * The short periodicity stores the counter using 16 bits.
     */
    Short(16),
    /**
     * The medium periodicity stores the counter using 24 bits.
     */
    Medium(24),
    /**
     * The long periodicity stores the counter using 32 bits.
     */
    Long(32);

    /**
     * Counter periodicity.
     */
    private final int periodicity;

    /**
     * SpinCounterPeriodicity Constructor.
     *
     * @param periodicity
     *            Counter periodicity.
     */
    private SpinCounterPeriodicity(int periodicity) {
        this.periodicity = periodicity;
    }

    /**
     * Gets the counter periodicity.
     *
     * @return periodicity
     */
    public int getPeriodicity() {
        return this.periodicity;
    }
}