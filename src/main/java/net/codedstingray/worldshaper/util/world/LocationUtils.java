/*
 * WorldShaper: a powerful in-game map editor for Minecraft
 * Copyright (C) 2023 CodedStingray
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.codedstingray.worldshaper.util.world;

import net.codedstingray.worldshaper.util.vector.vector3.Vector3i;
import net.codedstingray.worldshaper.util.vector.vector3.Vector3ii;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LocationUtils {

    public static Vector3i locationToBlockVector(Location location) {
        return new Vector3ii(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    //TODO: create new Vector type and switch to it
    public static Vector3d locationToEntityVector(Location location) {
        return new Vector3d(
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    public static Location vectorToLocation(Vector3i vector, @Nullable World world) {
        return new Location(
                world,
                vector.getX() + 0.5d,
                vector.getY() + 0.5d,
                vector.getZ() + 0.5d
        );
    }

    //TODO: create new Vector type and switch to it
    public static Location vectorToLocation(Vector3dc vector, @Nullable World world) {
        return new Location(
                world,
                vector.x(),
                vector.y(),
                vector.z()
        );
    }
}
