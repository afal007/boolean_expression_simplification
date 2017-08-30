package org.stepik.fal.compiler;

import org.stepik.fal.compiler.parsetree.Expression;
import org.stepik.fal.compiler.parsetree.Factor;
import org.stepik.fal.compiler.parsetree.Prime;
import org.stepik.fal.compiler.parsetree.Term;
import org.stepik.fal.exception.IllegalLexemeException;
import org.stepik.fal.exception.SyntaxErrorException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * author: Alexander Fal (falalexandr007@gmail.com)
 */
public class Optimizer {
    private static Expression parseTree;

    public static void simplify(String str) throws IllegalLexemeException, SyntaxErrorException {
        parseTree = SyntaxAnalyser.buildAST(str);
        simplifyAll();
        printResult(parseTree);
    }

    @Simplifier
    @Priority(1)
    private static void simplifyLiteralsNegation(Expression expression) {
        for(Term t : expression.getTerms()) {
            for(Factor f: t.getFactors()){
                if(!f.isExpression()) {
                    if(f.hasNegation()) {
                        Prime p = (Prime) f.getExpression();
                        Token token = p.getToken();
                        if (token.getType() == Token.SyntaxType.LITERAL) {
                            if (token.isTrue())
                                token.setName("FALSE");
                            else
                                token.setName("TRUE");
                            f.setHasNegation(false);
                        }
                    }
                } else
                    simplifyLiteralsNegation(f.getExpression());
            }
        }
    }

    @Simplifier
    @Priority(1)
    private static void simplifyLiteralsConjunction(Expression expression) {
        for (Term t : expression.getTerms()) {
            Iterator<Factor> factorIt = t.getFactors().iterator();
            while (factorIt.hasNext()) {
                Factor f = factorIt.next();
                if (!f.isExpression()) {
                    if(!f.hasNegation()) {
                        Prime p = (Prime) f.getExpression();
                        Token token = p.getToken();
                        if (token.getType() == Token.SyntaxType.LITERAL && t.getFactors().size() > 1) {
                            if (token.isTrue())
                                factorIt.remove();
                            if (token.isFalse()) {
                                t.getFactors().clear();
                                t.addFactor(new Factor(false, new Prime(new Token("FALSE", Token.SyntaxType.LITERAL, 0, 0))));
                                break;
                            }
                        }
                    }
                } else
                    simplifyLiteralsConjunction(f.getExpression());
            }
        }
    }

    @Simplifier
    @Priority(1)
    private static void simplifyLiteralsDisjunction(Expression expression) {
        Iterator<Term> termIt = expression.getTerms().iterator();
        while(termIt.hasNext()){
            Term t = termIt.next();
            for (Factor f : t.getFactors()) {
                if (!f.isExpression()) {
                    if(!f.hasNegation()) {
                        Prime p = (Prime) f.getExpression();
                        Token token = p.getToken();
                        if (token.getType() == Token.SyntaxType.LITERAL && expression.getTerms().size() > 1) {
                            if (token.isFalse())
                                termIt.remove();
                            if (token.isTrue()) {
                                expression.getTerms().clear();
                                Term term = new Term();
                                term.getFactors().add(new Factor(false, new Prime(new Token("TRUE", Token.SyntaxType.LITERAL, 0, 0))));
                                expression.addTerm(term);
                                return;
                            }
                        }
                    }
                } else
                    simplifyLiteralsDisjunction(f.getExpression());
            }
        }
    }

    @Simplifier
    @Priority(0)
    private static void simplifyExpressionDisjunction(Expression expression) {
        ArrayList<Term> toDelete = new ArrayList<>();

        ArrayList<Term> terms = expression.getTerms();
        int size = terms.size();
        for(int i = 0; i < size - 1; i++) {
            Term term = terms.get(i);
            if(toDelete.contains(term))
                continue;

            for(int j = i + 1; j < size; j++) {
                if(term.equals(terms.get(j)))
                    toDelete.add(terms.get(j));
            }
        }

        for(Term t : toDelete)
            expression.getTerms().remove(t);

        for(Term t : expression.getTerms()) {
            t.getFactors().forEach(f -> {
                if (!(f.getExpression() instanceof Prime)) {
                    simplifyExpressionDisjunction(f.getExpression());
                }
            });
        }
    }

    @Simplifier
    @Priority(0)
    private static void simplifyExpressionConjunction(Expression expression) {
        ArrayList<Factor> toDelete = new ArrayList<>();

        for (Term t : expression.getTerms()) {
            ArrayList<Factor> factors = t.getFactors();
            int size = factors.size();
            for(int i = 0; i < size - 1; i++) {
                Factor factor = factors.get(i);
                if(toDelete.contains(factor))
                    continue;

                for(int j = i + 1; j < size; j++) {
                    if(factor.equals(factors.get(j)))
                        toDelete.add(factors.get(j));
                }
            }

            toDelete.forEach(factors::remove);

            toDelete.clear();
        }

        for(Term t : expression.getTerms()) {
            t.getFactors().forEach(f -> {
                if (!(f.getExpression() instanceof Prime)) {
                    simplifyExpressionConjunction(f.getExpression());
                }
            });
        }
    }

    @Simplifier
    @Priority(2)
    private static void simplifyNestedExpression(Expression expression) {
        for(Term t : expression.getTerms()) {
            t.getFactors().stream().filter(f -> !(f.getExpression() instanceof Prime)).forEach(f -> {
                if (!f.hasNegation()) {
                    Factor factor = getFactorWithSoloNestedExpression(f);
                    if (factor != null) {
                        f.setExpression(factor.getExpression());
                        f.setHasNegation(factor.hasNegation());
                    }
                }
                simplifyNestedExpression(f.getExpression());
            });
        }
    }

    private static Factor getFactorWithSoloNestedExpression(Factor f) {
        Factor res = null;
        ArrayList<Term> terms = f.getExpression().getTerms();

        if(terms.size() == 1) {
            ArrayList<Factor> factors = terms.get(0).getFactors();
            if (factors.size() == 1) {
                Factor factor = factors.get(0);
                if(factor.isExpression()) {
                    res = factor;
                }
            }
        }

        return res;
    }

    @Simplifier
    @Priority(0)
    private static void simplify_X_AND_NOT_X(Expression expression) {
        for (Term t : expression.getTerms()) {
            ArrayList<Factor> factors = t.getFactors();
            int size = factors.size();

            factors_loop:
            for(int i = 0; i < size - 1; i++) {
                Factor factor = factors.get(i);

                for(int j = i + 1; j < size; j++) {
                    Factor f = factors.get(j);
                    if((factor.hasNegation() && !f.hasNegation() || !factor.hasNegation() && f.hasNegation()) &&
                            factor.getExpression().equals(f.getExpression())) {
                        factors.clear();
                        factors.add(new Factor(false, new Prime(new Token("FALSE", Token.SyntaxType.LITERAL, 0, 0))));
                        break factors_loop;
                    }
                }
            }
        }

        for(Term t : expression.getTerms()) {
            t.getFactors().forEach(f -> {
                if (f.isExpression()) {
                    simplify_X_AND_NOT_X(f.getExpression());
                }
            });
        }
    }

    @Simplifier
    @Priority(0)
    private static void simplify_X_OR_NOT_X(Expression expression) {
        ArrayList<Term> terms = expression.getTerms();
        int size = terms.size();
        terms_loop:
        for(int i = 0; i < size - 1; i++) {
            Term term = terms.get(i);

            if(term.getFactors().size() != 1)
                continue;

            Factor factor = term.getFactors().get(0);
            for(int j = i + 1; j < size; j++) {
                Term t = terms.get(j);
                Factor f = t.getFactors().get(0);
                if((factor.hasNegation() ^ f.hasNegation()) &&
                        factor.getExpression().equals(f.getExpression())) {
                    terms.clear();

                    Term toAdd = new Term();
                    toAdd.getFactors().add(new Factor(false, new Prime(new Token("TRUE", Token.SyntaxType.LITERAL, 0, 0))));
                    terms.add(toAdd);
                    break terms_loop;
                }

            }
        }

        for(Term t : expression.getTerms()) {
            t.getFactors().forEach(f -> {
                if (!(f.getExpression() instanceof Prime)) {
                    simplify_X_OR_NOT_X(f.getExpression());
                }
            });
        }
    }

    @Simplifier
    @Priority(2)
    private static void simplifyDoubleNegation(Expression expression) {
        for(Term t : expression.getTerms()) {
            t.getFactors().stream().filter(f -> !(f.getExpression() instanceof Prime)).forEach(f -> {
                if (f.hasNegation()) {
                    Expression e = getSoloNegatedNestedExpression(f);
                    if (e != null) {
                        f.setExpression(e);
                        f.setHasNegation(false);
                    }
                }
                if(f.isExpression())
                    simplifyDoubleNegation(f.getExpression());
            });
        }
    }

    private static Expression getSoloNegatedNestedExpression(Factor f) {
        Expression res = null;
        ArrayList<Term> terms = f.getExpression().getTerms();

        if(terms.size() == 1) {
            ArrayList<Factor> factors = terms.get(0).getFactors();
            if (factors.size() == 1) {
                Factor factor = factors.get(0);
                if(factor.hasNegation()) {
                    res = factor.getExpression();
                }
            }
        }

        return res;
    }

    @Simplifier
    @Priority(2)
    private static void simplifyNegationOfConjunctionAndDisjunction(Expression expression) {
        for (Term t : expression.getTerms()) {
            ArrayList<Factor> factors = t.getFactors();

            for(Factor f : factors) {
                Expression ex = f.getExpression();
                if(!(ex instanceof Prime)){
                    if(f.hasNegation()) {
                        if (isConjunction(ex)) {
                            f.setExpression(constructNegatedDisjunction(ex));
                            f.setHasNegation(false);
                        }
                        if (isDisjunction(ex)) {
                            f.setExpression(constructNegatedConjunction(ex));
                            f.setHasNegation(false);
                        }
                    }
                    simplifyNegationOfConjunctionAndDisjunction(f.getExpression());
                }
            }
        }
    }

    private static Expression constructNegatedConjunction(Expression expression) {
        Expression res = new Expression();
        res.addTerm(new Term());

        Term conjunction = res.getTerms().get(0);
        for(Term term : expression.getTerms()) {
            Expression ex = new Expression();
            ex.addTerm(new Term(term));

            Factor factor = new Factor();
            factor.setExpression(ex);
            factor.setHasNegation(true);

            conjunction.addFactor(factor);
        }

        return res;
    }

    private static Expression constructNegatedDisjunction(Expression expression) {
        Expression res = new Expression();
        Term t = expression.getTerms().get(0);
        for(Factor f : t.getFactors()) {
            Factor factor = new Factor();
            factor.setExpression(f.getExpression());
            factor.setHasNegation(!f.hasNegation());

            Term term = new Term();
            term.addFactor(factor);
            res.addTerm(term);
        }

        return res;
    }

    private static boolean isDisjunction(Expression expression) {
        return expression.getTerms().size() > 1;
    }

    private static boolean isConjunction(Expression expression) {
        ArrayList<Term> terms = expression.getTerms();

        return terms.size() == 1 && terms.get(0).getFactors().size() > 1;
    }

    @Simplifier
    @Priority(3)
    private static void simplifyOuterParenthesis(Expression fictive) {
        ArrayList<Term> terms = parseTree.getTerms();

        if(terms.size() == 1) {
            ArrayList<Factor> factors = terms.get(0).getFactors();
            if (factors.size() == 1) {
                Factor factor = factors.get(0);
                if (factor.isExpression() && !factor.hasNegation()) {
                    parseTree = factor.getExpression();
                }
            }
        }
    }

    @Simplifier
    @Priority(3)
    private static void simplifyInnerFactorParenthesis(Expression expression) {
        for(Term t : expression.getTerms()) {
            t.getFactors().stream().filter(Factor::isExpression).forEach(f -> {
                Factor factor = getFactorWithSoloNestedPrime(f);
                if (factor != null) {
                    f.setExpression(factor.getExpression());
                    f.setHasNegation(factor.hasNegation() ^ f.hasNegation());
                }
                simplifyInnerFactorParenthesis(f.getExpression());
            });
        }
    }

    private static Factor getFactorWithSoloNestedPrime(Factor f) {
        Factor res = null;
        ArrayList<Term> terms = f.getExpression().getTerms();

        if(terms.size() == 1) {
            ArrayList<Factor> factors = terms.get(0).getFactors();
            if (factors.size() == 1) {
                Factor factor = factors.get(0);
                if (!factor.isExpression())
                    res = factor;
            }
        }

        return res;
    }

    @Simplifier
    @Priority(3)
    private static void simplifyNestedDisjunctionParenthesis(Expression expression) {
        Iterator<Term> termIt = expression.getTerms().iterator();
        ArrayList<ArrayList<Term>> toAdd = new ArrayList<>();

        while(termIt.hasNext()){
            Term t = termIt.next();
            ArrayList<Factor> factors = t.getFactors();
            if(factors.size() > 1)
                continue;

            Factor f = factors.get(0);
            Expression ex = f.getExpression();

            if(!(ex instanceof Prime)){
                if(!f.hasNegation()) {
                    if (isDisjunction(ex)) {
                        termIt.remove();
                        toAdd.add(f.getExpression().getTerms());
                        continue;
                    }
                }
                simplifyNestedDisjunctionParenthesis(f.getExpression());
            }
        }

        for(ArrayList<Term> terms : toAdd)
            expression.getTerms().addAll(terms);
    }

    @Simplifier
    @Priority(3)
    private static void simplifyNestedConjunctionParenthesis(Expression expression) {
        for(Term t : expression.getTerms()) {
            ArrayList<ArrayList<Factor>> toAdd = new ArrayList<>();
            Iterator<Factor> factorIt = t.getFactors().iterator();

            while (factorIt.hasNext()) {
                Factor f = factorIt.next();
                Expression ex = f.getExpression();

                if(!(ex instanceof Prime)){
                    if(!f.hasNegation()) {
                        if (isConjunction(ex)) {
                            factorIt.remove();
                            toAdd.add(f.getExpression().getTerms().get(0).getFactors());
                            continue;
                        }
                    }
                    simplifyNestedConjunctionParenthesis(f.getExpression());
                }
            }

            for(ArrayList<Factor> factors : toAdd)
                t.getFactors().addAll(factors);
        }
    }

    private static void simplifyAll() {
        try {
            Class<?> c = Optimizer.class;
            Method[] allMethods = c.getDeclaredMethods();

            Arrays.sort(allMethods, (m1, m2) -> {
                Priority p1 = m1.getAnnotation(Priority.class);
                Priority p2 = m2.getAnnotation(Priority.class);

                if (p1 != null && p2 != null) {
                    return p1.value() - p2.value();
                } else
                if (p1 != null) {
                    return -1;
                } else
                if (p2 != null) {
                    return 1;
                }
                return 0;
            });

            Expression prevAst = new Expression();

            while(!prevAst.equals(parseTree)) {
                prevAst = new Expression(parseTree);
                for(Method m : allMethods) {
                    if (m.isAnnotationPresent(Simplifier.class))
                        m.invoke(null, parseTree);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printResult(Expression expression) {
        Iterator<Term> termIt = expression.getTerms().iterator();
        while(termIt.hasNext()){
            Term t = termIt.next();

            Iterator<Factor> factorIt = t.getFactors().iterator();
            while(factorIt.hasNext()) {
                Factor f = factorIt.next();

                if(f.hasNegation())
                    System.out.print("NOT ");

                if(f.getExpression() instanceof Prime) {
                    Prime p = (Prime) f.getExpression();
                    Token token = p.getToken();
                    System.out.print(token.getName());
                } else {
                    System.out.print("(");
                    printResult(f.getExpression());
                    System.out.print(")");
                }

                if(factorIt.hasNext())
                    System.out.print(" AND ");
            }
            if(termIt.hasNext())
                System.out.print(" OR ");
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Simplifier {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Priority {int value();}

}
//(x OR y OR z AND x) OR NOT (x AND z OR y OR x)