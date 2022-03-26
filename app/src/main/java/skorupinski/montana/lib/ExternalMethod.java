package skorupinski.montana.lib;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import skorupinski.montana.interpreter.MemoryValue;
import skorupinski.montana.interpreter.MemoryValue.*;

public class ExternalMethod {

    public final Method method;

    public final Class<?>[] paramTypes;

    public final Class<?> module;

    public ExternalMethod(Class<?> module, Method method, Class<?>[] paramTypes) {
        this.module = module;
        this.method = method;
        this.paramTypes = paramTypes;
    }

    private Object parseParam(MemoryValue param, Class<?> type) {
        if(param instanceof Singular) {
            Singular sing = (Singular) param;
            return type.cast(sing.value);

        } else if(param instanceof Array) {
            Array array = (Array) param;
            
            List<Object> list = new ArrayList<>();

            for(MemoryValue value : array.elements) {
                list.add(parseParam(value, type));
            }

            return list;
        }

        return null;
    }

    public MemoryValue call(MemoryValue[] params) {
        Object[] javaParams = new Object[params.length];

        for(int i = 0; i < params.length; i++) {
            javaParams[i] = parseParam(params[i], paramTypes[i]);
        }
        try {
            Object ret = method.invoke(module.getDeclaredConstructor().newInstance(), javaParams);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
}
