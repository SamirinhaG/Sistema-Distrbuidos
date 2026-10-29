package rr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marca os métodos de um objeto que podem ser invocados remotamente*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MetodoRemoto {
}
