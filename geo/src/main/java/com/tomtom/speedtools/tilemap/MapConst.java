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

package com.tomtom.speedtools.tilemap;

import com.tomtom.speedtools.geometry.GeoPoint;

/**
 * This class represents an (abstract) cacheable map. The type parameter T is the image tile type. It is parametrized to
 * allow retrieving tiles in different internal formats, such as AWT Image objects, or Processing PImage objects.
 */
@SuppressWarnings("PublicStaticArrayField")
public final class MapConst {

    // Needed for Mercator projection.
    public static final double WORLD_SIZE = 20037508.34278913 * 2.0;
    public static final double WORLD_RADIUS = 6378137.0;

    /**
     * We support 18 zoom levels with the following resolution (meters per pixel):
     */
    public static final double[] METERS_PER_PIXEL = {
            156543.033928040625d,
            78271.5169640203125d,
            39135.75848201015625d,
            19567.879241005078125d,
            9783.9396205025390625d,
            4891.96981025126953125d,
            2445.984905125634765625d,
            1222.9924525628173828125d,
            611.49622628140869140625d,
            305.748113140704345703125d,
            152.8740565703521728515625d,
            76.43702828517608642578125d,
            38.218514142588043212890625d,
            19.1092570712940216064453125d,
            9.55462853564701080322265625d,
            4.777314267823505401611328125d,
            2.3886571339117527008056640625d,
            1.19432856695587635040283203125d
    };

    /**
     * Size of a tile.
     */
    public static final int PIXELS_PER_TILE = 256;

    /**
     * Width of a single tile, in mercator-meters, for each zoom level.
     */
    public final static double[] METERS_PER_TILE = {
            METERS_PER_PIXEL[0] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[1] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[2] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[3] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[4] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[5] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[6] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[7] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[8] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[9] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[10] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[11] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[12] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[13] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[14] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[15] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[16] * PIXELS_PER_TILE,
            METERS_PER_PIXEL[17] * PIXELS_PER_TILE,
    };

    /**
     * WMS zoom levels are 0..17, for a total of 18 levels.
     */
    public final static int MINIMUM_ZOOM = 0;
    public final static int MAXIMUM_ZOOM = 17;

    /**
     * How many zoom levels are there.
     */
    public final static int ZOOM_LEVELS = 18;

    /**
     * How many tiles is the world at this zoom in total.
     */
    public final static long[] TILES_PER_ZOOM = {
            1,              // Level 0
            4,              // Level 1
            16,             // Level 2
            64,             // Level 3
            256,            // Level 4
            1024,           // Level 5
            4096,           // Level 6
            16384,          // Level 7
            65536,          // Level 8
            262144,         // Level 9
            1048576,        // Level 10
            4194304,        // Level 11
            16777216,       // Level 12
            67108864,       // Level 13
            268435456,      // Level 14
            1073741824,     // Level 15
            4294967296L,    // Level 16
            17179869184L    // Level 17
    };

    // Mercator projection does not work outside this latitude range.
    public static final double LATITUDE_MIN = -85.0;
    public static final double LATITUDE_MAX = 85.0;

    /**
     * Some interesting geo locations (for example, for testing).
     */
    public static final GeoPoint POS_LONDON = new GeoPoint(51.506, -0.75);   // London.
    public static final GeoPoint POS_PARIS = new GeoPoint(48.861, 2.335);   // Paris.
    public static final GeoPoint POS_AMSTERDAM = new GeoPoint(52.3765, 4.908);  // Amsterdam.

    private MapConst() {
        // Prevent instantiation.
    }
}
