package skorupinski.montana.lib.modules;

import javax.swing.JFrame;

public class Test {


    public void fuck() {
        System.out.println("fuck");
    }

    public void createWindow() {
        JFrame frame = new JFrame();
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
