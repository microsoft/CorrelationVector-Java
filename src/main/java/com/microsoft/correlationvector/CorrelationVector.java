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

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <summary>
 * This class represents a lightweight vector for identifying and measuring
 * causality.
 * </summary>
 */

public class CorrelationVector {

    private static final byte MaxVectorLength = 63;
    private static final byte MaxVectorLengthV2 = 127;
    private static final byte BaseLength = 16;
    private static final byte BaseLengthV2 = 22;

    private String baseVector = null;
    private CorrelationVectorVersion version;

    String GetBaseVector(){
        return baseVector;
    }

    private AtomicInteger extension;

    private static Random rng = new Random();

    private CorrelationVector(String baseVector, int extension, CorrelationVectorVersion version) {
        this.baseVector = baseVector;
        this.extension = new AtomicInteger(extension);
        this.version = version;
    }

    private static String GetBaseFromGuid(UUID uuid) {

        ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());
        // Removes the  base64 padding
        String cvBase = Base64Encoder.toBase64String(uuidBytes);
        return cvBase.substring(0, BaseLengthV2);
    }

    private static String GetUniqueValue(CorrelationVectorVersion version) {

        if (CorrelationVectorVersion.V1 == version)
        {
            UUID uuid = UUID.randomUUID();
            ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[12]);

            uuidBytes.putLong(uuid.getMostSignificantBits());
            uuidBytes.putInt((int)(uuid.getLeastSignificantBits()>> 32));
            String cvBase = Base64Encoder.toBase64String(uuidBytes);
            return cvBase;
        }
        else if (CorrelationVectorVersion.V2 == version)
        {
            return CorrelationVector.GetBaseFromGuid(UUID.randomUUID());
        }
        else
        {
            String e = "Unsupported correlation vector version:" + version;
            throw new IllegalArgumentException(e);
        }
    }

    private static CorrelationVectorVersion InferVersion(String correlationVector, boolean reportErrors) {

        int index = correlationVector == null ? -1 : correlationVector.indexOf('.');

        if (CorrelationVector.BaseLength == index)
        {
            return CorrelationVectorVersion.V1;
        }
        else if (CorrelationVector.BaseLengthV2 == index)
        {
            return CorrelationVectorVersion.V2;
        }
        else
        {
            if (reportErrors)
            {
                String e = MessageFormat.format("Invalid correlation vector {0}", correlationVector);
                InternalErrors.Report(new IllegalArgumentException(e));
            }

            // Fallback to V1 implementation for invalid CV's
            return CorrelationVectorVersion.V1;
        }
    }

    private static void Validate(String correlationVector, CorrelationVectorVersion version) {

        try
        {
            byte maxVectorLength;
            byte baseLength;

            if (CorrelationVectorVersion.V1 == version)
            {
                maxVectorLength = CorrelationVector.MaxVectorLength;
                baseLength = CorrelationVector.BaseLength;
            }
            else if (CorrelationVectorVersion.V2 == version)
            {
                maxVectorLength = CorrelationVector.MaxVectorLengthV2;
                baseLength = CorrelationVector.BaseLengthV2;
            }
            else
            {
                String e = MessageFormat.format("Unsupported correlation vector version: {0}", String.valueOf(version));
                throw new IllegalArgumentException(e);
            }

            if ( correlationVector == null || correlationVector.trim().isEmpty() || correlationVector.length() > maxVectorLength)
            {
                String e = MessageFormat.format("The {0} correlation vector can not be null or bigger than {1} characters ", String.valueOf(version),String.valueOf(maxVectorLength));
                throw new IllegalArgumentException(e);
            }

            String[] parts = correlationVector.split("\\.");

            if (parts.length < 2 || parts[0].length() != baseLength)
            {
                String e = MessageFormat.format("Invalid correlation vector {0}. Invalid base value {1}", correlationVector, parts[0]);
                throw new IllegalArgumentException();
            }

            for (int i = 1; i < parts.length; i++)
            {
                try{
                    int result =  Integer.parseInt(parts[i]);
                    if(result < 0){
                        throw new IllegalArgumentException(MessageFormat.format("Invalid correlation vector {0}. Invalid extension value {1}", correlationVector, parts[i]));
                    }
                }
                catch (NumberFormatException e) {
                    throw new IllegalArgumentException(MessageFormat.format("Exception Occurred: {0}, Invalid Correlation Vector {1}", e, correlationVector));
                }
            }
        }
        catch (IllegalArgumentException ex)
        {
            InternalErrors.Report(ex);
        }
    }

    /**
     * <summary>
     *   This is the header that should be used between services to pass the correlation
     *   vector.
     * </summary>
     */
    public final static String HeaderName = "MS-CV";

    /**
     * <summary>
     *   Gets or sets a value indicating whether or not to validate the correlation
     *   vector on creation.
     * </summary>
     */
    public static boolean ValidateCorrelationVectorDuringCreation = false;

    /**
     * <summary>
     *   Creates a new correlation vector by extending an existing value. This should be
     *   done at the entry point of an operation.
     * </summary>
     * <param name="correlationVector">
     *   Taken from the message header indicated by <see cref="HeaderName"/>.
     * </param>
     * <returns>
        A new correlation vector extended from the current vector.
     * </returns>
     */
    public static CorrelationVector extend(String correlationVector) {

        CorrelationVectorVersion version = CorrelationVector.InferVersion(
                correlationVector, CorrelationVector.ValidateCorrelationVectorDuringCreation);

        if (CorrelationVector.ValidateCorrelationVectorDuringCreation)
        {
            CorrelationVector.Validate(correlationVector, version);
        }

        return new CorrelationVector(correlationVector, 0, version);
    }

    /**
     * <summary>
     *   Creates a new correlation vector by applying the Spin operator to an existing value.
     *   This should be done at the entry point of an operation.
     * </summary>
     * <param name="correlationVector">
     *   Taken from the message header indicated by <see cref="HeaderName"/>.
     * </param>
     * <returns>
     *   A new correlation vector extended from the current vector.
     * </returns>
     */
    public static CorrelationVector Spin(String correlationVector) {

        SpinParameters defaultParameters = SpinParameters.getDefaultParameters();
        return CorrelationVector.Spin(correlationVector, defaultParameters);
    }

    /**
     * <summary>
     *   Creates a new correlation vector by applying the Spin operator to an existing value.
     *   This should be done at the entry point of an operation.
     * </summary>
     * <param name="correlationVector">
     *   Taken from the message header indicated by <see cref="HeaderName"/>.
     * </param>
     * <param name="parameters">
     *   The parameters to use when applying the Spin operator.
     * </param>
     * <returns>
     *   A new correlation vector extended from the current vector.
     * </returns>
     */
    public static CorrelationVector Spin(String correlationVector, SpinParameters parameters) {

        CorrelationVectorVersion version = CorrelationVector.InferVersion(
                correlationVector, CorrelationVector.ValidateCorrelationVectorDuringCreation);

        if (CorrelationVector.ValidateCorrelationVectorDuringCreation)
        {
            CorrelationVector.Validate(correlationVector, version);
        }

        byte[] entropy = new byte[parameters.getEntropyBytes()];
        rng.nextBytes(entropy);

        long value = (long)(DateTimeUtils.getTicksSinceEpoch() >> parameters.getTicksBitsToDrop());
        for (int i = 0; i < parameters.getEntropyBytes(); i++)
        {
            value = (value << 8) | (long)(entropy[i]);
        }

        // Generate a bitmask and mask the lower TotalBits in the value.
        // The mask is generated by (1 << TotalBits) - 1. We need to handle the edge case
        // when shifting 64 bits, as it wraps around.
        value &= (parameters.getTotalBits() == 64 ? 0 : (long)1 << parameters.getTotalBits()) - 1;

        String s = String.valueOf((int)value);
        if (parameters.getTotalBits() > 32)
        {
            s = (value >> 32) + "." + s;
        }

        return new CorrelationVector(correlationVector + "." + s, 0, version);
    }

    /**
     * <summary>
     *   Creates a new correlation vector by parsing its string representation
     * </summary>
     * <param name="correlationVector">correlationVector</param>
     * <returns>
     *   com.microsoft.correlationvector.CorrelationVector
     * </returns>
     */
    public static CorrelationVector Parse(String correlationVector) {

        if (!(correlationVector == null || correlationVector.trim().isEmpty()))
        {
            int p = correlationVector.lastIndexOf('.');
            if (p > 0)
            {
                int extension = Integer.parseInt(correlationVector.substring(p + 1));
                if (extension >= 0)
                {
                    return new CorrelationVector(correlationVector.substring(0, p), extension, CorrelationVector.InferVersion(correlationVector, false));
                }
            }
        }

        return new CorrelationVector();
    }

    /**
     * <summary>
     *   Initializes a new instance of the <see cref="com.microsoft.correlationvector.CorrelationVector"/> class. This
     *   should only be called when no correlation vector was found in the message header.
     * </summary>
     */
    public CorrelationVector()
    {
        this(CorrelationVectorVersion.V1);
    }

    /**
     * <summary>
     *   Initializes a new instance of the <see cref="com.microsoft.correlationvector.CorrelationVector"/> class of the
     *   given implemenation version. This should only be called when no correlation
     *   vector was found in the message header.
     * </summary>
     * <param name="version">The correlation vector implemenation version.</param>
     */
    public CorrelationVector(CorrelationVectorVersion version)
    {
        this(CorrelationVector.GetUniqueValue(version), 0, version);
    }

    /**
     * <summary>
     *   Initializes a new instance of the <see cref="com.microsoft.correlationvector.CorrelationVector"/> class of the
     *   V2 implemenation using the given <see cref="System.Guid"/> as the vector base.
     * </summary>
     * <param name="vectorBase">The <see cref="System.Guid"/> to use as a correlation vector base.</param>
     */
    public CorrelationVector(UUID vectorBase)
    {
        this(CorrelationVector.GetBaseFromGuid(vectorBase), 0, CorrelationVectorVersion.V2);
    }

    /**
     * <summary>
     *   Gets the value of the correlation vector as a string.
     * </summary>
     */
    public String getValue()
    {
        return this.baseVector + "." + this.extension;
    }

    /**
     * <summary>
     *   Increments the current extension by one. Do this before passing the value to an
     *   outbound message header.
     * </summary>
     * <returns>
     *   The new value as a string that you can add to the outbound message header
     *   indicated by <see cref="HeaderName"/>.
     * </returns>
     */
    public String increment()
    {
        int snapshot = 0;
        int next = 0;
        do
        {
            snapshot = this.extension.get();
            if (snapshot == Integer.MAX_VALUE)
            {
                return this.getValue();
            }
            next = snapshot + 1;
            int size = baseVector.length() + 1 + (int)Math.log10(next) + 1;
            if ((version == CorrelationVectorVersion.V1 &&
                    size > CorrelationVector.MaxVectorLength) ||
                    (version == CorrelationVectorVersion.V2 &&
                            size > CorrelationVector.MaxVectorLengthV2))
            {
                return this.getValue();
            }
        }
        while (!this.extension.compareAndSet(snapshot,next));
        return this.baseVector + "." + next;
    }

    /**
     * <summary>
     *   Gets the version of the correlation vector implementation.
     * </summary>
     */
    public CorrelationVectorVersion getVersion()
    {
        return this.version;
    }

    /**
     * <summary>
     * Returns a string that represents the current object.
     * </summary>
     * <returns>A string that represents the current object.</returns>
     */
    @Override
    public String toString()
    {
        return this.getValue();
    }

    /**
     * <summary>
     *   Determines whether two instances of the <see cref="com.microsoft.correlationvector.CorrelationVector"/> class
     *   are equal.
     * </summary>
     * <param name="vector">
     *   The correlation vector you want to compare with the current correlation vector.
     * </param>
     * <returns>
     *   True if the specified correlation vector is equal to the current correlation
     *   vector; otherwise, false.
     * </returns>
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof CorrelationVector)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        CorrelationVector vector = (CorrelationVector) o;
        return this.getValue().equals(vector.getValue());
    }
}

