package skorupinski.montana.interpreter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import skorupinski.montana.interpreter.MemoryValue.*;
import skorupinski.montana.lexer.Lexer;
import skorupinski.montana.lexer.Token;
import skorupinski.montana.lexer.TokenType;
import skorupinski.montana.lib.ModuleManager;
import skorupinski.montana.parser.AST;
import skorupinski.montana.parser.Parser;
import skorupinski.montana.parser.AST.*;
import skorupinski.montana.utils.Error.*;
import skorupinski.montana.utils.Values;

public class Interpreter {

    public Memory memory;

    private final SemanticAnalyzer analyzer;

    private final ModuleManager moduleManager;

    private String directory;
    
    public Interpreter() {
        memory = new Memory(0, null);
        analyzer = new SemanticAnalyzer();
        moduleManager = new ModuleManager();
    }

    private void typeMismatchError(Token token) {
        String message = "Type mismatch.";
        new SyntaxError(token, message).cast();
    }
    
    private void valueError(Token token) {
        String message = "Value cannot be converted to " + token.value + ".";
        String filePath = token.file;
        new ValueError(filePath, token.line, token.column, message).cast();
    }

    private void enterNewMemory() {
        memory = new Memory(memory.memoryLevel + 1, memory);
    }
    
    private void leaveMemory() {
        if(memory.enclosingMemoryBlock.memoryLevel != 0) {
            memory = memory.enclosingMemoryBlock;
        }
    }

    public MemoryValue visit(AST node) {
        String className = node.getClass().getSimpleName();

        try {
            Method method = getClass().getMethod("visit" + className, node.getClass());
            return (MemoryValue) method.invoke(this, node);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public MemoryValue visitBinaryOperator(BinaryOperator op) {
        Singular left = (Singular) visit(op.left);
        Singular right = (Singular) visit(op.right);
    
        switch(op.op.type) {
            case PLUS:
            {
                if(left.type == Type.STRING) {
                    if(right.type != Type.STRING) {
                        typeMismatchError(op.right.token);
                    }
    
                    String a = left.value;
                    String b = right.value;
    
                    return new Singular(a + b, Type.STRING);
    
    
                } else if(left.type == Type.FLOAT) {
                    if(right.type != Type.FLOAT) {
                        typeMismatchError(op.right.token);
                    }
    
                    double x = Double.parseDouble(left.value);
                    double y = Double.parseDouble(right.value);
    
                    return new Singular(Double.toString(x + y), Type.FLOAT);
                }
                typeMismatchError(op.left.token);
            }
            case MINUS:
            {   
                if(left.type != Type.FLOAT || right.type != Type.FLOAT) {
                    typeMismatchError(op.right.token);
                }
                double x = Double.parseDouble(left.value);
                double y = Double.parseDouble(right.value);
    
                return new Singular(Double.toString(x - y), Type.FLOAT);
            }
            case DIV:
            {
                if(left.type != Type.FLOAT || right.type != Type.FLOAT) {
                    typeMismatchError(op.right.token);
                }
                double x = Double.parseDouble(left.value);
                double y = Double.parseDouble(right.value);
    
                return new Singular(Double.toString(x / y), Type.FLOAT);
            }
            case MULT:
            {
                if(left.type != Type.FLOAT || right.type != Type.FLOAT) {
                    typeMismatchError(op.right.token);
                }
                double x = Double.parseDouble(left.value);
                double y = Double.parseDouble(right.value);
    
                return new Singular(Double.toString(x * y), Type.FLOAT);
            }
            case INT_DIV:
            {
                if(left.type != Type.FLOAT || right.type != Type.FLOAT) {
                    typeMismatchError(op.right.token);
                }
    
                int x = Integer.parseInt(left.value);
                int y = Integer.parseInt(right.value);
    
                return new Singular(Double.toString(x / y), Type.FLOAT);
            }
            case MODULO:
            {
                if(left.type != Type.FLOAT || right.type != Type.FLOAT) {
                    typeMismatchError(op.right.token);
                }
    
                double x = Double.parseDouble(left.value);
                double y = Double.parseDouble(right.value);
                return new Singular(Double.toString(x % y), Type.FLOAT);
            }
        }
        return null;
    }

    public Singular visitUnaryOperator(UnaryOperator op) {
        Singular expr = (Singular) visit(op.expr);

        if(op.op.typeOf(TokenType.MINUS)) {
            if(expr.type == Type.FLOAT) {
    
                double value = Double.parseDouble(expr.value);
                return new Singular(Double.toString(-value), Type.FLOAT);
    
            } else {
                typeMismatchError(op.expr.token);
            }
        } 
        return expr;
        
    }

    public Singular visitValue(Value val) {
        Type type = null;

        if(val.token.typeOf(TokenType.FLOAT)) {
            type = Type.FLOAT;
    
        } else if(val.token.typeOf(TokenType.BOOLEAN)) {
            type = Type.BOOLEAN;
    
        } else if(val.token.typeOf(TokenType.STRING)) {
            type = Type.STRING;
    
        } else if(val.token.typeOf(TokenType.NONE)) {
            type = Type.NONE;
        }
    
        return new Singular(val.value.value, type);
        
    }

    public Singular visitCompare(Compare c) {
        for(int i = 0; i < c.operators.size(); i++) {
            Token op = c.operators.get(i);
            AST left = c.comparables.get(i);
            AST right = c.comparables.get(i + 1);
    
            Singular leftMemoryValue = (Singular) visit(left);
            Singular rightMemoryValue = (Singular) visit(right);
    
            String leftValue = leftMemoryValue.value;
            String rightValue = rightMemoryValue.value;
 
            if(op.typeOf(TokenType.EQUALS)) {
                if(leftMemoryValue.type == Type.FLOAT && rightMemoryValue.type == Type.FLOAT) {
                    double leftVal = Double.parseDouble(leftValue);
                    double rightVal = Double.parseDouble(rightValue);
    
                    if(leftVal != rightVal) {
                        return new Singular(Values.FALSE, Type.BOOLEAN);
                    }
                } else if(!leftValue.equals(rightValue)) {
                    return new Singular(Values.FALSE, Type.BOOLEAN);
                }
            } else if(op.typeOf(TokenType.NOT_EQUALS)) {
                if(leftMemoryValue.type == Type.FLOAT && rightMemoryValue.type == Type.FLOAT) {
                    double leftVal = Double.parseDouble(leftValue);
                    double rightVal = Double.parseDouble(rightValue);
    
                    if(leftVal == rightVal) {
                        return new Singular(Values.FALSE, Type.BOOLEAN);
                    }
                } else if(leftValue.equals(rightValue)) {
                    return new Singular(Values.FALSE, Type.BOOLEAN);
                }
            } else if(op.typeOf(TokenType.MORE_OR_EQ)) {
                if(leftMemoryValue.type == Type.FLOAT && rightMemoryValue.type == Type.FLOAT) {
                    double leftVal = Double.parseDouble(leftValue);
                    double rightVal = Double.parseDouble(rightValue);
    
                    if(leftVal < rightVal) {
                        return new Singular(Values.FALSE, Type.BOOLEAN);
                    }
                } else {
                    typeMismatchError(left.token);
                }
            } else if(op.typeOf(TokenType.LESS_OR_EQ)) {
                if(leftMemoryValue.type == Type.FLOAT && rightMemoryValue.type == Type.FLOAT) {
                    double leftVal = Double.parseDouble(leftValue);
                    double rightVal = Double.parseDouble(rightValue);
    
                    if(leftVal > rightVal) {
                        return new Singular(Values.FALSE, Type.BOOLEAN);
                    }
                } else {
                    typeMismatchError(left.token);
                }
            } else if(op.typeOf(TokenType.LESS)) {
                if(leftMemoryValue.type == Type.FLOAT && rightMemoryValue.type == Type.FLOAT) {
                    double leftVal = Double.parseDouble(leftValue);
                    double rightVal = Double.parseDouble(rightValue);
    
                    if(leftVal >= rightVal) {
                        return new Singular(Values.FALSE, Type.BOOLEAN);
                    }
                } else {
                    typeMismatchError(left.token);
                }
            } else if(op.typeOf(TokenType.MORE)) {
                if(leftMemoryValue.type == Type.FLOAT && rightMemoryValue.type == Type.FLOAT) {
                    double leftVal= Double.parseDouble(leftValue);
                    double rightVal = Double.parseDouble(rightValue);
    
                    if(leftVal <= rightVal) {
                        return new Singular(Values.FALSE, Type.BOOLEAN);
                    }
                } else {
                    typeMismatchError(left.token);
                }
            }
        }
        return new Singular(Values.TRUE, Type.BOOLEAN);
        
    }

    public MemoryValue visitCompound(Compound comp) {
        if(memory.memoryLevel == 0) {
            enterNewMemory();
        }
    
        for(AST node : comp.children) {
            if(node instanceof Return) {
                Return ret = (Return) node;
                if(comp.insideFunction) {
                    MemoryValue returnValue = visitReturn(ret);
                    leaveMemory();
                    return returnValue;
                }

                new SyntaxError(ret.token, "Return statement without function declaration.").cast();
            }
    
            MemoryValue value = visit(node);
    
            if(comp.insideFunction && value != null) {
                leaveMemory();
                return value;
            } 
        }
    
        leaveMemory();
        if(memory.memoryLevel == 1) {
            Memory object_memory = memory;
            return new LangObject(object_memory);
        }
    
        return null;
        
    }

    public void visitAssign(Assign assign) {
        AST left = assign.left;

        if(left instanceof Variable) {
            Variable var = (Variable) left;
            String varName = var.token.value;
            memory.put(varName, visit(assign.right));

        } else if(left instanceof ArrayAccess) {
            ArrayAccess acc = (ArrayAccess) left;
            Array arr = (Array) visit(acc.array);

            Singular index = (Singular) visit(acc.index);
            if(index.type != Type.FLOAT) {
                typeMismatchError(acc.index.token);
            }

            MemoryValue newVal = visit(assign.right);
            arr.elements.set(Integer.parseInt(index.value), newVal);
        }
    }

    public MemoryValue visitVariable(Variable var) {
        MemoryValue val = memory.get(var.token.value, false);

        if(val == null) {
            new NameError(var.token, "Variable has not been initialized.").cast();
        } 
    
        return val;
    }

    public void visitNoOperator(NoOperator noOp) {}

    public Singular visitDoubleCondition(DoubleCondition cond) {
        String leftValue = ((Singular) visit(cond.left)).value;
        String rightValue = ((Singular) visit(cond.right)).value;
    
        if(cond.token.typeOf(TokenType.AND)) {
            if(leftValue.equals(Values.TRUE) && rightValue.equals(Values.TRUE)) {
                return new Singular(Values.TRUE, Type.BOOLEAN);
            } else {
                return new Singular(Values.FALSE, Type.BOOLEAN);
            }
    
        } else if(cond.token.typeOf(TokenType.OR)) {
            if(leftValue.equals(Values.TRUE) || rightValue.equals(Values.TRUE)) {
                return new Singular(Values.TRUE, Type.BOOLEAN);
            } else {
                return new Singular(Values.FALSE, Type.BOOLEAN);
            }
        }

        return null;
    }

    public Singular visitNegation(Negation neg) {
        Singular value = (Singular) visit(neg.statement);

        if(value.type != Type.BOOLEAN) {
            typeMismatchError(neg.statement.token);
        }
    
        if(value.value == Values.TRUE) {
            return new Singular(Values.FALSE, Type.BOOLEAN);
        } else if(value.value == Values.FALSE) {
            return new Singular(Values.TRUE, Type.BOOLEAN);
        }
        
        return null;
    }

    public void visitVariableDeclaration(VariableDeclaration decl) {
        for(Assign assignment : decl.assignments) {
            visit(assignment);
        }
    }

    public MemoryValue visitIfCondition(IfCondition cond) {
        AST condition = cond.condition;
        Compound statement = cond.statement;
    
        String condValue = ((Singular) visit(condition)).value;
    
        MemoryValue returnValue = null;
    
        if(condValue == Values.TRUE) {
            enterNewMemory();
            returnValue = visit(statement);
        } else {
            for(IfCondition else_ : cond.elses) {
                String else_condValue = ((Singular) visit(else_.condition)).value;
    
                if(else_condValue == Values.TRUE) {
                    enterNewMemory();
                    return visit(else_.statement);
                }
            }
        }
        return returnValue;
    }

    public void visitPrint(Print print) {
        MemoryValue printableValue = visit(print.printable);
        System.out.println(printableValue);
    }

    public Array visitArrayInit(ArrayInit arrayInit) {
        List<MemoryValue> elements = new ArrayList<>();

        for(AST el : arrayInit.elements) {
            MemoryValue element = visit(el);
            elements.add(element);
        }
    
        return new Array(elements);
    }

    public MemoryValue visitArrayAccess(ArrayAccess access) {
        MemoryValue arr = visit(access.array);

        if(arr.type != Type.ARRAY) {
            new SyntaxError(access.array.token, "Given object is not an array.").cast();
        }
    
        Array array = (Array) arr;
        
        MemoryValue index = visit(access.index);
    
        if(index.type != Type.FLOAT) {
            typeMismatchError(access.index.token);
        }
    
        Singular _index = (Singular) index;
        int i = (int) Double.parseDouble(_index.value);
    
        if(i > array.elements.size() - 1) {
            new SyntaxError(access.index.token, "Index out of bounds.").cast();
        }
    
        return array.elements.get(i);
    }

    public void visitFunctionInit(FunctionInit functionInit) {
        memory.put(functionInit.functionName, new Function(functionInit));
    }

    public MemoryValue visitFunctionCall(FunctionCall funcCall) {
        MemoryValue func = visit(funcCall.function);

        if(func.type != Type.FUNCTION) {
            new SyntaxError(funcCall.function.token, "Given object is not a function.").cast();
        }
    
        Function function = (Function) func;
        enterNewMemory();
    
        VariableDeclaration funcParams = function.function.params;
    
        if(funcParams != null) {
            visit(funcParams);
    
            if(funcParams.variables.size() != funcCall.params.size()) {
                new SyntaxError(funcCall.function.token, "Inconsistent number of arguments.").cast();
            }
    
            for(int i = 0; i < funcParams.variables.size(); i++) {
                Variable param = funcParams.variables.get(i);
                AST actualParam = funcCall.params.get(i);
                
                Assign assign = new Assign(param, new Token(TokenType.ASSIGN, "="), actualParam);
                visit(assign);
            }
        } else {
            if(funcCall.params.size() > 0) {
                String message = "Function " + function.function.functionName + " has no arguments, but " + 
                Integer.toString(funcCall.params.size()) + " were given.";
                new SyntaxError(funcCall.function.token, message).cast();
            }
        }
    
        MemoryValue ret = null;

        if(function.function.block != null) {
            ret = visit(function.function.block);
        } else {
            MemoryValue[] real = new MemoryValue[funcCall.params.size()];
            for(int i = 0; i < funcCall.params.size(); i++) {
                real[i] = visit(funcCall.params.get(i));
            }

            ret = function.function.method.call(real);
        }

    
        leaveMemory();
    
        if(ret == null) {
            return new Singular(Values.NONE, Type.NONE);
        }
        
        return ret;
    }

    public MemoryValue visitReturn(Return ret) {
        return visit(ret.returnable);
    }

    public MemoryValue visitWhileLoop(WhileLoop whileLoop) {
        AST condition = whileLoop.condition;
        Compound statement = whileLoop.statement;
    
        String condValue = ((Singular) visit(condition)).value;
    
        MemoryValue returnValue = null;
    
        while(condValue == Values.TRUE) {
            enterNewMemory();
            returnValue = visit(statement);
    
            condValue = ((Singular) visit(condition)).value;
        } 
    
        return returnValue;
    }

    public MemoryValue visitForLoop(ForLoop forLoop) {
        enterNewMemory();

        AST init = forLoop.init;
        visit(init);

        Memory backup = null;

        Assign assign = forLoop.assign;
        Compound statement = forLoop.statement;
        AST condition = forLoop.condition;
    
        String condValue = ((Singular) visit(condition)).value;
    
        MemoryValue returnValue = null;
    
        while(condValue == Values.TRUE) {
            backup = memory;

            returnValue = visit(statement);

            memory = backup;
            visit(assign);
            condValue = ((Singular) visit(condition)).value;
        } 
    
        return returnValue;
    }

    public Singular visitCastValue(CastValue cast) {
        MemoryValue memoryVal = visit(cast.value);

        if(memoryVal instanceof Singular) {
            Singular memoryValue = (Singular) memoryVal;

            String value = memoryValue.value;
    
            switch(cast.type.type) {
                case CAST_FLOAT:
                {
                    int dots = 0;
    
                    for(char c : value.toCharArray()) {
                        if(!Character.isDigit(c) && c != '.') {
                            valueError(cast.type);
                        }
    
                        if(c == '.') {
                            dots++;
    
                            if(dots > 1) {
                                valueError(cast.type);
                            }
                        }
                    }
                    String newValue = Double.toString(Double.parseDouble(value));
                    return new Singular(newValue, Type.FLOAT);
                }
                case CAST_INT:
                {
                    int dots = 0;
    
                    for(char c : value.toCharArray()) {
                        if(!Character.isDigit(c) && c != '.') {
                            valueError(cast.type);
                        }
    
                        if(c == '.') {
                            dots++;
    
                            if(dots > 1) {
                                valueError(cast.type);
                            }
                        }
                    }
                    String newValue = Integer.toString(Integer.parseInt(value));
                    return new Singular(newValue, Type.FLOAT);
                }
                case CAST_STRING:
                {
                    return new Singular(value, Type.STRING);
                }
                case CAST_BOOL:
                {
                    if(value == Values.TRUE || value == Values.FALSE) {
                        return new Singular(value, Type.BOOLEAN);
                    }
    
                    valueError(cast.type);
                }
            }
        } else if(memoryVal instanceof Array) {
            Array array = (Array) memoryVal;

            switch(cast.type.type) {
                case CAST_STRING:
                {
                    return new Singular(array.toString(), Type.STRING);
                }
                case CAST_INT:
                {
                    int length = array.elements.size();
                    return new Singular(Integer.toString(length), Type.FLOAT);
                }
                case CAST_FLOAT:
                {
                    double length = array.elements.size();
                    return new Singular(Double.toString(length), Type.FLOAT);
                }
                case CAST_BOOL:
                {
                    int length = array.elements.size();
                    if(length > 0) {
                        return new Singular(Values.TRUE, Type.BOOLEAN);
                    }
                    return new Singular(Values.FALSE, Type.BOOLEAN);
                }
            }
        }
    
        valueError(cast.type);

        return null;
    }

    public void visitImport(Import im) {
        String name = im.name;
        String path = im.path;
    
        if(im.token.typeOf(TokenType.BUILT_IN_LIB)) {
            moduleManager.importModule(path, memory, name);
            
        } else {
            String newPath = path;

            if(!new File(path).isAbsolute()) {
                newPath = directory;
                if(!(newPath.endsWith("\\") || newPath.endsWith("/"))) {
                    newPath += '/';
                }
                newPath += path;
            }
        
            LangObject object = (LangObject) new Interpreter().evaluate(newPath);
        
            memory.put(name, object);
        }
    }

    public MemoryValue visitObjectDive(ObjectDive dive) {
        MemoryValue parent = visit(dive.parent);

        if(parent instanceof LangObject) {
            LangObject object = (LangObject) parent;
            Memory enclosingMemory = memory;
            memory = object.objectMemory;
    
            MemoryValue value = visit(dive.child);

            memory = enclosingMemory;
            return value;
        }
    
        new ValueError(dive.token, "Variable is not object type.").cast();

        return null;
    }

    private String getDirectoryFromPath(String path) {
        return new File(path).getParentFile().getAbsolutePath();
    }
    
    public MemoryValue evaluate(String path) {
        this.directory = getDirectoryFromPath(path);
    
        Lexer lexer = new Lexer(path);
        Parser parser = new Parser(lexer);
    
        AST tree = parser.parse();
        System.out.println(tree);
        analyzer.visit(tree);
        return visit(tree);
    }
}
