package personthecat.mod.world.gen;

import java.awt.Point;
import java.util.Random;

import net.minecraft.world.gen.NoiseGeneratorSimplex;

public class RandomChunkSelector
{
	private NoiseGeneratorSimplex noise;

	/**
	 * To-do: add config options.
	 */
	
	private static final double 
	
		SELECTION_THRESHOLD = 0.95,
		DEFAULT_PROBABILITY = 0.0;

	public RandomChunkSelector(Long worldSeed)
	{
		this.noise = new NoiseGeneratorSimplex(new Random(worldSeed));		
	}
	
	public boolean getBooleanForCoordinates(int uniqueID, int x, int y)
	{
		int concatenatedValues = concatenateNumbers(x, y) + 1;
		
		return noise.getValue(uniqueID, concatenatedValues) > SELECTION_THRESHOLD;
	}
	
	public double getProbabilityForCoordinates(int ID, int x, int y)
	{
		Point center = new Point(x, y);
		
		if (getBooleanForCoordinates(ID, x, y)) return 100.0;

		if (getBooleanForDistance(ID, center, 1)) return 66.6;
		
		if (getBooleanForDistance(ID, center, 2)) return 33.3;
		
		return DEFAULT_PROBABILITY;
	}
	
	public boolean getBooleanForDistance(int ID, Point center, int radius)
	{
		int diameter = (radius * 2) + 1;
		int innerLength = diameter - 2;
		int shift = (radius - 1) * -1;
		int nRadius = radius * -1;
		
		int x = center.x, y = center.y;

		//Start with the corners.
		if (getBooleanForCoordinates(ID, x + radius, y + radius)) return true;
		if (getBooleanForCoordinates(ID, x + nRadius, y + nRadius)) return true;
		if (getBooleanForCoordinates(ID, x + radius, y + nRadius)) return true;
		if (getBooleanForCoordinates(ID, x + nRadius, y + radius)) return true;
		
		//Get the points between the corners.
		for (int i = 0 + shift; i < innerLength + shift; i++)
		{
			if (getBooleanForCoordinates(ID, x + radius, y + i)) return true;
			if (getBooleanForCoordinates(ID, x + i, y + radius)) return true;
			if (getBooleanForCoordinates(ID, x + nRadius, y + i)) return true;
			if (getBooleanForCoordinates(ID, x + i, y + nRadius)) return true;
		}
		
		return false;
	}
	
	/*
	 * Delete me.
	 */
	private static Point[] getRingOfPoints(int radius)
	{
		Point[] points = new Point[8 * radius];
		int diameter = (radius * 2) + 1;
		int innerLength = diameter - 2;
		int shift = (radius - 1) * -1;

		//Start with the corners.
		points[0] = new Point(radius, radius);
		points[1] = new Point(radius * -1, radius * -1);
		points[2] = new Point(radius, radius * -1);
		points[3] = new Point(radius * -1, radius);

		int index = 4;
		
		//Get the points between the corners.
		for (int i = 0 + shift; i < innerLength + shift; i++)
		{
			points[index] = new Point(radius, i);
			points[index + 1] = new Point(radius * -1, i);
			points[index + 2] = new Point(i, radius);
			points[index + 3] = new Point(i, radius * -1);
					
			index += 4;		
		}
		
		return points;
	}
	
	private static int concatenateNumbers(int... numbers)
	{
		StringBuilder sb = new StringBuilder(numbers.length);
		
		for (int number : numbers)
		{
			sb.append(Math.abs(number));
		}
		
		return Integer.valueOf(sb.toString());
	}
}