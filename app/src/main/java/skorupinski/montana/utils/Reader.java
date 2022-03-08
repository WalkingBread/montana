package skorupinski.montana.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
    
    public static String readAndSeparateLines(String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        String content = "";
        String line;

        while((line = br.readLine()) != null) {
            content += line + "\n";
        }
        
        return content;
    }
}
