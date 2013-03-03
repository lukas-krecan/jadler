/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * Request abstraction. It insulates the code from an implementation and serves as immutable copy to keep
 * values after the original instance has been recycled.
 */
public class Request {

    private final String method;

    private final URI requestUri;

    private final byte[] body;

    private final Map<String, List<String>> parameters;

    private final Map<String, List<String>> headers;

    private final InetSocketAddress localAddress;

    private final InetSocketAddress remoteAddress;

    private final String encoding;

    public Request(String method, URI requestUri, Map<String, List<String>> headers, InputStream body, InetSocketAddress localAddress, InetSocketAddress remoteAddress, String encoding) throws IOException {
        this.method = method;
        this.requestUri = requestUri;
        this.encoding = encoding!=null?encoding:"ISO-8859-1"; //HTTP default
        this.body = IOUtils.toByteArray(body);
        this.headers = copyHeaders(headers);
        this.parameters = readParameters();
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    private Map<String, List<String>> copyHeaders(Map<String, List<String>> headers) {
        if (headers==null) {
            return emptyMap();
        }
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for(Map.Entry<String, List<String>> header: headers.entrySet()) {
            result.put(header.getKey().toLowerCase(), unmodifiableList(new ArrayList<String>(header.getValue())));
        }
        return unmodifiableMap(result);
    }

    public String getMethod() {
        return method;
    }

    //TODO: confusing name
    public URI getRequestUri() {
        return requestUri;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> getHeaders(String name) {
        return headers.get(name.toLowerCase());
    }

    public String getFirstHeader(String name) {
        List<String> headerValues = getHeaders(name);
        return headerValues!=null&&!headerValues.isEmpty()?headerValues.get(0):null;
    }

    public InputStream getBody() {
        return new ByteArrayInputStream(body);
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    private String getEncodingInternal() {
        return encoding;
    }

    private String getContentType() {
        return getFirstHeader("content-type");
    }

    public String getEncoding() {
        return encoding;
    }

    public String getQueryString() {
        return requestUri!=null?requestUri.getQuery():null;
    }

    private Map<String, List<String>> readParameters() throws IOException {
           final MultiMap params = readParametersFromQueryString();

             //TODO: shitty attempt to check whether the body contains html form data. Please refactor.
           if (!StringUtils.isBlank(this.getContentType()) &&
               this.getContentType().contains("application/x-www-form-urlencoded")) {

               if ("POST".equalsIgnoreCase(this.getMethod()) || "PUT".equalsIgnoreCase(this.getMethod())) {
                   params.putAll(this.readParametersFromBody());
               }
           }

           final Map<String, List<String>> res = new HashMap<String, List<String>>();
           for(final Object o: params.entrySet()) {
               final Map.Entry<String, List<String>> e = (Map.Entry) o;
               res.put(e.getKey(), unmodifiableList(e.getValue()));
           }

           return unmodifiableMap(res);
       }


       private MultiMap readParametersFromQueryString() {
           return this.readParametersFromString(this.getQueryString());
       }


       private MultiMap readParametersFromBody() throws IOException {
           return this.readParametersFromString(new String(this.body, this.getEncodingInternal()));
       }


       private MultiMap readParametersFromString(final String parametersString) {
           final MultiMap res = new MultiValueMap();

           if (StringUtils.isBlank(parametersString)) {
               return res;
           }

           final String enc = this.getEncodingInternal();
           final String[] pairs = parametersString.split("&");

           for (final String pair : pairs) {
               final int idx = pair.indexOf('=');
               if (idx > -1) {

                   try {
                       final String name = URLDecoder.decode(StringUtils.substring(pair, 0, idx), enc);
                       final String value = URLDecoder.decode(StringUtils.substring(pair, idx + 1), enc);
                       res.put(name, value);
                   }
                   catch (final UnsupportedEncodingException ex) {
                       //indeed
                   }
               }
               else {
                   try {
                       res.put(URLDecoder.decode(pair, enc), "");
                   }
                   catch (final UnsupportedEncodingException ex) {
                       //no way
                   }
               }
           }

           return res;
       }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", requestUri=" + requestUri +
                ", parameters=" + parameters +
                ", headers=" + headers +
                '}';
    }
}
