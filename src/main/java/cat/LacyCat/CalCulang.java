package cat.LacyCat;

import cat.LacyCat.Custom.Grammar;
import cat.LacyCat.Others.DynamicClassLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CalCulang {
    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }

        try {
            String code = Files.readString(Paths.get(args[0]));
            Interpreter interpreter = new Interpreter();
            interpreter.debug = Boolean.parseBoolean(args[1]);
            if (!args[3].isEmpty()) {
                List<Class<?>> loadClasses = DynamicClassLoader.loadImplementations(args[3], Grammar.class);
                if (!loadClasses.isEmpty()) {
                    for (Class<?> implClass : loadClasses) {
                        try {
                            // 클래스의 인스턴스 생성
                            Object instance = implClass.getDeclaredConstructor().newInstance();

                            // 인터페이스로 캐스팅
                            if (instance instanceof Grammar) {
                                interpreter.addGrammar((Grammar) instance);
                                interpreter.runWithCustomGrammar(code);
                            } else {
                                System.out.println("Class does not implement Grammar: " + implClass.getName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            interpreter.run(code);
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error during interpretation: " + e.getMessage());
        }
    }
}