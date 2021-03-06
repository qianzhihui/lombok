package lombok;

import lombok.Setter;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 钱智慧
 * date 9/12/18 4:05 PM
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AllAs {
    Class<?> value();
}
