package org.stepik.fal.compiler.parsetree;

import org.stepik.fal.compiler.Token;

/**
 * author: Alexander Fal (falalexandr007@gmail.com)
 */
public class Prime extends Expression{
    private Token token;

    public Prime(Token token) {
        this.token = token;
    }
    Prime(Prime prime) {
        this.token = new Token(prime.getToken());
    }

    public Token getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Prime prime = (Prime) o;

        return token.equals(prime.token);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + token.hashCode();
        return result;
    }
}
