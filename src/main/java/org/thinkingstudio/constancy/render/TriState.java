package org.thinkingstudio.constancy.render;

public enum TriState {
    DEFAULT, TRUE, FALSE;
    public static TriState of(boolean value) { return value ? TRUE : FALSE; }
}
