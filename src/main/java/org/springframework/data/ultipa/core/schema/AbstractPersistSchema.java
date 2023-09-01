package org.springframework.data.ultipa.core.schema;

import org.springframework.data.ultipa.core.mapping.model.UltipaSystemProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

/**
 * An abstract implementation of the persistent schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
abstract class AbstractPersistSchema extends MapSchema implements PersistSchema {

    protected static final String SETTER_CLAUSE = "%s: %s";
    protected static final String SETTER_DELIMITER = ", ";
    protected static final String FILTER_CLAUSE = "%s == %s";
    protected static final String FILTER_DELIMITER = " && ";
    protected static final String SCHEMA_FILTER_CLAUSE = "@%s";
    protected static final String NULL_CLAUSE = "";
    private @Nullable Object source;
    private @Nullable String idName;
    private @Nullable Object idValue;
    private @Nullable Boolean isNew;
    private @Nullable Long systemUuid;
    private boolean persisted = false;

    AbstractPersistSchema() {
    }

    AbstractPersistSchema(@Nullable Object source) {
        this.source = source;
    }

    @Nullable
    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public void setSource(Object source) {
        this.source = source;
    }

    @Override
    public void setSystemUuid(Long uuid) {
        this.systemUuid = uuid;
    }

    @Nullable
    @Override
    public Long getSystemUuid() {
        return this.systemUuid;
    }

    @Override
    public void setIdName(String idName) {
        this.idName = idName;
    }

    @Override
    public void setIdValue(@Nullable Object idValue) {
        this.idValue = idValue;
    }

    @Override
    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    @Nullable
    @Override
    public Boolean getIsNew() {
        return this.isNew;
    }

    @Override
    public void persisted() {
        this.persisted = true;
    }

    @Override
    public boolean isPersisted() {
        return persisted;
    }

    @Override
    public String toUqlString() {
        if (isNew == null) {
            return getInsertIfAbsentUql();
        } else if (isNew) {
            return getInsertUql();
        } else {
            return getUpdateUql();
        }
    }

    protected final String getSchemaFilterClause() {
        return StringUtils.hasText(getSchema()) ? String.format(SCHEMA_FILTER_CLAUSE, getSchema()) : NULL_CLAUSE;
    }

    protected final String getIdentifierFilterClause() {
        return idValue == null ? NULL_CLAUSE : String.format(FILTER_CLAUSE, idName, idValue);
    }

    protected final String getIdentifierSetterClause() {
        return idValue == null ? NULL_CLAUSE : String.format(SETTER_CLAUSE, idName, idValue);
    }

    protected final String getPropertySetterClause() {
        return getDelegate().entrySet().stream()
                .filter(it -> !UltipaSystemProperty.isSystemProperty(it.getKey()))
                .map(it -> String.format(SETTER_CLAUSE, it.getKey(), it.getValue()))
                .collect(Collectors.joining(SETTER_DELIMITER));
    }

    protected abstract String getInsertIfAbsentUql();

    protected abstract String getInsertUql();

    protected abstract String getUpdateUql();

}
