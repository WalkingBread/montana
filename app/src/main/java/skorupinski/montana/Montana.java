package skorupinski.montana;

import skorupinski.montana.interpreter.Interpreter;

public class Montana {
    
    public static void main(String[] args) {
        Interpreter i = new Interpreter();
        i.evaluate("C:\\Users\\matis\\Desktop\\xx.txt");

        System.out.println(i.memory);
    }
}
