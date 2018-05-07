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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class InternalErrors {

    private static final int MaxSavedErrorsLimit = 100;

    private static AtomicLong MaxSavedErrorCount = new AtomicLong(1L);

    private static AtomicLong SavedErrorCount = new AtomicLong(0L);

    private static ConcurrentLinkedQueue<Exception> SavedErrors = new ConcurrentLinkedQueue<Exception>();

    public static void Report(Exception error)
    {
        if (MaxSavedErrorCount.get() == 0L)
        {
            return;
        }
        SavedErrors.add(error);
        SavedErrorCount.getAndIncrement();
        Purge();
    }

    private static void Purge()
    {

        long current = SavedErrorCount.get();
        while (current > MaxSavedErrorCount.get())
        {
            if (SavedErrors.poll()!= null)
            {
                current = SavedErrorCount.decrementAndGet();
            }
            else
            {
                break;
            }
        }
    }

    /**
     * <summary>
     *   True if any common schema API has reported an error since the last call to <see cref="ThrowSavedErrors"/>.
     * </summary>
     */
    public static boolean HasSavedErrors()
    {
        return SavedErrorCount.get() > 0L;
    }

    /**
     * <summary>
     *   Gets or sets the maximum number of errors this class will save between calls to
     *   <see cref="ThrowSavedErrors"/>. If this limit is breached, the oldest errors are
     *   removed first. Valid values range between 0 (save nothing) and 100. Any value
     *   set outside these bounds is set to the nearest legal value.
     * </summary>
     */
     public static int getMaxSavedErrors()
    {
        return (int)MaxSavedErrorCount.get();
    }

    public static void setMaxSavedErrors(int value)
    {
        long sanitized = value < 0 ? 0 : (value > MaxSavedErrorsLimit ? MaxSavedErrorsLimit : value);
        MaxSavedErrorCount.set(sanitized);
        InternalErrors.Purge();
    }

    /**
     * <summary>
     *   Throws an <see cref="com.microsoft.correlationvector.AggregateException"/> with all the currently saved errors.
     *   If there are no errors, no exception is thrown. When this call throws, the saved
     *   errors are cleared.
     * </summary>
     */
    public static void throwSavedErrors(){
        long remainder = SavedErrorCount.get();
        if (remainder == 0L)
        {
            return;
        }
        int capacity = (int) InternalErrors.MaxSavedErrorCount.get();
        ArrayList<Exception> innerExceptions = new ArrayList<Exception>(capacity);
        while (remainder > 0L)
        {
            Exception innerException = SavedErrors.poll();

            if (innerException != null)
            {
                remainder = SavedErrorCount.decrementAndGet();
                innerExceptions.add(innerException);
            }
            else
            {
                break;
            }
        }
        throw new AggregateException(innerExceptions);
    }

    /**
     * <summary>
     *   Clears all the saved errors and set the saved error count to 0.
     * </summary>
     */

    public static void clearErrors(){
        SavedErrors.clear();
        SavedErrorCount.set(0);
    }
}

