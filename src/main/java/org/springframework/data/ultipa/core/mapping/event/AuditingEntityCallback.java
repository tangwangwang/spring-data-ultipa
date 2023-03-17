package org.springframework.data.ultipa.core.mapping.event;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class AuditingEntityCallback implements BeforeConvertCallback<Object>, Ordered {

    private final ObjectFactory<IsNewAwareAuditingHandler> auditingHandlerFactory;

    /**
     * Creates a new {@link AuditingEntityCallback} using the given {@link MappingContext} and {@link AuditingHandler}
     * provided by the given {@link ObjectFactory}.
     *
     * @param auditingHandlerFactory must not be {@literal null}.
     */
    public AuditingEntityCallback(ObjectFactory<IsNewAwareAuditingHandler> auditingHandlerFactory) {
        Assert.notNull(auditingHandlerFactory, "IsNewAwareAuditingHandler must not be null!");
        this.auditingHandlerFactory = auditingHandlerFactory;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.cassandra.core.mapping.event.BeforeConvertCallback#onBeforeConvert(java.lang.Object, com.datastax.oss.driver.api.core.CqlIdentifier)
     */
    @Override
    public Object onBeforeConvert(Object entity, String schema) {
        return auditingHandlerFactory.getObject().markAudited(entity);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.core.Ordered#getOrder()
     */
    @Override
    public int getOrder() {
        return 100;
    }
}
