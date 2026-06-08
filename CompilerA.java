// (a) Analisador sintático mais a geração de código em C (instruções de geração misturadas).
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CompilerA {

    public static class Token {
        public enum Type {
            // Keywords / Commands
            ASSIGN("="),
            GET("G"),
            ADD("+"),
            SUB("-"),
            MULT("*"),
            DIV("/"),
            MOD("%"),
            PRINT("P"),
            IF("I"),
            WHILE("W"),
            LBRACE("{"),
            RBRACE("}"),
            
            // Operators in Comparisons
            LT("<"),
            NEQ("#"),
            
            // Operands
            VARIABLE("Variable"),
            NUMBER("Number"),
            
            // Special
            EOF("EOF");

            private final String label;
            Type(String label) { this.label = label; }
            public String getLabel() { return label; }
        }

        public final Type type;
        public final String value;
        public final int line;
        public final int col;

        public Token(Type type, String value, int line, int col) {
            this.type = type;
            this.value = value;
            this.line = line;
            this.col = col;
        }

        @Override
        public String toString() {
            return type + "('" + value + "') at line " + line + ", col " + col;
        }
    }

    public static class Lexer {
        private final String input;
        private int pos = 0;
        private int line = 1;
        private int col = 1;

        public Lexer(String input) {
            this.input = input;
        }

        private void skipWhitespace() {
            while (pos < input.length()) {
                char ch = input.charAt(pos);
                if (ch == '\n') {
                    line++;
                    col = 1;
                    pos++;
                } else if (ch == '\r') {
                    pos++;
                    if (pos < input.length() && input.charAt(pos) == '\n') {
                        line++;
                        col = 1;
                        pos++;
                    } else {
                        line++;
                        col = 1;
                    }
                } else if (ch == ' ' || ch == '\t') {
                    pos++;
                    col++;
                } else {
                    break;
                }
            }
        }

        public Token nextToken() {
            skipWhitespace();
            if (pos >= input.length()) {
                return new Token(Token.Type.EOF, "", line, col);
            }

            char ch = input.charAt(pos);
            int startLine = line;
            int startCol = col;

            // Variable check: lowercase letter
            if (ch >= 'a' && ch <= 'z') {
                pos++;
                col++;
                return new Token(Token.Type.VARIABLE, String.valueOf(ch), startLine, startCol);
            }

            // Number check: one or more digits
            if (ch >= '0' && ch <= '9') {
                StringBuilder sb = new StringBuilder();
                while (pos < input.length()) {
                    ch = input.charAt(pos);
                    if (ch >= '0' && ch <= '9') {
                        sb.append(ch);
                        pos++;
                        col++;
                    } else {
                        break;
                    }
                }
                return new Token(Token.Type.NUMBER, sb.toString(), startLine, startCol);
            }

            Token.Type type = null;
            switch (ch) {
                case '=': type = Token.Type.ASSIGN; break;
                case '<': type = Token.Type.LT; break;
                case '#': type = Token.Type.NEQ; break;
                case '{': type = Token.Type.LBRACE; break;
                case '}': type = Token.Type.RBRACE; break;
                case '+': type = Token.Type.ADD; break;
                case '-': type = Token.Type.SUB; break;
                case '*': type = Token.Type.MULT; break;
                case '/': type = Token.Type.DIV; break;
                case '%': type = Token.Type.MOD; break;
                case 'G': type = Token.Type.GET; break;
                case 'P': type = Token.Type.PRINT; break;
                case 'I': type = Token.Type.IF; break;
                case 'W': type = Token.Type.WHILE; break;
            }

            if (type != null) {
                pos++;
                col++;
                return new Token(type, String.valueOf(ch), startLine, startCol);
            }

            throw new RuntimeException("Erro na linha " + startLine + ", coluna " + startCol + ": Comando ou caractere inválido '" + ch + "'");
        }
    }

    public static class CommandResult {
        public final String lps1;
        public final String cCode;

        public CommandResult(String lps1, String cCode) {
            this.lps1 = lps1;
            this.cCode = cCode;
        }
    }

    public static class Parser {
        private final Lexer lexer;
        private Token currentToken;

        public Parser(Lexer lexer) {
            this.lexer = lexer;
            advance();
        }

        private void advance() {
            currentToken = lexer.nextToken();
        }

        private void match(Token.Type type, String errorMessage) {
            if (currentToken.type == type) {
                advance();
            } else {
                error(errorMessage);
            }
        }

        private void error(String message) {
            throw new RuntimeException("Erro na linha " + currentToken.line + ", coluna " + currentToken.col + ": " + message);
        }

        private String getIndent(int indent) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indent; i++) {
                sb.append("  ");
            }
            return sb.toString();
        }

        public String parseProgram() {
            StringBuilder sb = new StringBuilder();
            sb.append("#include <stdio.h>\n\n");
            sb.append("int main() {\n");
            sb.append("  int a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;\n");
            sb.append("  a = b = c = d = e = f = g = h = i = j = k = l = m = n = o = p = q = r = s = t = u = v = w = x = y = z = 0;\n");
            sb.append("  char str[512]; // auxiliar na leitura com G\n");

            while (currentToken.type != Token.Type.EOF) {
                CommandResult res = parseCommand(1);
                sb.append(res.cCode);
            }

            sb.append("  return 0;\n");
            sb.append("}\n");
            return sb.toString();
        }

        private CommandResult parseCommand(int indent) {
            String ind = getIndent(indent);
            switch (currentToken.type) {
                case ASSIGN: {
                    advance();
                    String var = currentToken.value;
                    match(Token.Type.VARIABLE, "Variável esperada");
                    String val = parseValue();
                    String lps1 = "= " + var + " " + val;
                    String cCode = ind + "// " + lps1 + "\n" +
                                   ind + var + " = " + val + ";\n";
                    return new CommandResult(lps1, cCode);
                }
                case GET: {
                    advance();
                    String var = currentToken.value;
                    match(Token.Type.VARIABLE, "Variável esperada");
                    String lps1 = "G " + var;
                    String cCode = ind + "// " + lps1 + "\n" +
                                   ind + "{\n" +
                                   ind + "  if (fgets(str, sizeof(str), stdin) != NULL) {\n" +
                                   ind + "    sscanf(str, \"%d\", &" + var + ");\n" +
                                   ind + "  }\n" +
                                   ind + "}\n";
                    return new CommandResult(lps1, cCode);
                }
                case ADD:
                case SUB:
                case MULT:
                case DIV:
                case MOD: {
                    String opToken = currentToken.value;
                    advance();
                    String var = currentToken.value;
                    match(Token.Type.VARIABLE, "Variável esperada");
                    String val1 = parseValue();
                    String val2 = parseValue();
                    String lps1 = opToken + " " + var + " " + val1 + " " + val2;
                    String cCode = ind + "// " + lps1 + "\n" +
                                   ind + var + " = " + val1 + " " + opToken + " " + val2 + ";\n";
                    return new CommandResult(lps1, cCode);
                }
                case PRINT: {
                    advance();
                    String val = parseValue();
                    String lps1 = "P " + val;
                    String cCode = ind + "// " + lps1 + "\n" +
                                   ind + "printf(\"%d\\n\", " + val + ");\n";
                    return new CommandResult(lps1, cCode);
                }
                case IF: {
                    advance();
                    ComparisonResult comp = parseComparison();
                    if (currentToken.type == Token.Type.LBRACE) {
                        // Composite command
                        advance();
                        StringBuilder bodyCode = new StringBuilder();
                        while (currentToken.type != Token.Type.RBRACE && currentToken.type != Token.Type.EOF) {
                            CommandResult res = parseCommand(indent + 1);
                            bodyCode.append(res.cCode);
                        }
                        match(Token.Type.RBRACE, "Fecha chave ('}') esperado");
                        String lps1 = "I " + comp.lps1 + " {";
                        String cCode = ind + "// " + lps1 + "\n" +
                                       ind + "if ( " + comp.cCode + " ) {\n" +
                                       bodyCode.toString() +
                                       ind + "}\n";
                        return new CommandResult(lps1, cCode);
                    } else {
                        // Simple command
                        CommandResult res = parseCommand(indent + 1);
                        String lps1 = "I " + comp.lps1 + " " + res.lps1;
                        String cCode = ind + "// " + lps1 + "\n" +
                                       ind + "if ( " + comp.cCode + " ) {\n" +
                                       res.cCode +
                                       ind + "}\n";
                        return new CommandResult(lps1, cCode);
                    }
                }
                case WHILE: {
                    advance();
                    ComparisonResult comp = parseComparison();
                    if (currentToken.type == Token.Type.LBRACE) {
                        // Composite command
                        advance();
                        StringBuilder bodyCode = new StringBuilder();
                        while (currentToken.type != Token.Type.RBRACE && currentToken.type != Token.Type.EOF) {
                            CommandResult res = parseCommand(indent + 1);
                            bodyCode.append(res.cCode);
                        }
                        match(Token.Type.RBRACE, "Fecha chave ('}') esperado");
                        String lps1 = "W " + comp.lps1 + " {";
                        String cCode = ind + "// " + lps1 + "\n" +
                                       ind + "while ( " + comp.cCode + " ) {\n" +
                                       bodyCode.toString() +
                                       ind + "}\n";
                        return new CommandResult(lps1, cCode);
                    } else {
                        // Simple command
                        CommandResult res = parseCommand(indent + 1);
                        String lps1 = "W " + comp.lps1 + " " + res.lps1;
                        String cCode = ind + "// " + lps1 + "\n" +
                                       ind + "while ( " + comp.cCode + " ) {\n" +
                                       res.cCode +
                                       ind + "}\n";
                        return new CommandResult(lps1, cCode);
                    }
                }
                case LBRACE: {
                    advance();
                    StringBuilder bodyCode = new StringBuilder();
                    while (currentToken.type != Token.Type.RBRACE && currentToken.type != Token.Type.EOF) {
                        CommandResult res = parseCommand(indent + 1);
                        bodyCode.append(res.cCode);
                    }
                    match(Token.Type.RBRACE, "Fecha chave ('}') esperado");
                    String lps1 = "{";
                    String cCode = ind + "{\n" +
                                   bodyCode.toString() +
                                   ind + "}\n";
                    return new CommandResult(lps1, cCode);
                }
                default:
                    error("Comando inexistente");
                    return null;
            }
        }

        private static class ComparisonResult {
            public final String lps1;
            public final String cCode;
            public ComparisonResult(String lps1, String cCode) {
                this.lps1 = lps1;
                this.cCode = cCode;
            }
        }

        private ComparisonResult parseComparison() {
            String var = currentToken.value;
            match(Token.Type.VARIABLE, "Variável esperada");

            String op = currentToken.value;
            String cOp;
            if (currentToken.type == Token.Type.ASSIGN) {
                cOp = "==";
                advance();
            } else if (currentToken.type == Token.Type.LT) {
                cOp = "<";
                advance();
            } else if (currentToken.type == Token.Type.NEQ) {
                cOp = "!=";
                advance();
            } else {
                error("Operador esperado");
                return null;
            }

            String val = parseValue();
            return new ComparisonResult(var + " " + op + " " + val, var + " " + cOp + " " + val);
        }

        private String parseValue() {
            if (currentToken.type == Token.Type.VARIABLE || currentToken.type == Token.Type.NUMBER) {
                String val = currentToken.value;
                advance();
                return val;
            } else {
                error("Valor esperado");
                return null;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java CompilerA <arquivo.lps1>");
            System.exit(1);
        }
        try {
            String content = Files.readString(Path.of(args[0]));
            Lexer lexer = new Lexer(content);
            Parser parser = new Parser(lexer);
            String cCode = parser.parseProgram();
            System.out.println(cCode);
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
