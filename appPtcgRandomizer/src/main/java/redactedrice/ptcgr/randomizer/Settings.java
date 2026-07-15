package redactedrice.ptcgr.randomizer;

import java.util.Random;

public class Settings {
    private final Random rand = new Random();
    private String seed = String.valueOf(rand.nextInt());
    private boolean logDetails;

    public int getSeedValue() {
        try {
            return Integer.parseInt(seed);
        } catch (NumberFormatException nfe) {
            return seed.hashCode();
        }
    }

    public void setSeed(String seed) {
        if (seed.trim().isEmpty() || seed.equalsIgnoreCase("random")) {
            this.seed = String.valueOf(rand.nextInt());
        } else {
            this.seed = seed;
        }
    }

    public String getSeedString() {
        return seed;
    }

    public boolean isLogDetails() {
        return logDetails;
    }

    public void setLogDetails(boolean logDetails) {
        this.logDetails = logDetails;
    }
}
