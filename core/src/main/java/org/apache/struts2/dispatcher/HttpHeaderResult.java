/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.struts2.dispatcher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.opensymphony.xwork2.util.ValueStack;


/**
 * <!-- START SNIPPET: description -->
 *
 * A custom Result type for setting HTTP headers and status by optionally evaluating against the ValueStack.
 * This result can also be used to send and error to the client.
 *
 * <!-- END SNIPPET: description -->
 * <p/>
 * <b>This result type takes the following parameters:</b>
 *
 * <!-- START SNIPPET: params -->
 *
 * <ul>
 *
 * <li><b>status</b> - the http servlet response status code that should be set on a response.</li>
 *
 * <li><b>parse</b> - true by default. If set to false, the headers param will not be parsed for Ognl expressions.</li>
 *
 * <li><b>headers</b> - header values.</li>
 * 
 * <li><b>error</b> - the http servlet response error code that should be set on a response.</li>
 *
 * <li><b>errorMessage</b> - error message to be set on response if 'error' is set.</li>
 * </ul>
 *
 * <!-- END SNIPPET: params -->
 *
 * <b>Example:</b>
 *
 * <pre><!-- START SNIPPET: example -->
 * &lt;result name="success" type="httpheader"&gt;
 *   &lt;param name="status"&gt;204&lt;/param&gt;
 *   &lt;param name="headers.a"&gt;a custom header value&lt;/param&gt;
 *   &lt;param name="headers.b"&gt;another custom header value&lt;/param&gt;
 * &lt;/result&gt;
 * 
 * &lt;result name="proxyRequired" type="httpheader"&gt;
 *   &lt;param name="error"&gt;305&lt;/param&gt;
 *   &lt;param name="errorMessage"&gt;this action must be accessed through a prozy&lt;/param&gt;
 * &lt;/result&gt;
 * <!-- END SNIPPET: example --></pre>
 *
 */
public class HttpHeaderResult implements Result {

    private static final long serialVersionUID = 195648957144219214L;

    /** The default parameter */
    public static final String DEFAULT_PARAM = "status";


    private boolean parse = true;
    private Map<String,String> headers;
    private int status = -1;
    private int error = -1;
    private String errorMessage;
    
    public HttpHeaderResult() {
        super();
        headers = new HashMap<String,String>();
    }

    public HttpHeaderResult(int status) {
        this();
        this.status = status;
        this.parse = false;
    }


    /**
     * Sets the http servlet error code that should be set on the reponse
     * 
     * @param error the Http error code
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    public void setError(int error) {
        this.error = error;
    }
    
    /**
     * Sets the error message that should be set on the reponse
     * 
     * @param errorMessage error message send to the client
     * @see javax.servlet.http.HttpServletResponse#sendError(int, String)
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns a Map of all HTTP headers.
     *
     * @return a Map of all HTTP headers.
     */
    public Map getHeaders() {
        return headers;
    }

    /**
     * Sets whether or not the HTTP header values should be evaluated against the ValueStack (by default they are).
     *
     * @param parse <tt>true</tt> if HTTP header values should be evaluated agains the ValueStack, <tt>false</tt>
     *              otherwise.
     */
    public void setParse(boolean parse) {
        this.parse = parse;
    }

    /**
     * Sets the http servlet response status code that should be set on a response.
     *
     * @param status the Http status code
     * @see javax.servlet.http.HttpServletResponse#setStatus(int)
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Adds an HTTP header to the response
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * Sets the optional HTTP response status code and also re-sets HTTP headers after they've
     * been optionally evaluated against the ValueStack.
     *
     * @param invocation an encapsulation of the action execution state.
     * @throws Exception if an error occurs when re-setting the headers.
     */
    public void execute(ActionInvocation invocation) throws Exception {
        HttpServletResponse response = ServletActionContext.getResponse();
        ValueStack stack = ActionContext.getContext().getValueStack();
        
        if (status != -1) {
            response.setStatus(status);
        } else if (error != -1) {
            if (errorMessage != null) {
                String finalMessage = parse ? TextParseUtil.translateVariables(
                    errorMessage, stack) : errorMessage;
                response.sendError(error, finalMessage);
            } else
                response.sendError(error);
        }

        if (headers != null) {
            for (Iterator iterator = headers.entrySet().iterator();
                 iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String value = (String) entry.getValue();
                String finalValue = parse ? TextParseUtil.translateVariables(value, stack) : value;
                response.addHeader((String) entry.getKey(), finalValue);
            }
        }
    }
}
