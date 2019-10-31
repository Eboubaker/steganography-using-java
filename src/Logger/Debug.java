package Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Debug {
	private static Logger logger = Logger.getLogger("Default");
	private static Logger tlogger = Logger.getLogger("WithTime");
	static {
		var handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter() {
            private static final String format = "[%s] %s \n";
            SimpleDateFormat formatter = new SimpleDateFormat("aa hh:mm:ss:SSS");
            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format,
                        formatter.format(new Date(lr.getMillis())),
                        lr.getMessage()
                );
            }
		});
		tlogger.setUseParentHandlers(false);
		tlogger.addHandler(handler);
		
		var thandler = new ConsoleHandler();
		thandler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord lr) {
                return lr.getMessage()+"\n";
            }
		});
		logger.setUseParentHandlers(false);
		logger.addHandler(thandler);
	}
	public static void tlog(String s) {
		tlogger.log(Level.INFO, s);
	}
	public static void log(String s) {
		logger.log(Level.INFO, s);
	}
}
