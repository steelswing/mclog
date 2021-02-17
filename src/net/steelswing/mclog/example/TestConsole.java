/*
 * Ну вы же понимаете, что код здесь только мой?
 * Well, you do understand that the code here is only mine?
 */

package net.steelswing.mclog.example;

import java.util.concurrent.atomic.AtomicInteger;
import net.steelswing.mclog.ServerConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MrJavaCoder
 */
public class TestConsole {

    private static final Logger log = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        ServerConsole console = new ServerConsole().addListener((ServerConsole cns, String cmd) -> {
            if (cmd.equalsIgnoreCase("stop")) {
                cns.setRunning(false); // stop console handler
            }
        });

        
        AtomicInteger counter = new AtomicInteger(0);
        while (console.isRunning()) {
            if (counter.incrementAndGet() % 10 == 0) {
                log.error("&cC&4C&eE&6E&2A&aA&3B&bB&11&99&dD&5D&fF&88&00");
            }

            Thread.sleep(20);
        }
    }
}
