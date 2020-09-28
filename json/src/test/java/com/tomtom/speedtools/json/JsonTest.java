/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomtom.speedtools.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.objects.Objects;
import com.tomtom.speedtools.testutils.ValidationFailException;
import com.tomtom.speedtools.testutils.constructorchecker.ConstructorChecker;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.image.BufferedImage;
import java.util.*;

@SuppressWarnings({"deprecation", "OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class JsonTest {
    private static final Logger LOG = LoggerFactory.getLogger(JsonTest.class);

    private static final String RECT_JSON =
            "{\"southWest\":{\"lat\":1.0,\"lon\":2.0},\"northEast\":{\"lat\":3.0,\"lon\":4.0}}";
    private static final String INT_JSON = "1";

    public enum MyEnumType {
        A, B, C, D;

        private int a;
        private int b;
    }

    ;

    @Test
    public void testJsonEnumMap() throws Exception {
        LOG.info("testJsonEnumMap");

        final Map<MyEnumType, Integer> x = new EnumMap<>(MyEnumType.class);
        x.put(MyEnumType.A, 100);
        x.put(MyEnumType.B, 200);
        x.put(MyEnumType.C, 300);
        x.put(MyEnumType.D, 400);

        final String s = Json.toJson(x);
        LOG.info("x.toJson = {}", s);
        Assert.assertEquals(
                "{\"A\":100,\"B\":200,\"C\":300,\"D\":400}",
                s);
    }

    @Test
    public void testJsonArray() throws Exception {
        LOG.info("testJsonArray");

        final int[] x = {1, 2, 3};
        final String s1 = Json.toJson(x);
        LOG.info("x.toJson = {}", s1);
        Assert.assertEquals("[1,2,3]", s1);

        final ArrayList<Integer> y = new ArrayList<>();
        y.add(10);
        y.add(20);
        y.add(30);
        final String s2 = Json.toJson(y);
        LOG.info("y.toJson = {}", s2);
        Assert.assertEquals("[10,20,30]", s2);

        final List<List<Integer>> z = new ArrayList<>();
        final List<Integer> a = new ArrayList<>();
        a.add(10);
        a.add(20);
        a.add(30);
        final List<Integer> b = new ArrayList<>();
        b.add(100);
        final List<Integer> c = Collections. EMPTY_LIST;
        z.add(a);
        z.add(b);
        z.add(c);
        final String s3 = Json.toJson(z);
        LOG.info("z.toJson = {}", s3);
        Assert.assertEquals("[[10,20,30],[100],[]]", s3);
    }

    @Test
    public void testJsonJaxbAwareAbstract() throws Exception {
        LOG.info("testJsonJaxbAware");

        final SomeConcrete x = new SomeConcrete(1, "x");
        final String s = Json.toJson(x);
        LOG.info("x.toJson = {}", s);
        Assert.assertEquals(
                "{\"CONCRETE\":{\"value\":1,\"name\":\"x\"}}",
                s);
    }

    @Test
    public void testFromJsonJaxbAwareAbstract() {
        LOG.info("testFromJsonJaxbAwareAbstract");

        final SomeConcrete x = new SomeConcrete(1, "x");
        final String x1 = Json.toJson(x);
        LOG.info("x1.toJson = {}", x1);
        final SomeConcrete x2 = Json.fromJson(x1, SomeConcrete.class);
        Assert.assertNotNull(x2);
        Assert.assertEquals(x.a, x2.a);
        Assert.assertEquals(x.x, x2.x);

        final SomeOther a = new SomeOther(2, "y");
        final String a1 = Json.toJson(a);
        LOG.info("a.toJson = {}", a1);
        final SomeAbstract a2 = Json.fromJson(a1, SomeAbstract.class);
        Assert.assertNotNull(a2);
        Assert.assertTrue(a2 instanceof SomeOther);
        LOG.info("a1 = {}", a1);
        Assert.assertEquals(a.a, a2.a);
        Assert.assertEquals(a.x, ((SomeOther) a2).x);

        final Simple z = new Simple(3, "z");
        final String z1 = Json.toJson(z);
        LOG.info("z.toJson = {}", z1);
        final Simple z2 = Json.fromJson(z1, Simple.class);
        Assert.assertNotNull(z2);
    }

    @Test
    public void testToJsonAbstract() {
        LOG.info("testToJsonAbstract");

        final SomeConcrete x = new SomeConcrete(1, "x");
        final String s = Json.toJson(x);
        LOG.info("x.toJson = {}", s);
        Assert.assertEquals(
                "{\"CONCRETE\":{\"value\":1,\"name\":\"x\"}}",
                s);
    }

    @Test
    public void testFromJsonAbstract1() {
        LOG.info("testFromJsonAbstract1");

        final SomeConcrete x = new SomeConcrete(1, "x");
        final String x1 = Json.toJson(x);
        LOG.info("x.toJson = {}", x1);
        final SomeConcrete x2 = Json.fromJson(x1, SomeConcrete.class);
        Assert.assertNotNull(x2);
        LOG.info("x2 = {}", x1);
        Assert.assertEquals(x.a, x2.a);
        Assert.assertEquals(x.x, x2.x);
    }

    @Test
    public void testFromJsonAbstract2() {
        LOG.info("testFromJsonAbstract2");

        final SomeAbstract x = new SomeConcrete(1, "x");
        final String x1 = Json.toJson(x);
        LOG.info("x.toJson = {}", x1);
        final SomeAbstract x2 = Json.fromJson(x1, SomeAbstract.class);
        Assert.assertNotNull(x2);
        LOG.info("x2 = {}", x1);
        Assert.assertEquals(x.a, x2.a);
    }

    @Test
    public void testToJsonRegularCases() throws ValidationFailException {
        LOG.info("testToJsonRegularCases");

        ConstructorChecker.validateMethod(Json.class, "toJson", INT_JSON);

        final Integer i = 1;
        final String x0 = Json.toJson(i);
        LOG.info("Json Integer: " + x0);
        Assert.assertEquals(INT_JSON, x0);

        final GeoRectangle a = new GeoRectangle(new GeoPoint(1.0, 2.0), new GeoPoint(3.0, 4.0));
        final String x1 = Json.toJson(a);
        LOG.info("Json GeoRectangle: " + x1);
        Assert.assertEquals(RECT_JSON, x1);

        final Uid<Integer> uid = new Uid<>("1-2-3-4-5");
        final String x2 = Json.toJson(uid);
        LOG.info("Json Uid: " + x2);
        Assert.assertEquals(
                "{\"uuid\":\"00000001-0002-0003-0004-000000000005\"}",
                x2);

        final Date d = new Date(Date.UTC(112, 4, 27, 11, 29, 10));
        final String x3 = Json.toJson(d);
        LOG.info("Json Date: " + x3);
        Assert.assertEquals(
                "\"2012-05-27T11:29:10.000+00:00\"",
                x3);

        // TODO: UTCTime parsing errors
        final DateTime e =
                new DateTime(2012, 4, 27, 11, 29, 10, DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Amsterdam")));
        final String x4 = Json.toJson(e);
        LOG.info("Json DateTime: " + x4);
        Assert.assertEquals(
                "\"2012-04-27T11:29:10.000+0200\"",
                x4);
    }

    @Test
    public void testFromJsonRegularCases() throws ValidationFailException {
        LOG.info("testFromJsonRegularCases");

        ConstructorChecker.validateMethod(Json.class, "fromJson", INT_JSON, Integer.class);

        Json.fromJson(INT_JSON, Integer.class);
        LOG.info("Expecting 1 error now: 'Cannot map JSON...'");
        final Integer i = Json.fromJson(RECT_JSON, Integer.class);
        Assert.assertNull(i);

        final Uid<Integer> uid = new Uid<>("1-2-3-4-5");
        final String x1 = Json.toJson(uid);
        LOG.info("Json Uid: {}", x1);
        final Uid<?> x2 = Json.fromJson(x1, Uid.class);
        Assert.assertNotNull(x2);
        final String x3 = x2.toString();
        Assert.assertEquals("00000001-0002-0003-0004-000000000005", x3);

        final Date w1 = new Date(Date.UTC(112, 1, 2, 3, 4, 5));
        final String w2 = Json.toJson(w1);
        final Date w3 = Json.fromJson(w2, Date.class);
        Assert.assertNotNull(w3);
        LOG.info("Json Date: {} == {} / {}", w1, w3, w2);
        Assert.assertEquals(w1.getTime(), w3.getTime());

        final DateTime v1 = new DateTime(2012, 1, 2, 3, 4, 5, 678);
        final String v2 = Json.toJson(v1);
        final DateTime v3 = Json.fromJson(v2, DateTime.class);
        Assert.assertNotNull(v3);
        LOG.info("Json DateTime: " + v1 + " == " + v3 + " / " + v2);
        Assert.assertEquals(v1.getMillis(), v3.getMillis());

        final GeoPoint q = new GeoPoint(-1.0, -2.0);
        final String z1 = Json.toJson(q);
        LOG.info("Json GeoPoint 1: {}", z1);
        final GeoPoint r = Json.fromJson(z1, GeoPoint.class);
        Assert.assertNotNull(r);
        LOG.info("Json GeoPoint 2: {}", r);
        Assert.assertEquals(q, r);
    }

    @Test
    public void testToStringJson() {
        LOG.info("testToStringJson");
        final GeoRectangle a = new GeoRectangle(new GeoPoint(1.0, 2.0), new GeoPoint(3.0, 4.0));
        final String x1 = Json.toStringJson(a);
        Assert.assertEquals(
                "{\"southWest\":{\"lat\":1.0,\"lon\":2.0},\"northEast\":{\"lat\":3.0,\"lon\":4.0}}",
                x1);
        Assert.assertEquals("null", Json.toStringJson(null));
    }

    @Test
    public void testFromJsonMixIn() throws ValidationFailException {
        LOG.info("testFromJsonMixIn");
        Json.getCurrentJsonObjectMapper().addMixInAnnotations(GeoPoint.class, MixInGeoPoint.class);

        final GeoPoint a = new GeoPoint(1.0, 2.0);
        final String y1 = Json.toJson(a);
        LOG.info("Json GeoPoint 1: {}", y1);
        final GeoPoint b = Json.fromJson(y1, GeoPoint.class);
        Assert.assertNotNull(b);
        LOG.info("Json GeoPoint 2: {}", b);
        Assert.assertEquals(a, b);

        Json.getCurrentJsonObjectMapper().addMixInAnnotations(GeoRectangle.class, MixInGeoRectangle.class);

        final GeoRectangle c = new GeoRectangle(new GeoPoint(3.0, 4.0), new GeoPoint(5.0, 6.0));
        final String y2 = Json.toJson(c);
        LOG.info("Json GeoRectangle 1: {}", y2);
        final GeoRectangle d = Json.fromJson(y2, GeoRectangle.class);
        Assert.assertNotNull(d);
        LOG.info("Json GeoRectangle 2: {}", d);
        Assert.assertEquals(c, d);
    }

    @Test
    public void testDateTimeMixIn() throws Exception {
        LOG.info("testDateTimeMixIn");

        final DateTime time1 = new DateTime(2010, 3, 7, 6, 30, 15, 10, DateTimeZone.UTC);
        LOG.info("time = {}", time1);
        final String json1 = Json.toJson(time1);
        LOG.info("time.toJson = {}", json1);

        Assert.assertEquals(
                "\"2010-03-07T06:30:15.010+0000\"",
                json1);

        final DateTime time2 = Json.fromJson(json1, DateTime.class);
        Assert.assertNotNull(time2);
        Assert.assertEquals(time1.toDate(), time2.toDate());
    }

    //
    // Mix-in annotations for Jackson.
    //
    //
    @Test
    public void testImageMixIn() throws Exception {
        LOG.info("testImageMixIn");

        @SuppressWarnings("IOResourceOpenedButNotSafelyClosed") final BufferedImage image1 = ImageIO.read(this.getClass().getResourceAsStream("jsontest.png"));
        LOG.info("image = {}", image1);
        final String json2 = Json.toJson(image1);
        LOG.info("image.toJson = {}", json2);

        Assert.assertTrue(json2.startsWith("\"iVBORw0KGgoAAAANSUhEUgAAALwAAAAbCAYAAADGUOX9AAAOZ0lEQVR42u2c"));
        Assert.assertTrue(json2.endsWith("IIug/T0xKHk+QOxoDXaf/AeOQWjzHxhS4AAAAAElFTkSuQmCC\""));

        final BufferedImage image2 = Json.fromJson(json2, BufferedImage.class);
        Assert.assertNotNull(image2);
        Assert.assertEquals(image1.getHeight(), image2.getHeight());
        Assert.assertEquals(image1.getWidth(), image2.getWidth());
        for (int x = 0; x < image1.getWidth(); ++x) {
            for (int y = 0; y < image1.getHeight(); ++y) {
                Assert.assertEquals(image1.getRGB(x, y), image2.getRGB(x, y));
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    public static abstract class MixInGeoPoint {
        @JsonCreator
        protected MixInGeoPoint(
                @JsonProperty("lat") final Double lat,
                @JsonProperty("lon") final Double lon) {
            LOG.debug("lat={}, lon={}", lat, lon);
        }
    }

    @SuppressWarnings("UnusedParameters")
    public static abstract class MixInGeoRectangle {
        @JsonCreator
        protected MixInGeoRectangle(
                @JsonProperty("southWest") final GeoPoint southWest,
                @JsonProperty("northEast") final GeoPoint northEast) {
            LOG.debug("southWest={}, northEast={}", southWest, northEast);
        }
    }


    @XmlRootElement(name = "abstract")
    @XmlAccessorType(XmlAccessType.FIELD)
    @JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @Type(value = SomeConcrete.class, name = "CONCRETE"),
            @Type(value = SomeOther.class, name = "OTHER")
    })
    public static class SomeAbstract {

        @XmlElement(name = "value")
        @Nullable
        public final Integer a;

        public SomeAbstract(@Nullable final Integer a) {
            this.a = a;
        }

        protected SomeAbstract() {
            this.a = null;
        }
    }

    public static final class SomeConcrete extends SomeAbstract {

        @XmlElement(name = "name")
        @Nullable
        public final String x;

        public SomeConcrete(@Nullable final Integer a, @Nullable final String x) {
            super(a);
            this.x = x;
        }

        @SuppressWarnings("UnusedDeclaration")
        private SomeConcrete() {
            this.x = null;
        }
    }

    public static final class SomeOther extends SomeAbstract {

        @XmlElement(name = "name")
        @Nullable
        public final String x;

        public SomeOther(@Nullable final Integer a, @Nullable final String x) {
            super(a);
            this.x = x;
        }

        @SuppressWarnings("UnusedDeclaration")
        private SomeOther() {
            this.x = null;
        }
    }

    @XmlRootElement(name = "simple")
    @XmlAccessorType(XmlAccessType.FIELD)
    @JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
    @JsonSubTypes(@Type(value = Simple.class, name = "SIMPLE"))
    public static final class Simple {
        @Nonnull
        public final SomeAbstract[] myAbstract;

        @XmlElement(name = "name")
        @Nullable
        public final String x;

        public Simple(@Nullable final Integer a, @Nullable final String x) {
            assert a != null;
            this.x = x;
            this.myAbstract = new SomeAbstract[]{new SomeConcrete(123, "abc"), new SomeOther(456, "def")};
        }

        @SuppressWarnings({"UnusedDeclaration", "ConstantConditions"})
        private Simple() {
            this.x = null;
            this.myAbstract = null;
        }
    }

    @SuppressWarnings("PackageVisibleField")
    public static class GeoPoint {
        final double lat;
        final double lon;

        public GeoPoint() {
            lat = 0.0;
            lon = 0.0;
        }

        public GeoPoint(@Nonnull final Double lat, @Nonnull final Double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @SuppressWarnings("FloatingPointEquality")
        @Override
        public boolean equals(@Nullable final Object obj) {
            if (obj instanceof GeoPoint) {
                final GeoPoint that = (GeoPoint) obj;
                return (lat == that.lat) && (lon == that.lon);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(lat, lon);
        }
    }

    @SuppressWarnings("PackageVisibleField")
    public static class GeoRectangle {
        @Nonnull
        final GeoPoint southWest;
        @Nonnull
        final GeoPoint northEast;

        @SuppressWarnings("ConstantConditions")
        public GeoRectangle() {
            southWest = null;
            northEast = null;
        }

        public GeoRectangle(@Nonnull final GeoPoint southWest, @Nonnull final GeoPoint northEast) {
            this.southWest = southWest;
            this.northEast = northEast;
        }

        @SuppressWarnings("FloatingPointEquality")
        @Override
        public boolean equals(@Nullable final Object obj) {
            if (obj instanceof GeoRectangle) {
                final GeoRectangle that = (GeoRectangle) obj;
                return southWest.equals(that.southWest) && northEast.equals(that.northEast);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(southWest, northEast);
        }
    }
}
