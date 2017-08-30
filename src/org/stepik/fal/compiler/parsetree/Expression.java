package org.stepik.fal.compiler.parsetree;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * author: Alexander Fal (falalexandr007@gmail.com)
 */
public class Expression {
    private ArrayList<Term> terms;

    public Expression() {
        terms = new ArrayList<>();
    }
    public Expression(Expression ex) {
        this.terms = new ArrayList<>();
        terms.addAll(ex.getTerms().stream().map(Term::new).collect(Collectors.toList()));
    }

    public void addTerm(Term term) {
        terms.add(term);
    }

    public ArrayList<Term> getTerms() {
        return terms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Expression that = (Expression) o;

        return terms.containsAll(that.terms) && that.terms.containsAll(terms) ;
    }

    @Override
    public int hashCode() {
        return terms.hashCode();
    }
}
