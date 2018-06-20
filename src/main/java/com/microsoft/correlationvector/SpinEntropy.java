/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 */
package com.microsoft.correlationvector;

/**
 * Entropy bytes that is used for CV's Spin operation.
 */
public enum SpinEntropy {

    /**
     * Do not generate entropy as part of the spin value.
     */
    None(0),
    /**
     * Generate entropy using 8 bits.
     */
    One(1),
    /**
     * Generate entropy using 16 bits.
     */
    Two(2),
    /**
     * Generate entropy using 24 bits.
     */
    Three(3),
    /**
     * Generate entropy using 32 bits.
     */
    Four(4);

    /**
     * Entropy byte size.
     */
    private final int entropyBytes;

    /**
     * SpinEntropy Constructor.
     *
     * @param entropyBytes
     *            number of bytes for entropy.
     */
    private SpinEntropy(int entropyBytes) {
        this.entropyBytes = entropyBytes;
    }

    /**
     * Gets the entropy byte size.
     *
     * @return entropy byte size.
     */
    public int getEntropyBytes() {
        return this.entropyBytes;
    }
}