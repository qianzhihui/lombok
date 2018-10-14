package lombok;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import lombok.core.AnnotationValues;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;

public class JhxUtil {
    //由于编译的特殊性，这里使用Map提前缓存其实意义不大
    //key是类型+属性名，value是注解集合
    private static Map<String, Set<Element>> elementMap = new HashMap<String, Set<Element>>();
    private static Map<String, List<? extends AnnotationMirror>> annoMap = new HashMap<String, List<? extends AnnotationMirror>>();
    private static ProcessingEnvironment procEnv;

    public static void setProcEnv(ProcessingEnvironment procEnv) {
        JhxUtil.procEnv = procEnv;
    }

    public static ProcessingEnvironment getProcEnv() {
        return procEnv;
    }

    public static void addAnnotations(JavacNode fieldNode, List<? extends AnnotationMirror> annotations, boolean all) {
        if (annotations.isEmpty()) {
            return;
        }
        JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) fieldNode.get();
        JCTree.JCModifiers mods = field.mods;
        JavacTreeMaker maker = fieldNode.getTreeMaker();

        Set<String> selfAnnos = new HashSet<String>();
        for (AnnotationMirror item : fieldNode.getElement().getAnnotationMirrors()) {
            selfAnnos.add(item.getAnnotationType().toString());
        }
        for (AnnotationMirror item : annotations) {
            String fullName = item.getAnnotationType().toString();
            if (selfAnnos.contains(fullName)) {
                continue;
            }


            if (!all && !fullName.endsWith(".Display")) {
                continue;
            }

            if (isSearchDateType(fieldNode.getElement()) && fullName.endsWith(".Display")) {
                addDisplayForDateField(fieldNode, item);
                continue;
            }

            boolean hasAnno = false;

            ArrayList<JCTree.JCExpression> args = new ArrayList<JCTree.JCExpression>();
            for (ExecutableElement key : item.getElementValues().keySet()) {
                AnnotationValue value = item.getElementValues().get(key);
                //注解属性如果有枚举，则不支持
                if (value.getClass().getName().endsWith("$Enum")) {
                    hasAnno = true;
                    break;
                }
                JCTree.JCExpression arg = maker.Assign(maker.Ident(fieldNode.toName(key.getSimpleName().toString())), maker.Literal(value.getValue()));
                args.add(arg);
            }

            if (hasAnno) {
                continue;
            }

            JCExpression annType = JavacHandlerUtil.chainDotsString(fieldNode, fullName);

            JCExpression[] array = args.toArray(new JCTree.JCExpression[0]);
            com.sun.tools.javac.util.List<JCTree.JCExpression> list = com.sun.tools.javac.util.List.from(array);
            JCTree.JCAnnotation annotation = maker.Annotation(annType, list);
            mods.annotations = mods.annotations.append(annotation);
        }
    }

    private static void addDisplayForDateField(JavacNode fieldNode, AnnotationMirror item) {
        JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) fieldNode.get();
        JCTree.JCModifiers mods = field.mods;
        JavacTreeMaker maker = fieldNode.getTreeMaker();
        String fullName = item.getAnnotationType().toString();

        String prefix;
        if (field.getName().toString().endsWith("From")) {
            prefix = "起始";
        } else {
            prefix = "截止";
        }

        ArrayList<JCTree.JCExpression> args = new ArrayList<JCTree.JCExpression>();
        for (ExecutableElement key : item.getElementValues().keySet()) {
            AnnotationValue value = item.getElementValues().get(key);
            JCTree.JCExpression arg = maker.Assign(maker.Ident(fieldNode.toName(key.getSimpleName().toString())), maker.Literal(prefix + value.getValue()));
            args.add(arg);
        }
        JCExpression annType = JavacHandlerUtil.chainDotsString(fieldNode, fullName);

        JCExpression[] array = args.toArray(new JCTree.JCExpression[0]);
        com.sun.tools.javac.util.List<JCTree.JCExpression> list = com.sun.tools.javac.util.List.from(array);
        JCTree.JCAnnotation annotation = maker.Annotation(annType, list);
        mods.annotations = mods.annotations.append(annotation);
    }

    public static <T extends Annotation> String getTarget(AnnotationValues<T> annotation) {
        Object instance = annotation.getActualExpression("value");
        JCTree.JCFieldAccess tree = (JCTree.JCFieldAccess) instance;
        Symbol.TypeSymbol element = tree.selected.type.asElement();
        String targetName = element.toString();
        if (!elementMap.containsKey(element.toString())) {
            elementMap.put(targetName, new HashSet<Element>());
        }
        for (TypeMirror item : allSuperTypes(tree.selected.type)) {
            Type type = (Type) item;
            elementMap.get(targetName).add(type.asElement());
        }

        elementMap.get(targetName).add(element);
        return targetName;
    }

    private static Set<TypeMirror> allSuperTypes(TypeMirror root) {
        Set<TypeMirror> ret = new HashSet<TypeMirror>();

        List<? extends TypeMirror> directSupers = procEnv.getTypeUtils().directSupertypes(root);
        for (TypeMirror item : directSupers) {
            if (!item.toString().equals("java.lang.Object")) {
                ret.add(item);
                ret.addAll(allSuperTypes(item));
            }
        }

        return ret;
    }


    static class TargetModel {
        //目标类名称
        String name;

        //目标类可能有父类，所以类元素可能不止一个
        List<Element> elements = new ArrayList<Element>();
    }

    public static List<? extends AnnotationMirror> annotationsOfField(String targetName, String fieldName) {
        String key = targetName + "#" + fieldName;
        if (!annoMap.containsKey(key)) {
            Set<Element> targets = elementMap.get(targetName);
            if (targets == null) {
                return Collections.emptyList();
            }
            for (Element target : targets) {
                for (Element item : target.getEnclosedElements()) {
                    if (item.getKind() == ElementKind.FIELD) {
                        if (isDateType(item) && (fieldName.equals(item + "From") || fieldName.equals(item + "To"))) {
                            annoMap.put(key, item.getAnnotationMirrors());
                        }
                        annoMap.put(targetName + "#" + item, item.getAnnotationMirrors());
                    }
                }
            }

        }

        List<? extends AnnotationMirror> list = annoMap.get(key) == null ? Collections.<AnnotationMirror>emptyList() : annoMap.get(key);
        return list;
    }


    private static void printElement(Element element) {
        warn(element + "#" + element.getKind() + "#" + element.getSimpleName() + "#" + element.getClass());
    }

    //判断field 元素是否是日期类型：LocalDate,LocaTime,LocalDateTime
    private static boolean isDateType(Element fieldElement) {
        if (fieldElement instanceof Symbol.VarSymbol) {
            Symbol.VarSymbol var = (Symbol.VarSymbol) fieldElement;
            String str = var.type.toString();
            return str.endsWith(".LocalDateTime") || str.endsWith(".LocalDate") || str.endsWith(".LocalTime");
        }
        return false;
    }

    private static boolean isSearchDateType(Element fieldElement) {
        if (fieldElement instanceof Symbol.VarSymbol) {
            Symbol.VarSymbol var = (Symbol.VarSymbol) fieldElement;
            String str = var.type.toString();
            boolean b = str.endsWith(".LocalDateTime") || str.endsWith(".LocalDate") || str.endsWith(".LocalTime");
            String name = var.getSimpleName().toString();
            return b && (name.endsWith("From") || name.endsWith("To"));
        }
        return false;
    }

    public static void warn(Object message) {
        procEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message.toString());
    }

    public static void err(Object message, Element element) {
        procEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message.toString(), element);
    }

    public static void err(Object message) {
        procEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message.toString());
    }

    public static void err(JavacNode typeNode, Throwable e) {
        JhxUtil.err(e, typeNode.getElement());
        for (StackTraceElement item : e.getStackTrace()) {
            JhxUtil.err(item.getFileName() + "#" + item.getLineNumber(), typeNode.getElement());
        }
    }
}
