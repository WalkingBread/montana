package skorupinski.montana.interpreter;

import java.util.HashMap;

public class Memory {

    public final int memoryLevel;

    public final Memory enclosingMemoryBlock;

    public final HashMap<String, MemoryValue> values;

    public Memory(int memoryLevel, Memory enclosingMemoryBlock) {
        this.memoryLevel = memoryLevel;
        this.enclosingMemoryBlock = enclosingMemoryBlock;

        values = new HashMap<>();
    }

    public MemoryValue get(String name, boolean onlyThisBlock) {
        if(values.containsKey(name)) {
            return values.get(name);
        }
    
        if(enclosingMemoryBlock != null && !onlyThisBlock) {
            return enclosingMemoryBlock.get(name, false);
        }
    
        return null;
    }

    public void put(String name, MemoryValue value) {
        Memory scope = this;
    
        while(scope.get(name, true) == null) {
            if(scope.memoryLevel != 1) {
                scope = scope.enclosingMemoryBlock;
            } else {
                break;
            }
        }
    
        scope.values.put(name, value);
    }

    @Override
    public String toString() {
        String result = "Symbols: \n";
    
        for(String value : values.keySet()) {
            result += "Name: " + value + ", Value: " + values.get(value).toString();
            result += "\n";
        }
    
        return result;
    }
    
}
