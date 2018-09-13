package lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree;
import lombok.core.*;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.JhxUtil;
import lombok.SameAs;
import org.mangosdk.spi.ProviderFor;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import lombok.core.AST.Kind;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.List;

/**
 * @author 钱智慧
 * date 9/12/18 4:07 PM
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleSameAs extends JavacAnnotationHandler<SameAs>{
    @Override
    public void handle(AnnotationValues<SameAs> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
        try{
            String targetClsName= JhxUtil.getTargetClassName(annotation);
            JavacNode typeNode = annotationNode.up();
            for (JavacNode child : typeNode.down()) {
                if(child.getKind()== Kind.FIELD){
                    List<? extends AnnotationMirror> annotations = JhxUtil.annotationsOfField(targetClsName, child.getElement().toString());
                    JhxUtil.addAnnotations(typeNode,child,annotations);
                }
            }
        }catch (Throwable e){
            JhxUtil.err("发生了异常："+e);
            for (StackTraceElement item : e.getStackTrace()) {
                JhxUtil.err(item.getFileName()+"#"+item.getLineNumber()+"#"+item.getClassName());
            }
        }

    }

    private void printElement(Element element){
        JhxUtil.warn(element.toString()+"#"+element.getKind()+"#"+element.getSimpleName()+"#"+element.getClass());
    }
}
