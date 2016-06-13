package co.neweden.Perks.commands;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SubCommandHelpContainer.class)
public @interface SubCommandHelp {

    String helpPage() default "";
    String usage();
    String description() default  "";
    String permission() default "";

}
