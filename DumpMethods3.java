import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;

public class DumpMethods3 {
    public static void main(String[] args) throws Exception {
        File file = new File("classes.jar");
        URL url = file.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        Class<?> clazz = classLoader.loadClass("libv2ray.CoreCallbackHandler");
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.println(m);
        }
    }
}
