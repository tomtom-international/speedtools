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

package com.tomtom.speedtools.utils;

import com.tomtom.speedtools.objects.Immutables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class StringUtils {

    private StringUtils() {
        // Prevent instantiation.
    }

    /**
     * Converts a {@code null} string to an empty string.
     *
     * @param s Input string, or {@code null}.
     * @return The empty string when {@code s} is {@code null}, {@code s} otherwise.
     */
    @Nonnull
    public static String nullToEmpty(@Nullable final String s) {
        return (s == null) ? "" : s;
    }

    /**
     * Converts an empty or null string to null.
     *
     * @param s Input string, or {@code null}.
     * @return Null when {@code s} is empty, {@code s} otherwise.
     */
    @Nullable
    public static String emptyToNull(@Nullable final String s) {
        return ((s != null) && s.isEmpty()) ? null : s;
    }

    /**
     * Trims a string and can accept {@code null} input strings. Returns {@code null} when input is {@code null}.
     *
     * @param s Input string, or {@code null}.
     * @return Trimmed string, or {@code null}.
     */
    @Nullable
    public static String trim(@Nullable final String s) {
        if (s == null) {
            return null;
        } else {
            return s.trim();
        }
    }

    /**
     * Trims a list of strings, retaining the order of the input list. Can accept {@code null} input lists. Returns
     * {@code null} when input is {@code null}.
     *
     * @param sList List of strings to trim, or {@code null}.
     * @return List containing trimmed strings, or {@code null}.
     */
    @Nullable
    public static List<String> trim(@Nullable final List<String> sList) {
        if (sList == null) {
            return null;
        }
        return trim(Immutables.listOf(sList), new ArrayList<>(sList.size()));
    }

    /**
     * Trims a set of strings. Can accept {@code null} input sets. Returns {@code null} when input is {@code null}.
     *
     * @param sSet Set of strings to trim, or {@code null}.
     * @return Set containing trimmed strings, or {@code null}.
     */
    @Nullable
    public static Set<String> trim(@Nullable final Set<String> sSet) {
        if (sSet == null) {
            return null;
        }
        return trim(sSet, new HashSet<>());
    }

    /**
     * Trims a collection of strings.
     *
     * @param in  Collection of strings to trim.
     * @param out Collection in which trimmed strings should be stored in.
     * @param <T> Collection type.
     * @return Returns {@code out} for convenience.
     */
    @Nonnull
    private static <T extends Collection<String>> T trim(@Nonnull final T in, @Nonnull final T out) {
        assert in != null;
        assert out != null;
        for (final String s : in) {
            out.add(trim(s));
        }
        return out;
    }

    /**
     * Converts a string to lower case in a locale insensitive manner, and can accept {@code null} input strings.
     * Returns {@code null} when input is {@code null}.
     *
     * The string conversion uses {@link String#toLowerCase() String.toLowerCase(Locale.ENGLISH)}. See {@link
     * String#toLowerCase()} on when to use the default system locale during conversion to lower case, and in which
     * cases this will produce the wrong results.
     *
     * @param s Input string, or {@code null}.
     * @return String converted to lower case, or {@code null}.
     */
    @Nullable
    public static String literalToLowerCase(@Nullable final String s) {
        if (s == null) {
            return null;
        } else {
            return s.toLowerCase(Locale.ENGLISH);
        }
    }

    /**
     * Converts a string to a list of Unicode code points. This method should be used when wanting to iterate over a
     * string character-by-character.
     *
     * This method is necessary because Java stores characters as UTF-16 values in a String, and {@link
     * String#charAt(int)} returns a 16-bit value. This works fine for characters in the Basic Multilingual Plane (BMP),
     * but Unicode allows for supplementary characters that take up more than 16 bits to represent them. For Strings,
     * this means that Java will store those characters as two characters, with the first character being a value in the
     * high-surrogates range, and the second value in the low-surrogates range. Therefore, {@code charAt(n)} may
     * actually return a char value that does not represent the actual character when a supplementary character is
     * stored, and only combining {@code charAt(n)} together with {@code charAt(n plus 1)} would result in the correct
     * character.
     *
     * The implementation of this method is based on <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5003547">JDK-5003547</a>
     * and <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5063163">JDK-5063163</a>.
     *
     * See {@link Character} and <a href="http://www.oracle.com/technetwork/articles/javase/supplementary-142654.html">Supplementary
     * Characters in the Java Platform</a> for further information on Java and Unicode.
     *
     * @param s Input string.
     * @return Array of all code points contained in-order in {@code s}. The length of the returned array is {@code <=}
     * the length of the given {@code s}.
     */
    @Nonnull
    public static int[] toCodePoints(@Nonnull final String s) {
        assert s != null;

        final int[] codePoints = new int[s.codePointCount(0, s.length())];

        assert codePoints != null;
        assert codePoints.length <= s.length();

        int stringIndex = 0;
        for (int codePointsIndex = 0; codePointsIndex < codePoints.length; codePointsIndex++) {
            codePoints[codePointsIndex] = s.codePointAt(stringIndex);
            stringIndex += Character.charCount(codePoints[codePointsIndex]);
        }

        return codePoints;
    }

    /**
     * Assembles a String with all the elements of an input collection using a provided separator. Example:
     * <pre>
     *      final List&lt;Integer&gt; list = new ArrayList&lt;Integer&gt;();
     *      list.add(1);
     *      list.add(2);
     *      list.add(3);
     * </pre>
     *
     * Calling mkString as shown below:
     * <pre>
     *      final String result = mkString("|", list);
     * </pre>
     * will provide:
     * <pre>
     *      result := "1|2|3"
     * </pre>
     *
     * @param values    Values.
     * @param separator Separator string between values.
     * @return String with values, separated by separator.
     */
    @Nonnull
    public static String mkString(@Nonnull final String separator, @Nonnull final Collection<?> values) {
        assert values != null;
        assert separator != null;
        boolean first = true;
        final StringBuilder sb = new StringBuilder();
        for (final Object value : values) {
            if (value != null) {
                final String str = value.toString();
                if (!str.isEmpty()) {
                    if (!first) {
                        sb.append(separator);
                    } else {
                        first = false;
                    }
                    sb.append(str);
                }
            }
        }
        return sb.toString();
    }

    @Nonnull
    public static <T> String mkRevString(@Nonnull final String separator, @Nonnull final Collection<T> values) {
        final LinkedList<T> rev = new LinkedList<>();
        for (final T value : values) {
            rev.addFirst(value);
        }
        return mkString(separator, rev);
    }

    /**
     * Same as mkString but for an Array.
     *
     * @param <T>       Element type.
     * @param separator The separator to use.
     * @param values    The values.
     * @return String with values, separated by separator.
     */
    @SafeVarargs
    @Nonnull
    public static <T> String mkString(@Nonnull final String separator, @Nonnull final T... values) {
        return mkString(separator, Arrays.asList(values));
    }

    /**
     * Same as mkRevString but for an Array.
     *
     * @param <T>       Element type.
     * @param separator The separator to use.
     * @param values    The values.
     * @return String with values, separated by separator.
     */
    @SafeVarargs
    @Nonnull
    public static <T> String mkRevString(@Nonnull final String separator, @Nonnull final T... values) {
        return mkRevString(separator, Arrays.asList(values));
    }

    /**
     * Method generates a random string from given characters with specified length.
     *
     * @param chars  The characters, Should at least contain 1 character.
     * @param length The length. Should be greater than 0.
     * @return Random String.
     */
    @Nonnull
    public static String generateRandomStringFromChars(@Nonnull final char[] chars, final int length) {
        assert chars != null;
        assert length > 0;
        assert chars.length > 0;

        final char[] code = new char[length];

        final Random random = new Random();

        for (int i = 0; i < length; i++) {
            final int index = random.nextInt(chars.length);
            code[i] = chars[index];
        }

        return new String(code);
    }

    /**
     * Method escapes a string in JSON format.
     *
     * @param input The original string.
     * @return The escaped string.
     */
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    public static String encodeToJsonString(@Nonnull final String input) {
        assert input != null;

        final int len = input.length();
        final StringBuilder sb = new StringBuilder(len + 2);    // Min. length = string length + 2 quotes.
        sb.append('"');
        for (int i = 0; i < len; ++i) {
            final char ch = input.charAt(i);
            switch (ch) {

                case '\\':
                    // Fall through.
                case '"':
                    // Fall through.
                case '/':
                    sb.append('\\');
                    sb.append(ch);
                    break;

                case '\b':
                    sb.append("\\b");
                    break;

                case '\t':
                    sb.append("\\t");
                    break;

                case '\n':
                    sb.append("\\n");
                    break;

                case '\f':
                    sb.append("\\f");
                    break;

                case '\r':
                    sb.append("\\r");
                    break;

                default:
                    if (((ch >= '\u0000') && (ch <= '\u001F')) ||
                            ((ch >= '\u007F') && (ch <= '\u009F')) ||
                            (((ch >= '\u2000') && (ch <= '\u20FF')))) {
                        sb.append("\\u").append(Integer.toHexString(((int) ch) + 0x10000).substring(1));
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Method unescapes a JSON string to regular string format.
     *
     * @param input The JSON string.
     * @return The regular unescaped string, or null if conversion failed.
     */
    @Nullable
    public static String decodeFromJsonString(@Nonnull final String input) {
        assert input != null;

        // String must be contained in "...".
        if ((input.length() < 2) || (input.charAt(0) != '"') || (input.charAt(input.length() - 1) != '"')) {
            return null;
        }

        final int len = input.length() - 2;
        final StringBuilder sb = new StringBuilder(len);
        int i = 1;
        while (i <= len) {
            char ch = input.charAt(i);
            if (ch == '\\') {

                // Bail out: "\" at end of string is not valid.
                if (i == len) {
                    return null;
                }

                ++i;
                ch = input.charAt(i);
                switch (ch) {

                    case '\\':
                        // Fall through.
                    case '"':
                        // Fall through.
                    case '/':
                        // Strip '\' and add character behind it.
                        sb.append(ch);
                        ++i;
                        break;

                    case 'b':
                        sb.append('\b');
                        ++i;
                        break;

                    case 't':
                        sb.append('\t');
                        ++i;
                        break;

                    case 'n':
                        sb.append('\n');
                        ++i;
                        break;

                    case 'f':
                        sb.append('\f');
                        ++i;
                        break;

                    case 'r':
                        sb.append('\r');
                        ++i;
                        break;

                    case 'u':

                        // Unicode character, format 'u' followed by 4 hex digits.
                        if (i > (len - 4)) {
                            return null;
                        }
                        ++i;
                        final String hex = input.substring(i, i + 4);
                        i = i + 4;
                        final int hexValue;
                        try {
                            hexValue = Integer.parseInt(hex, 16);
                        } catch (final NumberFormatException ignored) {

                            // Illegal format, bail out.
                            return null;
                        }
                        assert ((0x0000 <= hexValue) && (hexValue <= 0xFFFF));

                        //noinspection NumericCastThatLosesPrecision
                        sb.append((char) hexValue);
                        break;

                    default:
                        sb.append(ch);
                        ++i;
                        break;
                }
            } else {

                // Regular character, just add to string.
                sb.append(ch);
                ++i;
            }
        }
        return sb.toString();
    }
}
