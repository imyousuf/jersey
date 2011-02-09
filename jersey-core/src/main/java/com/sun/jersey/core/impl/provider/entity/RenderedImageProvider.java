/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.core.impl.provider.entity;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Produces("image/*")
@Consumes({"image/*", "application/octet-stream"})
public final class RenderedImageProvider extends AbstractMessageReaderWriterProvider<RenderedImage> {
    private static final MediaType IMAGE_MEDIA_TYPE = new MediaType("image", "*");

    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RenderedImage.class == type || BufferedImage.class == type;
    }
    
    public RenderedImage readFrom(
            Class<RenderedImage> type,
            Type genericType, 
            Annotation annotations[],
            MediaType mediaType, 
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {
        if (IMAGE_MEDIA_TYPE.isCompatible(mediaType)) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mediaType.toString());
            if (!readers.hasNext())
                throw new IOException("The image-based media type " + mediaType + "is not supported for reading");
            ImageReader reader = readers.next();

            ImageInputStream in = ImageIO.createImageInputStream(entityStream);
            reader.setInput(in, true, true);
            BufferedImage bi = reader.read(0, reader.getDefaultReadParam());
            in.close();
            reader.dispose();
            return bi;
        } else {
            return ImageIO.read(entityStream);
        }
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RenderedImage.class.isAssignableFrom(type);
    }
    
    public void writeTo(
            RenderedImage t,
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        String formatName = getWriterFormatName(mediaType);
        if (formatName == null) 
            throw new IOException("The image-based media type " + mediaType + " is not supported for writing");
        ImageIO.write(t, formatName, entityStream);
    }

    private String getWriterFormatName(MediaType t) {
        return getWriterFormatName(t.toString());
    }

    private String getWriterFormatName(String t) {
        Iterator<ImageWriter> i = ImageIO.getImageWritersByMIMEType(t);
        if (!i.hasNext()) return null;

        return i.next().getOriginatingProvider().getFormatNames()[0];
    }
}