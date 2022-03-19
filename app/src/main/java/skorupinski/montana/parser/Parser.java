package skorupinski.montana.parser;

import java.util.ArrayList;
import java.util.List;

import skorupinski.montana.lexer.Lexer;
import skorupinski.montana.lexer.Token;
import skorupinski.montana.lexer.TokenType;
import skorupinski.montana.parser.AST.*;
import skorupinski.montana.utils.Values;
import skorupinski.montana.utils.Error.SyntaxError;

public class Parser {
    
    private Lexer lexer;

    private Token currentToken;

    private boolean insideFunction;

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        currentToken = lexer.nextToken();

        insideFunction = false;
    }

    private void error(Token token) {
        String message = "Unexpected token: " + token.value;
        new SyntaxError(token.file, token.line, token.column, message).cast();
    }

    private void eat(TokenType type) {
        if(currentToken.type == type) {
            currentToken = lexer.nextToken();
        } else {
            error(currentToken);
        }
    }

    private AST expr() {
        AST node = eqNotEq();
    
        while(currentToken.typeOf(TokenType.AND) || currentToken.typeOf(TokenType.OR)) {
            Token op = currentToken;
            eat(currentToken.type);
    
            node = new DoubleCondition(node, op, expr());
        }
    
        return node;
    }

    private AST eqNotEq() {
        AST node = subAdd();
    
        if(currentToken.typeOf(TokenType.EQUALS) || 
           currentToken.typeOf(TokenType.NOT_EQUALS) ||
           currentToken.typeOf(TokenType.MORE_OR_EQ) ||
           currentToken.typeOf(TokenType.LESS_OR_EQ) ||
           currentToken.typeOf(TokenType.LESS) ||
           currentToken.typeOf(TokenType.MORE) 
        ) {
            List<AST> comparables = new ArrayList<>();
            comparables.add(node);
            List<Token> operators = new ArrayList<>();
    
            while(currentToken.typeOf(TokenType.EQUALS) || 
                  currentToken.typeOf(TokenType.NOT_EQUALS) ||
                  currentToken.typeOf(TokenType.MORE_OR_EQ) ||
                  currentToken.typeOf(TokenType.LESS_OR_EQ) ||
                  currentToken.typeOf(TokenType.LESS) ||
                  currentToken.typeOf(TokenType.MORE) 
            ) {
                Token op = currentToken;
                eat(currentToken.type);
    
                comparables.add(subAdd());
                operators.add(op);
            }
            node = new Compare(comparables, operators);
        }
        return node;
    }

    private AST subAdd() {
        AST node = term();
    
        while(currentToken.typeOf(TokenType.PLUS) || currentToken.typeOf(TokenType.MINUS)) {
            Token token = currentToken;
            eat(currentToken.type);
            node = new BinaryOperator(node, token, term());
        }
    
        return node;
    }

    private AST term() {
        AST node = cast();
    
        while(currentToken.typeOf(TokenType.MULT) || 
              currentToken.typeOf(TokenType.DIV) || 
              currentToken.typeOf(TokenType.INT_DIV) ||
              currentToken.typeOf(TokenType.MODULO)
        ) {
            Token token = currentToken;
            eat(currentToken.type);
            node = new BinaryOperator(node, token, cast());
        }
    
        return node;
    }

    private AST cast() {
        AST node = factor();
    
        while(currentToken.typeOf(TokenType.AS)) {
            eat(TokenType.AS);
    
            if(currentToken.typeOf(TokenType.CAST_INT) ||
               currentToken.typeOf(TokenType.CAST_STRING) ||
               currentToken.typeOf(TokenType.CAST_FLOAT) ||
               currentToken.typeOf(TokenType.CAST_BOOL)
            ) {
                Token type = currentToken;
                eat(currentToken.type);
    
                return new CastValue(node, type);
            }
    
            error(currentToken);
        }
    
        return node;
    }

    private AST factor() {
        Token token = currentToken;
    
        switch(token.type) {
            case PLUS:
            {
                eat(TokenType.PLUS);
                AST expr = factor();
                return new UnaryOperator(token, expr);
            }
            
            case MINUS:
            {
                eat(TokenType.MINUS);
                return new UnaryOperator(token, factor());
            }
    
            case FLOAT:
            {
                eat(TokenType.FLOAT);
                return new Value(token);
            }
    
            case NOT:
            {
                eat(TokenType.NOT);
                return new Negation(token, factor());
            }
    
            case STRING:
            {
                eat(TokenType.STRING);
                return new Value(token);
            }
    
            case BOOLEAN:
            {
                eat(TokenType.BOOLEAN);
                return new Value(token);
            }
            case NONE:
            {
                eat(TokenType.NONE);
                return new Value(token);
            }
            case L_PAREN:
            {
                eat(TokenType.L_PAREN);
                AST node = expr();
                eat(TokenType.R_PAREN);
                return node;
            }
            case L_SQUARED:
            {
                AST node = arrayInit();
                if(currentToken.typeOf(TokenType.L_SQUARED)) {
                    node = arrayAccess(node);
                }
                return node;
            }
            
            default:
                return specialOperations();
        }
        
    }

    private AST specialOperations() {
        AST node = variable();
        if(currentToken.typeOf(TokenType.L_SQUARED)) {
            node = arrayAccess(node);

        } else if(currentToken.typeOf(TokenType.L_PAREN)) {
            node = functionCall(node);

        } else if(currentToken.typeOf(TokenType.COLON)) {
            node = objectDive(node);
        }
        return node;
    }

    private List<AST> statementList() {
        List<AST> nodes = new ArrayList<>();

        AST node = statement();
        nodes.add(node);

        while(!(node instanceof NoOperator)) {
            node = statement();
            nodes.add(node);
        }
    
        return nodes;
    }

    private AST statement() {
        AST node;
    
        switch(currentToken.type) {
            case VARIABLE_DECL:
            {
                eat(TokenType.VARIABLE_DECL);
                node = variableDeclaration();
                eat(TokenType.SEMICOLON);
                break;
            }
            case IDENTIFIER:
                node = identifierStatement();
                eat(TokenType.SEMICOLON);
                break;
    
            case IF:
            {
                IfCondition cond = ifStatement();
                while(currentToken.typeOf(TokenType.ELSE)) {
                    cond.elses.add(elseStatement());
                }
    
                node = cond;
                break;
            }
            
            case WHILE:
                node = whileLoopStatement();
                break;

            case FOR:
                node = forLoopStatement();
                break;
    
            case PRINT:
                node = printStatement();
                eat(TokenType.SEMICOLON);
                break;
    
            case FUNCTION:
                node = functionInitStatement();
                break;
    
            case RETURN:
                node = returnStatement();
                eat(TokenType.SEMICOLON);
                break;
    
            case IMPORT:
                node = importStatement();
                eat(TokenType.SEMICOLON);
                break;
    
            default:
                node = empty();
        }
    
        return node;
    }

    private Compound compoundStatement() {
        eat(TokenType.L_CURLY);
        List<AST> nodes = statementList();
        eat(TokenType.R_CURLY);
    
        Compound root = new Compound(insideFunction, nodes);

        return root;
    }
    
    private Variable variable() {
        Variable node = new Variable(currentToken);
        eat(TokenType.IDENTIFIER);
    
        return node;
    }
    
    private VariableDeclaration standardVariableDeclaration() {
        Variable var = new Variable(currentToken);
        eat(TokenType.IDENTIFIER);
    
        List<Variable> variables = new ArrayList<>();
        variables.add(var);
    
        while(currentToken.typeOf(TokenType.COMMA)) {
            eat(TokenType.COMMA);
            var = variable();
            variables.add(var);
        }
    
        return new VariableDeclaration(variables);
    }
    
    private VariableDeclaration variableDeclaration() {
        VariableDeclaration variableDeclaration = standardVariableDeclaration();
    
        if(currentToken.typeOf(TokenType.ASSIGN)) {
            eat(TokenType.ASSIGN);
    
            Variable left = variableDeclaration.variables.get(0);
            AST right = expr();
            Assign assignment = new Assign(left, new Token(TokenType.ASSIGN, "="), right);
    
            variableDeclaration.assignments.add(assignment);
    
            int i = 1;
            while(currentToken.typeOf(TokenType.COMMA)) {
                eat(TokenType.COMMA);
                if(i > variableDeclaration.variables.size()) {
                    error(currentToken);
                }
    
                left = variableDeclaration.variables.get(i);
                right = expr();
                assignment = new Assign(left, new Token(TokenType.ASSIGN, "="), right);
    
                variableDeclaration.assignments.add(assignment);
                i++;
            }
        }
        return variableDeclaration;
    }
    
    private NoOperator empty() {
        return new NoOperator();
    }
    
    private ObjectDive objectDive(AST parent) {
        Token colon = currentToken;
        eat(TokenType.COLON);
        AST child = specialOperations();
    
        ObjectDive dive = new ObjectDive(parent, colon, child);
    
        if(currentToken.typeOf(TokenType.COLON)) {
            dive = objectDive(dive);
        }
    
        return dive;
    }
    
    private AST identifierStatement() {
        AST left = variable();
        Token token = currentToken;
    
        if(token.typeOf(TokenType.L_PAREN)) {
            return functionCall(left);
        }
    
        if(token.typeOf(TokenType.L_SQUARED)) {
            left = arrayAccess(left);
        }
    
        if(token.typeOf(TokenType.COLON)) {
            return objectDive(left);
        }
    
        eat(TokenType.ASSIGN);
        AST right = expr();
    
        return new Assign(left, token, right);
    }
    
    private IfCondition ifStatement() {
        eat(TokenType.IF);
        eat(TokenType.L_PAREN);
        AST condition = expr();
        eat(TokenType.R_PAREN);
    
        Compound statement = compoundStatement();
        return new IfCondition(condition, statement);
    }
    
    private IfCondition elseStatement() {
        eat(TokenType.ELSE);
        if(currentToken.typeOf(TokenType.IF)) {
            return ifStatement();
    
        } else if(currentToken.typeOf(TokenType.L_CURLY)) {
            AST condition = new Value(new Token(TokenType.BOOLEAN, Values.TRUE));
    
            return new IfCondition(condition, compoundStatement());
        }
        return null;
    }

    private Assign assignStatement() {
        AST left = variable();
        Token token = currentToken;
        eat(TokenType.ASSIGN);
        AST right = expr();
        return new Assign(left, token, right);
    }
    
    private WhileLoop whileLoopStatement() {
        eat(TokenType.WHILE);
        eat(TokenType.L_PAREN);
        AST condition = expr();
        eat(TokenType.R_PAREN);
    
        Compound statement = compoundStatement();
        return new WhileLoop(condition, statement);
    };

    private ForLoop forLoopStatement() {
        eat(TokenType.FOR);
        eat(TokenType.L_PAREN);

        AST init = null;

        System.out.println(currentToken);

        if(currentToken.typeOf(TokenType.VARIABLE_DECL)) {
            eat(TokenType.VARIABLE_DECL);
            init = variableDeclaration();

        } else {
            init = assignStatement();
        }

        eat(TokenType.SEMICOLON);

        AST condition = expr();
        eat(TokenType.SEMICOLON);

        Assign assign = assignStatement();

        eat(TokenType.R_PAREN);

        Compound statement = compoundStatement();

        return new ForLoop(init, condition, assign, statement);
    }
    
    private Print printStatement() {
        eat(TokenType.PRINT);
        AST printable = expr();
    
        return new Print(printable);
    }
    
    private FunctionInit functionInitStatement() {
        eat(TokenType.FUNCTION);
        String funcName = currentToken.value;
        eat(TokenType.IDENTIFIER);
        eat(TokenType.L_PAREN);
    
        VariableDeclaration params = new VariableDeclaration();
    
        if(currentToken.typeOf(TokenType.IDENTIFIER)) {
            params = standardVariableDeclaration();
        }
    
        eat(TokenType.R_PAREN);
    
        Compound block;
    
        if(insideFunction) {
            block = compoundStatement();
        } else {
            insideFunction = true;
            block = compoundStatement();
            insideFunction = false;
        }
    
        return new FunctionInit(funcName, params, block);
    }
    
    private FunctionCall functionCall(AST function) {
        eat(TokenType.L_PAREN);
    
        List<AST> params = collection(TokenType.R_PAREN);
        FunctionCall functionCall = new FunctionCall(function, params);
    
        while(currentToken.typeOf(TokenType.L_PAREN)) {
            eat(TokenType.L_PAREN);
    
            params = collection(TokenType.R_PAREN);
            functionCall = new FunctionCall(functionCall, params);
        }
    
        return functionCall;
    }
    
    private Return returnStatement() {
        Token token = currentToken;
        eat(TokenType.RETURN);
        AST returnable = expr();
    
        return new Return(token, returnable);
    }
    
    private Import importStatement() {
        eat(TokenType.IMPORT);
        Token path = currentToken;
    
        if(currentToken.typeOf(TokenType.STRING) || 
           currentToken.typeOf(TokenType.BUILT_IN_LIB)) 
        {
            eat(currentToken.type);
        } else {
            error(path);
        }
    
        eat(TokenType.AS);
        String name = currentToken.value;
        eat(TokenType.IDENTIFIER);
    
        return new Import(path, name);
    }
    
    private ArrayAccess arrayAccess(AST array) {
        eat(TokenType.L_SQUARED);
        AST index = expr();
        eat(TokenType.R_SQUARED);
    
        ArrayAccess access = new ArrayAccess(array, index);
    
        while(currentToken.typeOf(TokenType.L_SQUARED)) {
            eat(TokenType.L_SQUARED);
            index = expr();
            eat(TokenType.R_SQUARED);
            access = new ArrayAccess(access, index);
        }
    
        return access;
    }
    
    private ArrayInit arrayInit() {
        eat(TokenType.L_SQUARED);
        List<AST> elements = collection(TokenType.R_SQUARED);
    
        return new ArrayInit(elements);
    }


    private List<AST> collection(TokenType ending) {
        List<AST> collection = new ArrayList<>();
        if(!currentToken.typeOf(ending)) {
            AST element = expr();
            collection.add(element);
    
            while(currentToken.typeOf(TokenType.COMMA)) {
                eat(TokenType.COMMA);
    
                element = expr();
                collection.add(element);
            }
        }
        eat(ending);
    
        return collection;
    }

    public AST parse() {
        Compound program = new Compound(insideFunction, statementList());
    
        if(!currentToken.typeOf(TokenType.END_OF_FILE)) {
            error(currentToken);
        }
    
        return program;
    }
}
