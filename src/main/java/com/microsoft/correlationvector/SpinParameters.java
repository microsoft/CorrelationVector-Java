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

public class SpinParameters {

    // Internal value for entropy bytes.
    private int entropyBytes;
    SpinCounterInterval interval;
    SpinCounterPeriodicity periodicity;

    public static SpinParameters getDefaultParameters() {
        SpinParameters params = new SpinParameters();
        params.setInterval(SpinCounterInterval.Coarse);
        params.setPeriodicity(SpinCounterPeriodicity.Short);
        params.setEntropy(SpinEntropy.Two);
        return params;
    }

    /**
     * <summary>
     *   The interval (proportional to time) by which the counter increments.
     * </summary>
     */
    public SpinCounterInterval getInterval(){
        return interval;
    }

    public void setInterval(SpinCounterInterval value){
        interval = value;
    }

    /**
     * <summary>
     *   How frequently the counter wraps around to zero, as determined by the amount
     *   of space to store the counter.
     * </summary>
     */
    public SpinCounterPeriodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(SpinCounterPeriodicity value) {
        periodicity = value;
    }

    /**
     * <summary>
     *   The number of bytes to use for entropy. Valid values from a
     *   minimum of 0 to a maximum of 4.
     * </summary>
     */
    public SpinEntropy getEntropy()
    {
        switch (this.entropyBytes) {
            case 0: return SpinEntropy.None;
            case 1: return SpinEntropy.One;
            case 2: return SpinEntropy.Two;
            case 3: return SpinEntropy.Three;
            case 4: return SpinEntropy.Four;
        }

        return SpinEntropy.None;
    }

    public void setEntropy(SpinEntropy value) {
        switch (value){
            case None:
                this.entropyBytes = 0;
                break;
            case One:
                this.entropyBytes = 1;
                break;
            case Two:
                this.entropyBytes = 2;
                break;
            case Three:
                this.entropyBytes = 3;
                break;
            case Four:
                this.entropyBytes = 4;
                break;
        }
    }

    /**
     * <summary>
     *   The number of least significant bits to drop in DateTime.Ticks when
     *   computing the counter.
     * </summary>
     */
    public int getTicksBitsToDrop()
    {
        switch (interval) {
            case Coarse:
                return 24;

            case Fine:
                return 16;

            default:
                return 24;
        }
    }

    /**
     * <summary>
     *   The number of bytes used to store the entropy.
     * </summary>
     */
    public int getEntropyBytes() {
        return this.entropyBytes;
    }

    public int getTotalBits() {
        int counterBits;
        switch (periodicity) {
            case None:
                counterBits = 0;
                break;
            case Short:
                counterBits = 16;
                break;
            case Medium:
                counterBits = 24;
                break;
            case Long:
                counterBits = 32;
                break;
            default:
                counterBits = 0;
                break;
        }
        return counterBits + entropyBytes * 8;
    }
}
