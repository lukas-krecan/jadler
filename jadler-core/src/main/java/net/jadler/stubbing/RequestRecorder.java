/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;


/**
 * A component which records HTTP requests.
 */
public interface RequestRecorder {
    
    /**
     * Records a request
     * @param request http request to be recorded
     */
    void recordRequest(Request request);
}
