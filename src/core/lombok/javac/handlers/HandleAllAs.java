package lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree;
import lombok.core.*;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.JhxUtil;
import lombok.AllAs;
import org.mangosdk.spi.ProviderFor;
import lombok.core.AST.Kind;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.List;

/**
 * @author 钱智慧
 * date 9/12/18 4:07 PM
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleAllAs extends JavacAnnotationHandler<AllAs>{
    @Override
    public void handle(AnnotationValues<AllAs> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
        Element target= JhxUtil.getTargetType(annotation);
        JavacNode typeNode = annotationNode.up();
        try{
            for (JavacNode child : typeNode.down()) {
                if(child.getKind()== Kind.FIELD){
                    List<? extends AnnotationMirror> annotations = JhxUtil.annotationsOfField(target.toString(), child.getElement().toString());
                    JhxUtil.addAnnotations(typeNode,child,annotations,true);
                }
            }
        }catch (Throwable e){
            JhxUtil.err(typeNode, e);
        }

    }
}
