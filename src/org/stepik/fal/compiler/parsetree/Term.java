package org.stepik.fal.compiler.parsetree;

import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * author: Alexander Fal (falalexandr007@gmail.com)
 */
public class Term{
    private ArrayList<Factor> factors;

    public Term() {
        this.factors = new ArrayList<>();
    }
    public Term(Term term) {
        this.factors = new ArrayList<>();
        this.factors.addAll(term.getFactors().stream().map(Factor::new).collect(Collectors.toList()));
    }

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public ArrayList<Factor> getFactors() {
        return factors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term term = (Term) o;

        return factors.containsAll(term.factors) && term.factors.containsAll(factors) ;
    }

    @Override
    public int hashCode() {
        return factors.hashCode();
    }
}
