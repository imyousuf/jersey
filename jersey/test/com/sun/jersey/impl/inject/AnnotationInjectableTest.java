/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */
package com.sun.jersey.impl.inject;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.inject.InjectableContext;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.SingletonInjectable;
import java.lang.reflect.Type;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Apr 12, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class AnnotationInjectableTest extends AbstractResourceTester {
    
    public AnnotationInjectableTest(String testName) {
        super( testName );
    }

    @Target({FIELD, PARAMETER, CONSTRUCTOR })
    @Retention(RUNTIME)
    @Documented
    public static @interface MyAnnotation {
    }
    
    public static class MyAnnotationInjectableProvider implements 
            InjectableProvider<MyAnnotation, Type, SingletonInjectable<String>> {        
        final String value;
        
        public MyAnnotationInjectableProvider(String value) {
            this.value = value;
        }
        
        public SingletonInjectable<String> getInjectable(InjectableContext ic, MyAnnotation a, Type c) {
            return new SingletonInjectable<String>() {
                public String getValue(HttpContext c) {
                    return value;
                }                    
            };
        }        
    }
    
    @Path("/")
    public static class FieldInjected {
        @MyAnnotation String injectedValue;
        
        @GET
        public String get() {
            return injectedValue;
        }                
    }
    
    @Override
    protected void initiate(WebApplication a) {
        a.addInjectable(new MyAnnotationInjectableProvider("foo"));        
    }
    
    public void testFieldInjected() throws IOException {                
        initiateWebApplication(FieldInjected.class);
        
        assertEquals("foo", resource("/").get(String.class));   
    }
    
    @Path("/")
    public static class MethodInjected {
        @GET
        public String get(@MyAnnotation String injectedValue) {
            return injectedValue;
        }                
    }
    
    public void testMethodInjected() throws IOException {                
        initiateWebApplication(MethodInjected.class);
                
        assertEquals("foo", resource("/").get(String.class));   
    }
    
    @Path("/")
    public static class ConstructorInjected {
        String injectedValue;
        
        public ConstructorInjected(@MyAnnotation String injectedValue) {
            this.injectedValue = injectedValue;
        }
        
        @GET
        public String get() {
            return injectedValue;
        }                
    }
    
    public void testConstructorInjected() throws IOException {                
        initiateWebApplication(ConstructorInjected.class);
                
        assertEquals("foo", resource("/").get(String.class));   
    }
}
