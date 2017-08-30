package org.stepik.fal.compiler;

/**
 * author: Alexander Fal (falalexandr007@gmail.com)
 */
public class Token {
    private String name;
    private SyntaxType type;
    private int start, end;

    Token() {
        name = "";
        start = 0;
        end = 0;
        type = SyntaxType.UNKNOWN;
    }
    public Token(Token token) {
        this.name = token.getName();
        this.type = token.getType();
        this.start = token.getStart();
        this.end = token.getEnd();
    }
    Token(String name, SyntaxType type, int start, int end) {
        this.name = name;
        this.type = type;
        this.start = start;
        this.end = end;
    }

    enum SyntaxType {
        NOT,
        AND,
        OR,
        LITERAL,
        LP,
        RP,
        IDENTIFIER,
        EOF,
        UNKNOWN
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    SyntaxType getType() {
        return type;
    }

    void setType(SyntaxType type) {
        this.type = type;
    }

    Integer getStart() {
        return start;
    }

    void setStart(Integer start) {
        this.start = start;
    }

    Integer getEnd() {
        return end;
    }

    void setEnd(Integer end) {
        this.end = end;
    }

    void setEof(Integer index) {
        this.name = "EOF";
        this.type = SyntaxType.EOF;
        this.end = this.start = index;
    }

    boolean isTrue() {
        return name.equals("TRUE");
    }

    boolean isFalse() {
        return name.equals("FALSE");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        return name != null ? name.equals(token.name) : token.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + start;
        result = 31 * result + end;
        return result;
    }
}
