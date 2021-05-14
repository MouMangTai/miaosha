package com.moumangtai.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtil implements BeanFactoryAware {

    private static BeanFactory beanFactory;
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        SpringUtil.beanFactory = beanFactory;
    }

    /**
     * 从Bean工厂中获取指定类型的对象
     * @param c
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class c){
        return (T) beanFactory.getBean(c);
    }
}
