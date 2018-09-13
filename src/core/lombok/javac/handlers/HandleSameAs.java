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

/**
 * @author 钱智慧
 * date 9/12/18 4:07 PM
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleSameAs extends JavacAnnotationHandler<SameAs>{
    @Override
    public void handle(AnnotationValues<SameAs> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
        try{
            for (Element element : JhxUtil.elementMap()) {
                printElement(element);
                for (Element item : element.getEnclosedElements()) {
                    if(item.getKind()== ElementKind.FIELD){
                        printElement(item);
                        for (AnnotationMirror mirror : item.getAnnotationMirrors()) {
                           // JhxUtil.warn(mirror.getAnnotationType()+"#"+mirror.getElementValues());
                        }
                    }
                }
                break;
            }


            JavacNode typeNode = annotationNode.up();
            for (JavacNode child : typeNode.down()) {
                if(child.getKind()== Kind.FIELD){
                    JCVariableDecl field = (JCVariableDecl) child.get();
                    JavacTreeMaker maker = child.getTreeMaker();
                    JCExpression arg = maker.Assign(maker.Ident(child.toName("message")), maker.Literal("不能为空"));
                    JavacHandlerUtil.addAnnotation(field.mods,child,field.pos, JavacHandlerUtil.getGeneratedBy(field), typeNode.getContext(),
                            "javax.validation.constraints.NotNull",arg);
                }
            }
        }catch (Throwable e){
            JhxUtil.warn("发生了异常："+e.getMessage());
        }

//        annotationNode.addError("@Synchronized is legal only on methods.");
    }

    private void printElement(Element element){
        JhxUtil.warn(element.toString()+"#"+element.getKind()+"#"+element.getSimpleName()+"#"+element.getClass());
    }
}
