/*
 * Copyright 2013, 2014 biylda <biylda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package menion.android.whereyougo.maps.utils;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Vector;

import cz.matejcik.openwig.Engine;
import cz.matejcik.openwig.EventTable;
import cz.matejcik.openwig.Zone;
import cz.matejcik.openwig.formats.CartridgeFile;
import locus.api.android.objects.PackPoints;
import locus.api.objects.extra.GeoDataExtra;
import locus.api.objects.styles.GeoDataStyle;
import locus.api.objects.styles.LineStyle;
import locus.api.objects.extra.Location;
import locus.api.objects.geoData.Track;
import locus.api.objects.geoData.Point;
import menion.android.whereyougo.gui.activity.MainActivity;
import menion.android.whereyougo.gui.activity.wherigo.DetailsActivity;
import menion.android.whereyougo.gui.utils.UtilsWherigo;

public class LocusMapDataProvider implements MapDataProvider {
    private static LocusMapDataProvider instance = null;
    private ArrayList<Track> tracks = null;
    private final PackPoints pack;

    private LocusMapDataProvider() {
        tracks = new ArrayList<>();
        pack = new PackPoints("WhereYouGo");
    }

    public static LocusMapDataProvider getInstance() {
        if (instance == null)
            instance = new LocusMapDataProvider();
        return instance;
    }

    public void addAll() {
        Vector<CartridgeFile> v = new Vector<>();
        v.add(MainActivity.cartridgeFile);
        addCartridges(v);
        addZones((Vector<Zone>) Engine.instance.cartridge.zones, DetailsActivity.et);
        if (DetailsActivity.et != null && !(DetailsActivity.et instanceof Zone))
            addOther(DetailsActivity.et, true);
    }

    public void addCartridges(Vector<CartridgeFile> cartridges) {
        if (cartridges == null)
            return;
        // Bitmap b = Images.getImageB(R.drawable.wherigo, (int) Utils.getDpPixels(32.0f));
        // pack.setBitmap(b);
        for (CartridgeFile cartridge : cartridges) {
            // do not show waypoints that are "Play anywhere" (with zero
            // coordinates)
            if (cartridge.latitude % 360.0 == 0 && cartridge.longitude % 360.0 == 0) {
                continue;
            }

            // construct waypoint
            Location loc = new Location();
            loc.setLatitude(cartridge.latitude);
            loc.setLongitude(cartridge.longitude);
            Point wpt = new Point(cartridge.name, loc);
            wpt.addParameter(GeoDataExtra.PAR_DESCRIPTION, cartridge.description);
            wpt.addParameterUrl(cartridge.url);
            pack.addPoint(wpt);
        }
    }

    public void addOther(EventTable et, boolean mark) {
        if (et == null || !et.isLocated() || !et.isVisible())
            return;

        Location loc = UtilsWherigo.extractLocation(et);
        pack.addPoint(new Point(et.name, loc));
    }

    public void addZone(Zone z, boolean mark) {
        if (z == null || !z.isLocated() || !z.isVisible())
            return;

        ArrayList<Location> locations = new ArrayList<>();
        for (int i = 0; i < z.points.length; i++) {
            Location location = new Location();
            location.setLatitude(z.points[i].latitude);
            location.setLongitude(z.points[i].longitude);
            locations.add(location);
        }
        if (locations.size() >= 3)
            locations.add(locations.get(0));

        Track track = new Track();
        LineStyle lineStyle = new LineStyle();
        lineStyle.setColoring(LineStyle.Coloring.SIMPLE);
        lineStyle.setColorBase(Color.MAGENTA);
        lineStyle.setWidth(2.0f);
        lineStyle.setUnits(LineStyle.Units.PIXELS);
        GeoDataStyle geoDataStyle = new GeoDataStyle();
        geoDataStyle.setLineStyle(lineStyle);
        track.setStyleNormal(geoDataStyle);
        track.setPoints(locations);
        track.setName(z.name);
        tracks.add(track);
    }

    public void addZones(Vector<Zone> zones) {
        addZones(zones, null);
    }

    public void addZones(Vector<Zone> zones, EventTable mark) {
        if (zones == null)
            return;
        // show zones
        for (Zone z : zones) {
            addZone(z, z == mark);
        }
    }

    public void clear() {
        tracks.clear();
    }

    public PackPoints getPoints() {
        return pack;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }
}
