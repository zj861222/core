package com.framework.core.dal.datasource.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * add by zhangjun
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Database {

    /**
     * 强制数据库操作使用主库
     * @return
     */
    boolean ForceSlave() default false ;

    /**
     * 指定数据源,可以配合@Transactional一起使用.
     * @return
     */
    String DataSource() default "";

}
