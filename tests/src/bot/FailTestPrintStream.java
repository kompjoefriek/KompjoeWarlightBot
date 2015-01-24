package bot;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 * Used to check if someone wrote to a PrintStream, like System.err
 */
public class FailTestPrintStream extends PrintStream
{
	PrintStream m_stream;
	boolean m_failed;

	public FailTestPrintStream(PrintStream stream)
	{
		super(new ByteArrayOutputStream());
		m_stream = stream;
		m_failed = false;
	}

	public boolean hasFailed()
	{
		return m_failed;
	}

	public PrintStream append(char c)
	{
		m_failed = true;
		return m_stream.append(c);
	}

	public PrintStream append(CharSequence csq)
	{
		m_failed = true;
		return m_stream.append(csq);
	}

	public PrintStream append(CharSequence csq, int start, int end)
	{
		m_failed = true;
		return m_stream.append(csq, start, end);
	}

	public PrintStream format(Locale l, String format, Object... args)
	{
		m_failed = true;
		return m_stream.format(l, format, args);
	}

	public PrintStream format(String format, Object... args)
	{
		m_failed = true;
		return m_stream.format(format, args);
	}

	public void print(boolean b)
	{
		m_failed = true;
		m_stream.print(b);
	}

	public void print(char c)
	{
		m_failed = true;
		m_stream.print(c);
	}

	public void print(char[] s)
	{
		m_failed = true;
		m_stream.print(s);
	}

	public void print(double d)
	{
		m_failed = true;
		m_stream.print(d);
	}

	public void print(float f)
	{
		m_failed = true;
		m_stream.print(f);
	}

	public void print(int i)
	{
		m_failed = true;
		m_stream.print(i);
	}

	public void print(long l)
	{
		m_failed = true;
		m_stream.print(l);
	}

	public void print(Object obj)
	{
		m_failed = true;
		m_stream.print(obj);
	}

	public void print(String s)
	{
		m_failed = true;
		m_stream.print(s);
	}

	public PrintStream printf(Locale l, String format, Object... args)
	{
		m_failed = true;
		return m_stream.printf(l, format, args);
	}

	public PrintStream printf(String format, Object... args)
	{
		m_failed = true;
		return m_stream.printf(format, args);
	}

	public void println()
	{
		m_failed = true;
		m_stream.println();
	}

	public void println(boolean x)
	{
		m_failed = true;
		m_stream.println(x);
	}

	public void println(char x)
	{
		m_failed = true;
		m_stream.println(x);
	}

	public void println(char[] x)
	{
		m_failed = true;
		m_stream.println(x);
	}

	public void println(double x)
	{
		m_failed = true;
		m_stream.println(x);
	}

	public void println(float x)
	{
		m_failed = true;
		m_stream.println(x);
	}

	public void println(int x)
	{
		m_failed = true;
		m_stream.println(x);
	}

	public void println(long x)
	{
		m_failed = true;
		m_stream.println(x);
	}

	public void println(Object x)
	{
		m_failed = true;
		m_stream.println(x);
	}

	public void println(String x)
	{
		m_failed = true;
		m_stream.println(x);
	}

	public void write(byte[] buf, int off, int len)
	{
		m_failed = true;
		m_stream.write(buf, off, len);
	}

	public void write(int b)
	{
		m_failed = true;
		m_stream.write(b);
	}
}
