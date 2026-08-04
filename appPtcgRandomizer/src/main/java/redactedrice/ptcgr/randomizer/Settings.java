package redactedrice.ptcgr.randomizer;

import java.util.Random;

import redactedrice.randomizer.utils.LogLevel;

public class Settings {
    private final Random rand = new Random();
    private String seed = String.valueOf(rand.nextInt());
    private boolean logDetails;
    private LogLevel logLevel = LogLevel.INFO;

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

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel != null ? logLevel : LogLevel.INFO;
    }
}
