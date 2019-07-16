package net.woggioni.worth.serialization.binary;

public enum BinaryMarker {
    Float(0x0),
    Int(0x1),
    Null(0x2),
    True(0x3),
    False(0x4),
    Reference(0x5),
    Id(0x6),
    EmptyString(0x0d),
    LargeString(0x5d),
    EmptyObject(0x5e),
    LargeObject(0xae),
    EmptyArray(0xaf),
    LargeArray(0xff);

    public int value;
    BinaryMarker(int b) {
        value = b;
    }
}
