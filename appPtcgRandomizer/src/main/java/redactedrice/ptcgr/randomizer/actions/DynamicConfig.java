package redactedrice.ptcgr.randomizer.actions;

public class DynamicConfig<T> {
    private T value;
    private final Class<T> type;

    public DynamicConfig(Class<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
    
    public Class<T> getType() {
        return type;
    }
}