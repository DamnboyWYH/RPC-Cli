package ExtensionSPIImpl;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtensionSPI {
    //String value() default "";

}
