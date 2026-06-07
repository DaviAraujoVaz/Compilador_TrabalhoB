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

    // --- AST (ASA) Nodes ---

    public interface ASTNode {
        String toCCode(int indent);
        String toLPS1();
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
            sb.append("  a = b = c = d = e = f = g = h = i = j = k = l = m = n = o = p = q = r = s = t = u = v = w = x = y = z = 0;\n");
            sb.append("  char str[512]; // auxiliar na leitura com G\n");

            for (CommandNode cmd : commands) {
                sb.append(cmd.toCCode(indent));
            }

            sb.append("  return 0;\n");
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
                   ind + "{\n" +
                   ind + "  if (fgets(str, sizeof(str), stdin) != NULL) {\n" +
                   ind + "    sscanf(str, \"%d\", &" + variable + ");\n" +
                   ind + "  }\n" +
                   ind + "}\n";
        }

        @Override
        public String toLPS1() {
            return "G " + variable;
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

        public ProgramNode parseProgram() {
            List<CommandNode> commands = new ArrayList<>();
            while (currentToken.type != Token.Type.EOF) {
                commands.add(parseCommand());
            }
            return new ProgramNode(commands);
        }

        private CommandNode parseCommand() {
            switch (currentToken.type) {
                case ASSIGN: {
                    advance();
                    char var = currentToken.value.charAt(0);
                    match(Token.Type.VARIABLE, "Variável esperada");
                    ValueNode val = parseValue();
                    return new AssignCommandNode(var, val);
                }
                case GET: {
                    advance();
                    char var = currentToken.value.charAt(0);
                    match(Token.Type.VARIABLE, "Variável esperada");
                    return new GetCommandNode(var);
                }
                case ADD:
                case SUB:
                case MULT:
                case DIV:
                case MOD: {
                    String op = currentToken.value;
                    advance();
                    char var = currentToken.value.charAt(0);
                    match(Token.Type.VARIABLE, "Variável esperada");
                    ValueNode val1 = parseValue();
                    ValueNode val2 = parseValue();
                    return new BinaryOpCommandNode(op, var, val1, val2);
                }
                case PRINT: {
                    advance();
                    ValueNode val = parseValue();
                    return new PrintCommandNode(val);
                }
                case IF: {
                    advance();
                    ComparisonNode comp = parseComparison();
                    if (currentToken.type == Token.Type.LBRACE) {
                        advance();
                        List<CommandNode> inner = new ArrayList<>();
                        while (currentToken.type != Token.Type.RBRACE && currentToken.type != Token.Type.EOF) {
                            inner.add(parseCommand());
                        }
                        match(Token.Type.RBRACE, "Fecha chave ('}') esperado");
                        return new IfCommandNode(comp, new CompositeCommandNode(inner));
                    } else {
                        CommandNode cmd = parseCommand();
                        return new IfCommandNode(comp, cmd);
                    }
                }
                case WHILE: {
                    advance();
                    ComparisonNode comp = parseComparison();
                    if (currentToken.type == Token.Type.LBRACE) {
                        advance();
                        List<CommandNode> inner = new ArrayList<>();
                        while (currentToken.type != Token.Type.RBRACE && currentToken.type != Token.Type.EOF) {
                            inner.add(parseCommand());
                        }
                        match(Token.Type.RBRACE, "Fecha chave ('}') esperado");
                        return new WhileCommandNode(comp, new CompositeCommandNode(inner));
                    } else {
                        CommandNode cmd = parseCommand();
                        return new WhileCommandNode(comp, cmd);
                    }
                }
                case LBRACE: {
                    advance();
                    List<CommandNode> inner = new ArrayList<>();
                    while (currentToken.type != Token.Type.RBRACE && currentToken.type != Token.Type.EOF) {
                        inner.add(parseCommand());
                    }
                    match(Token.Type.RBRACE, "Fecha chave ('}') esperado");
                    return new CompositeCommandNode(inner);
                }
                default:
                    error("Comando inexistente");
                    return null;
            }
        }

        private ComparisonNode parseComparison() {
            char var = currentToken.value.charAt(0);
            match(Token.Type.VARIABLE, "Variável esperada");

            String op = currentToken.value;
            if (currentToken.type == Token.Type.ASSIGN || currentToken.type == Token.Type.LT || currentToken.type == Token.Type.NEQ) {
                advance();
            } else {
                error("Operador esperado");
            }

            ValueNode val = parseValue();
            return new ComparisonNode(var, op, val);
        }

        private ValueNode parseValue() {
            if (currentToken.type == Token.Type.VARIABLE) {
                char varName = currentToken.value.charAt(0);
                advance();
                return new VariableNode(varName);
            } else if (currentToken.type == Token.Type.NUMBER) {
                int val = Integer.parseInt(currentToken.value);
                advance();
                return new NumberNode(val);
            } else {
                error("Valor esperado");
                return null;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java CompilerB <arquivo.lps1>");
            System.exit(1);
        }
        try {
            String content = Files.readString(Path.of(args[0]));
            Lexer lexer = new Lexer(content);
            Parser parser = new Parser(lexer);
            ProgramNode program = parser.parseProgram();
            String cCode = program.toCCode(1);
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
