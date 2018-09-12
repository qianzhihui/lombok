package lombok.core;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

public class JhxUtil {
    public static ProcessingEnvironment pe;
    public static void warn(String message){
        pe.getMessager().printMessage(Diagnostic.Kind.WARNING,message);
    }
}
