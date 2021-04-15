/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class to print a database schema from registered mappers.
 */
public final class SchemaPrinter {
    @Nonnull
    private final MapperRegistry mapperRegistry;

    public SchemaPrinter(@Nonnull final MapperRegistry mapperRegistry) {
        assert mapperRegistry != null;
        this.mapperRegistry = mapperRegistry;
    }

    @Nonnull
    public String printHtml() {
        final StringBuilder out = new StringBuilder();
        out.append("<html>\n");
        out.append("  <head>\n");
        out.append("    <style type=\"text/css\" media=\"projection\">\n");
        out.append("      .entity { margin: 8px; border: 2px solid black; } \n");
        out.append("      table { padding: 0; border-spacing: 0; } \n");
        out.append("      td { vertical-align: top; } \n");
        out.append("      .field { padding: 4px; } \n");
        out.append("      .title { padding: 4px; text-align: center; font-weight: bold; border-bottom: " +
                "2px solid " +
                "black; } " +
                '\n');
        out.append("    </style>\n");
        out.append("  </head>\n");
        out.append("  <body>\n");
        printHtmlTopLevelEntities("    ", out);
        out.append("  </body>\n");
        out.append("</html>\n");
        return out.toString();
    }

    private void printHtmlTopLevelEntities(@Nonnull final String indent, @Nonnull final StringBuilder out) {
        assert indent != null;
        assert out != null;
        out.append(indent).append("<h1>Entities</h1>\n");
        for (final EntityMapper<?> mapper : findTopLevelEntityMappers()) {
            printHtmlEntityWithChildren(mapper, indent, out);
        }
    }

    private void printHtmlEntityWithChildren(@Nonnull final EntityMapper<?> mapper, @Nonnull final String indent, @Nonnull final StringBuilder out) {
        assert mapper != null;
        assert indent != null;
        assert out != null;
        final List<EntityMapper<?>> children = findChildEntityMappers(mapper);
        out.append(indent).append("<table>\n");
        out.append(indent).append("  <tr>\n");
        out.append(indent).append("    <td>\n");
        printHtmlEntity(mapper, indent + "      ", out);
        out.append(indent).append("    </td>\n");
        out.append(indent).append("    <td>\n");
        for (final EntityMapper<?> child : children) {
            printHtmlEntityWithChildren(child, indent + "      ", out);
        }
        out.append(indent).append("    </td>\n");
        out.append(indent).append("  </tr>\n");
        out.append(indent).append("</table>\n");
    }

    private void printHtmlEntity(@Nonnull final EntityMapper<?> mapper, @Nonnull final String indent, @Nonnull final StringBuilder out) {
        assert mapper != null;
        assert indent != null;
        assert out != null;
        out.append(indent).append("<div class=\"entity\">\n");
        out.append(indent).append("  <div class=\"title\">\n");
        out.append(indent).append("    ").append(displayName(mapper)).append('\n');
        out.append(indent).append("  </div>\n");
        out.append(indent).append("  <div class=\"fields\">\n");
        for (final EntityMapper<?>.Field<?> field : mapper.getFields()) {
            out.append(indent).append("    <div class=\"field\">\n");
            if (!isEntityMapper(field.getMapper())) {
                out.append(indent).append("      <span class=\"field-name\">").append(field.getFieldName());
                out.append(" : ").append(displayName(field.getMapper()));
                if (field.getMapper() instanceof CollectionMapper) {
                    out.append(" [0, n]");
                }
                out.append("</span>\n");

            }
            out.append(indent).append("    </div>\n");
        }
        out.append(indent).append("  </div>\n");
        out.append(indent).append("</div>\n");
    }

    @Nonnull
    private List<EntityMapper<?>> findEntityMappers() {
        final List<EntityMapper<?>> result = new ArrayList<>();
        for (final Mapper<?> mapper : mapperRegistry.getMappers()) {
            if (mapper instanceof EntityMapper) {
                result.add((EntityMapper<?>) mapper);
            }
        }
        return result;
    }

    @Nonnull
    private List<EntityMapper<?>> findTopLevelEntityMappers() {
        final List<EntityMapper<?>> result = new ArrayList<>();
        for (final EntityMapper<?> mapper : findEntityMappers()) {
            if (mapper.getSuperEntities().isEmpty()) {
                result.add(mapper);
            }
        }
        sort(result);
        return result;
    }

    @Nonnull
    private List<EntityMapper<?>> findChildEntityMappers(@Nonnull final EntityMapper<?> parent) {
        assert parent != null;
        final List<EntityMapper<?>> result = new ArrayList<>();
        for (final EntityMapper<?> mapper : findEntityMappers()) {
            for (final EntityMapper<?>.SuperEntity superEntity : mapper.getSuperEntities()) {
                if (superEntity.getSuperMapper().equals(parent)) {
                    result.add(mapper);
                }
            }
        }
        sort(result);
        return result;
    }

    private static void sort(@Nonnull final List<? extends Mapper<?>> mappers) {
        assert mappers != null;
        Collections.sort(mappers, (@Nonnull final Mapper<?> o1, @Nonnull final Mapper<?> o2) ->
                displayName(o1).compareTo(displayName(o2)));
    }

    @Nonnull
    private static String displayName(@Nonnull final Mapper<?> mapper) {
        assert mapper != null;
        String name = mapper.getClass().getSimpleName();
        if (name.endsWith("Mapper")) {
            name = name.substring(0, name.length() - "Mapper".length());
        }
        return name;
    }

    private static boolean isEntityMapper(@Nonnull final Mapper<?> mapper) {
        assert mapper != null;
        if (mapper instanceof CollectionMapper) {
            return ((CollectionMapper<?>) mapper).getElementMapper() instanceof EntityMapper;
        }
        return mapper instanceof EntityMapper;
    }
}
