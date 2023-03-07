package org.springframework.data.ultipa.repository.config;

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * {@link UltipaRepositoriesRegistrar} to enable {@link EnableUltipaRepositories} annotation. The
 * {@link RepositoryBeanDefinitionRegistrarSupport} is a dedicated implementation of Spring's
 * {@link ImportBeanDefinitionRegistrar}, a dedicated SPI to register beans during processing of configuration classes.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
     */
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableUltipaRepositories.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getExtension()
     */
    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new UltipaRepositoryConfigurationExtension();
    }

}
