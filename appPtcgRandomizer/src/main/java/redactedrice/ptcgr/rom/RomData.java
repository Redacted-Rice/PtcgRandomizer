package redactedrice.ptcgr.rom;

public class RomData {
	// These should never be modified
    public final byte[] rawBytes;
    public final RandomizationData original;
    // This will be modified as part of randomization and used to save
    public RandomizationData modified;
    
    public RomData(byte[] rawBytes, RandomizationData original) {
    	this.rawBytes = rawBytes;
    	this.original = original;
    }
    
    public void prepareForModification() {
    	// Do a fresh read as this is easier and a better guarantee of isolation
    	// than doing a deep copy
    	modified = RomIO.readFromBytes(rawBytes);
    }
}
