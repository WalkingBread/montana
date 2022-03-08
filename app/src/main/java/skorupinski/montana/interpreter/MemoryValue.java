package skorupinski.montana.interpreter;

import java.util.ArrayList;
import java.util.List;

import skorupinski.montana.parser.AST.FunctionInit;

public class MemoryValue {
    
    public enum Type {
        FLOAT,
        STRING,
        BOOLEAN,
        ARRAY,
        FUNCTION,
        OBJECT,
        NONE
    }

    public final Type type;

    protected MemoryValue(Type type) {
        this.type = type;
    }

    public static class Singular extends MemoryValue {

        public final String value;

        public Singular(String value, Type type) {
            super(type);

            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    public static class Array extends MemoryValue {

        public final List<MemoryValue> elements;

        public Array(List<MemoryValue> elements) {
            super(Type.ARRAY);

            this.elements = elements;
        }

        public Array() {
            super(Type.ARRAY);

            elements = new ArrayList<>();
        }

        @Override
        public String toString() {
            String result = "[";
            for(int i = 0; i < elements.size(); i++) {
                MemoryValue val = elements.get(i);
                result += val.toString();
        
                if(i != elements.size() - 1) {
                    result += ", ";
                }
            }
            result += "]";
            return result;
        }
        
    }

    public static class Function extends MemoryValue {

        public final FunctionInit function;

        public Function(FunctionInit function) {
            super(Type.FUNCTION);

            this.function = function;
        }

        @Override
        public String toString() {
            return "function " + function.functionName;
        }

    }

    public static class LangObject extends MemoryValue {

        public final Memory objectMemory;

        public LangObject(Memory objectMemory) {
            super(Type.OBJECT);

            this.objectMemory = objectMemory;
        }

        @Override
        public String toString() {
            return "object";
        }

    }
}
