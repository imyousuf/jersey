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
package com.sun.jersey.spi.resource;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceConstructor;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.service.ComponentConstructor;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A utility class to obtain the most suitable constructor for a 
 * resource class.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ResourceConstructor extends ComponentConstructor {
    private final ServerInjectableProviderContext sipc;

    public ResourceConstructor(ServerInjectableProviderContext sipc) {
        super(sipc);
        this.sipc = sipc;
    }

    /**
     * Get the most suitable constructor. The constructor with the most
     * parameters and that has the most parameters associated with 
     * Injectable instances will be chosen.
     * 
     * @param <T> the type of the resource.
     * @param c the class to instantiate.
     * @param ar the abstract resource.
     * @param s the scope for which the injectables will be used.
     * @return a list constructor and list of injectables for the constructor
     *         parameters.
     */
    @SuppressWarnings("unchecked")
    public <T> ConstructorInjectablePair<T> getConstructor(Class<T> c, AbstractResource ar,
            Scope s) {
        if (ar.getConstructors().isEmpty())
            return null;
        
        SortedSet<ConstructorInjectablePair<T>> cs = new TreeSet<ConstructorInjectablePair<T>>(
                new ConstructorComparator());        
        for (AbstractResourceConstructor arc : ar.getConstructors()) {
            List<Injectable> is = sipc.getInjectable(arc.getParameters(), s);
            cs.add(new ConstructorInjectablePair<T>(arc.getCtor(), is));
        }
                
        return cs.first();        
    }
 }