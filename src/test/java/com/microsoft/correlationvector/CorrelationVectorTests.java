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

        Assert.assertEquals(numberOfThreads, unique.size());
    }

    // create correlation vector from string
    @Test
    public void createCorrelationVectorFromString() {
        final CorrelationVector cV = CorrelationVector.extend("tul4NUsfs9Cl7mOf.1");
        final String[] splitCv = cV.getValue().split("\\.");

        Assert.assertEquals(3, splitCv.length);
        Assert.assertEquals("0", splitCv[2]);

        final String incrementedCv = cV.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(3, splitIncrementedCv.length);
        Assert.assertEquals("1", splitIncrementedCv[2]);

        Assert.assertEquals("tul4NUsfs9Cl7mOf.1.1", cV.toString());
    }

    @Test
    public void createCorrelationVectorFromStringV2() {
        final CorrelationVector cV = CorrelationVector.extend("KZY+dsX2jEaZesgCPjJ2Ng.1");
        final String[] splitCv = cV.getValue().split("\\.");

        Assert.assertEquals(3, splitCv.length);
        Assert.assertEquals("0", splitCv[2]);

        final String incrementedCv = cV.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(3, splitIncrementedCv.length);
        Assert.assertEquals("1", splitIncrementedCv[2]);

        Assert.assertEquals("KZY+dsX2jEaZesgCPjJ2Ng.1.1", cV.toString());
    }

    // create, extend and increment correlation vector default
    @Test
    public void createExtendAndIncrementCorrelationVectorDefault() {
        final CorrelationVector cv = new CorrelationVector();
        final String[] splitCv = cv.getValue().split("\\.");

        Assert.assertEquals(2, splitCv.length);
        Assert.assertEquals(16, splitCv[0].length());
        Assert.assertEquals("0", splitCv[1]);

        final String incrementedCv = cv.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(2, splitIncrementedCv.length);
        Assert.assertEquals("1", splitIncrementedCv[1]);
    }

    // create, extend and increment correlation vector V1
    @Test
    public void createExtendAndIncrementCorrelationVectorV1() {
        final CorrelationVector cv = new CorrelationVector();
        final String[] splitCv = cv.getValue().split("\\.");

        Assert.assertEquals(2, splitCv.length);
        Assert.assertEquals(16, splitCv[0].length());
        Assert.assertEquals("0", splitCv[1]);

        final String incrementedCv = cv.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(2, splitIncrementedCv.length);
        Assert.assertEquals("1", splitIncrementedCv[1]);
    }

    // create, extend and increment correlation vector V2
    @Test
    public void createExtendAndIncrementCorrelationVectorV2() {
        final CorrelationVector cv = new CorrelationVector(CorrelationVectorVersion.V2);
        Assert.assertEquals(cv.getVersion(), CorrelationVectorVersion.V2);

        final String[] splitCv = cv.getValue().split("\\.");

        Assert.assertEquals(2, splitCv.length);
        Assert.assertEquals(22, splitCv[0].length());
        Assert.assertEquals("0", splitCv[1]);

        final String incrementedCv = cv.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(2, splitIncrementedCv.length);
        Assert.assertEquals("1", splitIncrementedCv[1]);
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
        Assert.assertEquals(CorrelationVectorVersion.V2, cV.getVersion());

        final String[] splitCv = cV.getValue().split("\\.");
        Assert.assertEquals(2, splitCv.length);
        Assert.assertEquals(22, splitCv[0].length());
        Assert.assertEquals(expectedCvBase, splitCv[0]);
        Assert.assertEquals("0", splitCv[1]);

        final String incrementedCv = cV.increment();
        final String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(2, splitIncrementedCv.length);
        Assert.assertEquals("1", splitIncrementedCv[1]);
    }

    @Test
    public void extendOverMaxCVLength() {

        final String baseVector = "tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.214748364.23";

        CorrelationVector cv = CorrelationVector.extend(baseVector);
        Assert.assertEquals(baseVector + CorrelationVector.CV_TERMINATOR, cv.getValue());
    }

    @Test
    public void extendOverMaxCVLengthV2() {

        final String baseVector = "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2141";

        CorrelationVector cv = CorrelationVector.extend(baseVector);
        Assert.assertEquals(baseVector + CorrelationVector.CV_TERMINATOR, cv.getValue());
    }

    // extend null correlation vector
    @Test(expected = IllegalArgumentException.class)
    public void extendNullCorrelationVector() {
        final String nullString = "";

        final CorrelationVector cV = CorrelationVector.extend(nullString);
        Assert.assertEquals(".0", cV.toString());

        CorrelationVector.VALIDATE_CV_DURING_CREATION = true;

        final CorrelationVector cV2 = CorrelationVector.extend(nullString);
        Assert.assertEquals(".0", cV2.toString());
    }

    @Test
    public void immutableCVWithTerminator() {

        String cvStr = "tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479.0!";

        Assert.assertEquals(cvStr, CorrelationVector.parse(cvStr).increment());
        Assert.assertEquals(cvStr, CorrelationVector.extend(cvStr).getValue());
        Assert.assertEquals(cvStr, CorrelationVector.spin(cvStr).getValue());
    }

    @Test
    public void immutableCVWIthTerminatorV2() {

        String cvStr = "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214.0!";

        Assert.assertEquals(cvStr, CorrelationVector.parse(cvStr).increment());
        Assert.assertEquals(cvStr, CorrelationVector.extend(cvStr).getValue());
        Assert.assertEquals(cvStr, CorrelationVector.spin(cvStr).getValue());
    }

    // Increment correlation vector to check for any errors
    @Test
    public void incrementPastMaxWithNoErrors() {
        final CorrelationVector cV = CorrelationVector
                .extend("tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479");
        cV.increment();
        Assert.assertEquals("tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479.1", cV.getValue());

        for (int i = 0; i < 20; ++i) {
            cV.increment();
        }

        Assert.assertEquals("tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479.9!", cV.getValue());
    }

    // Increment correlation vector V2 to check for any errors
    @Test
    public void incrementPastMaxWithNoErrorsV2() {
        final CorrelationVector cV = CorrelationVector.extend(
                "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214");
        cV.increment();
        Assert.assertEquals(
                "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214.1",
                cV.getValue());

        for (int i = 0; i < 20; ++i) {
            cV.increment();
        }

        Assert.assertEquals(
                "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214.9!",
                cV.getValue());
    }

    @Test
    public void spinOverMaxCVLength() {
        final String baseVector = "tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.214748364.23";

        CorrelationVector cv = CorrelationVector.spin(baseVector);
        Assert.assertEquals(baseVector + CorrelationVector.CV_TERMINATOR, cv.getValue());
    }

    @Test
    public void spinOverMaxCVLengthV2() {
        final String baseVector = "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214";

        CorrelationVector cv = CorrelationVector.spin(baseVector);
        Assert.assertEquals(baseVector + CorrelationVector.CV_TERMINATOR, cv.getValue());
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
    public void throwWithTooBigCorrelationVectorValueV2() {
        CorrelationVector.VALIDATE_CV_DURING_CREATION = true;
        CorrelationVector.extend(
                "KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647");
    }

    // check extension length for correlation vector
    @Test(expected = IllegalArgumentException.class)
    public void throwWithTooBigExtensionCorrelationVectorValue() {
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
