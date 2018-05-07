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

public class AggregateException extends RuntimeException {

    private final ArrayList<Exception> innerExceptions;

    public AggregateException(ArrayList<Exception> exceptions){
        super("An aggregate exception occured");
        innerExceptions = exceptions;
    }

    public ArrayList<Exception> getInnerExceptions(){
        return  innerExceptions;
    }
}
