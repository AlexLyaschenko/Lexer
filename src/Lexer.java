import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
//python -m tokenize 1.py

class Lexer {
    private int tripleQStart = 0;
    private int tripleQEnd = 0;
    private int tripleSStart = 0;
    private int tripleSEnd = 0;
    private int aStart = 0; //[
    private int aEnd = 0; //]
    private int bStart = 0; //{
    private int bEnd = 0; //}
    private int cStart = 0; //(
    private int cEnd = 0; //)
    private String source_code;
    private boolean sign = false;
    private static final char EOF = '$';
    private LinkedList<Integer> depth_stack = new LinkedList<>();
    private int counter = 0;
    private int state = 0;
    private String buffer1 = "";
    private boolean k = true;
    private String indent = "";
    boolean checkIndent = true;
    private boolean checkTripleSingle = false;
    private boolean checkTripleDouble = false;

    private ArrayList<Token> tokenList = new ArrayList<>();

    Lexer(String source_code_file) {
        File input_file = new File(source_code_file);
        byte[] bytes = new byte[1];
        try {
            bytes = Files.readAllBytes(input_file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.source_code = new String(bytes, StandardCharsets.UTF_8);
        this.source_code += EOF;
        this.source_code += "    ";
        this.source_code = "   " + this.source_code;
        depth_stack.addFirst(0);
    }

    void parse() {
        for (int i = 0; i < source_code.length(); i++) {
            char c = source_code.charAt(i);
            if (i > 3 && source_code.charAt(i - 1) == '\n') checkIndent(i);
            switch (state) {

                case 0:
                    StartState_EmptyString(c);
                    break;
                case 1:
                    comment(c);
                    break;
                case 2:
                    boolean b12 = singleQuote(c, i);
                    break;
                case 3:
                    tripleQuote(c, i);
                    break;
                case 4:// + - % & | ^
                    boolean b = waitForSymbolEquals(c);
                    if (b) i--;
                    break;
                case 5:// !
                    boolean bo = waitEqualsforExclamation(c);
                    if (bo) i--;
                    break;
                case 6:// / // /= //=
                    boolean boo = oneDivision(c, i);
                    if (boo) i--;
                    break;
                case 7:// * ** *= **=
                    boolean a = oneMultiplication(c, i);
                    if (a) i--;
                    break;
                case 8:// < << <= <<=
                    boolean aq = leftArrow(c, i);
                    if (aq) i--;
                    break;
                case 9:// > >> >= >>=
                    boolean aw = rightArrow(c, i);
                    if (aw) i--;
                    break;
                case 10: // "afwefew"
                    doubleQuote(c, i);
                    break;
                case 11: // """ fwefew  fwe"""
                    doubleTripleQuote(c, i);
                    break;
                case 12:// 0O 0o 0X 0x 0b 0B 0.
                    boolean b5 = firstZero(c);
                    if (b5) i--;
                    break;
                case 13:
                    int b1 = oct(c);
                    if (b1 == 1) i--;
                    else if (b1 == 2) i -= 2;
                    break;
                case 14:
                    int b2 = hex(c);
                    if (b2 == 1) i--;
                    else if (b2 == 2) i -= 2;
                    break;
                case 15:
                    int b3 = bin(c);
                    if (b3 == 1) i--;
                    else if (b3 == 2) i -= 2;
                    break;
                case 16:
                    boolean b4 = fraction(c);
                    if (b4) i--;
                    break;
                case 17:
                    boolean b6 = decimal(c);
                    if (b6) i--;
                    break;
                case 18:
                    int b7 = exponent(c);
                    if (b7 == 1) i--;
                    else if (b7 == 2) i -= 2;
                    else if (b7 == 3) i -= 3;
                    break;
                default:
                    System.out.println("ERROR: unknown state");
                    return;
            }
        }
        while (depth_stack.getFirst() != 0) {
            tokenList.add(new Token(TokenName.DEDENT, ""));
            depth_stack.pollFirst();
        }

        ////
        for (int i = 0; i < indent.length(); i++) {
            tokenList.add(new Token(TokenName.DEDENT, ""));
        }
//        String[] str = source_code.split("\n");
//        String s = str[str.length - 2];
//        for (int i = 0; i < s.length(); i++) {
//            if (s.charAt(i) != ' ') {
//                for (int j = 0; j < i / 4; j++) {
//                    tokenList.add(new Token(TokenName.DEDENT, ""));
//                }
//                break;
//            }
//        }
//        if (numIndent > numDedent) {
//            int i = 0;
//            while (numIndent != numDedent) {
//                numDedent++;
//                i++;
//                tokenList.add(new Token(TokenName.DEDENT, ""));
//                //if (i == 2) break;
//            }
//        }
        if (aEnd != aStart)
            tokenList.add(new Token(TokenName.ERRORTOKEN, "Missing []"));
        if (bEnd != bStart)
            tokenList.add(new Token(TokenName.ERRORTOKEN, "Missing {}"));
        if (cEnd != cStart)
            tokenList.add(new Token(TokenName.ERRORTOKEN, "Missing ()"));
        if (tripleSStart != tripleSEnd)
            tokenList.add(new Token(TokenName.ERRORTOKEN, "Missing '''"));
        if (tripleQStart != tripleQEnd) {
            tokenList.add(new Token(TokenName.ERRORTOKEN, "Missing \"\"\""));
        }
        tokenList.add(new Token(TokenName.ENDMARKER, ""));
    }

    private void checkIndent(int i) {
        if (checkIndent) {
            i++;
            int p = 1;
            while (source_code.charAt(i) == ' ') {
                p++;
                i++;
                if (source_code.length() == i)
                    return;
            }
            if (source_code.charAt(i) == '\n') return;
            if (checkTripleDouble) return;
            if (checkTripleSingle) return;
            if (aStart != aEnd) return;
            if (bStart != bEnd) return;
            if (cStart != cEnd) return;
            if (indent.length() < p / 4) {
                if (buffer1.length() != 0) {
                    addNameKeyword(buffer1);
                    //tokenList.add(new Token(TokenName.NAME, buffer1));
                    buffer1 = "";
                }
                while (indent.length() != p / 4) {
                    indent += " ";
                    tokenList.add(new Token(TokenName.INDENT, "|" + indent + "|"));
                }
            } else if (indent.length() > p / 4) {
                if (buffer1.length() != 0) {
                    addNameKeyword(buffer1);
                    //tokenList.add(new Token(TokenName.NAME, buffer1));
                    buffer1 = "";
                }
                while (indent.length() != p / 4) {
                    indent = indent.substring(0, indent.length() - 1);
                    tokenList.add(new Token(TokenName.DEDENT, ""));

                }
            }
        }
        checkIndent = true;
    }

    private void StartState_EmptyString(char input) {
        counter = 0;
        if (input == EOF) {
        } else if (input == ' ') {
            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
        } else if (input == '\n' || input == '\r') {
            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            tokenList.add(new Token(TokenName.NL, ""));
            state = 0;
        } else if (input == '#') {
            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            buffer1 += input;
            state = 1;

        } else if (input == '\'') {

            if (buffer1.length() != 0) {
                if (buffer1.equals("ur") || buffer1.equals("ub") || buffer1.equals("u") || buffer1.equals("r") || buffer1.equals("b")) {
                } else {
                    addNameKeyword(buffer1);
                    buffer1 = "";
                }
            }
            buffer1 += input;
            state = 2;
        } else if (input == ',' || input == '(' || input == ')' || input == '[' || input == ']' || input == '{' || input == '}' || input == '`' || input == ':' || input == ';' || input == '~' || input == '@') {

            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            if (input == '[') aStart++;
            else if (input == ']') aEnd++;
            else if (input == '(') cStart++;
            else if (input == ')') cEnd++;
            else if (input == '{') bStart++;
            else if (input == '}') bEnd++;
            addOPTokensChar(input);
            state = 0;
        } else if (input == '+' || input == '-' || input == '%' || input == '&' || input == '|' || input == '^' || input == '=') {

            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            buffer1 += input;
            state = 4;
        } else if (input == '!') {

            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            buffer1 += input;
            state = 5;
        } else if (input == '/') {

            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            buffer1 += input;
            state = 6;
        } else if (input == '*') {

            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            buffer1 += input;
            state = 7;
        } else if (input == '<') {

            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            buffer1 += input;
            state = 8;
        } else if (input == '>') {

            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            buffer1 += input;
            state = 9;
        } else if (input == '"') {
            if (buffer1.length() != 0) {
                if (buffer1.equals("ur") || buffer1.equals("ub") || buffer1.equals("u") || buffer1.equals("r") || buffer1.equals("b")) {
                } else {
                    addNameKeyword(buffer1);
                    buffer1 = "";
                }
            }
            buffer1 += input;
            state = 10;
        } else if (input == '0') {

            if (buffer1.length() != 0) {
                buffer1 += input;
                state = 0;
            } else {
                buffer1 += input;
                state = 12;
            }
        } else if (input == '.') {

            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            buffer1 += input;
            state = 16;
        } else if (input == '1' || input == '2' || input == '3' || input == '4' || input == '5' || input == '6' || input == '7' ||
                input == '8' || input == '9') {

            if (buffer1.length() != 0) {
                buffer1 += input;
                state = 0;
            } else {
                buffer1 += input;
                state = 17;
            }
        } else if (input == '\\') {
            if (buffer1.length() != 0) {
                addNameKeyword(buffer1);
                buffer1 = "";
            }
            state = 0;
            tokenList.add(new Token(TokenName.ERRORTOKEN, "\\"));
        } else {
            k = false;
            buffer1 += input;
            state = 0;
        }
    }    // 0

    private void comment(char input) {
        switch (input) {
            case '\r':
            case '\n':
                tokenList.add(new Token(TokenName.COMMENT, buffer1));
                tokenList.add(new Token(TokenName.NL, ""));
                buffer1 = "";
                state = 0;
                break;
            case EOF:
                tokenList.add(new Token(TokenName.COMMENT, buffer1));
                tokenList.add(new Token(TokenName.NL, ""));
                break;
            default:
                buffer1 += input;
        }
    }

    private boolean singleQuote(char input, int i) {
        checkTripleSingle = true;
        switch (input) {
            case '\'':
                if (source_code.charAt(i - 1) == '\'' && source_code.charAt(i - 2) == '\'' && source_code.charAt(i - 3) != '\'') {
                    buffer1 += input;
                    tripleSStart++;
                    state = 3;
                } else if (buffer1.charAt(buffer1.length() - 1) != '\\' && source_code.charAt(i + 1) != '\'' && i != 2) {
                    buffer1 += input;
                    state = 0;
                    checkTripleSingle = false;
                    tokenList.add(new Token(TokenName.STRING, buffer1));
                    buffer1 = "";
                } else {
                    buffer1 += input;
                    state = 2;
                }
                return false;
            case '\n':
                tokenList.add(new Token(TokenName.STRING, buffer1 + "'"));
                tokenList.add(new Token(TokenName.ERRORTOKEN, " Error, missing '"));
                checkTripleSingle = false;
                buffer1 = "";
                tokenList.add(new Token(TokenName.NL, ""));
                state = 0;
                return true;
            default:
                buffer1 += input;
                return false;
        }
    }

    private void doubleQuote(char input, int i) {
        checkTripleDouble = true;
        switch (input) {
            case '"':
                if (source_code.charAt(i - 1) == '"' && source_code.charAt(i - 2) == '"' && source_code.charAt(i - 3) != '"') {
                    buffer1 += input;
                    state = 11;
                    tripleQStart++;
                } else if (buffer1.charAt(buffer1.length() - 1) != '\\' && source_code.charAt(i + 1) != '"' && i != 2) {
                    buffer1 += input;
                    state = 0;
                    checkTripleDouble = false;
                    tokenList.add(new Token(TokenName.STRING, buffer1));
                    buffer1 = "";
                } else {
                    buffer1 += input;
                    state = 10;
                }
                break;
            case '\n':
                tokenList.add(new Token(TokenName.STRING, buffer1 + "\""));
                tokenList.add(new Token(TokenName.ERRORTOKEN, " Error, missing \""));
                checkTripleDouble = false;
                buffer1 = "";
                tokenList.add(new Token(TokenName.NL, ""));
                state = 0;
                break;
            default:
                buffer1 += input;
        }
    }

    private void doubleTripleQuote(char input, int i) {
        switch (input) {
            case '"':
                if (source_code.charAt(i - 1) == '"' && source_code.charAt(i - 2) == '"' && i > 5 && source_code.charAt(i - 3) != '\\') {
                    buffer1 += input;
                    state = 0;
                    tripleQEnd++;
                    checkTripleDouble = false;
                    tokenList.add(new Token(TokenName.STRING, buffer1));
                    buffer1 = "";
                } else {
                    state = 11;
                    buffer1 += input;
                }
                break;
            default:
                buffer1 += input;
        }
    }

    private void tripleQuote(char input, int i) {
        switch (input) {
            case '\'':
                if (source_code.charAt(i - 1) == '\'' && source_code.charAt(i - 2) == '\'' && i > 5 && source_code.charAt(i - 3) != '\\') {
                    buffer1 += input;
                    state = 0;

                    tripleSEnd++;
                    checkTripleSingle = false;
                    tokenList.add(new Token(TokenName.STRING, buffer1));
                    buffer1 = "";
                } else {
                    state = 3;
                    buffer1 += input;
                }
                break;
            default:
                buffer1 += input;
        }
    }

    private boolean waitForSymbolEquals(char input) {
        switch (input) {
            //+=
            case '=': {
                buffer1 += input;
                addOPTokensString(buffer1);
                buffer1 = "";
                state = 0;
                return false;
            }
            default:
                addOPTokensString(buffer1);
                buffer1 = "";
                state = 0;
                return true;
        }
    }

    private boolean waitEqualsforExclamation(char input) {
        switch (input) {
            case '=':
                buffer1 += input;
                addOPTokensString(buffer1);
                buffer1 = "";
                state = 0;
                return false;
            default:
                tokenList.add(new Token(TokenName.ERRORTOKEN, buffer1));
                buffer1 = "";
                state = 0;
                return true;
        }
    }

    private boolean oneDivision(char input, int i) {
        if (source_code.charAt(i + 1) != '=' || (source_code.charAt(i - 1) == '/' && source_code.charAt((i - 2)) == '/' && input == '='))
            switch (input) {
                case '=':
                case '/':
                    buffer1 += input;
                    addOPTokensString(buffer1);
                    buffer1 = "";
                    state = 0;
                    return false;
                default:
                    addOPTokensString(buffer1);
                    buffer1 = "";
                    state = 0;
                    return true;
            }
        else {
            buffer1 += input;
            return false;
        }
    }

    private boolean oneMultiplication(char input, int i) {
        if (source_code.charAt(i + 1) != '=' || (source_code.charAt(i - 1) == '*' && source_code.charAt((i - 2)) == '*' && input == '='))
            switch (input) {
                case '=':
                case '*':
                    buffer1 += input;
                    addOPTokensString(buffer1);
                    buffer1 = "";
                    state = 0;
                    return false;
                default:
                    addOPTokensString(buffer1);
                    buffer1 = "";
                    state = 0;
                    return true;
            }
        else {
            buffer1 += input;
            return false;
        }
    }

    private boolean leftArrow(char input, int i) {
        if (source_code.charAt(i + 1) != '=' || (source_code.charAt(i - 1) == '<' && source_code.charAt((i - 2)) == '<' && input == '=') || source_code.charAt(i) != '<')
            switch (input) {
                case '=':
                case '<':
                    buffer1 += input;
                    addOPTokensString(buffer1);
                    buffer1 = "";
                    state = 0;
                    return false;
                default:
                    if (input == '>' && buffer1.length() == 1) {
                        state = 0;
                        buffer1 += input;
                        addOPTokensString(buffer1);
                        buffer1 = "";
                        return false;
                    } else {
                        addOPTokensString(buffer1);
                        buffer1 = "";
                        state = 0;
                        return true;
                    }
            }
        else if (input == '>' && buffer1.length() == 1) {
            state = 0;
            buffer1 += input;
            addOPTokensString(buffer1);
            buffer1 = "";
            return false;
        } else {
            buffer1 += input;
            return false;
        }
    }

    private boolean rightArrow(char input, int i) {
        if (source_code.charAt(i + 1) != '=' || (source_code.charAt(i - 1) == '>' && source_code.charAt((i - 2)) == '>' && input == '=') || source_code.charAt(i) != '>')
            switch (input) {
                case '=':
                case '>':
                    buffer1 += input;
                    addOPTokensString(buffer1);
                    buffer1 = "";
                    state = 0;
                    return false;
                default:
                    addOPTokensString(buffer1);
                    buffer1 = "";
                    state = 0;
                    return true;
            }
        else {
            buffer1 += input;
            return false;
        }
    }

    private boolean firstZero(char input) {
        switch (input) {
            case 'o':
            case 'O':
                buffer1 += input;
                state = 13;
                break;
            case 'x':
            case 'X':
                buffer1 += input;
                state = 14;
                break;
            case 'b':
            case 'B':
                buffer1 += input;
                state = 15;
                break;
            case '.':
                buffer1 += input;
                state = 16;
                break;
            default:
                tokenList.add(new Token(TokenName.NUMBER, buffer1));
                buffer1 = "";
                state = 0;
                return true;
        }
        return false;
    }

    private int oct(char input) {
        switch (input) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
                buffer1 += input;
                return 0;
            default:
                state = 0;
                if (buffer1.length() == 2) {
                    tokenList.add(new Token(TokenName.NUMBER, buffer1.substring(0, buffer1.length() - 1)));
                    buffer1 = "";
                    return 2;
                } else {
                    tokenList.add(new Token(TokenName.NUMBER, buffer1));
                    buffer1 = "";
                    return 1;
                }
        }

    }

    private int hex(char input) {
        switch (input) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'f':
                buffer1 += input;
                return 0;
            default:
                state = 0;
                if (buffer1.length() == 2) {
                    tokenList.add(new Token(TokenName.NUMBER, buffer1.substring(0, buffer1.length() - 1)));
                    buffer1 = "";
                    return 2;
                } else {
                    tokenList.add(new Token(TokenName.NUMBER, buffer1));
                    buffer1 = "";
                    return 1;
                }
        }
    }

    private int bin(char input) {
        switch (input) {
            case '0':
            case '1':
                buffer1 += input;
                return 0;
            default:
                state = 0;
                if (buffer1.length() == 2) {
                    tokenList.add(new Token(TokenName.NUMBER, buffer1.substring(0, buffer1.length() - 1)));
                    buffer1 = "";
                    return 2;
                } else {
                    tokenList.add(new Token(TokenName.NUMBER, buffer1));
                    buffer1 = "";
                    return 1;
                }
        }
    }

    private boolean fraction(char input) {
        switch (input) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                buffer1 += input;
                return false;
            case 'j':
            case 'J':
                if (buffer1.length() == 1) {
                    addOPTokensString(buffer1);
                    buffer1 = "";
                    state = 0;
                    return true;
                }
                buffer1 += input;
                state = 0;
                tokenList.add(new Token(TokenName.NUMBER, buffer1));
                buffer1 = "";
                return false;
            case 'e':
            case 'E':
                buffer1 += input;
                state = 18;
                return false;
            default:
                state = 0;
                if (buffer1.length() == 1) {
                    addOPTokensString(buffer1);
                } else if (buffer1.length() > 2 || (buffer1.charAt(1) != 'j' && buffer1.charAt(1) != 'J'))
                    tokenList.add(new Token(TokenName.NUMBER, buffer1));

                buffer1 = "";
                return true;
        }
    }

    private boolean decimal(char input) {
        switch (input) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                buffer1 += input;
                return false;
            case '.':
                buffer1 += input;
                state = 16;
                return false;
            case 'l':
            case 'L':
            case 'j':
            case 'J':
                buffer1 += input;
                state = 0;
                tokenList.add(new Token(TokenName.NUMBER, buffer1));
                buffer1 = "";
                return false;
            case 'e':
            case 'E':
                buffer1 += input;
                state = 18;
                return false;
            default:
                state = 0;
                tokenList.add(new Token(TokenName.NUMBER, buffer1));
                buffer1 = "";
                return true;
        }
    }

    private int exponent(char input) {
        if (buffer1.length() == 2) {
            boolean sd = false;
            for (int i = 0; i < buffer1.length(); i++) {
                if (buffer1.charAt(i) == '0' || buffer1.charAt(i) == '1' || buffer1.charAt(i) == '2' || buffer1.charAt(i) == '3' || buffer1.charAt(i) == '4' ||
                        buffer1.charAt(i) == '5' || buffer1.charAt(i) == '6' || buffer1.charAt(i) == '7' || buffer1.charAt(i) == '8' ||
                        buffer1.charAt(i) == '9') {
                    sd = true;
                }
            }
            if (!sd) {
                addOPTokensString(buffer1.substring(0, buffer1.length() - 1));
                buffer1 = "";
                state = 0;
                return 2;
            }
        }
        switch (input) {
            case '+':
            case '-':
                if (sign) {
                    if (buffer1.charAt(buffer1.length() - 1) != '+' && buffer1.charAt(buffer1.length() - 1) != '-') {
                        tokenList.add(new Token(TokenName.NUMBER, buffer1));
                        buffer1 = "";
                        state = 0;
                        sign = false;
                        return 1;
                    }
                } else {
                    if (buffer1.charAt(buffer1.length() - 1) == 'e' || buffer1.charAt(buffer1.length() - 1) == 'E') {
                        sign = true;
                        buffer1 += input;
                        return 0;
                    } else {
                        tokenList.add(new Token(TokenName.NUMBER, buffer1));
                        buffer1 = "";
                        state = 0;
                        return 1;
                    }
                }
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                buffer1 += input;
                return 0;
            case 'j':
            case 'J':
                if (buffer1.charAt(buffer1.length() - 1) == '1' || buffer1.charAt(buffer1.length() - 1) == '3' || buffer1.charAt(buffer1.length() - 1) == '4' ||
                        buffer1.charAt(buffer1.length() - 1) == '5' || buffer1.charAt(buffer1.length() - 1) == '6' || buffer1.charAt(buffer1.length() - 1) == '7' ||
                        buffer1.charAt(buffer1.length() - 1) == '8' || buffer1.charAt(buffer1.length() - 1) == '9' || buffer1.charAt(buffer1.length() - 1) == '0' ||
                        buffer1.charAt(buffer1.length() - 1) == '2') {
                    buffer1 += input;
                    tokenList.add(new Token(TokenName.NUMBER, buffer1));
                    buffer1 = "";
                    state = 0;
                    return 0;
                } else {
                    if (buffer1.charAt(buffer1.length() - 1) != 'e' && buffer1.charAt(buffer1.length() - 1) != 'E' &&
                            buffer1.charAt(buffer1.length() - 1) != '+' && buffer1.charAt(buffer1.length() - 1) != '-') {
                        tokenList.add(new Token(TokenName.NUMBER, buffer1));
                        buffer1 = "";
                        state = 0;
                        return 1;
                    } else if (buffer1.charAt(buffer1.length() - 1) == 'e' || buffer1.charAt(buffer1.length() - 1) == 'E') {
                        tokenList.add(new Token(TokenName.NUMBER, buffer1.substring(0, buffer1.length() - 1)));
                        buffer1 = "";
                        state = 0;
                        return 2;
                    } else if (buffer1.charAt(buffer1.length() - 1) == '+' || buffer1.charAt(buffer1.length() - 1) == '-') {
                        tokenList.add(new Token(TokenName.NUMBER, buffer1.substring(0, buffer1.length() - 2)));
                        buffer1 = "";
                        state = 0;
                        return 3;
                    }
                }
            default:
                if (buffer1.charAt(buffer1.length() - 1) == 'e' || buffer1.charAt(buffer1.length() - 1) == 'E') {
                    String buf = buffer1.substring(0, buffer1.length() - 1);
                    tokenList.add(new Token(TokenName.NUMBER, buf));
                    buffer1 = "";
                    sign = false;
                    state = 0;
                    return 2;
                } else if (buffer1.charAt(buffer1.length() - 1) == '+' || buffer1.charAt(buffer1.length() - 1) == '-') {
                    String buf = buffer1.substring(0, buffer1.length() - 2);
                    tokenList.add(new Token(TokenName.NUMBER, buf));
                    buffer1 = "";
                    state = 0;
                    sign = false;
                    return 3;
                }
                tokenList.add(new Token(TokenName.NUMBER, buffer1));
                buffer1 = "";
                state = 0;
                sign = false;
                return 1;
        }
    }

    void showResult() {
        for (int i = 0; i < tokenList.size(); i++) {
            for (int j = 1; j < tokenList.size(); j++) {
                if (tokenList.get(j - 1).name == TokenName.DEDENT && tokenList.get(j).name == TokenName.NL) {
                    Token t = tokenList.get(j - 1);
                    tokenList.set(j - 1, tokenList.get(j));
                    tokenList.set(j, t);
                    break;
                }
            }
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter("tokens.txt", false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Token token : tokenList) {
            System.out.println(token.toString());
            try {
                writer.write(token.toString() + '\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addOPTokensChar(char input) {
        switch (input) {
            case ',':
                tokenList.add(new Token(TokenName.COMMA, "" + input));
                break;
            case '(':
                tokenList.add(new Token(TokenName.LPAR, "" + input));
                break;
            case ')':
                tokenList.add(new Token(TokenName.RPAR, "" + input));
                break;
            case '[':
                tokenList.add(new Token(TokenName.LSQB, "" + input));
                break;
            case ']':
                tokenList.add(new Token(TokenName.RSQB, "" + input));
                break;
            case '{':
                tokenList.add(new Token(TokenName.LBRACE, "" + input));
                break;
            case '}':
                tokenList.add(new Token(TokenName.RBRACE, "" + input));
                break;
            case '`':
            case '~':
                tokenList.add(new Token(TokenName.TILDE, "" + input));
                break;
            case ':':
                tokenList.add(new Token(TokenName.COLON, "" + input));
                break;
            case ';':
                tokenList.add(new Token(TokenName.SEMI, "" + input));
                break;
            case '+':
                tokenList.add(new Token(TokenName.PLUS, "" + input));
                break;
            case '-':
                tokenList.add(new Token(TokenName.MINUS, "" + input));
                break;
            case '%':
                tokenList.add(new Token(TokenName.PERCENT, "" + input));
                break;
            case '&':
                tokenList.add(new Token(TokenName.AMPER, "" + input));
                break;
            case '|':
                tokenList.add(new Token(TokenName.VBAR, "" + input));
                break;
            case '^':
                tokenList.add(new Token(TokenName.CIRCUMFLEX, "" + input));
                break;
            case '=':
                tokenList.add(new Token(TokenName.EQUAL, "" + input));
                break;
            case '*':
                tokenList.add(new Token(TokenName.STAR, "" + input));
                break;
            case '/':
                tokenList.add(new Token(TokenName.SLASH, "" + input));
                break;
            case '<':
                tokenList.add(new Token(TokenName.LESS, "" + input));
                break;
            case '>':
                tokenList.add(new Token(TokenName.GREATER, "" + input));
                break;
            case '.':
                tokenList.add(new Token(TokenName.DOT, "" + input));
                break;
            case '@':
                tokenList.add(new Token(TokenName.AT, "" + input));
                break;
        }
    }

    private void addOPTokensString(String input) {
        switch (input) {
            case ",":
                tokenList.add(new Token(TokenName.COMMA, input));
                break;
            case "(":
                tokenList.add(new Token(TokenName.LPAR, input));
                break;
            case ")":
                tokenList.add(new Token(TokenName.RPAR, input));
                break;
            case "[":
                tokenList.add(new Token(TokenName.LSQB, input));
                break;
            case "]":
                tokenList.add(new Token(TokenName.RSQB, input));
                break;
            case "{":
                tokenList.add(new Token(TokenName.LBRACE, input));
                break;
            case "}":
                tokenList.add(new Token(TokenName.RBRACE, input));
                break;
            case "`":
            case "~":
                tokenList.add(new Token(TokenName.TILDE, input));
                break;
            case ":":
                tokenList.add(new Token(TokenName.COLON, input));
                break;
            case ";":
                tokenList.add(new Token(TokenName.SEMI, input));
                break;
            case "+":
                tokenList.add(new Token(TokenName.PLUS, input));
                break;
            case "-":
                tokenList.add(new Token(TokenName.MINUS, input));
                break;
            case "%":
                tokenList.add(new Token(TokenName.PERCENT, input));
                break;
            case "&":
                tokenList.add(new Token(TokenName.AMPER, input));
                break;
            case "|":
                tokenList.add(new Token(TokenName.VBAR, input));
                break;
            case "^":
                tokenList.add(new Token(TokenName.CIRCUMFLEX, input));
                break;
            case "=":
                tokenList.add(new Token(TokenName.EQUAL, input));
                break;
            case "*":
                tokenList.add(new Token(TokenName.STAR, input));
                break;
            case "/":
                tokenList.add(new Token(TokenName.SLASH, input));
                break;
            case "<":
                tokenList.add(new Token(TokenName.LESS, input));
                break;
            case ">":
                tokenList.add(new Token(TokenName.GREATER, input));
                break;
            case ".":
                tokenList.add(new Token(TokenName.DOT, input));
                break;
            case "@":
                tokenList.add(new Token(TokenName.AT, input));
                break;
            case "==":
                tokenList.add(new Token(TokenName.EQUEQUAL, input));
                break;
            case "!=":
                tokenList.add(new Token(TokenName.NOTEQUAL, input));
                break;
            case "<=":
                tokenList.add(new Token(TokenName.LESSQUAL, input));
                break;
            case ">=":
                tokenList.add(new Token(TokenName.GREATERQUAL, input));
                break;
            case "<<":
                tokenList.add(new Token(TokenName.LEFTSHIFT, input));
                break;
            case ">>":
                tokenList.add(new Token(TokenName.RIGHTSHIFT, input));
                break;
            case "**":
                tokenList.add(new Token(TokenName.DOUBLESTAR, input));
                break;
            case "+=":
                tokenList.add(new Token(TokenName.PLUSEQUAL, input));
                break;
            case "-=":
                tokenList.add(new Token(TokenName.MINUSEQUAL, input));
                break;
            case "*=":
                tokenList.add(new Token(TokenName.STAREQUAL, input));
                break;
            case "/=":
                tokenList.add(new Token(TokenName.SLASHEQUAL, input));
                break;
            case "%=":
                tokenList.add(new Token(TokenName.PERCENTEQUAL, input));
                break;
            case "&=":
                tokenList.add(new Token(TokenName.AMPEREQUAL, input));
                break;
            case "|=":
                tokenList.add(new Token(TokenName.VBAREQUAL, input));
                break;
            case "^=":
                tokenList.add(new Token(TokenName.CIRCUMFLEXEQUAL, input));
                break;
            case "<<=":
                tokenList.add(new Token(TokenName.LEFTSHIFTEQUAL, input));
                break;
            case ">>=":
                tokenList.add(new Token(TokenName.RIGHTSHIFTEQUAL, input));
                break;
            case "**=":
                tokenList.add(new Token(TokenName.DOUBLESTAREQUAL, input));
                break;
            case "//":
                tokenList.add(new Token(TokenName.DOUBLESLASH, input));
                break;
            case "//=":
                tokenList.add(new Token(TokenName.DOUBLESLASHEQUAL, input));
                break;
            case "<>":
                tokenList.add(new Token(TokenName.NOTEQUAL, input));
                break;
            default:
                tokenList.add(new Token(TokenName.OP, input));
                break;
        }
    }

    private void addNameKeyword(String input) {
        switch (input) {
            case "False":
            case "True":
            case "None":
            case "and":
            case "as":
            case "with":
            case "assert":
            case "break":
            case "class":
            case "continue":
            case "def":
            case "del":
            case "elif":
            case "else":
            case "except":
            case "finally":
            case "for":
            case "from":
            case "global":
            case "if":
            case "import":
            case "in":
            case "is":
            case "lambda":
            case "nonlocal":
            case "not":
            case "or":
            case "pass":
            case "raise":
            case "return":
            case "try":
            case "while":
            case "yield":
                tokenList.add(new Token(TokenName.KEYWORD, input));
                break;
            default:
                tokenList.add(new Token(TokenName.NAME, input));
                break;
        }
    }


}
