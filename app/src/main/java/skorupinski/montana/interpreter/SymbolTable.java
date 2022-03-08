package skorupinski.montana.interpreter;

import java.util.HashMap;

public class SymbolTable {
    
    public final int scopeLevel;

    public final SymbolTable enclosingScope;

    public final HashMap<String, Symbol> symbols;

    public SymbolTable(int scopeLevel, SymbolTable enclosingScope) {
        this.scopeLevel = scopeLevel;
        this.enclosingScope = enclosingScope;

        symbols = new HashMap<>();
    }

    public void define(Symbol symbol) {
        symbols.put(symbol.name, symbol);
    }

    public Symbol lookup(String name, boolean onlyThisScope) {
        if(symbols.containsKey(name)) {
            return symbols.get(name);
        }
    
        if(!onlyThisScope && enclosingScope != null) {
            return enclosingScope.lookup(name, false);
        }
    
        return null;
    }

    @Override
    public String toString() {
        String result = "Symbols: \n";
    
        for(Symbol symbol : symbols.values()) {
            result += symbol.name;
            result += "\n";
        }
    
        return result;
    }
}
