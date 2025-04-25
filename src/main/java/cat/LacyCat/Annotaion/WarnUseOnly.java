package cat.LacyCat.Annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface WarnUseOnly {
    String value() default "이 메서드는 사용 시 주의가 필요합니다.";
}