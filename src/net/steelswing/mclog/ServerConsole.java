/*
 * Ну вы же понимаете, что код здесь только мой?
 * Well, you do understand that the code here is only mine?
 */

package net.steelswing.mclog;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import jline.console.ConsoleReader;
import net.steelswing.mclog.util.DefaultUncaughtExceptionHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.AnsiConsole;

/**
 *
 * @author MrJavaCoder
 */
public class ServerConsole {

    private static final Logger log = LogManager.getLogger();
    private List<InputListener> inputListeners = new ArrayList<>();

    protected boolean running = true;
    protected ConsoleReader reader;
    public static boolean useJLine = true;

    public ServerConsole() throws Exception {
        if (System.console() == null && System.getProperty("jline.terminal") == null) {
            System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
            useJLine = false;
        }

        AnsiConsole.systemInstall();

        try {
            reader = new ConsoleReader(System.in, System.out);
            reader.setExpandEvents(false); // Avoid parsing exceptions for uncommonly used event designators
        } catch (IOException e) {
            try {
                // Try again with useJLine disabled for Windows users without C++ 2008 Redistributable
                System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
                System.setProperty("user.language", "en");
                useJLine = false;
                reader = new ConsoleReader(System.in, System.out);
                reader.setExpandEvents(false);
            } catch (IOException ex) {
                log.warn((String) null, ex);
            }
        }
        
         

        Thread thread = new Thread("Server console handler") {
            @Override
            public void run() {
                // CraftBukkit start
                jline.console.ConsoleReader bufferedreader = reader;

                // MC-33041, SPIGOT-5538: if System.in is not valid due to javaw, then return
                try {
                    System.in.available();
                } catch (IOException ex) {
                    return;
                }
                // CraftBukkit end

                String s;

                try {
                    // CraftBukkit start - JLine disabling compatibility
                    while (running) {
                        if (useJLine) {
                            s = bufferedreader.readLine(">", null);
                        } else {
                            s = bufferedreader.readLine();
                        }

                        // SPIGOT-5220: Throttle if EOF (ctrl^d) or stdin is /dev/null
                        if (s == null) {
                            try {
                                Thread.sleep(50L);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                            continue;
                        }
                        if (s.trim().length() > 0) { // Trim to filter lines which are just spaces
                            invokeCommand(s);
                        }
                        // CraftBukkit end
                    }
                } catch (IOException ioexception) {
                    log.error("Exception handling console input", ioexception);
                }

            }
        };

        java.util.logging.Logger global = java.util.logging.Logger.getLogger("");
        global.setUseParentHandlers(false);
        for (java.util.logging.Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }
        global.addHandler(new ForwardLogHandler());

        final org.apache.logging.log4j.core.Logger logger = ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger());
        for (org.apache.logging.log4j.core.Appender appender : logger.getAppenders().values()) {
            if (appender instanceof org.apache.logging.log4j.core.appender.ConsoleAppender) {
                logger.removeAppender(appender);
            }
        }

        new TerminalConsoleWriterThread(System.out, this.reader).start();

        System.setOut(new PrintStream(new LoggerOutputStream(logger, Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(logger, Level.WARN), true));

        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(log));
        thread.start();

    }

    /**
     * Internal method
     *
     * @param command
     */
    protected void invokeCommand(String command) {
        for (int i = 0; i < inputListeners.size(); i++) {
            inputListeners.get(i).onCommand(this, command);
        }
    }

    public ServerConsole addListener(InputListener listener) {
        inputListeners.add(listener);
        return this;
    }

    /**
     * Method for remove InputListener
     *
     * @param listener
     * @return true, if listener removed.
     */
    public boolean removeListener(InputListener listener) {
        for (int i = 0; i < inputListeners.size(); i++) {
            if (inputListeners.get(i).equals(listener)) {
                inputListeners.remove(i);
                return true;
            }
        }

        return false;
    }

    public List<InputListener> getInputListeners() {
        return inputListeners;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public ConsoleReader getReader() {
        return reader;
    }

    public void setReader(ConsoleReader reader) {
        this.reader = reader;
    }

    public static boolean isUseJLine() {
        return useJLine;
    }

    public static void setUseJLine(boolean useJLine) {
        ServerConsole.useJLine = useJLine;
    }

    public static Logger getLogger() {
        return log;
    }
}
