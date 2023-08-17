package org.springframework.data.ultipa.core.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.data.ultipa.core.convert.UltipaConverter;
import org.springframework.data.ultipa.core.exception.ParameterBindingException;
import org.springframework.data.ultipa.core.mapping.model.UltipaEnumTypeHolder;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class Query {

    private final static List<Class<?>> antiInjectionTypes = Arrays.asList(
            String.class, UUID.class, Character.class, char.class,
            Enum.class, Date.class, Timestamp.class, Instant.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class
    );
    private final static List<Class<?>> timeTypes = Arrays.asList(Date.class, Instant.class, LocalDate.class, LocalTime.class, LocalDateTime.class);
    private final UltipaOperations operations;
    private final UltipaConverter converter;
    private final ExpressionParser parser;
    private final String queryString;
    private final Map<String, Object> paramMap;
    private long skip;
    private int limit;
    private Sort sort = Sort.unsorted();
    private final @Nullable String sortPrefix;

    protected Query(UltipaOperations operations, String queryString, @Nullable ExpressionParser parser,
                    @Nullable Map<String, Object> paramMap, @Nullable String sortPrefix) {
        this.operations = operations;
        this.converter = operations.getConverter();
        this.queryString = queryString;
        this.parser = Optional.ofNullable(parser).orElse(new SpelExpressionParser(new SpelParserConfiguration(true, true)));
        this.paramMap = Optional.ofNullable(paramMap).orElse(new HashMap<>());
        this.sortPrefix = sortPrefix;
    }

    protected Query(UltipaOperations operations, String queryString, @Nullable ExpressionParser parser,
                    @Nullable Map<String, Object> paramMap, Pageable pageable, @Nullable String sortPrefix) {
        this(operations, queryString, parser, paramMap, sortPrefix);
        with(pageable);
    }

    protected Query(UltipaOperations operations, String queryString, @Nullable ExpressionParser parser,
                    @Nullable Map<String, Object> paramMap, Sort sort, @Nullable String sortPrefix) {
        this(operations, queryString, parser, paramMap, sortPrefix);
        with(sort);
    }

    public Query skip(long skip) {
        this.skip = skip;
        return this;
    }

    public Query limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Query with(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null!");

        if (pageable.isUnpaged()) {
            return this;
        }

        this.limit = pageable.getPageSize();
        this.skip = pageable.getOffset();

        return with(pageable.getSort());
    }

    public Query with(Sort sort) {
        Assert.notNull(sort, "Sort must not be null!");

        if (sort.isUnsorted()) {
            return this;
        }

        this.sort = this.sort.and(sort);

        return this;
    }

    private String formatUql() {
        String simpleReplaceString = simpleReplacePlaceholders(queryString);
        String antiInjectionReplaceString = antiInjectionReplacePlaceholders(simpleReplaceString);
        return antiInjectionReplaceString + appendSort() + appendSkipAndLimit();
    }

    private String appendSkipAndLimit() {
        StringBuilder sb = new StringBuilder();
        if (this.skip > 0L) {
            sb.append(" SKIP ").append(this.skip).append(" ");
        }

        if (this.limit > 0) {
            sb.append(" LIMIT ").append(this.limit).append(" ");
        }
        return sb.toString();
    }

    private String appendSort() {
        StringBuilder sb = new StringBuilder();
        if (sort.isSorted()) {
            sb.append(" ORDER BY ");
            this.sort.stream().forEach((order) -> {
                if (StringUtils.hasText(sortPrefix)) {
                    sb.append(sortPrefix).append(".");
                }
                sb.append(order.getProperty()).append(" ").append(order.isAscending() ? "ASC," : "DESC,");
            });
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public void execute() {
        operations.execute(formatUql());
    }

    @Nullable
    public <T> T findOne(Class<T> domainClass) {
        return operations.findOne(formatUql(), domainClass);
    }

    public <T> List<T> findAll(Class<T> domainClass) {
        return operations.findAll(formatUql(), domainClass);
    }

    @Nullable
    public Map<String, Object> findOne() {
        return operations.findOne(formatUql());
    }

    public List<Map<String, Object>> findAll() {
        return operations.findAll(formatUql());
    }

    @Nullable
    public List<Object> findArray() {
        return operations.findArray(formatUql());
    }

    public List<List<Object>> findArrays() {
        return operations.findArrays(formatUql());
    }

    public long count() {
        Object count = Optional.ofNullable(findOne())
                .map(Map::entrySet)
                .map(Collection::stream)
                .map(Stream::findFirst)
                .flatMap(Function.identity())
                .map(Map.Entry::getValue)
                .orElse(0);
        // noinspection DataFlowIssue
        return converter.getConversionService().convert(count, Long.class);
    }

    public boolean exists() {
        return operations.findAll(formatUql()).size() > 0;
    }

    /**
     * Simple placeholder substitution based on {@code ${}}
     *
     * @param source The string to replace
     * @return The replaced string
     */
    private String simpleReplacePlaceholders(String source) {
        return new PropertyPlaceholderHelper("${", "}").replacePlaceholders(source, placeholder -> {
            Object value = resolverPlaceholder(placeholder);
            if (isArrayOrCollection(value)) {
                Class<?> valueClass = value.getClass();
                Iterable<?> iterable;
                if (valueClass.isArray()) {
                    iterable = Arrays.asList((Object[]) value);
                } else {
                    iterable = (Iterable<?>) value;
                }
                StringBuilder target = new StringBuilder("[");
                boolean crop = false;
                for (Object element : iterable) {
                    target.append(convertUqlValue(element));
                    target.append(",");
                    crop = true;
                }
                if (crop) {
                    target.deleteCharAt(target.length() - 1);
                }
                target.append("]");
                return target.toString();
            } else {
                return convertUqlValue(value);
            }
        });
    }

    /**
     * Substitution of anti-injection placeholders according to {@code #{}},
     *
     * @param source The string to replace
     * @return The replaced string
     */
    private String antiInjectionReplacePlaceholders(String source) {
        return new PropertyPlaceholderHelper("#{", "}").replacePlaceholders(source, placeholder -> {
            Object value = resolverPlaceholder(placeholder);
            if (isArrayOrCollection(value)) {
                Class<?> valueClass = value.getClass();
                Iterable<?> iterable;
                if (valueClass.isArray()) {
                    iterable = Arrays.asList((Object[]) value);
                } else {
                    iterable = (Iterable<?>) value;
                }
                StringBuilder target = new StringBuilder("[");
                boolean crop = false;
                for (Object element : iterable) {
                    String elementUqlValue = convertUqlValue(element);
                    if (isNeedAntiInjection(element)) {
                        target.append(String.format("\"%s\"", elementUqlValue));
                    } else {
                        target.append(elementUqlValue);
                    }
                    target.append(",");
                    crop = true;
                }
                if (crop) {
                    target.deleteCharAt(target.length() - 1);
                }
                target.append("]");
                return target.toString();
            } else {
                String uqlValue = convertUqlValue(value);
                return isNeedAntiInjection(value) ? String.format("\"%s\"", uqlValue) : uqlValue;
            }
        });
    }

    /**
     * Determine if double quotes are required
     *
     * @param value Judgment object
     * @return Judgment result
     */
    private boolean isNeedAntiInjection(@Nullable Object value) {
        if (value != null) {
            Class<?> valueClass = value.getClass();
            for (Class<?> type : antiInjectionTypes) {
                if (type == valueClass || type.isAssignableFrom(valueClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine if it is an array of collections
     *
     * @param value Judgment object
     * @return Judgment result
     */
    private boolean isArrayOrCollection(@Nullable Object value) {
        if (value != null) {
            Class<?> valueClass = value.getClass();
            return valueClass.isArray() || Iterable.class.isAssignableFrom(valueClass);
        }
        return false;
    }

    @Nullable
    private Object resolverPlaceholder(String placeholder) {
        if (!StringUtils.hasText(placeholder)) {
            throw new ParameterBindingException("No content in placeholder.");
        }
        String[] placeholders = placeholder.split(":");
        if (placeholders.length == 0) {
            throw new ParameterBindingException("No content in placeholder.");
        }

        Object value = readValue(placeholders[0].trim());

        return formatValue(value, placeholders[1]);
    }

    @Nullable
    private Object readValue(String placeholder) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext(paramMap);
            context.addPropertyAccessor(new MapAccessor());
            Expression expression = parser.parseExpression(placeholder);
            return expression.getValue(context);
        } catch (ExpressionException e) {
            throw new ParameterBindingException("Parameter '" + placeholder + "' not found. Available parameters are " + paramMap.keySet());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    private Object formatValue(@Nullable Object value, String placeholder) {
        if (value == null) {
            return null;
        }
        switch (placeholder.toLowerCase(Locale.ROOT)) {
            case "field":
                if (value instanceof Enum) {
                    Class<? extends Enum> type = (Class<? extends Enum>) value.getClass();
                    if (UltipaEnumTypeHolder.hasEnumField(type)) {
                        PropertyAccessor enumAccessor = new BeanWrapperImpl(value);
                        value = enumAccessor.getPropertyValue(UltipaEnumTypeHolder.getRequiredEnumField(type).getName());
                    } else {
                        value = ((Enum<?>) value).name();
                    }
                }
                break;
            case "name":
                if (value instanceof Enum) {
                    return ((Enum<?>) value).name();
                }
                break;
            case "ordinal":
                if (value instanceof Enum) {
                    return ((Enum<?>) value).ordinal();
                }
                break;
            case "json":
                try {
                    return converter.getObjectMapper().writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException(String.format("%s serialize failed.", value.getClass()), e);
                }
            case "string":
                if (value instanceof Boolean) {
                    return value.toString();
                }
                break;
            case "int":
            case "int32":
            case "int64":
            case "uint":
            case "uint32":
            case "uint64":
            case "float":
            case "double":
                if (value instanceof Boolean) {
                    return (Boolean) value ? 1 : 0;
                }
                break;
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String convertUqlValue(@Nullable Object value) {
        ConversionService conversionService = converter.getConversionService();
        if (value == null) {
            return "null";
        }

        Class<?> valueType = value.getClass();

        // handle enumeration
        if (Enum.class.isAssignableFrom(valueType)) {
            Class<? extends Enum> enumType = (Class<? extends Enum>) valueType;
            if (UltipaEnumTypeHolder.hasEnumField(enumType)) {
                PropertyAccessor enumAccessor = new BeanWrapperImpl(value);
                value = enumAccessor.getPropertyValue(UltipaEnumTypeHolder.getRequiredEnumField(enumType).getName());
            } else {
                value = ((Enum<?>) value).name();
            }
        }

        // handle datetime
        for (Class<?> timeType : timeTypes) {
            if (valueType == timeType || timeType.isAssignableFrom(valueType)) {
                return Optional.ofNullable(value)
                        .map(v -> conversionService.convert(v, LocalDateTime.class))
                        .map(d -> conversionService.convert(d, String.class))
                        .orElse("null");
            }
        }

        return Optional.ofNullable(value)
                .map(String::valueOf)
                // because ultipa db interpret character \\t as \t, so need to replace \\t as \\\\t to keep character \\t
                .map(v -> v.replace("\\", "\\\\"))
                .map(v -> v.replace("\"", "\\\""))
                .orElse("null");
    }

}
