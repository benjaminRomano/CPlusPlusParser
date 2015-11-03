package org.bromano.cplusplusparser.parser;

public class ParserResult<T> {
    public String error;
    public T value;

    public ParserResult() {
    }

    public ParserResult(String error) {
        this.error = error;
    }

    public ParserResult(T value) {
        this.value = value;
    }
}
