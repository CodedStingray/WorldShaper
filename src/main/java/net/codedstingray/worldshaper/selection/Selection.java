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

package net.codedstingray.worldshaper.selection;

import net.codedstingray.worldshaper.event.SelectionModifiedEvent;
import net.codedstingray.worldshaper.util.vector.vector3.Vector3i;
import org.bukkit.Bukkit;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * A Selection is defined by an ordered set of control positions.
 */
@ParametersAreNonnullByDefault
public class Selection implements Iterable<Vector3i> {

    private static final String MESSAGE_CONTROL_POSITION_MUST_NOT_BE_NULL = "A selection's control position must not be null.";
    private static final String MESSAGE_WORLD_UUID_MUST_NOT_BE_NULL = "A selection's world UUID must not be null.";
    private static final String MESSAGE_INDEX_MUST_BE_AT_LEAST_0 = "Selection control position index must be at least 1.";

    private final UUID playerUUID;

    private UUID worldUUID;

    private final List<Vector3i> controlPositions = new ArrayList<>();
    private List<Vector3i> unmodifiableControlPositions;

    public Selection(UUID player) {
        playerUUID = player;
        unmodifiableControlPositions = controlPositions;
    }

    /**
     * The {@link UUID} of the {@link org.bukkit.World world} the selection is currently in. If a new Position is set
     * into a different world, all other positions will be deleted and this world UUID will be updated to the new world.
     *
     * @return The UUID of the current world
     */
    public UUID getWorldUUID() {
        return worldUUID;
    }

    /**
     * Adds a control position to the end of the list of control positions. If the given world UUID does not match the
     * current world UUID, the list of positions will be cleared before the new position gets added. The world UUID will
     * then be set to the given UUID.
     *
     * @param position The position to be set
     * @param world The UUID of the world the position has been set in
     * @return The index of the added position
     */
    public int addControlPosition(Vector3i position, UUID world) {
        Objects.requireNonNull(position, MESSAGE_CONTROL_POSITION_MUST_NOT_BE_NULL);
        Objects.requireNonNull(world, MESSAGE_WORLD_UUID_MUST_NOT_BE_NULL);

        clearPositionsIfWorldIsDifferent(world);

        int index = controlPositions.indexOf(null);
        if (index == -1) {
            controlPositions.add(position);
            index = controlPositions.size() - 1;
        } else {
            controlPositions.set(index, position);
        }

        onSelectionModified();
        return index;
    }

    /**
     * Sets the control position at the given index to the given position. If the given world UUID does not match the
     * current world UUID, the list of positions will be cleared before the new position gets added. The world UUID will
     * then be set to the given UUID.
     *
     * @param index The index within the set of control positions at which the control position should be set
     * @param position The position to be set
     * @param world The UUID of the world the position has been set in
     */
    public boolean setControlPosition(int index, Vector3i position, UUID world) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(MESSAGE_INDEX_MUST_BE_AT_LEAST_0);
        }
        Objects.requireNonNull(position, MESSAGE_CONTROL_POSITION_MUST_NOT_BE_NULL);
        Objects.requireNonNull(world, MESSAGE_WORLD_UUID_MUST_NOT_BE_NULL);

        clearPositionsIfWorldIsDifferent(world);

        if (index >= controlPositions.size()) {
            for (int i = controlPositions.size(); i <= index; i++) {
                controlPositions.add(null);
            }
        }

        if (position.equals(controlPositions.get(index))) {
            onSelectionNotModified();
            return false;
        }
        controlPositions.set(index, position.toImmutable());
        onSelectionModified();
        return true;
    }

    /**
     * Removes the control position at the given index if it currently exists.
     *
     * @param index The index at which to remove the control position
     * @return {@code true} if the list has been modified, {@code false} otherwise
     */
    public boolean removeControlPosition(int index) {
        if (index < 0 || index >= controlPositions.size() || controlPositions.get(index) == null) {
            return false;
        }
        controlPositions.set(index, null);
        while (controlPositions.size() > 0 && controlPositions.get(controlPositions.size() - 1) == null) {
            controlPositions.remove(controlPositions.size() - 1);
        }

        onSelectionModified();
        return true;
    }

    /**
     * Clears the control positions and world UUID.
     */
    public void clearControlPositions() {
        controlPositions.clear();
        worldUUID = null;
        onSelectionModified();
    }

    /**
     * Returns the control position at the given index within the set of control positions.
     *
     * @param index The index of the queried control position
     * @return The control position at the given index, or null if no control position is found at this index.
     */
    public Vector3i getControlPosition(int index) {
        if (index < 0 || index >= controlPositions.size()) {
            return null;
        }

        return controlPositions.get(index);
    }

    /**
     * Returns the control positions.
     *
     * @return An unmodifiable copy of the control positions
     */
    public List<Vector3i> getControlPositions() {
        return unmodifiableControlPositions;
    }

    private void clearPositionsIfWorldIsDifferent(UUID world) {
        if (!world.equals(this.worldUUID)) {
            controlPositions.clear();
            this.worldUUID = world;
        }
    }

    private void onSelectionModified() {
        unmodifiableControlPositions = Collections.unmodifiableList(controlPositions);
        Bukkit.getPluginManager().callEvent(new SelectionModifiedEvent(this, playerUUID, true));
    }

    private void onSelectionNotModified() {
        Bukkit.getPluginManager().callEvent(new SelectionModifiedEvent(this, playerUUID, false));
    }

    @Override
    public Iterator<Vector3i> iterator() {
        return new Iterator<>() {
            private int currentIndex;

            {
                currentIndex = -1;
                currentIndex = findNextFilledIndex();
            }

            @Override
            public boolean hasNext() {
                return currentIndex >= 0;
            }

            @Override
            public Vector3i next() {
                Vector3i nextVector = controlPositions.get(currentIndex);
                currentIndex = findNextFilledIndex();
                return nextVector;
            }

            private int findNextFilledIndex() {
                for (int i = currentIndex + 1; i < controlPositions.size(); i++) {
                    if (controlPositions.get(i) != null) {
                        return i;
                    }
                }
                return -1;
            }
        };
    }
}
