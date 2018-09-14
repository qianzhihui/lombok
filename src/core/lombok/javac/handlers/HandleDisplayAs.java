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
        JavacNode typeNode = annotationNode.up();
        try {
            for (AnnotationMirror mirror : JhxUtil.getProcEnv().getElementUtils().getAllAnnotationMirrors(typeNode.getElement())) {
                if (mirror.getAnnotationType().toString().equals(AllAs.class.getName())) {
                    JhxUtil.err("AllAs和DisplayAs不能同时使用#" + typeNode.getName(), typeNode.getElement());
                    return;
                }
            }
            for (JavacNode child : typeNode.down()) {
                if (child.getKind() == Kind.FIELD) {
                    String target = JhxUtil.getTarget(annotation);
                    List<? extends AnnotationMirror> annotations = JhxUtil.annotationsOfField(target, child.getElement().toString());
                    JhxUtil.addAnnotations(child, annotations, false);
                }
            }
        } catch (Throwable e) {
            JhxUtil.err(typeNode, e);
        }

    }
}
