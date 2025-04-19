package cat.LacyCat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CalCulang {
    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }

        try {
            String code = Files.readString(Paths.get(args[0]));
            Interpreter interpreter = new Interpreter();
            interpreter.debug = Boolean.parseBoolean(args[1]);
            interpreter.run(code);
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error during interpretation: " + e.getMessage());
        }
    }
}