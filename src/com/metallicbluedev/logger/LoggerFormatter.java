package com.metallicbluedev.logger;

import com.metallicbluedev.utils.*;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.*;

/**
 * Ligne de sortie du journal.
 *
 * @version 3.00.00
 * @author Sebastien Villemain
 */
public class LoggerFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder str = new StringBuilder();

        str.append(DateHelper.formatInternational(new Date()));
        str.append(" [");
        str.append(record.getLevel().toString());
        str.append("] ");
        str.append(record.getSourceClassName());
        str.append(": ");
        str.append(record.getMessage());
        str.append(StringHelper.LINE_SEPARATOR);

        if (record.getLevel() == Level.SEVERE
            && record.getThrown() != null) {
            Throwable t = record.getThrown();

            str.append("Class exception name: ");
            str.append(t.getClass());
            str.append(StringHelper.LINE_SEPARATOR);
            str.append("Cause: ");
            str.append(t.getCause());
            str.append(StringHelper.LINE_SEPARATOR);
            str.append("Localized message: ");
            str.append(t.getLocalizedMessage());
            str.append(StringHelper.LINE_SEPARATOR);
            str.append("Stack trace: ");

            for (StackTraceElement traceElement : t.getStackTrace()) {
                str.append(traceElement.getClassName());
                str.append(".");
                str.append(traceElement.getMethodName());
                str.append("(");
                str.append(traceElement.getFileName());
                str.append(":");
                str.append(traceElement.getLineNumber());
                str.append(")");
                str.append(StringHelper.LINE_SEPARATOR);
            }
        }
        return str.toString();
    }
}
