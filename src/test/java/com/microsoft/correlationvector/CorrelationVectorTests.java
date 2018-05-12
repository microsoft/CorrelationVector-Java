package com.microsoft.correlationvector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CorrelationVectorTests {


    @Before public void reset(){
        CorrelationVector.ValidateCorrelationVectorDuringCreation = false;
        InternalErrors.clearErrors();
    }

    // check if correlation vector increment is unique across all threads
    @Test public void CorrelationVector_Increment_Is_Unique_Across_Multiple_Threads(){
        final int numberOfThreads = 1000;
        CorrelationVector cV = new CorrelationVector();
        final CorrelationVector cV2 = CorrelationVector.extend(cV.getValue());

        ArrayList<Thread> threads = new ArrayList<>();
        final ConcurrentLinkedQueue<String> incrementedCvs = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < numberOfThreads; i++)
        {
            Thread currentThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    incrementedCvs.add(cV2.increment());
                }
            });
            currentThread.start();
            threads.add(currentThread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }

        HashSet<String> unique = new HashSet<String>();
        for (String incrementedCv : incrementedCvs) {
            unique.add(incrementedCv);
        }

        Assert.assertEquals(unique.size(), numberOfThreads);
    }

    // create, extend and increment correlation vector default
    @Test public void createExtendAndIncrementCorrelationVectorDefault(){
        CorrelationVector cv = new CorrelationVector();
        String[] splitCv = cv.getValue().split("\\.");

        Assert.assertEquals(splitCv.length, 2);
        Assert.assertEquals(splitCv[0].length(), 16);
        Assert.assertEquals(splitCv[1], "0");

        String incrementedCv = cv.increment();
        String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length,2);
        Assert.assertEquals(splitIncrementedCv[1], "1");
    }

    // create, extend and increment correlation vector V1
    @Test public void createExtendAndIncrementCorrelationVectorV1(){
        CorrelationVector cv = new CorrelationVector();
        String[] splitCv = cv.getValue().split("\\.");

        Assert.assertEquals(splitCv.length, 2);
        Assert.assertEquals(splitCv[0].length(), 16);
        Assert.assertEquals(splitCv[1], "0");

        String incrementedCv = cv.increment();
        String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length,2);
        Assert.assertEquals(splitIncrementedCv[1], "1");
    }

    // create, extend and increment correlation vector V2
    @Test public void createExtendAndIncrementCorrelationVectorV2(){
        CorrelationVector cv = new CorrelationVector(CorrelationVectorVersion.V2);
        Assert.assertEquals(cv.getVersion(), CorrelationVectorVersion.V2);

        String[] splitCv = cv.getValue().split("\\.");

        Assert.assertEquals(splitCv.length, 2);
        Assert.assertEquals(splitCv[0].length(), 22);
        Assert.assertEquals(splitCv[1], "0");

        String incrementedCv = cv.increment();
        String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length,2);
        Assert.assertEquals(splitIncrementedCv[1], "1");
    }

    // create, extend and increment correlation vector V2 from guid
    @Test public void createExtendAndIncrementCorrelationVectorV2fromUuid(){
        UUID uuid = UUID.randomUUID();

        ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());

        String expectedCvBase = Base64Encoder.toBase64String(uuidBytes).substring(0,22);

        CorrelationVector cV = new CorrelationVector(uuid);
        Assert.assertEquals(cV.getVersion(), CorrelationVectorVersion.V2);

        String[] splitCv = cV.getValue().split("\\.");
        Assert.assertEquals(splitCv.length, 2);
        Assert.assertEquals(splitCv[0].length(),22);
        Assert.assertEquals(expectedCvBase,splitCv[0]);
        Assert.assertEquals(splitCv[1], "0");

        String incrementedCv = cV.increment();
        String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length,2);
        Assert.assertEquals(splitIncrementedCv[1], "1");
    }

    // create correlation vector from string
    @Test public void createCorrelationVectorFromString(){
        CorrelationVector cV = CorrelationVector.extend("tul4NUsfs9Cl7mOf.1");
        String[] splitCv = cV.getValue().split("\\.");

        Assert.assertEquals(splitCv.length,3);
        Assert.assertEquals(splitCv[2],"0");

        String incrementedCv = cV.increment();
        String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length, 3);
        Assert.assertEquals(splitIncrementedCv[2], "1");

        Assert.assertEquals(cV.toString(), "tul4NUsfs9Cl7mOf.1.1");
    }

    @Test public void createCorrelationVectorFromStringV2(){
        CorrelationVector cV = CorrelationVector.extend("KZY+dsX2jEaZesgCPjJ2Ng.1");
        String[] splitCv = cV.getValue().split("\\.");

        Assert.assertEquals(splitCv.length,3);
        Assert.assertEquals(splitCv[2],"0");

        String incrementedCv = cV.increment();
        String[] splitIncrementedCv = incrementedCv.split("\\.");
        Assert.assertEquals(splitIncrementedCv.length, 3);
        Assert.assertEquals(splitIncrementedCv[2], "1");

        Assert.assertEquals(cV.toString(), "KZY+dsX2jEaZesgCPjJ2Ng.1.1");
    }

    // extend null correlation vector
    @Test public void extendNullCorrelationVector(){
        String nullString = "";

        CorrelationVector cV = CorrelationVector.extend(nullString);
        Assert.assertEquals(cV.toString(),".0");
        boolean cVHasErrors = InternalErrors.HasSavedErrors();
        Assert.assertFalse(cVHasErrors);

        CorrelationVector.ValidateCorrelationVectorDuringCreation = true;

        CorrelationVector cV2 = CorrelationVector.extend(nullString);
        Assert.assertEquals(cV2.toString(),".0");
        boolean cV2HasErrors = InternalErrors.HasSavedErrors();
        Assert.assertTrue(cV2HasErrors);
    }

    // Check for insufficient chars correlation vector value
    @Test public void throwWithInsufficientCharsCorrelationVectorValue(){
        boolean hasErrors = InternalErrors.HasSavedErrors();
        Assert.assertFalse(hasErrors);

        CorrelationVector.ValidateCorrelationVectorDuringCreation = true;

        CorrelationVector cV2 = CorrelationVector.extend("tul4NUsfs9Cl7mO.1");
        hasErrors = InternalErrors.HasSavedErrors();
        Assert.assertTrue(hasErrors);
    }

    // Check for too many chars in Correlation Vector
    @Test public void throwWithTooManyCharsCorrelationVectorValue(){
        CorrelationVector cV = CorrelationVector.extend("tul4NUsfs9Cl7mOfN/dupsl.1");
        boolean hasErrors = InternalErrors.HasSavedErrors();
        Assert.assertFalse(hasErrors);

        CorrelationVector.ValidateCorrelationVectorDuringCreation = true;

        CorrelationVector cV2 = CorrelationVector.extend("tul4NUsfs9Cl7mOfN/dupsl.1");
        hasErrors = InternalErrors.HasSavedErrors();
        Assert.assertTrue(hasErrors);
    }

    // check length for correlation vector
    @Test public void throwWithTooBigCorrelationVectorValue(){
        CorrelationVector.ValidateCorrelationVectorDuringCreation = true;
        CorrelationVector cV = CorrelationVector.extend("tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.2147483647.2147483647");
        boolean hasErrors = InternalErrors.HasSavedErrors();
        Assert.assertTrue(hasErrors);
    }

    // check length for correlation vector V2
    @Test public void ThrowWithTooBigCorrelationVectorValueV2(){
        CorrelationVector.ValidateCorrelationVectorDuringCreation = true;
        CorrelationVector cV = CorrelationVector.extend("KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647");

        boolean hasErrors = InternalErrors.HasSavedErrors();
        Assert.assertTrue(hasErrors);
    }

    // check extension length for correlation vector
    @Test public void ThrowWithTooBigExtensionCorrelationVectorValue(){
        CorrelationVector.ValidateCorrelationVectorDuringCreation = true;

        CorrelationVector cV = CorrelationVector.extend("tul4NUsfs9Cl7mOf.11111111111111111111111111111");
        boolean hasErrors = InternalErrors.HasSavedErrors();
        Assert.assertTrue(hasErrors);
    }

    // Increment correlation vector to check for any errors
    @Test public void incrementPastMaxWithNoErrors(){
        CorrelationVector cV = CorrelationVector.extend("tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479");
        cV.increment();
        Assert.assertEquals(cV.getValue(),"tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479.1");

        for (int i = 0; i < 20; ++i)
        {
            cV.increment();
        }

        Assert.assertEquals(cV.getValue(),"tul4NUsfs9Cl7mOf.2147483647.2147483647.2147483647.21474836479.9");
    }

    // Increment correlation vector V2 to check for any errors
    @Test public void incrementPastMaxWithNoErrorsV2(){
        CorrelationVector cV = CorrelationVector.extend("KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214");
        cV.increment();
        Assert.assertEquals(cV.getValue(),"KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214.1");

        for (int i = 0; i < 20; ++i)
        {
            cV.increment();
        }

        Assert.assertEquals(cV.getValue(),"KZY+dsX2jEaZesgCPjJ2Ng.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.2147483647.214.9");
    }

    // Validate spin sort
    @Test public void spinSortValidation(){
        CorrelationVector cV = new CorrelationVector();

        SpinParameters params = new SpinParameters();
        params.setEntropy(SpinEntropy.Two);
        params.setInterval(SpinCounterInterval.Fine);
        params.setPeriodicity(SpinCounterPeriodicity.Short);

        long lastSpinValue = 0;
        int wrappedCounter = 0;

        for(int i = 0; i < 100; ++i){
            CorrelationVector cV2 = CorrelationVector.Spin(cV.getValue(),params);

            // The cV after a spin will look like <cvBase>.0.<spinValue>.0, so the spinValue is at index = 2
            String[] splitCv = cV2.getValue().split("\\.");
            long spinValue = Long.parseLong(splitCv[2]);

            // Count the number of times the counter wraps.
            if (spinValue <= lastSpinValue){
                wrappedCounter++;
            }

            lastSpinValue = spinValue;

            // Wait for 10ms
            try{
                Thread.sleep(10);
            }
            catch (InterruptedException ex){
                Thread.currentThread().interrupt();
            };
        }
    }

}
