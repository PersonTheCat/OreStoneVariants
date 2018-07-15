package personthecat.mod.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Works similarly to Random.class. Does not update the seed upon use.
 * 
 * Delete me.
 */
public class LessRandom
{
	private final AtomicLong seed;
	private final long precalculated;
	
	public LessRandom(long seed)
	{
		this.seed = new AtomicLong(scramble(seed));
		
		this.precalculated = updatedSeed();
	}
	
	private static long scramble(long seed)
	{
		return (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
	}
	
	private long updatedSeed()
	{
		long newSeed = 0;
		
		do
		{
			newSeed = (seed.get() * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
		}
		
		while (!seed.compareAndSet(seed.get(), newSeed));
		
		return newSeed;
	}
	
	private int next()
	{
		return (int) (precalculated >>> (17));
	}
	
	public int getInt(int bound)
	{
		int r = next();
		int m = bound - 1;
		
        if ((bound & m) == 0) r = (int) ((bound * (long) r) >> 31);
        
        else for (int u = r; u - (r = u % bound) + m < 0; u = next());
        
        return r;
	}
}
