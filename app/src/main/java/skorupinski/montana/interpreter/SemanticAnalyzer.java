package skorupinski.montana.interpreter;

import java.lang.reflect.Method;

import skorupinski.montana.lexer.Token;
import skorupinski.montana.parser.AST;
import skorupinski.montana.parser.AST.*;
import skorupinski.montana.utils.Error.*;

public class SemanticAnalyzer {
    
    private SymbolTable currentScope;

    public SemanticAnalyzer() {
        currentScope = null;
    }

    private void enterNewScope() {
        int scopeLevel = currentScope.scopeLevel + 1;
        currentScope = new SymbolTable(scopeLevel, currentScope);
    }
    
    private void leaveScope() {
        currentScope = currentScope.enclosingScope;
    }
    
    private void nameError(Token token, String message) {
        int line = token.line;
        int column = token.column;
        String filePath = token.file;
    
        new NameError(filePath, line, column, message).cast();
    }

    public void visit(AST node) {
        String className = node.getClass().getSimpleName();

        try {
            Method method = getClass().getMethod("visit" + className, node.getClass());
            method.invoke(this, node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void visitBinaryOperator(BinaryOperator op) {
        visit(op.left);
        visit(op.right);
    }
    
    public void visitUnaryOperator(UnaryOperator op) {
        visit(op.expr);
    }
    
    public void visitValue(Value val) {}
    
    public void visitCompare(Compare c) {
        for(AST node : c.comparables) {
            visit(node);
        }
    }
    
    public void visitCompound(Compound comp) {
        if(currentScope == null) {
            currentScope = new SymbolTable(1, null);
        }
    
        for(AST node : comp.children) {
            visit(node);
        }
    }
    
    public void visitAssign(Assign assign) {
        visit(assign.left);
        visit(assign.right);
    }
    
    public void visitVariable(Variable var) {
        String varName = var.variable.value;
        Symbol varSymbol = currentScope.lookup(varName, false);
    
        if(varSymbol == null) {
            nameError(var.variable, "Variable " + var.variable.value + " has not been declared.");
        }
    }
    
    public void visitNoOperator(NoOperator noOp) {}
    
    public void visitDoubleCondition(DoubleCondition cond) {
        visit(cond.left);
        visit(cond.right);
    }
    
    public void visitNegation(Negation neg) {
        visit(neg.statement);
    }
    
    public void visitVariableDeclaration(VariableDeclaration decl) {
        for(Variable var : decl.variables) {
            String name = var.variable.value;
    
            if(currentScope.lookup(name, true) != null) {
                nameError(var.variable, "Variable "  + name + " has already been declared.");
            }
    
            Symbol symbol = new Symbol(name);
    
            currentScope.define(symbol);
        }
    }
    
    public void visitIfCondition(IfCondition cond) {
        visit(cond.condition);
    
        enterNewScope();
        visit(cond.statement);
        leaveScope();
    }
    
    public void visitPrint(Print print) {
        visit(print.printable);
    }
    
    public void visitArrayInit(ArrayInit arrayInit) {
        for(AST node : arrayInit.elements) {
            visit(node);
        }
    }
    
    public void visitArrayAccess(ArrayAccess access) {
        visit(access.array);
        visit(access.index);
    }
    
    public void visitFunctionInit(FunctionInit functionInit) {
        Symbol functionSymbol = new Symbol(functionInit.functionName);
        currentScope.define(functionSymbol);
    
        enterNewScope();
    
        visit(functionInit.params);
        visit(functionInit.block);
    
        leaveScope();
    }
    
    public void visitFunctionCall(FunctionCall funcCall) {
        visit(funcCall.function);
        for(AST param : funcCall.params) {
            visit(param);
        }
    }
    
    public void visitReturn(Return ret) {
        visit(ret.returnable);
    }
    
    public void visitWhileLoop(WhileLoop whileLoop) {
        visit(whileLoop.condition);
    
        enterNewScope();
        visit(whileLoop.statement);
        leaveScope();
    }

    public void visitForLoop(ForLoop forLoop) {
        visit(forLoop.init);
        visit(forLoop.condition);
        visit(forLoop.assign);

        enterNewScope();
        visit(forLoop.statement);
        leaveScope();
    }
    
    public void visitCastValue(CastValue cast) {
        visit(cast.value);
    }
    
    public void visitImport(Import im) {
        Symbol importName = new Symbol(im.name);
        currentScope.define(importName);
    }
    
    public void visitObjectDive(ObjectDive dive) {
        visit(dive.parent);
        //visit(dive.child);
    }
}
