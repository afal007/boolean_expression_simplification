package org.stepik.fal.compiler;

import org.stepik.fal.exception.IllegalLexemeException;

import java.util.ArrayList;


/**
 * author: Alexander Fal (falalexandr007@gmail.com)
 */
class LexicalAnalyser {
    private static String str;
    private static int charIndex;

    private enum State {
        INIT(false),
        T(false),
        TR(false),
        TRU_OR_FALS(false),
        F(false),
        FA(false),
        FAL(false),
        O(false),
        N(false),
        NO(false),
        A(false),
        AN(false),
        IDENTIFIER(false),
        END_OR(true),
        END_AND(true),
        END_NOT(true),
        END_LITERAL(true),
        END_IDENTIFIER(true),
        END_IDENTIFIER_TRUNCATE(true),
        END_LP(true),
        END_RP(true),
        ERROR(true);

        final boolean endState;

        State(boolean endState){
            this.endState = endState;
        }
    }


    static ArrayList<Token> getTokens(String _str) throws IllegalLexemeException {
        str = _str;
        charIndex = 0;
        ArrayList<Token> tokens = new ArrayList<>();

        tokens.add(getToken());
        while(!tokens.get(tokens.size() - 1).getName().equals("EOF"))
            tokens.add(getToken());

        return tokens;
    }

    /**
     * Get next token from string
     */
    private static Token getToken() throws IllegalLexemeException {
        StringBuilder lexeme = new StringBuilder("");
        State state = State.INIT;
        Token token = new Token();
        char ch = ' ';

        outerloop:
        while(true) {

            // If we are in ending or error state there is no need to read next character
            if(!state.endState) {
                if(charIndex < str.length()) {          // If we have more characters in string
                    ch = str.charAt(charIndex++);       // Read next char
                    lexeme.append(ch);
                } else {                                // If we have no more char and not in ending or error state
                    if(state.equals(State.INIT)) {      // Means that we scanned all string and next token is EOF
                        token.setEof(charIndex);
                        break;
                    } else {                            // Means that we are in some intermediate state which is wrong
                        state = State.ERROR;
                    }
                }
            }

            switch (state) {
                case INIT:
                    token.setStart(charIndex - 1);                  // -1 because we already incremented the value

                    state = initSwitch(ch);
                    if(state == State.INIT)                         // Means that ch was a whitespace
                        lexeme.setLength(lexeme.length() - 1);

                    break;

                case T:
                    if(ch == 'R')
                        state = State.TR;
                    else
                        state = State.ERROR;
                    break;

                case TR:
                    if(ch == 'U')
                        state = State.TRU_OR_FALS;
                    else
                        state = State.ERROR;
                    break;

                case TRU_OR_FALS:
                    if(ch == 'E')
                        state = State.END_LITERAL;
                    else
                        state = State.ERROR;
                    break;

                case F:
                    if(ch == 'A')
                        state = State.FA;
                    else
                        state = State.ERROR;
                    break;

                case FA:
                    if(ch == 'L')
                        state = State.FAL;
                    else
                        state = State.ERROR;
                    break;

                case FAL:
                    if(ch == 'S')
                        state = State.TRU_OR_FALS;
                    else
                        state = State.ERROR;
                    break;

                case O:
                    if(ch == 'R')
                        state = State.END_OR;
                    else
                        state = State.ERROR;
                    break;

                case N:
                    if(ch == 'O')
                        state = State.NO;
                    else
                        state = State.ERROR;
                    break;

                case NO:
                    if(ch == 'T')
                        state = State.END_NOT;
                    else
                        state = State.ERROR;
                    break;

                case A:
                    if(ch == 'N')
                        state = State.AN;
                    else
                        state = State.ERROR;
                    break;

                case AN:
                    if(ch == 'D')
                        state = State.END_AND;
                    else
                        state = State.ERROR;
                    break;

                case IDENTIFIER:
                    state = identifierSwitch(ch);
                    break;

                case END_IDENTIFIER_TRUNCATE:
                    lexeme.setLength(lexeme.length() - 1);
                    charIndex--;
                    state = State.END_IDENTIFIER;
                case END_IDENTIFIER:
                case END_NOT:
                case END_AND:
                case END_OR:
                case END_LITERAL:
                case END_LP:
                case END_RP:
                    token.setName(lexeme.toString());
                    // Convert state type to token syntax category by cutting first 4 symbols
                    token.setType(Token.SyntaxType.valueOf(state.toString().substring(4)));
                    token.setEnd(charIndex);

                    lexeme.setLength(0);
                    break outerloop;

                case ERROR:
                    throw new IllegalLexemeException("Parse error: Could not parse fragment at [" + token.getStart() + "..." + --charIndex + "]");
            }
        }

        return token;
    }

    private static State identifierSwitch(char ch) {
        State state;
        switch (ch) {
            case 'T':
            case 'F':
            case 'O':
            case 'A':
            case 'N':
            case '(':
            case ')':
            case ' ':
                state = State.END_IDENTIFIER_TRUNCATE;
                break;
            default:
                // I'm not sure if this is bad style, but switch for each lowercase letter is really ugly
                if(ch >= 'a' && ch <= 'z') {
                    if (charIndex >= str.length()) {
                        state = State.END_IDENTIFIER;
                    } else {
                        state = State.IDENTIFIER;
                    }
                }
                else {
                    state = State.ERROR;
                }
        }

        return state;
    }
    private static State initSwitch(char ch) {
        State state;
        switch (ch) {
            case ')':
                state = State.END_RP;
                break;
            case '(':
                state = State.END_LP;
                break;
            case 'N':
                state = State.N;
                break;
            case 'T':
                state = State.T;
                break;
            case 'F':
                state = State.F;
                break;
            case 'O':
                state = State.O;
                break;
            case 'A':
                state = State.A;
                break;
            case ' ':
                state = State.INIT;
                break;
            default:
                //I'm not sure if this is bad style, but switch for each lowercase letter is really ugly
                if(ch >= 'a' && ch <= 'z')
                    if (charIndex >= str.length()) {
                        state = State.END_IDENTIFIER;
                    } else {
                        state = State.IDENTIFIER;
                    }
                else
                    state = State.ERROR;
        }

        return state;
    }
}
