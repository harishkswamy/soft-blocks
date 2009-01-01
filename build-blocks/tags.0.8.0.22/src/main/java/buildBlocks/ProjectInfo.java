package buildBlocks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hkrishna
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProjectInfo
{
    String group();

    String id() default "";

    String name() default "";

    String desc() default "";

    String version();
}
