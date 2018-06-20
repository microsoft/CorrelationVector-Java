/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 */
package com.microsoft.correlationvector;

/**
 * Configuration parameters used by CV's Spin operation.
 */
public class SpinParameters {

    /**
     * Number of bits in a byte.
     */
    public static final int BITS_IN_BYTE = 8;

    private SpinEntropy entropy;
    private SpinCounterInterval interval;
    private SpinCounterPeriodicity periodicity;

    /**
     * Gets a default set of configuration parameters for Spin operation.
     *
     * @return Default SpinParameters.
     */
    public static SpinParameters getDefaultParameters() {
        final SpinParameters params = new SpinParameters();
        params.setInterval(SpinCounterInterval.Coarse);
        params.setPeriodicity(SpinCounterPeriodicity.Short);
        params.setEntropy(SpinEntropy.Two);
        return params;
    }

    /**
     * Gets the number of bytes to use for entropy. Valid values from a minimum of 0
     * to a maximum of 4.
     */
    public SpinEntropy getEntropy() {
        return this.entropy;
    }

    /**
     * Gets the number of bytes used to store the entropy.
     *
     * @return entropyBytes.
     */
    public int getEntropyBytes() {
        return this.entropy.getEntropyBytes();
    }

    /**
     * The interval (proportional to time) by which the counter increments.
     *
     * @return Counter Interval.
     */
    public SpinCounterInterval getInterval() {
        return this.interval;
    }

    /**
     * Gets how frequently the counter wraps around to zero, as determined by the
     * amount of space to store the counter.
     *
     * @return counter periodicity.
     */
    public SpinCounterPeriodicity getPeriodicity() {
        return this.periodicity;
    }

    /**
     * Gets the number of least significant bits to drop in DateTime.Ticks when
     * computing the counter.
     *
     * @return number of ticksBits to drop.
     */
    public int getTicksBitsToDrop() {
        return this.interval.getTicksBitsToDrop();
    }

    /**
     * Gets the total number of bits to keep for the Spin operation.
     *
     * @return Total number of bits.
     */
    public int getTotalBits() {
        return this.periodicity.getPeriodicity() + (this.entropy.getEntropyBytes() * BITS_IN_BYTE);
    }

    /**
     * Sets the entropy.
     *
     * @param value
     *            entropy value.
     */
    public void setEntropy(SpinEntropy value) {
        this.entropy = value;
    }

    /**
     * Sets the SpinCounterInterval with given value.
     *
     * @param value
     *            - SpinCounterInterval.
     */
    public void setInterval(SpinCounterInterval value) {
        this.interval = value;
    }

    /**
     * Sets the counter periodicity with the given value.
     *
     * @param value
     *            - SpinCounterPeriodicity.
     */
    public void setPeriodicity(SpinCounterPeriodicity value) {
        this.periodicity = value;
    }
}
