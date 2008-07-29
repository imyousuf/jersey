/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.uri.rules;

import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.impl.http.header.AcceptableMediaType;
import com.sun.jersey.impl.model.HttpHelper;
import com.sun.jersey.impl.model.method.ResourceMethod;
import com.sun.jersey.api.Responses;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRuleContext;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * The rule for accepting an HTTP method.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpMethodRule implements UriRule {
    private static final Logger LOGGER = 
            Logger.getLogger(HttpMethodRule.class.getName());

    private final Map<String, List<ResourceMethod>> map;
    
    private final String allow;
            
    private final boolean isSubResource;
    
    public HttpMethodRule(Map<String, List<ResourceMethod>> methods) {
        this(methods, false);
    }
          
    public HttpMethodRule(Map<String, List<ResourceMethod>> methods, boolean isSubResource) {
        this.map = methods;
        this.isSubResource = isSubResource;
        this.allow = getAllow(methods);
    }
    
    private String getAllow(Map<String, List<ResourceMethod>> methods) {
        StringBuilder s = new StringBuilder();
        boolean first = true;
        for (String method : methods.keySet()) {
            if (!first) s.append(",");
            first = false;
            
            s.append(method);
        }
        
        return s.toString();
    }

    public boolean accept(CharSequence path, Object resource, UriRuleContext context) {        
        // If the path is not empty then do not accept
        if (path.length() > 0) return false;
        
        final HttpRequestContext request = context.getRequest();
        final HttpResponseContext response = context.getResponse();
        final String httpMethod = request.getMethod();
        final MediaType contentType = HttpHelper.getContentType(request);
        
        // Get the list of resource methods for the HTTP method
        List<ResourceMethod> methods = map.get(httpMethod);
        if (methods == null) {
            // No resource methods are found
            response.setResponse(Responses.methodNotAllowed().
                    header("Allow", allow).build());
            // Allow any further matching rules to be processed
            return false;
        }

        // Get the list of matching methods
        List<MediaType> accept = request.getAcceptableMediaTypes();
        LinkedList<ResourceMethod> matches = 
            new LinkedList<ResourceMethod>();
        MatchStatus s = match(methods, contentType, accept, matches);
        if (s == MatchStatus.MATCH) {
            // If there is a match choose the first method
            ResourceMethod method = matches.get(0);

            // If a sub-resource method then need to push the resource
            // (again) as as to keep in sync with the ancestor URIs
            if (isSubResource) {
                context.pushResource(resource, method.getTemplate());        
                // Set the template values
                context.setTemplateValues(method.getTemplate().getTemplateVariables());
            }
            
            method.getDispatcher().dispatch(resource, context);

            // Verify the response
            // TODO verification for HEAD
            if (!httpMethod.equals("HEAD"))
                verifyResponse(method, accept, response);  
            return true;
        } else if (s == MatchStatus.NO_MATCH_FOR_CONSUME) {
            throw new WebApplicationException(Responses.unsupportedMediaType().build());
        } else if (s == MatchStatus.NO_MATCH_FOR_PRODUCE) {
            throw new WebApplicationException(Responses.notAcceptable().build());
        }
        
        return true;
    }

    private boolean hasMessageBody(MultivaluedMap<String, String> headers) {
        return (headers.getFirst("Content-Length") != null || 
                headers.getFirst("Transfer-Encoding") != null);
    }
        
    private enum MatchStatus {
        MATCH, NO_MATCH_FOR_CONSUME, NO_MATCH_FOR_PRODUCE
    }
    
    /**
     * Find the subset of methods that match the 'Content-Type' and 'Accept'.
     *
     * @param methods the list of resource methods
     * @param contentType the 'Content-Type'.
     * @param accept the 'Accept' as a list. This list
     *        MUST be ordered with the highest quality acceptable Media type 
     *        occuring first (see 
     *        {@link com.sun.ws.rest.impl.model.MimeHelper#ACCEPT_MEDIA_TYPE_COMPARATOR}).
     * @param matches the list to add the matches to.
     * @return the match status.
     */
    private MatchStatus match(List<ResourceMethod> methods,
            MediaType contentType,
            List<MediaType> accept,
            LinkedList<ResourceMethod> matches) {
                
        if (contentType != null) {
            // Find all methods that consume the MIME type of 'Content-Type'
            for (ResourceMethod method : methods)
                if (method.consumes(contentType))
                    matches.add(method);
            
            if (matches.isEmpty())
                return MatchStatus.NO_MATCH_FOR_CONSUME;
            
        } else {
            matches.addAll(methods);
        }

        // Find all methods that produce the one or more Media types of 'Accept'
        ListIterator<ResourceMethod> i = matches.listIterator();
        int currentQuality = AcceptableMediaType.MINUMUM_QUALITY;
        int currentIndex = 0;
        while(i.hasNext()) {
            int index = i.nextIndex();
            int quality = i.next().produces(accept);
            
            if (quality == -1) {
                // No match
                i.remove();
            } else if (quality < currentQuality) {
                // Match but of a lower quality than a previous match
                i.remove();
            } else if (quality > currentQuality) {
                // Match and of a higher quality than the pervious match
                currentIndex = index;
                currentQuality = quality;
            }
        }

        if (matches.isEmpty())
            return MatchStatus.NO_MATCH_FOR_PRODUCE;

        // Remove all methods of a lower quality at the 
        // start of the list
        while (currentIndex > 0) {
            matches.removeFirst();
            currentIndex--;
        }
        
        return MatchStatus.MATCH;
    }    
    
    private void verifyResponse(ResourceMethod method, 
            List<MediaType> accept,
            HttpResponseContext responseContext) {        
        Object entity = responseContext.getEntity();
        MediaType contentType = HttpHelper.getContentType(
                responseContext.getHttpHeaders().getFirst("Content-Type"));
        
        if (!responseContext.isCommitted() && contentType != null && entity == null) {
            String ct = contentType.toString();
            String error = "The \"Content-Type\" header is set to " + ct + ", but the response has no entity";
            LOGGER.severe(error);
            // TODO should this be ContainerException ???
            throw new WebApplicationException(Response.serverError().
                    entity(error).type("text/plain").build());            
        } else if (contentType != null && !method.produces(contentType)) {
            // Check if 'Content-Type' of the responseContext is a member of @Produces
            // The resource is not honoring the @Produces contract
            // The 'Content-Type' is not a member of @Produces.
            // Check if the 'Content-Type' is acceptable
            if (!HttpHelper.produces(contentType, accept)) {
                String error = ImplMessages.RESOURCE_NOT_ACCEPTABLE(
                        method,
                        contentType);
                LOGGER.severe(error);
                
                // The resource is returning a MIME type that is not acceptable
                // Return 500 Internal Server Error
                // TODO should this be ContainerException ???
                throw new WebApplicationException(Response.serverError().
                        entity(error).type("text/plain").build());
            } else {
                String error = ImplMessages.RESOURCE_MIMETYPE_NOT_IN_PRODUCE_MIME(
                        method,
                        contentType,
                        method.getProduces());
                LOGGER.warning(error);
            }
        }   
    }
}