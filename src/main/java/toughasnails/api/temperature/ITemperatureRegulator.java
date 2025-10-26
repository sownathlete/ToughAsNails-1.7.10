package toughasnails.api.temperature;

import toughasnails.api.temperature.Temperature;

/**
 * Backported interface for Forge 1.7.10.
 * 
 * 1.9.4 used BlockPos; 1.7.10 uses direct integer coordinates.
 */
public interface ITemperatureRegulator {

    /**
     * Returns the temperature that this regulator maintains.
     */
    public Temperature getRegulatedTemperature();

    /**
     * Checks whether the given coordinates are affected by this regulator.
     * 
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param z Z-coordinate
     * @return true if the position is regulated
     */
    public boolean isPosRegulated(int x, int y, int z);
}
