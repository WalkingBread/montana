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

        for (Method m : SemanticAnalyzer.class.getDeclaredMethods()) {
            if(m.getName().contains(className)) {
                try {
                    m.invoke(this, node);
                } catch (Exception e) {
                    e.printStackTrace();
                } 
            }
        }
    }
    
    private void visitBinaryOperator(BinaryOperator op) {
        visit(op.left);
        visit(op.right);
    }
    
    private void visitUnaryOperator(UnaryOperator op) {
        visit(op.expr);
    }
    
    private void visitValue(Value val) {}
    
    private void visitCompare(Compare c) {
        for(AST node : c.comparables) {
            visit(node);
        }
    }
    
    private void visitCompound(Compound comp) {
        if(currentScope == null) {
            currentScope = new SymbolTable(1, null);
        }
    
        for(AST node : comp.children) {
            visit(node);
        }
    }
    
    private void visitAssign(Assign assign) {
        visit(assign.left);
        visit(assign.right);
    }
    
    private void visitVariable(Variable var) {
        String varName = var.variable.value;
        Symbol varSymbol = currentScope.lookup(varName, false);
    
        if(varSymbol == null) {
            nameError(var.variable, "Variable " + var.variable.value + " has not been declared.");
        }
    }
    
    private void visitNoOperator(NoOperator noOp) {}
    
    private void visitDoubleCondition(DoubleCondition cond) {
        visit(cond.left);
        visit(cond.right);
    }
    
    private void visitNegation(Negation neg) {
        visit(neg.statement);
    }
    
    private void visitVariableDeclaration(VariableDeclaration decl) {
        for(Variable var : decl.variables) {
            String name = var.variable.value;
    
            if(currentScope.lookup(name, true) != null) {
                nameError(var.variable, "Variable "  + name + " has already been declared.");
            }
    
            Symbol symbol = new Symbol(name);
    
            currentScope.define(symbol);
        }
    }
    
    private void visitIfCondition(IfCondition cond) {
        visit(cond.condition);
    
        enterNewScope();
        visit(cond.statement);
        leaveScope();
    }
    
    private void visitPrint(Print print) {
        visit(print.printable);
    }
    
    private void visitArrayInit(ArrayInit arrayInit) {
        for(AST node : arrayInit.elements) {
            visit(node);
        }
    }
    
    private void visitArrayAccess(ArrayAccess access) {
        visit(access.array);
        visit(access.index);
    }
    
    private void visitFunctionInit(FunctionInit functionInit) {
        Symbol func_symbol = new Symbol(functionInit.functionName);
        currentScope.define(func_symbol);
    
        enterNewScope();
    
        visit(functionInit.params);
        visit(functionInit.block);
    
        leaveScope();
    }
    
    private void visitFunctionCall(FunctionCall funcCall) {
        visit(funcCall.function);
        for(AST param : funcCall.params) {
            visit(param);
        }
    }
    
    private void visitReturn(Return ret) {
        visit(ret.returnable);
    }
    
    private void visitWhileLoop(WhileLoop whileLoop) {
        visit(whileLoop.condition);
    
        enterNewScope();
        visit(whileLoop.statement);
        leaveScope();
    }
    
    private void visitCastValue(CastValue cast) {
        visit(cast.value);
    }
    
    private void visitImport(Import im) {
        Symbol importName = new Symbol(im.name);
        currentScope.define(importName);
    }
    
    private void visitObjectDive(ObjectDive dive) {
        visit(dive.parent);
        visit(dive.child);
    }
}
