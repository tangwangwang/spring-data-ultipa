package org.springframework.data.ultipa.repository.query;

import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaParametersParameterAccessor extends ParametersParameterAccessor {
    /**
     * Creates a new {@link ParametersParameterAccessor}.
     *
     * @param parameters must not be {@literal null}.
     * @param values     must not be {@literal null}.
     */
    public UltipaParametersParameterAccessor(Parameters<?, ?> parameters, Object[] values) {
        super(parameters, values);
    }

    public Map<String, Object> getParamMap() {
        Map<String, Object> paramMap = new HashMap<>();

        Object[] values = getValues();
        for (Parameter parameter : getParameters()) {
            parameter.getName().ifPresent(name -> paramMap.put(name, values[parameter.getIndex()]));
        }

        return paramMap;
    }


}