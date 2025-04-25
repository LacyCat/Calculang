package cat.LacyCat.Others;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class DynamicClassLoader {

    public static List<Class<?>> loadImplementations(String directoryPath, Class<?> targetInterface) throws Exception {
        List<Class<?>> implementations = new ArrayList<>();

        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Provided path is not a directory.");
        }

        // 디렉토리에 있는 모든 파일 탐색
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = file.getName().replace(".class", "");
                URL classUrl = directory.toURI().toURL();

                // 클래스 로드
                try (URLClassLoader classLoader = new URLClassLoader(new URL[]{classUrl})) {
                    Class<?> loadedClass = classLoader.loadClass(className);

                    // 인터페이스 구현 여부 확인
                    if (targetInterface.isAssignableFrom(loadedClass) && !loadedClass.isInterface()) {
                        implementations.add(loadedClass);
                    }
                }
            }
        }
        return implementations;
    }
}