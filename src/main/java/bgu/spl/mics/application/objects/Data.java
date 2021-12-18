package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int size;

    public Data(String type, int size) {
        this.type = Type.valueOf(type);
        this.size = size;
    }

    public Type getType() {
        return type;
    }

    public int getSize() { return size; }
}
