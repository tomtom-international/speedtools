/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.mongodb.mappers;

import com.tomtom.speedtools.objects.Objects;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.util.Collection;

public class MapperTestUtils {

    public enum Color {
        RED,
        GREEN,
        BLUE,
        YELLOW
    }

    public static class ColorMapper extends EnumerationMapper<Color> {
        public final Value red = value(Color.RED, "red");
        public final Value green = value(Color.GREEN, "green");
        public final Value blue = value(Color.BLUE, "blue");
        public final Value yellow = value(Color.YELLOW, "yellow");
    }

    public static class Box {
        private final Color color;
        private final DateTime created;
        private final String name;
        private final double price;

        public Box(
                final Color color,
                final DateTime created,
                final String name,
                final double price) {
            super();
            this.color = color;
            this.created = created;
            this.name = name;
            this.price = price;
        }

        public Color getColor() {
            return color;
        }

        public DateTime getCreated() {
            return created;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public boolean canEqual(@Nonnull final Object obj) {
            assert obj != null;
            return obj instanceof Box;
        }

        @Override
        public boolean equals(final Object obj) {
            boolean eq;
            if (this == obj) {
                eq = true;
            } else if ((obj instanceof Box)) {
                final Box that = (Box) obj;
                eq = that.canEqual(this);
                eq = eq && color.equals(that.color);
                eq = eq && created.equals(that.created);
                eq = eq && name.equals(that.name);
                eq = eq && (Double.compare(price, that.price) == 0);
            } else {
                eq = false;
            }
            return eq;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(color, created, price);
        }
    }

    public static class BoxMapper extends EntityMapper<Box> {
        public final EntityType entityType = entityType(Box.class, "box");

        public final Field<Color> color = field("color", ColorMapper.class, "getColor", CONSTRUCTOR);
        public final Field<DateTime> created = dateTimeField("created", "getCreated", CONSTRUCTOR);
        public final Field<String> name = stringField("name", "getName", CONSTRUCTOR);
        public final Field<Double> price = doubleField("price", "getPrice", CONSTRUCTOR);
    }

    public static class Container extends Box {
        private final Collection<Box> boxes;
        private final Color shade;
        private final DateTime shipped;
        private final String line;
        private final double weight;

        public Container(
                final Color color,
                final DateTime created,
                final String name,
                final double price,
                final Collection<Box> boxes,
                final Color shade,
                final DateTime shipped,
                final String line,
                final double weight) {
            super(color, created, name, price);
            this.boxes = boxes;
            this.shade = shade;
            this.shipped = shipped;
            this.line = line;
            this.weight = weight;
        }

        public Collection<Box> getBoxes() {
            return boxes;
        }

        public Color getShade() {
            return shade;
        }

        public DateTime getShipped() {
            return shipped;
        }

        public String getLine() {
            return line;
        }

        public double getWeight() {
            return weight;
        }

        @Override
        public boolean canEqual(@Nonnull final Object obj) {
            assert obj != null;
            return obj instanceof Container;
        }

        @Override
        public boolean equals(final Object obj) {
            boolean eq;
            if (this == obj) {
                eq = true;
            } else if ((obj instanceof Container)) {
                final Container that = (Container) obj;
                eq = that.canEqual(this);
                eq = eq && boxes.equals(that.boxes);
                eq = eq && shade.equals(that.shade);
                eq = eq && shipped.equals(that.shipped);
                eq = eq && line.equals(that.line);
                eq = eq && (Double.compare(weight, that.weight) == 0);
            } else {
                eq = false;
            }
            return eq;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(boxes, shade, shipped, weight);
        }
    }

    public static class ContainerMapper extends EntityMapper<Container> {
        public final EntityType entityType = entityType(Container.class, "container");
        public final SuperEntity superEntity = superEntity(BoxMapper.class);

        public final Field<Collection<Box>> collection =
                collectionField("boxes", BoxMapper.class, "getBoxes", CONSTRUCTOR);
        public final Field<Color> shade = field("shade", ColorMapper.class, "getShade", CONSTRUCTOR);
        public final Field<DateTime> shipped = dateTimeField("shipped", "getShipped", CONSTRUCTOR);
        public final Field<String> line = stringField("line", "getLine", CONSTRUCTOR);
        public final Field<Double> weight = doubleField("weight", "getWeight", CONSTRUCTOR);
    }
}
