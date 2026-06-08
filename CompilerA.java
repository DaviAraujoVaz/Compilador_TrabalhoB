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

            // Number check: um único dígito
            if (ch >= '0' && ch <= '9') {
                pos++;
                col++;
                return new Token(Token.Type.NUMBER, String.valueOf(ch), startLine, startCol);
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
        private int traceIndent = 0;
        private StringBuilder traceLog = new StringBuilder();

        public Parser(Lexer lexer) {
            this.lexer = lexer;
            advance();
        }

        public String getTraceLog() {
            return traceLog.toString();
        }

        private String getTraceIndent() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < traceIndent; i++) sb.append("  ");
            return sb.toString();
        }

        private void printTrace(String msg) {
            String line = getTraceIndent() + msg;
            System.out.println(line);
            traceLog.append(line).append("\n");
        }

        private void logHeader(String msg) {
            System.out.println(msg);
            traceLog.append(msg).append("\n");
        }

        private void advance() {
            currentToken = lexer.nextToken();
        }

        private void match(Token.Type type, String errorMessage) {
            if (currentToken.type == type) {
                printTrace("Match: " + type.name() + " ('" + currentToken.value + "')");
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
            logHeader("--- FASE UNICA: Analise Sintatica e Traducao Direta Simultaneas (Compilador A) ---");
            printTrace("-> Analisando Program");
            traceIndent++;
            
            StringBuilder sb = new StringBuilder();
            sb.append("#include <stdio.h>\n\n");
            sb.append("int main() {\n");
            sb.append("  int a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;\n");
            sb.append("  char str[512]; // auxiliar na leitura com G\n");

            while (currentToken.type != Token.Type.EOF) {
                CommandResult res = parseCommand(1);
                sb.append(res.cCode);
            }

            traceIndent--;
            sb.append("  gets(str);\n");
            sb.append("}\n");
            logHeader("--- FIM DA FASE UNICA: Analise e Traducao concluidas simultaneamente ---\n");
            System.out.println("--- Codigo C Gerado ---");
            return sb.toString();
        }

        private CommandResult parseCommand(int indent) {
            printTrace("-> Analisando Command e Gerando Codigo em Tempo Real");
            traceIndent++;
            try {
                String ind = getIndent(indent);
                switch (currentToken.type) {
                    case ASSIGN: {
                        printTrace("Regra: AssignCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        String var = currentToken.value;
                        match(Token.Type.VARIABLE, "Variável esperada");
                        String val = parseValue();
                        String lps1 = "= " + var + " " + val;
                        String cCode = ind + "// " + lps1 + "\n" +
                                       ind + var + " = " + val + ";\n";
                        printTrace("[+] Gerando C agora: " + var + " = " + val + ";");
                        return new CommandResult(lps1, cCode);
                    }
                    case GET: {
                        printTrace("Regra: GetCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        String var = currentToken.value;
                        match(Token.Type.VARIABLE, "Variável esperada");
                        String lps1 = "G " + var;
                        String cCode = ind + "// " + lps1 + "\n" +
                                       ind + "{ gets(str);\n" +
                                       ind + "  sscanf(str, \"%d\", &" + var + ");\n" +
                                       ind + "}\n";
                        printTrace("[+] Gerando C agora: gets(str) para variavel " + var);
                        return new CommandResult(lps1, cCode);
                    }
                    case ADD:
                    case SUB:
                    case MULT:
                    case DIV:
                    case MOD: {
                        printTrace("Regra: BinOpCommand (" + currentToken.value + ")");
                        String opToken = currentToken.value;
                        printTrace("Match: " + currentToken.type.name() + " ('" + opToken + "')");
                        advance();
                        String var = currentToken.value;
                        match(Token.Type.VARIABLE, "Variável esperada");
                        String val1 = parseValue();
                        String val2 = parseValue();
                        String lps1 = opToken + " " + var + " " + val1 + " " + val2;
                        String cCode = ind + "// " + lps1 + "\n" +
                                       ind + var + " = " + val1 + " " + opToken + " " + val2 + ";\n";
                        printTrace("[+] Gerando C agora: " + var + " = " + val1 + opToken + val2 + ";");
                        return new CommandResult(lps1, cCode);
                    }
                    case PRINT: {
                        printTrace("Regra: PrintCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        String val = parseValue();
                        String lps1 = "P " + val;
                        String cCode = ind + "// " + lps1 + "\n" +
                                       ind + "printf(\"%d\\n\", " + val + ");\n";
                        printTrace("[+] Gerando C agora: printf(...)");
                        return new CommandResult(lps1, cCode);
                    }
                    case IF: {
                        printTrace("Regra: IfCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        ComparisonResult comp = parseComparison();
                        if (currentToken.type == Token.Type.LBRACE) {
                            printTrace("Match: " + currentToken.type.name() + " ('{')");
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
                            printTrace("[+] Gerando C agora: if com bloco");
                            return new CommandResult(lps1, cCode);
                        } else {
                            CommandResult res = parseCommand(indent + 1);
                            String lps1 = "I " + comp.lps1 + " " + res.lps1;
                            String cCode = ind + "// " + lps1 + "\n" +
                                           ind + "if ( " + comp.cCode + " ) {\n" +
                                           res.cCode +
                                           ind + "}\n";
                            printTrace("[+] Gerando C agora: if simples");
                            return new CommandResult(lps1, cCode);
                        }
                    }
                    case WHILE: {
                        printTrace("Regra: WhileCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        ComparisonResult comp = parseComparison();
                        if (currentToken.type == Token.Type.LBRACE) {
                            printTrace("Match: " + currentToken.type.name() + " ('{')");
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
                            printTrace("[+] Gerando C agora: while com bloco");
                            return new CommandResult(lps1, cCode);
                        } else {
                            CommandResult res = parseCommand(indent + 1);
                            String lps1 = "W " + comp.lps1 + " " + res.lps1;
                            String cCode = ind + "// " + lps1 + "\n" +
                                           ind + "while ( " + comp.cCode + " ) {\n" +
                                           res.cCode +
                                           ind + "}\n";
                            printTrace("[+] Gerando C agora: while simples");
                            return new CommandResult(lps1, cCode);
                        }
                    }
                    case LBRACE: {
                        printTrace("Regra: CompositeCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('{')");
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
                        printTrace("[+] Gerando C agora: bloco composto { ... }");
                        return new CommandResult(lps1, cCode);
                    }
                    default:
                        error("Comando inexistente");
                        return null;
                }
            } finally {
                traceIndent--;
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
            printTrace("-> Analisando Comparison");
            traceIndent++;
            try {
                String var = currentToken.value;
                match(Token.Type.VARIABLE, "Variável esperada");

                String op = currentToken.value;
                String cOp;
                if (currentToken.type == Token.Type.ASSIGN) {
                    cOp = "==";
                    printTrace("Match: Operador =");
                    advance();
                } else if (currentToken.type == Token.Type.LT) {
                    cOp = "<";
                    printTrace("Match: Operador <");
                    advance();
                } else if (currentToken.type == Token.Type.NEQ) {
                    cOp = "!=";
                    printTrace("Match: Operador #");
                    advance();
                } else {
                    error("Operador esperado");
                    return null;
                }

                String val = parseValue();
                return new ComparisonResult(var + " " + op + " " + val, var + " " + cOp + " " + val);
            } finally {
                traceIndent--;
            }
        }

        private String parseValue() {
            printTrace("-> Analisando Value");
            traceIndent++;
            try {
                if (currentToken.type == Token.Type.VARIABLE || currentToken.type == Token.Type.NUMBER) {
                    String val = currentToken.value;
                    printTrace("Match: " + currentToken.type.name() + " ('" + val + "')");
                    advance();
                    return val;
                } else {
                    error("Valor esperado");
                    return null;
                }
            } finally {
                traceIndent--;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java CompilerA <arquivo.lps1>");
            System.exit(1);
        }
        try {
            Path inputPath = Path.of(args[0]);
            String content = Files.readString(inputPath);
            String fileName = inputPath.getFileName().toString();
            if (fileName.contains(".")) {
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            }
            
            Lexer lexer = new Lexer(content);
            Parser parser = null;
            String cCode = null;
            try {
                parser = new Parser(lexer);
                cCode = parser.parseProgram();
            } catch (RuntimeException re) {
                System.err.println(re.getMessage());
                if (parser != null) {
                    Path logDir = Path.of("log");
                    if (!Files.exists(logDir)) Files.createDirectories(logDir);
                    Path logPath = logDir.resolve(fileName + "_A.log");
                    String fullLog = parser.getTraceLog() + "\n[!] ERRO SINTATICO/LEXICO: " + re.getMessage() + "\n--- Compilacao Abortada ---\n";
                    Files.writeString(logPath, fullLog);
                    System.out.println("Arquivo de log (com rastro do erro) salvo em: " + logPath.toString());
                }
                System.exit(1);
            }

            System.out.println(cCode);
            
            Path saidaDir = Path.of("saida");
            if (!Files.exists(saidaDir)) {
                Files.createDirectories(saidaDir);
            }
            Path outputPath = saidaDir.resolve(fileName + "_A.c");
            Files.writeString(outputPath, cCode);
            System.out.println("Codigo C gerado foi salvo em: " + outputPath.toString());
            
            Path logDir = Path.of("log");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            Path logPath = logDir.resolve(fileName + "_A.log");
            String fullLog = parser.getTraceLog() + "\n--- Codigo C Gerado ---\n" + cCode;
            Files.writeString(logPath, fullLog);
            System.out.println("Arquivo de log salvo em: " + logPath.toString());
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo ou escrever a saida: " + e.getMessage());
            System.exit(1);
        }
    }
}
