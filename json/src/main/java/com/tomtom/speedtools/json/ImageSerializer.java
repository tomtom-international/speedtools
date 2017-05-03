/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomtom.speedtools.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public final class ImageSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(ImageSerializer.class);

    private ImageSerializer() {
        // Prevent instantiation.
    }

    @JsonSerialize(using = ToBytesSerializer.class)
    @JsonDeserialize(using = FromBytesDeserializer.class)
    public interface BytesMixIn {
        // Empty.
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public static class ToBytesSerializer extends JsonSerializer<Image> {

        @Override
        public void serialize(
                @Nonnull final Image t,
                @Nonnull final JsonGenerator jsonGenerator,
                @Nonnull final SerializerProvider serializerProvider)
                throws IOException {
            assert t != null;
            assert jsonGenerator != null;
            assert serializerProvider != null;
            final byte[] b64 = writeAsBytes(t);
            jsonGenerator.writeBinary(b64);
        }
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public static class FromBytesDeserializer extends JsonDeserializer<Image> {

        @Override
        @Nullable
        public Image deserialize(
                @Nonnull final JsonParser jsonParser,
                @Nonnull final DeserializationContext deserializationContext)
                throws IOException {
            assert jsonParser != null;
            assert deserializationContext != null;
            final byte[] bytes = jsonParser.getBinaryValue();
            final BufferedImage image = readFromBytes(bytes);
            return image;
        }
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public static class FromBytesDeserializerForBufferedImage extends JsonDeserializer<BufferedImage> {

        @Override
        @Nullable
        public BufferedImage deserialize(
                @Nonnull final JsonParser jsonParser,
                @Nonnull final DeserializationContext deserializationContext)
                throws IOException {
            assert jsonParser != null;
            assert deserializationContext != null;
            final byte[] bytes = jsonParser.getBinaryValue();
            final BufferedImage image = readFromBytes(bytes);
            return image;
        }
    }

    @Nullable
    private static BufferedImage readFromBytes(@Nonnull final byte[] bytes) throws IOException {
        assert bytes != null;
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(is);
        }
    }

    @Nonnull
    private static byte[] writeAsBytes(@Nonnull final Image v) throws IOException {
        assert v != null;

        /**
         * Create a PNG output stream.
         */
        final String mimeType = "image/png";
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            final Iterator<ImageWriter> it = ImageIO.getImageWritersByMIMEType(mimeType);
            if (it.hasNext()) {
                final ImageWriter w = it.next();
                final ImageOutputStream os = ImageIO.createImageOutputStream(stream);
                w.setOutput(os);
                w.write(convertToBufferedImage(v));
                os.close();
                w.dispose();
            } else {
                LOG.info("writeAsBytes: No encoder for MIME type " + mimeType);
                throw new IOException("No encoder for MIME type " + mimeType);
            }
            return stream.toByteArray();
        }
    }

    @Nonnull
    private static BufferedImage convertToBufferedImage(@Nonnull final Image image) throws IOException {
        assert image != null;

        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        /**
         * Load the image in the background and wait
         * until is is downloaded.
         */
        final MediaTracker tracker = new MediaTracker(
                new Component() {
                    // Empty.
                });
        tracker.addImage(image, 0);
        try {
            tracker.waitForAll();
        } catch (final InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }

        /**
         * Create a buffered image with the right dimensions.
         */
        final BufferedImage bufImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        /**
         * Draw the image in the buffer and return it as base64 data.
         */
        final Graphics g = bufImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        return bufImage;
    }
}
