package Lab5;

public enum DataType {
    tInt("int"), tLongInt("long int"), tLongLongInt("long long int"), tBlock(null);

    private String value;

    DataType(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }
}
