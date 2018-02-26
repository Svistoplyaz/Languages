package semester7.Lab5;

public enum DataType {
    tInt("int"), tInt64("__int64"), tBlock(null), tArray("array");

    private String value;

    DataType(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }
}
