package lombok.javac.handlers;

import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.SameAs;
import lombok.javac.JavacTreeMaker;
import org.mangosdk.spi.ProviderFor;

import java.lang.reflect.Modifier;

/**
 * @author 钱智慧
 * date 9/12/18 4:07 PM
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleSameAs extends JavacAnnotationHandler<SameAs>{
    @Override
    public void handle(AnnotationValues<SameAs> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
//        annotationNode.addError("@Synchronized is legal only on methods.");
    }
}
