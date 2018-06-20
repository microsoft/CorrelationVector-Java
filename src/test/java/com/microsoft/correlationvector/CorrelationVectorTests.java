package com.microsoft.correlationvector;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CorrelationVectorTests {

    @Before
    public void reset() {
        CorrelationVector.VALIDATE_CV_DURING_CREATION = false;
    }

    // check if correlation vector increment is unique across all threads
    @Test
    public void correlationVectorIncrementIsUniqueAcrossMultipleThreads() {
        final int numberOfThreads = 1000;
        final CorrelationVector cV = new CorrelationVector();
        final CorrelationVector cV2 = CorrelationVector.extend(cV.getValue());

        final ArrayList<Thread> threads = new ArrayList<>();
        final ConcurrentLinkedQueue<String> incrementedCvs = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < numberOfThreads; i++) {
            final Thread currentThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    incrementedCvs.add(cV2.increment());
                }
            });
            currentThread.start();
            threads.add(currentThread);
        }

        for (final Thread thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
            }
        }

        final HashSet<String> unique = new HashSet<String>();
        for (final String incrementedCv : incrementedCvs) {
            unique.add(incrementedCv);
        }

        Assert.assertEquals(unique.size(), numberOfThreads);
    }

    // create correlation vector from string
    @Test
    public void createCorrelationVectorFromString() {
        final CorrelationVector cV = CorrelationVector.extend("tul4NUsfs9Cl7mOf.1");
        final String[] splitCv = cV.getValue().split("\\.");

        Assert.assertEquals(splitCv.length, 3);
        Assert.assertEquals(splitCv[2], "0");

        final String incrementedCv = cV.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length, 3);
        Assert.assertEquals(splitIncrementedCv[2], "1");

        Assert.assertEquals(cV.toString(), "tul4NUsfs9Cl7mOf.1.1");
    }

    @Test
    public void createCorrelationVectorFromStringV2() {
        final CorrelationVector cV = CorrelationVector.extend("KZY+dsX2jEaZesgCPjJ2Ng.1");
        final String[] splitCv = cV.getValue().split("\\.");

        Assert.assertEquals(splitCv.length, 3);
        Assert.assertEquals(splitCv[2], "0");

        final String incrementedCv = cV.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length, 3);
        Assert.assertEquals(splitIncrementedCv[2], "1");

        Assert.assertEquals(cV.toString(), "KZY+dsX2jEaZesgCPjJ2Ng.1.1");
    }

    // create, extend and increment correlation vector default
    @Test
    public void createExtendAndIncrementCorrelationVectorDefault() {
        final CorrelationVector cv = new CorrelationVector();
        final String[] splitCv = cv.getValue().split("\\.");

        Assert.assertEquals(splitCv.length, 2);
        Assert.assertEquals(splitCv[0].length(), 16);
        Assert.assertEquals(splitCv[1], "0");

        final String incrementedCv = cv.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length, 2);
        Assert.assertEquals(splitIncrementedCv[1], "1");
    }

    // create, extend and increment correlation vector V1
    @Test
    public void createExtendAndIncrementCorrelationVectorV1() {
        final CorrelationVector cv = new CorrelationVector();
        final String[] splitCv = cv.getValue().split("\\.");

        Assert.assertEquals(splitCv.length, 2);
        Assert.assertEquals(splitCv[0].length(), 16);
        Assert.assertEquals(splitCv[1], "0");

        final String incrementedCv = cv.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length, 2);
        Assert.assertEquals(splitIncrementedCv[1], "1");
    }

    // create, extend and increment correlation vector V2
    @Test
    public void createExtendAndIncrementCorrelationVectorV2() {
        final CorrelationVector cv = new CorrelationVector(CorrelationVectorVersion.V2);
        Assert.assertEquals(cv.getVersion(), CorrelationVectorVersion.V2);

        final String[] splitCv = cv.getValue().split("\\.");

        Assert.assertEquals(splitCv.length, 2);
        Assert.assertEquals(splitCv[0].length(), 22);
        Assert.assertEquals(splitCv[1], "0");

        final String incrementedCv = cv.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length, 2);
        Assert.assertEquals(splitIncrementedCv[1], "1");
    }

    // create, extend and increment correlation vector V2 from guid
    @Test
    public void createExtendAndIncrementCorrelationVectorV2fromUuid() {
        final UUID uuid = UUID.randomUUID();

        final ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());

        final String expectedCvBase = Base64Encoder.toBase64String(uuidBytes).substring(0, 22);

        final CorrelationVector cV = new CorrelationVector(uuid);
        Assert.assertEquals(cV.getVersion(), CorrelationVectorVersion.V2);

        final String[] splitCv = cV.getValue().split("\\.");
        Assert.assertEquals(splitCv.length, 2);
        Assert.assertEquals(splitCv[0].length(), 22);
        Assert.assertEquals(expectedCvBase, splitCv[0]);
        Assert.assertEquals(splitCv[1], "0");

        final String incrementedCv = cV.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length, 2);
        Assert.assertEquals(splitIncrementedCv[1], "1");
    }

    // extend null correlation vector
    @Test(expected = IllegalArgumentException.class)
    public void extendNullCorrelationVector() {
        final String nullString = "";

        final CorrelationVector cV = CorrelationVector.extend(nullString);
        Assert.assertEquals(cV.toString(), ".0");

        CorrelationVector.VALIDATE_CV_DURING_CREATION = true;

        final CorrelationVector cV2 = CorrelationVector.extend(nullString);
        Assert.assertEquals(cV2.toString(), ".0");
    }

    // Increment correlation vector to check for any errors
    @Test
    public void incrementPastMaxWithNoErrors() {
        final CorrelationVector cV = CorrelationVector
                .extend("tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479");
        cV.increment();
        Assert.assertEquals(cV.getValue(), "tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479.1");

        for (int i = 0; i < 20; ++i) {
            cV.increment();
        }

        Assert.assertEquals(cV.getValue(), "tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479.9");
    }

    // Increment correlation vector V2 to check for any errors
    @Test
    public void incrementPastMaxWithNoErrorsV2() {
        final CorrelationVector cV = CorrelationVector.extend(
                "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214");
        cV.increment();
        Assert.assertEquals(cV.getValue(),
                "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214.1");

        for (int i = 0; i < 20; ++i) {
            cV.increment();
        }

        Assert.assertEquals(cV.getValue(),
                "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214.9");
    }

    // Validate spin sort
    @Test
    public void spinSortValidation() {
        final CorrelationVector cV = new CorrelationVector();

        final SpinParameters params = new SpinParameters();
        params.setEntropy(SpinEntropy.Two);
        params.setInterval(SpinCounterInterval.Fine);
        params.setPeriodicity(SpinCounterPeriodicity.Short);

        long lastSpinValue = 0;
        int wrappedCounter = 0;

        for (int i = 0; i < 100; ++i) {
            final CorrelationVector cV2 = CorrelationVector.spin(cV.getValue(), params);

            // The cV after a spin will look like <cvBase>.0.<spinValue>.0, so the spinValue
            // is at index = 2
            final String[] splitCv = cV2.getValue().split("\\.");
            final long spinValue = Long.parseLong(splitCv[2]);

            // Count the number of times the counter wraps.
            if (spinValue <= lastSpinValue) {
                wrappedCounter++;
            }

            lastSpinValue = spinValue;

            // Wait for 10ms
            try {
                Thread.sleep(10);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            ;
        }
    }

    // Check for insufficient chars correlation vector value
    @Test(expected = IllegalArgumentException.class)
    public void throwWithInsufficientCharsCorrelationVectorValue() {
        CorrelationVector.VALIDATE_CV_DURING_CREATION = true;

        CorrelationVector.extend("tul4NUsfs9Cl7mO.1");
    }

    // check length for correlation vector
    @Test(expected = IllegalArgumentException.class)
    public void throwWithTooBigCorrelationVectorValue() {
        CorrelationVector.VALIDATE_CV_DURING_CREATION = true;
        CorrelationVector.extend("tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.2147483647.2147483647");

    }

    // check length for correlation vector V2
    @Test(expected = IllegalArgumentException.class)
    public void ThrowWithTooBigCorrelationVectorValueV2() {
        CorrelationVector.VALIDATE_CV_DURING_CREATION = true;
        CorrelationVector.extend(
                "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647");
    }

    // check extension length for correlation vector
    @Test(expected = IllegalArgumentException.class)
    public void ThrowWithTooBigExtensionCorrelationVectorValue() {
        CorrelationVector.VALIDATE_CV_DURING_CREATION = true;

        CorrelationVector.extend("tul4NUsfs9Cl7mOf.11111111111111111111111111111");
    }

    // Check for too many chars in Correlation Vector

    @Test(expected = IllegalArgumentException.class)
    public void throwWithTooManyCharsCorrelationVectorValue() {
        CorrelationVector.extend("tul4NUsfs9Cl7mOfN/dupsl.1");

        CorrelationVector.VALIDATE_CV_DURING_CREATION = true;

        CorrelationVector.extend("tul4NUsfs9Cl7mOfN/dupsl.1");
    }

}
