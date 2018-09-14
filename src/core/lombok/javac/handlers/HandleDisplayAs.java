package lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree;
import lombok.JhxUtil;
import lombok.AllAs;
import lombok.DisplayAs;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import org.mangosdk.spi.ProviderFor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.List;

/**
 * @author 钱智慧
 * date 9/12/18 4:07 PM
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleDisplayAs extends JavacAnnotationHandler<DisplayAs> {
    @Override
    public void handle(AnnotationValues<DisplayAs> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
        Element target = JhxUtil.getTargetType(annotation);
        JavacNode typeNode = annotationNode.up();
        try {
            for (AnnotationMirror mirror : JhxUtil.getProcEnv().getElementUtils().getAllAnnotationMirrors(typeNode.getElement())) {
                if(mirror.getAnnotationType().toString().equals(AllAs.class.getName())){
                    JhxUtil.err("SameAs和SameDisplayAs不能同时使用#"+typeNode.getName(),typeNode.getElement());
                    return;
                }
            }
            for (JavacNode child : typeNode.down()) {
                if (child.getKind() == Kind.FIELD) {
                    List<? extends AnnotationMirror> annotations = JhxUtil.annotationsOfField(target.toString(), child.getElement().toString());
                    JhxUtil.addAnnotations(typeNode, child, annotations, false);
                }
            }
        } catch (Throwable e) {
            JhxUtil.err(e,typeNode.getElement());
            for (StackTraceElement item : e.getStackTrace()) {
                JhxUtil.err(item.getFileName() + "#" + item.getLineNumber() + "#" + item.getClassName());
            }
        }

    }

    private void printElement(Element element) {
        JhxUtil.warn(element.toString() + "#" + element.getKind() + "#" + element.getSimpleName() + "#" + element.getClass());
    }
}
