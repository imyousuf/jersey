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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.ClientResponse;
import java.util.GregorianCalendar;
import javax.ws.rs.GET;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class PreconditionTest extends AbstractResourceTester {
    
    public PreconditionTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class LastModifiedResource {
        @Context Request request;

        @GET
        public Response doGet() {
            GregorianCalendar lastModified = new GregorianCalendar(2007, 0, 0, 0, 0, 0);
            ResponseBuilder rb = request.evaluatePreconditions(lastModified.getTime());
            if (rb != null)
                return rb.build();
            
            return Response.ok("foo", "text/plain").build();
        }
    }
    
    public void testIfUnmodifiedSinceBeforeLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ClientResponse response = resource("/", false).
                header("If-Unmodified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(412, response.getStatus());
    }    

    public void testIfUnmodifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ClientResponse response = resource("/").
                header("If-Unmodified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }    

    public void testIfModifiedSinceBeforeLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ClientResponse response = resource("/").
                header("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }    

    public void testIfModifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ClientResponse response = resource("/", false).
                header("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(304, response.getStatus());
    }    

    public void testIfUnmodifiedSinceBeforeLastModified_IfModifiedSinceBeforeLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ClientResponse response = resource("/", false).
                header("If-Unmodified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                header("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(412, response.getStatus());
    }    

    public void testIfUnmodifiedSinceBeforeLastModified_IfModifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ClientResponse response = resource("/", false).
                header("If-Unmodified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                header("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(412, response.getStatus());
    }    
    
    public void testIfUnmodifiedSinceAfterLastModified_IfModifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ClientResponse response = resource("/", false).
                header("If-Unmodified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                header("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(304, response.getStatus());
    }    

    public void testIfUnmodifiedSinceAfterLastModified_IfModifiedSinceBeforeLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ClientResponse response = resource("/").
                header("If-Unmodified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                header("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }
        
    
    @Path("/")
    public static class EtagResource {
        @Context Request request;

        @GET
        public Response doGet() {
            ResponseBuilder rb = request.evaluatePreconditions(new EntityTag("1"));
            if (rb != null)
                return rb.build();
            
            return Response.ok("foo", "text/plain").build();
        }
    }

    public void testIfMatchWithMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/").
                header("If-Match", "\"1\"").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());        
    }
    
    public void testIfMatchWithoutMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-Match", "\"2\"").
                get(ClientResponse.class);
        assertEquals(412, response.getStatus());        
    }
    
    public void testIfMatchWildCard() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/").
                header("If-Match", "*").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());        
    }
    
    public void testIfNonMatchWithMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-None-Match", "\"1\"").
                get(ClientResponse.class);
        assertEquals(304, response.getStatus());
        assertEquals(new EntityTag("1"), response.getEntityTag());
    }
    
    public void testIfNonMatchWithoutMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/").
                header("If-None-Match", "\"2\"").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }
    
    public void testIfNonMatchWildCard() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-None-Match", "*").
                get(ClientResponse.class);
        assertEquals(304, response.getStatus());
        assertEquals(new EntityTag("1"), response.getEntityTag());
    }
    
    
    public void testIfMatchWithMatchingETag_IfNonMatchWithMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-Match", "\"1\"").
                header("If-None-Match", "\"1\"").
                get(ClientResponse.class);
        assertEquals(304, response.getStatus());
        assertEquals(new EntityTag("1"), response.getEntityTag());
    }
    
    public void testIfMatchWithMatchingETag_IfNonMatchWithoutMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/").
                header("If-Match", "\"1\"").
                header("If-None-Match", "\"2\"").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }
    
    public void testIfMatchWithoutMatchingETag_IfNonMatchWithMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-Match", "\"2\"").
                header("If-None-Match", "\"1\"").
                get(ClientResponse.class);
        assertEquals(412, response.getStatus());
    }
    
    public void testIfMatchWithoutMatchingETag_IfNonMatchWithoutMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-Match", "\"2\"").
                header("If-None-Match", "\"2\"").
                get(ClientResponse.class);
        assertEquals(412, response.getStatus());
    }
    
    @Path("/")
    public static class LastModifiedEtagResource {
        @Context Request request;

        @GET
        public Response doGet() {
            GregorianCalendar lastModified = new GregorianCalendar(2007, 0, 0, 0, 0, 0);
            ResponseBuilder rb = request.evaluatePreconditions(
                    lastModified.getTime(),
                    new EntityTag("1"));
            if (rb != null)
                return rb.build();
            
            return Response.ok("foo", "text/plain").build();
        }
    }
    
    public void testIfNonMatchWithMatchingETag_IfModifiedSinceBeforeLastModified() {
        initiateWebApplication(LastModifiedEtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-None-Match", "\"1\"").
                header("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }
    
    public void testIfNonMatchWithMatchingETag_IfModifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedEtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-None-Match", "\"1\"").
                header("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(304, response.getStatus());
    }
    
    public void testIfNonMatchWithoutMatchingETag_IfModifiedSinceBeforeLastModified() {
        initiateWebApplication(LastModifiedEtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-None-Match", "\"2\"").
                header("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }
    
    public void testIfNonMatchWithoutMatchingETag_IfModifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedEtagResource.class);
        ClientResponse response = resource("/", false).
                header("If-None-Match", "\"2\"").
                header("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }
}
