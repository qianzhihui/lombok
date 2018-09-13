package lombok;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import lombok.core.AnnotationValues;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import org.netbeans.modules.java.JavaNode;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.swing.*;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;

public class JhxUtil {
    //key是类型+属性名，value是注解集合
    private static Map<String, Element> elementMap = new HashMap<String, Element>();
    private static Map<String, List<? extends AnnotationMirror>> annoMap = new HashMap<String, List<? extends AnnotationMirror>>();
    private static ProcessingEnvironment procEnv;

    public static void setProcEnv(ProcessingEnvironment procEnv) {
        JhxUtil.procEnv = procEnv;
    }

    public static List<? extends AnnotationMirror> annotationsOfField(Element cls, Element field) {
        String key = key(cls, field);
        if (!annoMap.containsKey(key)) {
            for (Element item : elementMap.get(cls.toString()).getEnclosedElements()) {
                if (item.getKind() == ElementKind.FIELD) {
                    annoMap.put(key(cls, item), item.getAnnotationMirrors());
                }
            }
        }
        return annoMap.get(key);
    }

    public static void addAnnotations(JavacNode typeNode, JavacNode child, List<? extends AnnotationMirror> annotations) {
        JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) child.get();
        JCTree.JCModifiers mods = field.mods;
        JavacTreeMaker maker = child.getTreeMaker();
        int pos = field.pos;
        JCTree source = JavacHandlerUtil.getGeneratedBy(field);
        Context context = typeNode.getContext();

        for (AnnotationMirror item : annotations) {

            com.sun.tools.javac.util.List<JCTree.JCExpression> args=com.sun.tools.javac.util.List.nil();
            for (ExecutableElement key : item.getElementValues().keySet()) {

                AnnotationValue value = item.getElementValues().get(key);
                JhxUtil.warn(key.getSimpleName()+"#"+value.getValue()+"#"+value.getClass());
                JCTree.JCExpression arg = maker.Assign(maker.Ident(child.toName(key.getSimpleName().toString())), maker.Literal(value));
                args.add(arg);
            }
            String simpleName = item.getAnnotationType().toString();

//            annType.pos = pos;
//            if (arg != null) {
//                arg.pos = pos;
//                if (arg instanceof JCTree.JCAssign) {
//                    ((JCTree.JCAssign) arg).lhs.pos = pos;
//                    ((JCTree.JCAssign) arg).rhs.pos = pos;
//                }
//            }
            //JCTree.JCAnnotation annotation = maker.Annotation((JCTree) item.getAnnotationType().asElement(), args);
//            annotation.pos = pos;
            //mods.annotations = mods.annotations.append(annotation);
        }
    }

    public static <T extends Annotation> String getTargetClassName(AnnotationValues<T> annotation) {
        Object instance = annotation.getActualExpression("value");
        JCTree.JCFieldAccess tree = (JCTree.JCFieldAccess) instance;
        String targetClsName = tree.selected.type.toString();
        return targetClsName;
    }

    public static List<? extends AnnotationMirror> annotationsOfField(String clsName, String fieldName) {
        String key = clsName + "#" + fieldName;
        if (!annoMap.containsKey(key)) {
            for (Element item : elementMap.get(clsName).getEnclosedElements()) {
                if (item.getKind() == ElementKind.FIELD) {
                    annoMap.put(key(item.getEnclosingElement(), item), item.getAnnotationMirrors());
                }
            }
        }
        return annoMap.get(key);
    }

    private static String key(Element cls, Element field) {
        return cls + "#" + field;
    }

    public static void gatherElements(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Getter.class)) {
            elementMap.put(element.toString(), element);
        }
    }


    public static void warn(Object message) {
        procEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message.toString());
    }

    public static void err(Object message) {
        procEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message.toString());
    }
}
