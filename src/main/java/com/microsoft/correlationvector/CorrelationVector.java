/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 */
package com.microsoft.correlationvector;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents a lightweight vector for identifying and measuring
 * causality.
 */
public class CorrelationVector {

    /**
     * This is the header that should be used between services to pass the
     * correlation vector.
     */
    public static final String HEADER_NAME = "MS-CV";
    /**
     * CV vector delimiter.
     */
    public static final char CV_DELIMITER = '.';
    /**
     * CV vector terminator.
     */
    public static final char CV_TERMINATOR = '!';
    /**
     * Gets or sets a value indicating whether or not to validate the correlation
     * vector on creation.
     */
    public static boolean VALIDATE_CV_DURING_CREATION = false;

    private static final byte MAX_CV_LENGTH = 63;
    private static final byte MAX_CV_LENGTH_V2 = 127;
    private static final byte CV_BASE_LENGTH = 16;
    private static final byte CV_BASE_LENGTH_V2 = 22;

    private static final Random rng = new Random();

    private final AtomicInteger extension;

    private final CorrelationVectorVersion version;

    private final String baseVector;
    /**
     * Indicates whether the CV object is immutable.
     */
    private boolean isImmutable;

    /**
     * Creates a new correlation vector by extending an existing value. This should
     * be done at the entry point of an operation, taken from the message header
     * indicated by the CV header name.
     *
     * @return A new correlation vector extended from the current vector.
     */
    public static CorrelationVector extend(String correlationVector) {

        if (isImmutable(correlationVector)) {
            return CorrelationVector.parse(correlationVector);
        }

        final CorrelationVectorVersion version = CorrelationVector.inferVersion(correlationVector,
                CorrelationVector.VALIDATE_CV_DURING_CREATION);

        if (CorrelationVector.VALIDATE_CV_DURING_CREATION) {
            CorrelationVector.validate(correlationVector, version);
        }

        if (isOversized(correlationVector, 0, version)) {
            return parse(correlationVector + CV_TERMINATOR);
        } else {
            return new CorrelationVector(correlationVector, 0, version, false);
        }
    }

    /**
     * Creates a new correlation vector by parsing its string representation.
     *
     * @param correlationVector
     *            CV in string.
     * @return CV.
     */
    public static CorrelationVector parse(String correlationVector) {

        if (!((correlationVector == null) || correlationVector.trim().isEmpty())) {
            final int p = correlationVector.lastIndexOf(CV_DELIMITER);
            final boolean isImmutable = isImmutable(correlationVector);

            if (p > 0) {
                final String extensionVal = isImmutable
                        ? correlationVector.substring(p + 1, correlationVector.length() - 1)
                        : correlationVector.substring(p + 1);
                int extension = 0;
                try {
                    extension = Integer.parseInt(extensionVal);
                } catch (NumberFormatException nfe) {
                    return new CorrelationVector();
                }
                if (extension >= 0) {
                    return new CorrelationVector(correlationVector.substring(0, p), extension,
                            CorrelationVector.inferVersion(correlationVector, false), isImmutable);
                }
            }
        }

        return new CorrelationVector();
    }

    /**
     * Creates a new correlation vector by applying the Spin operator to an existing
     * value. This should be done at the entry point of an operation, taken from the
     * message header indicated by the CV header name.
     *
     * @return A new correlation vector extended from the current vector.
     */
    public static CorrelationVector spin(String correlationVector) {

        final SpinParameters defaultParameters = SpinParameters.getDefaultParameters();
        return CorrelationVector.spin(correlationVector, defaultParameters);
    }

    /**
     * Creates a new correlation vector by applying the Spin operator to an existing
     * value. This should be done at the entry point of an operation.
     *
     * @param correlationVector
     *            CV in string.
     * @param parameters
     *            The parameters to use when applying the Spin operator.
     * @return A new correlation vector extended from the current vector.
     */
    public static CorrelationVector spin(String correlationVector, SpinParameters parameters) {

        if (isImmutable(correlationVector)) {
            return parse(correlationVector);
        }

        final CorrelationVectorVersion version = CorrelationVector.inferVersion(correlationVector,
                CorrelationVector.VALIDATE_CV_DURING_CREATION);

        if (CorrelationVector.VALIDATE_CV_DURING_CREATION) {
            CorrelationVector.validate(correlationVector, version);
        }

        final byte[] entropy = new byte[parameters.getEntropyBytes()];
        rng.nextBytes(entropy);

        long value = DateTimeUtils.getTicksSinceEpoch() >> parameters.getTicksBitsToDrop();
        for (int i = 0; i < parameters.getEntropyBytes(); i++) {
            value = (value << 8) | (entropy[i]);
        }

        // Generate a bitmask and mask the lower TotalBits in the value.
        // The mask is generated by (1 << TotalBits) - 1. We need to handle the edge
        // case when shifting 64 bits, as it wraps around.
        value &= (parameters.getTotalBits() == 64 ? 0 : (long) 1 << parameters.getTotalBits()) - 1;

        String s = String.valueOf((int) value);
        if (parameters.getTotalBits() > 32) {
            s = (value >> 32) + CV_DELIMITER + s;
        }

        String baseVector = new StringBuilder(correlationVector).append(CV_DELIMITER).append(s).toString();
        if (isOversized(baseVector, 0, version)) {
            return parse(correlationVector + CV_TERMINATOR);
        } else {
            return new CorrelationVector(correlationVector + CV_DELIMITER + s, 0, version, false);
        }
    }

    /**
     * Gets the CV base for the given uuid.
     * 
     * @param uuid
     *            uuid.
     * @return Generated CV base.
     */
    private static String getBaseFromGuid(UUID uuid) {

        final ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());
        // Removes the base64 padding
        final String cvBase = Base64Encoder.toBase64String(uuidBytes);
        return cvBase.substring(0, CV_BASE_LENGTH_V2);
    }

    /**
     * Generates a unique base64 string for the given CV version.
     *
     * @param version
     *            CV version
     * @return A unique base54 string.
     */
    private static String getUniqueValue(CorrelationVectorVersion version) {

        if (CorrelationVectorVersion.V1 == version) {
            final UUID uuid = UUID.randomUUID();
            final ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[12]);

            uuidBytes.putLong(uuid.getMostSignificantBits());
            uuidBytes.putInt((int) (uuid.getLeastSignificantBits() >> 32));
            final String cvBase = Base64Encoder.toBase64String(uuidBytes);
            return cvBase;
        } else if (CorrelationVectorVersion.V2 == version) {
            return CorrelationVector.getBaseFromGuid(UUID.randomUUID());
        } else {
            throw new IllegalArgumentException("Unsupported correlation vector version:" + version);
        }
    }

    /**
     * Infer the CV string's version.
     *
     * @param correlationVector
     *            CV in string.
     * @param reportErrors
     *            whether to report errors.
     * @return the inferred CV version.
     */
    private static CorrelationVectorVersion inferVersion(String correlationVector, boolean reportErrors) {

        final int index = correlationVector == null ? -1 : correlationVector.indexOf(CV_DELIMITER);

        if (CorrelationVector.CV_BASE_LENGTH == index) {
            return CorrelationVectorVersion.V1;
        } else if (CorrelationVector.CV_BASE_LENGTH_V2 == index) {
            return CorrelationVectorVersion.V2;
        } else {
            // By default not reporting error, just return V1
            return CorrelationVectorVersion.V1;
        }
    }

    /**
     * Gets the length of an integer. The given integer must be non-negative.
     * 
     * @param i
     *            non-negative integer.
     * @return length of the given integer.
     */
    private static int intLength(int i) {
        return (i == 0) ? 1 : (int) Math.log10(i) + 1;
    }

    /**
     * Checks if the given CV string is immutable. If the given non-empty string
     * ends with the CV termination sign, the CV is said to be immutable.
     * 
     * @param correlationVector
     *            CV in string.
     * @return true is the given CV string is immutable.
     */
    private static boolean isImmutable(String correlationVector) {
        return correlationVector != null && !correlationVector.isEmpty()
                && correlationVector.endsWith(CV_TERMINATOR + "");
    }

    /**
     * Checks if the given CV is oversized.
     * 
     * @param baseVector
     *            baseVector from the incoming request.
     * @param extension
     *            extension number.
     * @param version
     *            CV version.
     * @return true is the CV is oversized.
     */
    private static boolean isOversized(String baseVector, int extension, CorrelationVectorVersion version) {
        if (baseVector == null || baseVector.isEmpty()) {
            return false;
        }

        int cvLen = baseVector.length() + 1 + intLength(extension);
        return (version == CorrelationVectorVersion.V1 && cvLen > MAX_CV_LENGTH)
                || (version == CorrelationVectorVersion.V2 && cvLen > MAX_CV_LENGTH_V2);
    }

    /**
     * Validates the CV string with the given CV version.
     *
     * @param correlationVector
     *            CV in string.
     * @param version
     *            CV version.
     */
    private static void validate(String correlationVector, CorrelationVectorVersion version) {

        byte maxVectorLength;
        byte baseLength;

        if (CorrelationVectorVersion.V1 == version) {
            maxVectorLength = CorrelationVector.MAX_CV_LENGTH;
            baseLength = CorrelationVector.CV_BASE_LENGTH;
        } else if (CorrelationVectorVersion.V2 == version) {
            maxVectorLength = CorrelationVector.MAX_CV_LENGTH_V2;
            baseLength = CorrelationVector.CV_BASE_LENGTH_V2;
        } else {
            throw new IllegalArgumentException(
                    MessageFormat.format("Unsupported correlation vector version: {0}", String.valueOf(version)));
        }

        if ((correlationVector == null) || correlationVector.trim().isEmpty()
                || (correlationVector.length() > maxVectorLength)) {
            throw new IllegalArgumentException(
                    MessageFormat.format("The {0} correlation vector can not be null or bigger than {1} characters ",
                            String.valueOf(version), String.valueOf(maxVectorLength)));
        }

        final String[] parts = correlationVector.split("\\.");

        if ((parts.length < 2) || (parts[0].length() != baseLength)) {
            throw new IllegalArgumentException(MessageFormat
                    .format("Invalid correlation vector {0}. Invalid base value {1}", correlationVector, parts[0]));
        }

        for (int i = 1; i < parts.length; i++) {
            try {
                final int result = Integer.parseInt(parts[i]);
                if (result < 0) {
                    throw new IllegalArgumentException(
                            MessageFormat.format("Invalid correlation vector {0}. Invalid extension value {1}",
                                    correlationVector, parts[i]));
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid correlation vector {0}. Invalid extension value {1}", correlationVector, parts[i]));
            }
        }
    }

    /**
     * Initializes a new instance of the Correlation Vector with V1 implementation.
     * This should only be called when no correlation vector was found in the
     * message header.
     */
    public CorrelationVector() {
        this(CorrelationVectorVersion.V1);
    }

    /**
     * Initializes a new instance of the Correlation Vector of the given
     * implementation version. This should only be called when no correlation vector
     * was found in the message header.
     *
     * @param version
     *            The correlation vector implementation version.
     */
    public CorrelationVector(CorrelationVectorVersion version) {
        this(CorrelationVector.getUniqueValue(version), 0, version, false);
    }

    /**
     * Initializes a new instance of the Correlation Vector of the V2 implementation
     * using the given Guid as the vector base.
     *
     * @param vectorBase
     *            The Guid to use as a correlation vector base.
     */
    public CorrelationVector(UUID vectorBase) {
        this(CorrelationVector.getBaseFromGuid(vectorBase), 0, CorrelationVectorVersion.V2, false);
    }

    private CorrelationVector(String baseVector, int extension, CorrelationVectorVersion version, boolean isImmutable) {
        this.baseVector = baseVector;
        this.extension = new AtomicInteger(extension);
        this.version = version;
        this.isImmutable = isImmutable || isOversized(baseVector, extension, version);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        /*
         * Check if o is an instance of Complex or not "null instanceof [type]" also
         * returns false
         */
        if (!(o instanceof CorrelationVector)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        final CorrelationVector vector = (CorrelationVector) o;
        return this.getValue().equals(vector.getValue());
    }

    public String getBaseVector() {
        return this.baseVector;
    }

    /**
     * Gets the value of the correlation vector as a string.
     *
     * @return CV in string.
     */
    public String getValue() {
        final StringBuilder s = new StringBuilder(this.baseVector).append(CV_DELIMITER).append(this.extension);
        if (this.isImmutable) {
            s.append(CV_TERMINATOR);
        }
        return s.toString();
    }

    /**
     * Gets the version of the correlation vector implementation.
     *
     * @return version of the CV.
     */
    public CorrelationVectorVersion getVersion() {
        return this.version;
    }

    /**
     * Increments the current extension by one. Do this before passing the value to
     * an outbound message header.
     *
     * @return The new value as a string that you can add to the outbound message
     *         header.
     */
    public String increment() {

        if (this.isImmutable) {
            return getValue();
        }

        int snapshot = 0;
        int next = 0;
        do {
            snapshot = this.extension.get();
            if (snapshot == Integer.MAX_VALUE) {
                return this.getValue();
            }
            next = snapshot + 1;

            if (isOversized(this.baseVector, next, this.version)) {
                this.isImmutable = true;
                return this.getValue();
            }
        } while (!this.extension.compareAndSet(snapshot, next));

        return this.baseVector + CV_DELIMITER + next;
    }

    /**
     * Returns a string representation of the CV.
     *
     * @return CV in string.
     */
    @Override
    public String toString() {
        return this.getValue();
    }
}
