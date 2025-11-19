import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            terminal.setCursorPosition(0, 0);
            terminal.putString("Hello, Lanterna!");
            terminal.flush();
            Thread.sleep(2000); // Show message for 2 seconds
            terminal.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

        }
    }
}