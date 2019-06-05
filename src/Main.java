public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer("1.py");
        lexer.parse();
        lexer.showResult();
    }
}
