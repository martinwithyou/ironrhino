/* This software is published under the terms of the OpenSymphony Software
 * License version 1.1, of which a copy has been included with this
 * distribution in the LICENSE.txt file. */
package org.ironrhino.core.performance;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Provides a PrintWriter that routes through to another PrintWriter, however the destination
 * can be changed at any point. The destination can be passed in using a factory, so it will not be created
 * until it's actually needed.
 *
 * @author Joe Walnes
 * @version $Revision: 1.1 $
 */
public class RoutablePrintWriter extends PrintWriter {

    private PrintWriter destination;
    private DestinationFactory factory;

    /**
     * Factory to lazily instantiate the destination.
     */
    public static interface DestinationFactory {
        PrintWriter activateDestination() throws IOException;
    }

    public RoutablePrintWriter(DestinationFactory factory) {
        super(new NullWriter());
        this.factory = factory;
    }

    private PrintWriter getDestination() {
        if (destination == null) {
            try {
                destination = factory.activateDestination();
            } catch (IOException e) {
                setError();
            }
        }
        return destination;
    }

    public void updateDestination(DestinationFactory factory) {
        destination = null;
        this.factory = factory;
    }

    @Override
	public void close() {
        getDestination().close();
    }

    @Override
	public void println(Object x) {
        getDestination().println(x);
    }

    @Override
	public void println(String x) {
        getDestination().println(x);
    }

    @Override
	public void println(char x[]) {
        getDestination().println(x);
    }

    @Override
	public void println(double x) {
        getDestination().println(x);
    }

    @Override
	public void println(float x) {
        getDestination().println(x);
    }

    @Override
	public void println(long x) {
        getDestination().println(x);
    }

    @Override
	public void println(int x) {
        getDestination().println(x);
    }

    @Override
	public void println(char x) {
        getDestination().println(x);
    }

    @Override
	public void println(boolean x) {
        getDestination().println(x);
    }

    @Override
	public void println() {
        getDestination().println();
    }

    @Override
	public void print(Object obj) {
        getDestination().print(obj);
    }

    @Override
	public void print(String s) {
        getDestination().print(s);
    }

    @Override
	public void print(char s[]) {
        getDestination().print(s);
    }

    @Override
	public void print(double d) {
        getDestination().print(d);
    }

    @Override
	public void print(float f) {
        getDestination().print(f);
    }

    @Override
	public void print(long l) {
        getDestination().print(l);
    }

    @Override
	public void print(int i) {
        getDestination().print(i);
    }

    @Override
	public void print(char c) {
        getDestination().print(c);
    }

    @Override
	public void print(boolean b) {
        getDestination().print(b);
    }

    @Override
	public void write(String s) {
        getDestination().write(s);
    }

    @Override
	public void write(String s, int off, int len) {
        getDestination().write(s, off, len);
    }

    @Override
	public void write(char buf[]) {
        getDestination().write(buf);
    }

    @Override
	public void write(char buf[], int off, int len) {
        getDestination().write(buf, off, len);
    }

    @Override
	public void write(int c) {
        getDestination().write(c);
    }

    @Override
	public boolean checkError() {
        return getDestination().checkError();
    }

    @Override
	public void flush() {
        getDestination().flush();
    }

    /**
     * Just to keep super constructor for PrintWriter happy - it's never actually used.
     */
    private static class NullWriter extends Writer {

        protected NullWriter() {
            super();
        }

        @Override
		public void write(char cbuf[], int off, int len) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
		public void flush() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
		public void close() throws IOException {
            throw new UnsupportedOperationException();
        }

    }

}
