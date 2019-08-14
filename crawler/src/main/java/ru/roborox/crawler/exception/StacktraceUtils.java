package ru.roborox.crawler.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StacktraceUtils {

	public static String toString(Throwable e) {
		if (e == null) {
			return null;
		}
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		return stringWriter.toString();
	}
}
