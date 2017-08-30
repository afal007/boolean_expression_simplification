package org.stepik.fal.compiler;

import org.stepik.fal.compiler.parsetree.Expression;
import org.stepik.fal.compiler.parsetree.Factor;
import org.stepik.fal.compiler.parsetree.Prime;
import org.stepik.fal.compiler.parsetree.Term;
import org.stepik.fal.exception.IllegalLexemeException;
import org.stepik.fal.exception.SyntaxErrorException;

import java.util.ArrayList;

/**
 * author: Alexander Fal (falalexandr007@gmail.com)
 */
class SyntaxAnalyser {
    private static ArrayList<Token> tokens;
    private static Token curTok;
    private static int tokenIndex;


    static Expression buildAST(String str) throws SyntaxErrorException, IllegalLexemeException {
        tokens = LexicalAnalyser.getTokens(str);
        tokenIndex = 0;

        Expression ret = expression();
        if(curTok.getType() != Token.SyntaxType.EOF)
            throw new SyntaxErrorException("Expected EOF but got " + curTok.getType().toString() +
                    " at " + "[" + curTok.getStart() + "..." + curTok.getEnd() + "]");

        return ret;
    }

    private static Expression expression() throws SyntaxErrorException {
        Expression e = new Expression();
        e.addTerm(term());

        while(true) {
            switch (curTok.getType()) {
                case OR:
                    e.addTerm(term());
                    break;
                case RP:
                case EOF:
                    return e;
                default:
                    throw new SyntaxErrorException("Expected TERM but got " + curTok.getType().toString() +
                                                    " at " + "[" + curTok.getStart() + "..." + curTok.getEnd() + "]");
            }
        }
    }

    private static Term term() throws SyntaxErrorException {
        Term t = new Term();
        t.addFactor(factor());

        while(true) {
            switch (curTok.getType()) {
                case AND:
                    t.addFactor(factor());
                    break;
                case OR:
                case RP:
                case EOF:
                    return t;
                default:
                    throw new SyntaxErrorException("Expected FACTOR but got " + curTok.getType().toString() +
                                                    " at " + "[" + curTok.getStart() + "..." + curTok.getEnd() + "]");
            }
        }
    }

    private static Factor factor() throws SyntaxErrorException {
        Factor f = new Factor();
        curTok = tokens.get(tokenIndex++);

        switch (curTok.getType()) {
            case NOT:
                f.setHasNegation(true);
                f.setExpression(prime(true));
                break;
            case LITERAL:
            case IDENTIFIER:
            case LP:
                f.setHasNegation(false);
                f.setExpression(prime(false));
                break;
            default:
                throw new SyntaxErrorException( "Expected PRIME or NOT PRIME but got " + curTok.getType().toString() +
                        " at " + "[" + curTok.getStart() + "..." + curTok.getEnd() + "]");
        }

        return f;
    }

    private static Expression prime(boolean get) throws SyntaxErrorException {
        Expression e;
        if(get)
            curTok = tokens.get(tokenIndex++);

        switch (curTok.getType()) {
            case LITERAL:
            case IDENTIFIER:
                e = new Prime(new Token(curTok));
                curTok = tokens.get(tokenIndex++);
                break;
            case LP:
                e = expression();
                if(!curTok.getType().equals(Token.SyntaxType.RP))
                    throw new SyntaxErrorException("Expected RP but got " + curTok.getType().toString() + " at " + "[" +
                                                    curTok.getStart() + "..." + curTok.getEnd() + "]");
                curTok = tokens.get(tokenIndex++);
                break;
            default:
                throw new SyntaxErrorException( "Expected LITERAL or IDENTIFIER or LP but got " +
                        curTok.getType().toString() + " at " + "[" + curTok.getStart() + "..." + curTok.getEnd() + "]");
        }

        return e;
    }
}
