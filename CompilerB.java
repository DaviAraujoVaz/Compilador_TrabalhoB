// (b) O analisador sintático mais a construção da Árvore de Sintaxe Abstrata (ASA). Geração de código em C em métodos da ASA.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CompilerB {

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

    // --- AST (ASA) Nodes ---

    public interface ASTNode {
        String toCCode(int indent);
        String toLPS1();
        void printTree(int depth, StringBuilder logger);
    }

    public static void logTreeLine(StringBuilder logger, int depth, String msg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) sb.append("  ");
        sb.append(msg);
        String line = sb.toString();
        System.out.println(line);
        if (logger != null) logger.append(line).append("\n");
    }

    public interface CommandNode extends ASTNode {}

    public static class ProgramNode implements ASTNode {
        public final List<CommandNode> commands;
        public ProgramNode(List<CommandNode> commands) {
            this.commands = commands;
        }

        @Override
        public String toCCode(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append("#include <stdio.h>\n\n");
            sb.append("int main() {\n");
            sb.append("  int a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;\n");
            sb.append("  char str[512]; // auxiliar na leitura com G\n");

            for (CommandNode cmd : commands) {
                sb.append(cmd.toCCode(indent));
            }

            sb.append("  gets(str);\n");
            sb.append("}\n");
            return sb.toString();
        }

        @Override
        public String toLPS1() {
            StringBuilder sb = new StringBuilder();
            for (CommandNode cmd : commands) {
                sb.append(cmd.toLPS1()).append("\n");
            }
            return sb.toString();
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "[ProgramNode]");
            for (CommandNode cmd : commands) {
                cmd.printTree(depth + 1, logger);
            }
        }
    }

    public interface ValueNode extends ASTNode {}

    public static class VariableNode implements ValueNode {
        public final char name;
        public VariableNode(char name) {
            this.name = name;
        }

        @Override
        public String toCCode(int indent) {
            return String.valueOf(name);
        }

        @Override
        public String toLPS1() {
            return String.valueOf(name);
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "Variavel: " + name);
        }
    }

    public static class NumberNode implements ValueNode {
        public final int value;
        public NumberNode(int value) {
            this.value = value;
        }

        @Override
        public String toCCode(int indent) {
            return String.valueOf(value);
        }

        @Override
        public String toLPS1() {
            return String.valueOf(value);
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "Numero: " + value);
        }
    }

    public static class ComparisonNode implements ASTNode {
        public final char variable;
        public final String operator; // "=", "<", "#"
        public final ValueNode value;

        public ComparisonNode(char variable, String operator, ValueNode value) {
            this.variable = variable;
            this.operator = operator;
            this.value = value;
        }

        @Override
        public String toCCode(int indent) {
            String cOp;
            switch (operator) {
                case "=": cOp = "=="; break;
                case "<": cOp = "<"; break;
                case "#": cOp = "!="; break;
                default: throw new IllegalStateException("Operador inválido: " + operator);
            }
            return variable + " " + cOp + " " + value.toCCode(0);
        }

        @Override
        public String toLPS1() {
            return variable + " " + operator + " " + value.toLPS1();
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "[Comparacao ('" + operator + "')]");
            logTreeLine(logger, depth + 1, "Esq: " + variable);
            value.printTree(depth + 1, logger);
        }
    }

    public static class AssignCommandNode implements CommandNode {
        public final char variable;
        public final ValueNode value;

        public AssignCommandNode(char variable, ValueNode value) {
            this.variable = variable;
            this.value = value;
        }

        @Override
        public String toCCode(int indent) {
            String ind = getIndent(indent);
            return ind + "// " + toLPS1() + "\n" +
                   ind + variable + " = " + value.toCCode(0) + ";\n";
        }

        @Override
        public String toLPS1() {
            return "= " + variable + " " + value.toLPS1();
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "[Atribuicao (=)]");
            logTreeLine(logger, depth + 1, "Var: " + variable);
            value.printTree(depth + 1, logger);
        }
    }

    public static class GetCommandNode implements CommandNode {
        public final char variable;

        public GetCommandNode(char variable) {
            this.variable = variable;
        }

        @Override
        public String toCCode(int indent) {
            String ind = getIndent(indent);
            return ind + "// " + toLPS1() + "\n" +
                   ind + "{ gets(str);\n" +
                   ind + "  sscanf(str, \"%d\", &" + variable + ");\n" +
                   ind + "}\n";
        }

        @Override
        public String toLPS1() {
            return "G " + variable;
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "[Leitura (G)] -> Var: " + variable);
        }
    }

    public static class BinaryOpCommandNode implements CommandNode {
        public final String op; // "+", "-", "*", "/", "%"
        public final char variable;
        public final ValueNode val1;
        public final ValueNode val2;

        public BinaryOpCommandNode(String op, char variable, ValueNode val1, ValueNode val2) {
            this.op = op;
            this.variable = variable;
            this.val1 = val1;
            this.val2 = val2;
        }

        @Override
        public String toCCode(int indent) {
            String ind = getIndent(indent);
            return ind + "// " + toLPS1() + "\n" +
                   ind + variable + " = " + val1.toCCode(0) + " " + op + " " + val2.toCCode(0) + ";\n";
        }

        @Override
        public String toLPS1() {
            return op + " " + variable + " " + val1.toLPS1() + " " + val2.toLPS1();
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "[Operacao Binaria ('" + op + "')]");
            logTreeLine(logger, depth + 1, "Dest: " + variable);
            val1.printTree(depth + 1, logger);
            val2.printTree(depth + 1, logger);
        }
    }

    public static class PrintCommandNode implements CommandNode {
        public final ValueNode value;

        public PrintCommandNode(ValueNode value) {
            this.value = value;
        }

        @Override
        public String toCCode(int indent) {
            String ind = getIndent(indent);
            return ind + "// " + toLPS1() + "\n" +
                   ind + "printf(\"%d\\n\", " + value.toCCode(0) + ");\n";
        }

        @Override
        public String toLPS1() {
            return "P " + value.toLPS1();
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "[Impressao (P)]");
            value.printTree(depth + 1, logger);
        }
    }

    public static class IfCommandNode implements CommandNode {
        public final ComparisonNode comparison;
        public final CommandNode command;

        public IfCommandNode(ComparisonNode comparison, CommandNode command) {
            this.comparison = comparison;
            this.command = command;
        }

        @Override
        public String toCCode(int indent) {
            String ind = getIndent(indent);
            StringBuilder sb = new StringBuilder();
            if (command instanceof CompositeCommandNode) {
                CompositeCommandNode comp = (CompositeCommandNode) command;
                sb.append(ind).append("// I ").append(comparison.toLPS1()).append(" {\n");
                sb.append(ind).append("if ( ").append(comparison.toCCode(0)).append(" ) {\n");
                for (CommandNode cmd : comp.commands) {
                    sb.append(cmd.toCCode(indent + 1));
                }
                sb.append(ind).append("}\n");
            } else {
                sb.append(ind).append("// I ").append(comparison.toLPS1()).append(" ").append(command.toLPS1()).append("\n");
                sb.append(ind).append("if ( ").append(comparison.toCCode(0)).append(" ) {\n");
                sb.append(command.toCCode(indent + 1));
                sb.append(ind).append("}\n");
            }
            return sb.toString();
        }

        @Override
        public String toLPS1() {
            if (command instanceof CompositeCommandNode) {
                return "I " + comparison.toLPS1() + " {";
            } else {
                return "I " + comparison.toLPS1() + " " + command.toLPS1();
            }
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "[If]");
            comparison.printTree(depth + 1, logger);
            command.printTree(depth + 1, logger);
        }
    }

    public static class WhileCommandNode implements CommandNode {
        public final ComparisonNode comparison;
        public final CommandNode command;

        public WhileCommandNode(ComparisonNode comparison, CommandNode command) {
            this.comparison = comparison;
            this.command = command;
        }

        @Override
        public String toCCode(int indent) {
            String ind = getIndent(indent);
            StringBuilder sb = new StringBuilder();
            if (command instanceof CompositeCommandNode) {
                CompositeCommandNode comp = (CompositeCommandNode) command;
                sb.append(ind).append("// W ").append(comparison.toLPS1()).append(" {\n");
                sb.append(ind).append("while ( ").append(comparison.toCCode(0)).append(" ) {\n");
                for (CommandNode cmd : comp.commands) {
                    sb.append(cmd.toCCode(indent + 1));
                }
                sb.append(ind).append("}\n");
            } else {
                sb.append(ind).append("// W ").append(comparison.toLPS1()).append(" ").append(command.toLPS1()).append("\n");
                sb.append(ind).append("while ( ").append(comparison.toCCode(0)).append(" ) {\n");
                sb.append(command.toCCode(indent + 1));
                sb.append(ind).append("}\n");
            }
            return sb.toString();
        }

        @Override
        public String toLPS1() {
            if (command instanceof CompositeCommandNode) {
                return "W " + comparison.toLPS1() + " {";
            } else {
                return "W " + comparison.toLPS1() + " " + command.toLPS1();
            }
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "[While]");
            comparison.printTree(depth + 1, logger);
            command.printTree(depth + 1, logger);
        }
    }

    public static class CompositeCommandNode implements CommandNode {
        public final List<CommandNode> commands;

        public CompositeCommandNode(List<CommandNode> commands) {
            this.commands = commands;
        }

        @Override
        public String toCCode(int indent) {
            String ind = getIndent(indent);
            StringBuilder sb = new StringBuilder();
            sb.append(ind).append("{\n");
            for (CommandNode cmd : commands) {
                sb.append(cmd.toCCode(indent + 1));
            }
            sb.append(ind).append("}\n");
            return sb.toString();
        }

        @Override
        public String toLPS1() {
            return "{";
        }

        @Override
        public void printTree(int depth, StringBuilder logger) {
            logTreeLine(logger, depth, "[Bloco {}]");
            for (CommandNode cmd : commands) {
                cmd.printTree(depth + 1, logger);
            }
        }
    }

    private static String getIndent(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    // --- Parser class that builds the AST ---

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

        public ProgramNode parseProgram() {
            logHeader("--- FASE 1: Iniciando Analise Sintatica e Construcao da ASA (Compilador B) ---");
            printTrace("-> Analisando Program");
            traceIndent++;
            
            List<CommandNode> commands = new ArrayList<>();
            while (currentToken.type != Token.Type.EOF) {
                commands.add(parseCommand());
            }
            
            traceIndent--;
            logHeader("--- FASE 1 CONCLUIDA: Arvore de Sintaxe Abstrata (ASA) montada na memoria com sucesso ---\n");
            return new ProgramNode(commands);
        }

        private CommandNode parseCommand() {
            printTrace("-> Analisando Command (Para instanciar um Nó na ASA)");
            traceIndent++;
            try {
                switch (currentToken.type) {
                    case ASSIGN: {
                        printTrace("Regra: AssignCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        char var = currentToken.value.charAt(0);
                        match(Token.Type.VARIABLE, "Variável esperada");
                        ValueNode val = parseValue();
                        printTrace("[+] Instanciando Nó na ASA: AssignCommandNode");
                        return new AssignCommandNode(var, val);
                    }
                    case GET: {
                        printTrace("Regra: GetCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        char var = currentToken.value.charAt(0);
                        match(Token.Type.VARIABLE, "Variável esperada");
                        printTrace("[+] Instanciando Nó na ASA: GetCommandNode");
                        return new GetCommandNode(var);
                    }
                    case ADD:
                    case SUB:
                    case MULT:
                    case DIV:
                    case MOD: {
                        printTrace("Regra: BinOpCommand (" + currentToken.value + ")");
                        String op = currentToken.value;
                        printTrace("Match: " + currentToken.type.name() + " ('" + op + "')");
                        advance();
                        char var = currentToken.value.charAt(0);
                        match(Token.Type.VARIABLE, "Variável esperada");
                        ValueNode val1 = parseValue();
                        ValueNode val2 = parseValue();
                        printTrace("[+] Instanciando Nó na ASA: BinaryOpCommandNode");
                        return new BinaryOpCommandNode(op, var, val1, val2);
                    }
                    case PRINT: {
                        printTrace("Regra: PrintCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        ValueNode val = parseValue();
                        printTrace("[+] Instanciando Nó na ASA: PrintCommandNode");
                        return new PrintCommandNode(val);
                    }
                    case IF: {
                        printTrace("Regra: IfCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        ComparisonNode comp = parseComparison();
                        if (currentToken.type == Token.Type.LBRACE) {
                            printTrace("Match: " + currentToken.type.name() + " ('{')");
                            advance();
                            List<CommandNode> inner = new ArrayList<>();
                            while (currentToken.type != Token.Type.RBRACE && currentToken.type != Token.Type.EOF) {
                                inner.add(parseCommand());
                            }
                            match(Token.Type.RBRACE, "Fecha chave ('}') esperado");
                            printTrace("[+] Instanciando Nó na ASA: IfCommandNode com Bloco");
                            return new IfCommandNode(comp, new CompositeCommandNode(inner));
                        } else {
                            CommandNode cmd = parseCommand();
                            printTrace("[+] Instanciando Nó na ASA: IfCommandNode Simples");
                            return new IfCommandNode(comp, cmd);
                        }
                    }
                    case WHILE: {
                        printTrace("Regra: WhileCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('" + currentToken.value + "')");
                        advance();
                        ComparisonNode comp = parseComparison();
                        if (currentToken.type == Token.Type.LBRACE) {
                            printTrace("Match: " + currentToken.type.name() + " ('{')");
                            advance();
                            List<CommandNode> inner = new ArrayList<>();
                            while (currentToken.type != Token.Type.RBRACE && currentToken.type != Token.Type.EOF) {
                                inner.add(parseCommand());
                            }
                            match(Token.Type.RBRACE, "Fecha chave ('}') esperado");
                            printTrace("[+] Instanciando Nó na ASA: WhileCommandNode com Bloco");
                            return new WhileCommandNode(comp, new CompositeCommandNode(inner));
                        } else {
                            CommandNode cmd = parseCommand();
                            printTrace("[+] Instanciando Nó na ASA: WhileCommandNode Simples");
                            return new WhileCommandNode(comp, cmd);
                        }
                    }
                    case LBRACE: {
                        printTrace("Regra: CompositeCommand");
                        printTrace("Match: " + currentToken.type.name() + " ('{')");
                        advance();
                        List<CommandNode> inner = new ArrayList<>();
                        while (currentToken.type != Token.Type.RBRACE && currentToken.type != Token.Type.EOF) {
                            inner.add(parseCommand());
                        }
                        match(Token.Type.RBRACE, "Fecha chave ('}') esperado");
                        printTrace("[+] Instanciando Nó na ASA: CompositeCommandNode");
                        return new CompositeCommandNode(inner);
                    }
                    default:
                        error("Comando inexistente");
                        return null;
                }
            } finally {
                traceIndent--;
            }
        }

        private ComparisonNode parseComparison() {
            printTrace("-> Analisando Comparison");
            traceIndent++;
            try {
                char var = currentToken.value.charAt(0);
                match(Token.Type.VARIABLE, "Variável esperada");

                String op = currentToken.value;
                if (currentToken.type == Token.Type.ASSIGN || currentToken.type == Token.Type.LT || currentToken.type == Token.Type.NEQ) {
                    printTrace("Match: Operador " + op);
                    advance();
                } else {
                    error("Operador esperado");
                }

                ValueNode val = parseValue();
                return new ComparisonNode(var, op, val);
            } finally {
                traceIndent--;
            }
        }

        private ValueNode parseValue() {
            printTrace("-> Analisando Value");
            traceIndent++;
            try {
                if (currentToken.type == Token.Type.VARIABLE) {
                    char varName = currentToken.value.charAt(0);
                    printTrace("Match: " + currentToken.type.name() + " ('" + varName + "')");
                    advance();
                    return new VariableNode(varName);
                } else if (currentToken.type == Token.Type.NUMBER) {
                    int val = Integer.parseInt(currentToken.value);
                    printTrace("Match: " + currentToken.type.name() + " ('" + val + "')");
                    advance();
                    return new NumberNode(val);
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
            System.err.println("Uso: java CompilerB <arquivo.lps1>");
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
            ProgramNode program = null;
            try {
                parser = new Parser(lexer);
                program = parser.parseProgram();
            } catch (RuntimeException re) {
                System.err.println(re.getMessage());
                if (parser != null) {
                    Path logDir = Path.of("log");
                    if (!Files.exists(logDir)) Files.createDirectories(logDir);
                    Path logPath = logDir.resolve(fileName + "_B.log");
                    String fullLog = parser.getTraceLog() + "\n[!] ERRO SINTATICO/LEXICO: " + re.getMessage() + "\n--- Compilacao Abortada ---\n";
                    Files.writeString(logPath, fullLog);
                    System.out.println("Arquivo de log (com rastro do erro) salvo em: " + logPath.toString());
                }
                System.exit(1);
            }
            
            System.out.println("--- FASE 2: Percorrendo a ASA para Geracao de Codigo (toCCode) ---");
            StringBuilder astLogger = new StringBuilder();
            program.printTree(0, astLogger);
            
            System.out.println("\n--- Codigo C Gerado ---");
            String cCode = program.toCCode(1);
            System.out.println(cCode);
            
            Path saidaDir = Path.of("saida");
            if (!Files.exists(saidaDir)) {
                Files.createDirectories(saidaDir);
            }
            Path outputPath = saidaDir.resolve(fileName + "_B.c");
            Files.writeString(outputPath, cCode);
            System.out.println("Codigo C gerado foi salvo em: " + outputPath.toString());
            
            Path logDir = Path.of("log");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            Path logPath = logDir.resolve(fileName + "_B.log");
            String fullLog = parser.getTraceLog() + 
                             "\n--- FASE 2: Percorrendo a ASA para Geracao de Codigo (toCCode) ---\n" + 
                             astLogger.toString() +
                             "\n--- Codigo C Gerado ---\n" + cCode;
            Files.writeString(logPath, fullLog);
            System.out.println("Arquivo de log salvo em: " + logPath.toString());
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo ou escrever a saida: " + e.getMessage());
            System.exit(1);
        }
    }
}
