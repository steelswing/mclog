/*
 * Ну вы же понимаете, что код здесь только мой?
 * Well, you do understand that the code here is only mine?
 */

package net.steelswing.mclog;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.console.ConsoleReader;
import net.steelswing.mclog.util.QueueLogAppender;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Erase;

/**
 *
 * @author MrJavaCoder
 */
public class TerminalConsoleWriterThread extends Thread {

    private final ConsoleReader reader;
    private final OutputStream output;

    public TerminalConsoleWriterThread(OutputStream output, ConsoleReader reader) {
        super("TerminalConsoleWriter");
        this.output = output;
        this.reader = reader;

        this.setDaemon(true);
    }

    @Override
    public void run() {
        String message;

        // Using name from log4j config in vanilla jar
        while (true) {
            message = QueueLogAppender.getNextLogEvent("TerminalConsole");
            if (message == null) {
                continue;
            }
            
            if(!ServerConsole.useJLine) {
                message = ChatColor.clearColor(message);
            }

            try {
                if (ServerConsole.useJLine) {
                    message = colorize(message);

                    reader.print(Ansi.ansi().eraseLine(Erase.ALL).toString() + ConsoleReader.RESET_LINE);
                    reader.flush();
                    output.write(message.getBytes());
                    output.write(Ansi.ansi().reset().toString().getBytes());
                    output.flush();

                    try {
                        reader.drawLine();
                    } catch (Throwable ex) {
                        reader.getCursorBuffer().clear();
                    }
                    reader.flush();
                } else {
                    output.write(message.getBytes());
                    output.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(TerminalConsoleWriterThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String colorize(String in) {
        return ColorWriter.replace(in.replace("&", "§"));
    }
}
