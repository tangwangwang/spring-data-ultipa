package org.springframework.data.ultipa.core.convert;

import org.springframework.data.convert.*;
import org.springframework.data.mapping.Alias;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.ultipa.core.schema.Schema;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Ultipa specific {@link TypeMapper} implementation.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class DefaultUltipaTypeMapper extends DefaultTypeMapper<Schema> implements UltipaTypeMapper {

    private final TypeAliasAccessor<Schema> accessor;
    private final @Nullable String typeKey;

    /**
     * Create a new {@link UltipaTypeMapper} with fully-qualified type hints using {@code _class}.
     */
    public DefaultUltipaTypeMapper() {
        this(DEFAULT_TYPE_KEY);
    }

    /**
     * Create a new {@link UltipaTypeMapper} with fully-qualified type hints using {@code typeKey}.
     *
     * @param typeKey name of the field to read and write type hints. Can be {@literal null} to disable type hints.
     */
    public DefaultUltipaTypeMapper(@Nullable String typeKey) {
        this(typeKey, Collections.singletonList(new SimpleTypeInformationMapper()));
    }

    /**
     * Create a new {@link UltipaTypeMapper} with fully-qualified type hints using {@code typeKey}.
     *
     * @param typeKey        name of the field to read and write type hints. Can be {@literal null} to disable type hints.
     * @param mappingContext the mapping context.
     */
    public DefaultUltipaTypeMapper(@Nullable String typeKey,
                                   MappingContext<? extends PersistentEntity<?, ?>, ?> mappingContext) {
        this(typeKey, new SchemaTypeAliasAccessor(typeKey), mappingContext,
                Collections.singletonList(new SimpleTypeInformationMapper()));
    }

    /**
     * Create a new {@link UltipaTypeMapper} with fully-qualified type hints using {@code typeKey}. Uses
     * {@link TypeInformationMapper} to map type hints.
     *
     * @param typeKey name of the field to read and write type hints. Can be {@literal null} to disable type hints.
     * @param mappers must not be {@literal null}.
     */
    public DefaultUltipaTypeMapper(@Nullable String typeKey, List<? extends TypeInformationMapper> mappers) {
        this(typeKey, new SchemaTypeAliasAccessor(typeKey), null, mappers);
    }

    private DefaultUltipaTypeMapper(@Nullable String typeKey, TypeAliasAccessor<Schema> accessor,
                                    @Nullable MappingContext<? extends PersistentEntity<?, ?>, ?> mappingContext,
                                    List<? extends TypeInformationMapper> mappers) {
        super(accessor, mappingContext, mappers);

        this.typeKey = typeKey;
        this.accessor = accessor;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.ultipa.core.convert.UltipaTypeMapper#isTypeKey(java.lang.String)
     */
    @Override
    public boolean isTypeKey(String key) {
        return typeKey != null && typeKey.equals(key);
    }

    /**
     * {@link TypeAliasAccessor} to store aliases in a {@link Schema}.
     */
    public static final class SchemaTypeAliasAccessor implements TypeAliasAccessor<Schema> {

        private final @Nullable String typeKey;

        public SchemaTypeAliasAccessor(@Nullable String typeKey) {
            this.typeKey = typeKey;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.convert.TypeAliasAccessor#readAliasFrom(java.lang.Object)
         */
        public Alias readAliasFrom(Schema source) {
            return typeKey == null ? Alias.empty() : Alias.ofNullable(source.get(typeKey));
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.convert.TypeAliasAccessor#writeTypeTo(java.lang.Object, java.lang.Object)
         */
        public void writeTypeTo(Schema sink, Object alias) {
            if (typeKey == null) {
                return;
            }

            sink.put(typeKey, alias);
        }
    }
}
