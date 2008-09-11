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
package com.sun.jersey.impl.container.grizzly;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class GZIPContentEncodingTest extends AbstractGrizzlyServerTester {
    
    @Path("/")
    public static class Resource {
        @GET
        public String get() { return "GET"; }
        
        @POST
        public String post(String content) { return content; }
    }
    
    public GZIPContentEncodingTest(String testName) {
        super(testName);
    }
        
    
    public void testGet() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, 
                GZIPContentEncodingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, 
                GZIPContentEncodingFilter.class.getName());        
        startServer(rc);


        Client c = Client.create();
        c.addFilter(new com.sun.jersey.api.client.filter.GZIPContentEncodingFilter());
        WebResource r = c.resource(getUri().build());

        assertEquals("GET", r.get(String.class));
    }    

    public void testPost() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, 
                GZIPContentEncodingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, 
                GZIPContentEncodingFilter.class.getName());        
        startServer(rc);

        Client c = Client.create();
        c.addFilter(new com.sun.jersey.api.client.filter.GZIPContentEncodingFilter());
        WebResource r = c.resource(getUri().build());

        assertEquals("POST", r.post(String.class, "POST"));
    }    
}