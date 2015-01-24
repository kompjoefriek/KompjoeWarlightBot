package bot;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.testng.annotations.Test;

/**
 * Test if BotParser handles situations correctly
 */
public class TestBotParser
{
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testStartupWithErrors1() throws Exception
	{
		Bot bot = new Gir();
		BotParser parser = parser = new BotParser(bot, "tests/data/startup_with_errors_1.txt");
		// This should not write to stderr and cause no exceptions and stuff

		FailTestPrintStream stream = new FailTestPrintStream(System.err);
		System.setErr(stream);
		parser.run();

		Assert.assertFalse("Data was written to System.err", stream.hasFailed());
	}
}
