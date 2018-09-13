package lombok;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JhxUtil {
    private static Map<String,Element> elementMap = new HashMap<String,Element>();
    private static ProcessingEnvironment procEnv;

    public static void setProcEnv(ProcessingEnvironment procEnv) {
        JhxUtil.procEnv = procEnv;
    }

    public static Map<String,Element> elementMap(){
        return elementMap;
    }

    public static void gatherElements(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Getter.class)) {
            for (Element item :element.getEnclosedElements()){
                if(item.getKind()== ElementKind.FIELD){
                    elementMap.put(element.toString(),element);
                }
            }
        }
    }


    public static void warn(Object message) {
        procEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message.toString());
    }
}
