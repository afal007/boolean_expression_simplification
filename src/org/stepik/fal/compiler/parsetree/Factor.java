package org.stepik.fal.compiler.parsetree;

/**
 * author: Alexander Fal (falalexandr007@gmail.com)
 */
public class Factor {
    private boolean hasNegation;
    private Expression expression;

    public Factor() {
        hasNegation = false;
        expression = null;
    }

    public Factor(boolean hasNegation, Expression expression) {
        this.hasNegation = hasNegation;
        this.expression = expression;
    }

    Factor(Factor factor) {
        this.hasNegation = factor.hasNegation();
        Expression ex = factor.getExpression();
        if(ex instanceof Prime)
            this.expression = new Prime((Prime) ex);
        else
            this.expression = new Expression(ex);
    }

    public boolean hasNegation() {
        return hasNegation;
    }

    public void setHasNegation(boolean hasNegation) {
        this.hasNegation = hasNegation;
    }

    public boolean isExpression() {
        return !(expression instanceof Prime);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Factor factor = (Factor) o;

        return hasNegation == factor.hasNegation && expression.equals(factor.expression);

    }

    @Override
    public int hashCode() {
        int result = (hasNegation ? 1 : 0);
        result = 31 * result + expression.hashCode();
        return result;
    }
}
