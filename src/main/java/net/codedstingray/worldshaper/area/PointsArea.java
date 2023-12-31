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

package net.codedstingray.worldshaper.area;

import net.codedstingray.worldshaper.WorldShaper;
import net.codedstingray.worldshaper.selection.Selection;
import net.codedstingray.worldshaper.selection.type.SelectionType;
import net.codedstingray.worldshaper.selection.type.SelectionTypeIndefinitePositions;
import net.codedstingray.worldshaper.util.vector.vector3.Vector3i;
import net.codedstingray.worldshaper.util.world.Direction;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@ParametersAreNonnullByDefault
public class PointsArea implements Area {

    public static final String NAME = "points";

    private List<Vector3i> points =  Collections.emptyList();

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    @Nonnull
    @Override
    public SelectionType getDefaultSelectionType() {
        return WorldShaper.getInstance().getPluginData().getSelectionTypeByName(SelectionTypeIndefinitePositions.NAME);
    }

    @Override
    public boolean isValid() {
        return points.size() > 0;
    }

    @Override
    public void updateArea(Selection selection) {
        points = Collections.unmodifiableList(selection.getControlPositions());
    }

    @Override
    public boolean isInArea(Vector3i position) {
        return points.contains(position);
    }

    @Override
    public void move(Direction direction, int distance) {
        Vector3i moveVector = direction.baseVector.scale(distance);
        points.forEach(point -> point.add(moveVector));
    }

    @Override
    public void expand(Direction direction, int amount) {
        throw new IllegalStateException("Area type \"Points\" doesn't support operation \"expand\"");
    }

    @Override
    public void retract(Direction direction, int amount) {
        throw new IllegalStateException("Area type \"Points\" doesn't support operation \"retract\"");
    }

    @Override
    public Iterator<Vector3i> iterator() {
        return points.iterator();
    }
}
