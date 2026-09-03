package redactedrice.ptcgr.rom;


import java.util.HashMap;
import java.util.Map;

public class Texts {
    private Map<Short, String> textMap;
    private Map<String, Short> reverseMap;

    public Texts() {
        textMap = new HashMap<>();
        reverseMap = new HashMap<>();

        // Put in the "null pointer" reservation at ID 0
        textMap.put((short) 0, "");
        reverseMap.put("", (short) 0);
    }

    public Texts copy() {
        Texts copy = new Texts();
        copy.textMap.clear();
        copy.reverseMap.clear();
        copy.textMap.putAll(textMap);
        copy.reverseMap.putAll(reverseMap);
        return copy;
    }

    public short insertTextAtNextId(String text) {
        short nextId = count();
        textMap.put(nextId, text);
        reverseMap.put(text, nextId);
        return nextId;
    }

    public short getId(String text) {
        Short id = reverseMap.get(text);
        if (id == null) {
            return 0;
        }
        return id;
    }

    public short insertTextOrGetId(String text) {
        Short id = reverseMap.get(text);
        if (id == null) {
            id = insertTextAtNextId(text);
        }
        return id;
    }

    public String getAtId(short id) {
        return textMap.get(id);
    }

    public void putAtId(short id, String text) {
        textMap.put(id, text);
        reverseMap.put(text, id);
    }

    public short count() {
        return (short) textMap.size();
    }

    public boolean hasTextId(short textId) {
        return textMap.containsKey(textId);
    }
}
