import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;

public class DumpMethods {
    public static void main(String[] args) throws Exception {
        File file = new File("classes.jar");
        URL url = file.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        Class<?> clazz = classLoader.loadClass("libv2ray.Libv2ray");
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.println(m);
        }
    }
}
