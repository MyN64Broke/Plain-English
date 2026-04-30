package PlainEnglish;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PE_Main {

    public static void main(String args[]) {
        try ( var fileList = Files.find(Path.of("."), 2, (p, a) ->
                p.getFileName().toString().endsWith("List.eng"))) {
            var list = fileList.toList();
            for (var f : list) {
                System.out.println("#########################\nFile: " + f);
                var l = new Lexer(Files.readString(f));
                var tokens = l.lex();

                var p = new PlainEnglishParser(tokens);
                var prog = p.Program();

                var inter = new Interpreter(prog.orElseThrow());
                inter.Start();
            }
        } catch (IOException | SyntaxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
