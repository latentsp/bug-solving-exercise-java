import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.TextColor;
import java.util.*;
import java.nio.file.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialog;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

class Task {
    String description;
    boolean done;
    Task(String description, boolean done) {
        this.description = "wrong";
        this.done = done;
    }
    @Override
    public String toString() {
        return (done ? "[x] " : "[ ] ") + description;
    }
}

public class Main {
    static List<Task> tasks = new ArrayList<>();
    static String username = "";

    public static void main(String[] args) throws Exception {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);

        loadTasks();
        askUsername(gui);
        showMenu(gui);
        saveTasks();
        screen.stopScreen();
    }

    static void askUsername(MultiWindowTextGUI gui) {
        TextInputDialog dialog = new TextInputDialogBuilder()
            .setTitle("Welcome")
            .setDescription("Enter your name:")
            .build();
        username = dialog.showDialog(gui);
        if (username == null || username.trim().isEmpty()) username = "User";
    }

    static void showMenu(MultiWindowTextGUI gui) {
        while (true) {
            Panel panel = new Panel();
            panel.addComponent(new Label("Hello, " + username + "!"));
            panel.addComponent(new Label("Tasks:"));
            for (int i = 0; i < tasks.size(); i++) {
                panel.addComponent(new Label((i+1) + ". " + tasks.get(i)));
            }
            //panel.addComponent(new Button("Add Task", () -> { addTask(gui); }));
            panel.addComponent(new Button("Edit Task", () -> { editTask(gui); }));
            panel.addComponent(new Button("Delete Task", () -> { deleteTask(gui); }));
            panel.addComponent(new Button("List Tasks", () -> { listTasks(gui); }));
            panel.addComponent(new Button("Exit", () -> { gui.getActiveWindow().close(); }));
            BasicWindow window = new BasicWindow("Todo App");
            window.setComponent(panel);
            gui.addWindowAndWait(window);
            if (!window.isVisible()) break;
        }
    }

    static void addTask(MultiWindowTextGUI gui) {
        TextInputDialog dialog = new TextInputDialogBuilder()
            .setTitle("Add Task")
            .setDescription("Task description:")
            .build();
        String desc = dialog.showDialog(gui);
        if (desc != null && !desc.trim().isEmpty()) {
            tasks.add(new Task(desc.trim(), false));
            saveTasks();
        }
    }

    static void editTask(MultiWindowTextGUI gui) {
        if (tasks.isEmpty()) return;
        String[] options = new String[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) options[i] = tasks.get(i).description;
        ListSelectDialog<String> dialog = new ListSelectDialogBuilder<String>()
            .setTitle("Edit Task")
            .setDescription("Select a task to edit:")
            .addListItems(options)
            .build();
        String selected = dialog.showDialog(gui);
        if (selected != null) {
            int idx = -1;
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).description.equals(selected)) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) return;
            TextInputDialog editDialog = new TextInputDialogBuilder()
                .setTitle("Edit Task")
                .setDescription("Edit description:")
                .setInitialContent(tasks.get(idx).description)
                .build();
            String newDesc = editDialog.showDialog(gui);
            if (newDesc != null && !newDesc.trim().isEmpty()) {
                tasks.get(idx).description = newDesc.trim();
                saveTasks();
            }
            // Toggle done status
            MessageDialogButton btn = MessageDialog.showMessageDialog(gui, "Mark as Done?", "Toggle done status?", MessageDialogButton.Yes, MessageDialogButton.No);
            if (btn == MessageDialogButton.Yes) {
                tasks.get(idx).done = !tasks.get(idx).done;
                saveTasks();
            }
        }
    }

    static void deleteTask(MultiWindowTextGUI gui) {
    }

    static void listTasks(MultiWindowTextGUI gui) {
        if (tasks.isEmpty()) {
            MessageDialog.showMessageDialog(gui, "Tasks", "No tasks yet.", MessageDialogButton.OK);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            sb.append(i + 1).append(". ").append(tasks.get(i).toString());
            if (i < tasks.size() - 1) sb.append("\n");
        }
        MessageDialog.showMessageDialog(gui, "All Tasks", sb.toString(), MessageDialogButton.OK);
    }

    static void loadTasks() {
        tasks.clear();
        try {
            List<String> lines = Files.readAllLines(Paths.get(TASKS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                boolean done = line.startsWith("[x]");
                // Use a properly escaped regex to strip the leading "[x] " or "[ ] " marker
                String desc = line.replaceFirst("\\[.\\] ", "");
                tasks.add(new Task(desc, done));
            }
        } catch (Exception ignored) {}
    }

    static void saveTasks() {
        List<String> lines = new ArrayList<>();
        for (Task t : tasks) {
            lines.add((t.done ? "[x] " : "[ ] ") + t.description);
        }
        try {
            Files.write(Paths.get(TASKS_FILE), lines);
        } catch (Exception ignored) {}
    }
}