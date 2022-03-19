package skorupinski.montana.parser;

import java.util.ArrayList;
import java.util.List;

import skorupinski.montana.lexer.Token;

public abstract class AST {

    public final Token token;
    
    protected AST(Token token) {
        this.token = token;
    }

    protected abstract String tree(int level);

    protected String branch(int level) {
        String branch = "";
        for(int i = 0; i < level; i++) {
            branch += "-";
        }  

        branch += getClass().getSimpleName() + "\n";
        return branch;
    }
    
    @Override
    public String toString() {
        return tree(0) + '\n';
    }

    public static class Value extends AST {

        public final Token value;

        public Value(Token value) {
            super(value);
            this.value = value;
        }

        @Override
        protected String tree(int level) {
            return branch(level);
        }

    }

    public static class BinaryOperator extends AST {

        public final AST left;

        public final AST right;

        public final Token op;

        public BinaryOperator(AST left, Token op, AST right) {
            super(op);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + left.tree(level + 1) + right.tree(level + 1);
        }

    }

    public static class UnaryOperator extends AST {

        public final Token op;

        public final AST expr;

        public UnaryOperator(Token op, AST expr) {
            super(op);
            this.op = op;
            this.expr = expr;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + expr.tree(level + 1);
        }
    }

    public static class Compound extends AST {

        public final List<AST> children;

        public final boolean insideFunction;

        public Compound(boolean insideFunction, List<AST> children) {
            super(null);
            this.insideFunction = true;
            this.children = children;
        }

        @Override
        protected String tree(int level) {
            String tree = branch(level);
            for(AST ast : children) {
                tree += ast.tree(level + 1);
            }
            return tree;
        }
    }

    public static class Variable extends AST {

        public final Token variable;

        protected Variable(Token variable) {
            super(variable);
            this.variable = variable;
        }

        @Override
        protected String tree(int level) {
            return branch(level);
        }

    }

    public static class Assign extends AST {

        public final AST left;

        public final AST right;

        public final Token op;

        public Assign(AST left, Token op, AST right) {
            super(op);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + left.tree(level + 1) + right.tree(level + 1);
        }
    }

    public static class VariableDeclaration extends AST {

        public final List<Variable> variables;
        
        public final List<Assign> assignments;

        public VariableDeclaration(List<Variable> variables) {
            super(null);
            this.variables = variables;
            assignments = new ArrayList<>();
        }

        public VariableDeclaration() {
            super(null);
            this.variables = new ArrayList<>();
            assignments = new ArrayList<>();
        }

        @Override
        protected String tree(int level) {
            String tree = branch(level);
            for(AST ast : variables) {
                tree += ast.tree(level + 1);
            }

            for(AST ast : assignments) {
                tree += ast.tree(level + 1);
            }
            return tree;
        }
    }

    public static class NoOperator extends AST {

        public NoOperator() {
            super(null);
        }

        @Override
        protected String tree(int level) {
            return branch(level);
        }
    }

    public static class Compare extends AST {

        public final List<AST> comparables;

        public final List<Token> operators;

        protected Compare(List<AST> comparables, List<Token> operators) {
            super(null);
            this.comparables = comparables;
            this.operators = operators;
        }

        protected String tree(int level) {
            String tree = branch(level);
            for(AST ast : comparables) {
                tree += ast.tree(level + 1);
            }

            return tree;
        }
    }

    public static class Negation extends AST {

        public final AST statement;

        public final Token op;

        protected Negation(Token op, AST statement) {
            super(op);
            this.op = op;
            this.statement = statement;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + statement.tree(level + 1);
        }
        
    }

    public static class DoubleCondition extends AST {

        public final AST left;

        public final AST right;

        public final Token op;

        public DoubleCondition(AST left, Token op, AST right) {
            super(op);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + left.tree(level + 1) + right.tree(level + 1);
        }

    }

    public static class IfCondition extends AST {

        public final AST condition;

        public final Compound statement;

        public final List<IfCondition> elses;

        public IfCondition(AST condition, Compound statement) {
            super(null);
            this.condition = condition;
            this.statement = statement;

            elses = new ArrayList<>();
        }

        @Override
        protected String tree(int level) {
            String tree = branch(level);
            tree += condition.tree(level + 1);
            tree += statement.tree(level + 1);
            for(AST ast : elses) {
                tree += ast.tree(level + 1);
            }

            return tree;
        }
    }

    public static class Print extends AST {
        
        public final AST printable;

        public Print(AST printable) {
            super(null);
            this.printable = printable;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + printable.tree(level + 1);
        }
    }

    public static class ArrayInit extends AST {

        public final List<AST> elements;

        protected ArrayInit(List<AST> elements) {
            super(null);
            this.elements = elements;
        }

        @Override
        protected String tree(int level) {
            String tree = branch(level);
            for(AST ast : elements) {
                tree += ast.tree(level + 1);
            }

            return tree;
        }
        
    }

    public static class ArrayAccess extends AST {

        public final AST array;

        public final AST index;

        public ArrayAccess(AST array, AST index) {
            super(null);
            this.index = index;
            this.array = array;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + array.tree(level + 1) + index.tree(level + 1);
        }
    }

    public static class FunctionInit extends AST {

        public final String functionName;

        public final VariableDeclaration params;

        public final Compound block;

        protected FunctionInit(String functionName, VariableDeclaration params, Compound block) {
            super(null);
            this.functionName = functionName;
            this.params = params;
            this.block = block;
        }
        
        @Override
        protected String tree(int level) {
            return branch(level) + params.tree(level + 1) + block.tree(level + 1);
        }
    }

    public static class FunctionCall extends AST {

        public final AST function;

        public final List<AST> params;

        protected FunctionCall(AST function, List<AST> params) {
            super(null);
            this.function = function;
            this.params = params;
        }
        
        @Override
        protected String tree(int level) {
            String tree = branch(level) + function.tree(level + 1);
            for(AST ast : params) {
                tree += ast.tree(level + 1);
            }

            return tree;
        }
    }

    public static class Return extends AST {

        public final AST returnable;

        protected Return(Token token, AST returnable) {
            super(token);
            this.returnable = returnable;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + returnable.tree(level + 1);
        }
        
    }

    public static class WhileLoop extends AST {

        public final AST condition;

        public final Compound statement;

        public WhileLoop(AST condition, Compound statement) {
            super(null);
            this.condition = condition;
            this.statement = statement;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + condition.tree(level + 1) + statement.tree(level + 1);
        }

    }

    public static class ForLoop extends AST {

        public final AST init;

        public final AST condition;

        public final Assign assign;

        public final Compound statement;

        protected ForLoop(AST init, AST condition, Assign assign, Compound statement) {
            super(null);
            this.init = init;
            this.condition = condition;
            this.assign = assign;
            this.statement = statement;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + init.tree(level + 1) + condition.tree(level + 1) + assign.tree(level + 1) + statement.tree(level + 1);
        }
        
    }

    public static class ClassInit extends AST {

        public final String name;

        public final Compound block;

        public ClassInit(String name, Compound block) {
            super(null);
            this.name = name;
            this.block = block;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + block.tree(level + 1);
        }

    }

    public static class CastValue extends AST {

        public final AST value;

        public final Token type;

        public CastValue(AST value, Token type) {
            super(type);
            this.value = value;
            this.type = type;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + value.tree(level + 1);
        }
    }

    public static class Import extends AST {

        public final String path;

        public final String name;

        public Import(Token path, String name) {
            super(path);
            this.path = path.value;
            this.name = name;
        }

        @Override
        protected String tree(int level) {
            return branch(level);
        }
    }

    public static class ObjectDive extends AST {

        public final AST parent;

        public final AST child;

        public ObjectDive(AST parent, Token colon, AST child) {
            super(colon);
            this.parent = parent;
            this.child = child;
        }

        @Override
        protected String tree(int level) {
            return branch(level) + parent.tree(level + 1) + child.tree(level + 1);
        }
    }
}
