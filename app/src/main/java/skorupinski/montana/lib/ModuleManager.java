package skorupinski.montana.lib;

import java.lang.reflect.Method;

import skorupinski.montana.interpreter.Memory;
import skorupinski.montana.interpreter.MemoryValue.Function;
import skorupinski.montana.interpreter.MemoryValue.LangObject;
import skorupinski.montana.lexer.Token;
import skorupinski.montana.lexer.TokenType;
import skorupinski.montana.parser.AST.FunctionInit;
import skorupinski.montana.parser.AST.Variable;
import skorupinski.montana.parser.AST.VariableDeclaration;

public class ModuleManager {

    private static final String PATH = "skorupinski.montana.lib.modules";
    
    public ModuleManager() {}

    public void importModule(String name, Memory memory, String as) {
        try {
            Class<?> module = Class.forName(PATH + "." + name);

            Memory objectMemory = new Memory(memory.memoryLevel + 1, memory);

            for(Method method : module.getDeclaredMethods()) {
                String functionName = method.getName();

                VariableDeclaration decl = new VariableDeclaration();
                for(int i = 0; i < method.getParameterCount(); i++) {
                    String paramName = "a";
                    for(int j = 0; j < i; i++) {
                        paramName += "a";
                    }
                    decl.variables.add(new Variable(new Token(TokenType.IDENTIFIER, paramName)));
                }

                ExternalMethod ext = new ExternalMethod(module, method, method.getParameterTypes());

                FunctionInit init = new FunctionInit(functionName, decl, ext);

                objectMemory.put(functionName, new Function(init));
            }
            memory.put(as, new LangObject(objectMemory));
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
