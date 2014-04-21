package bot;

import main.Region;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Text cases for all invalid and wanted moves
 */
public class TestPathFinding
{
	private BotState m_currentState;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public TestRule globalTimeout = new Timeout(50); // 50 milliseconds (tested on an Intel E8400)

	@Before
	public void setUp() throws Exception
	{
		m_currentState = new BotState();
		m_currentState.setDebugMode();
	}

	@After
	public void tearDown() throws Exception
	{
		m_currentState = null;
	}

	private void setupDefaultMap()
	{
		m_currentState.updateSettings("your_bot","player1");
		m_currentState.updateSettings("opponent_bot","player2");

		m_currentState.setupMap("setup_map super_regions 1 5 2 2 3 5 4 3 5 7 6 2".split(" "));
		m_currentState.setupMap("setup_map regions 1 1 2 1 3 1 4 1 5 1 6 1 7 1 8 1 9 1 10 2 11 2 12 2 13 2 14 3 15 3 16 3 17 3 18 3 19 3 20 3 21 4 22 4 23 4 24 4 25 4 26 4 27 5 28 5 29 5 30 5 31 5 32 5 33 5 34 5 35 5 36 5 37 5 38 5 39 6 40 6 41 6 42 6".split(" "));
		m_currentState.setupMap("setup_map neighbors 1 2,4,30 2 4,3,5 3 5,6,14 4 5,7 5 6,7,8 6 8 7 8,9 8 9 9 10 10 11,12 11 12,13 12 13,21 14 15,16 15 16,18,19 16 17 17 19,20,27,32,36 18 19,20,21 19 20 20 21,22,36 21 22,23,24 22 23,36 23 24,25,26,36 24 25 25 26 27 28,32,33 28 29,31,33,34 29 30,31 30 31,34,35 31 34 32 33,36,37 33 34,37,38 34 35 36 37 37 38 38 39 39 40,41 40 41,42 41 42".split(" "));
	}

	@Test
	public void testNoFindFlag() throws Exception
	{
		exception.expect(FindPathException.class);

		setupDefaultMap();

		m_currentState.updateMap("update_map 5 player1 2 8 player2 2".split(" "));
		Region fromRegion = m_currentState.getVisibleMap().getRegion(5); // Ontario
		Assert.assertNotNull("Could not find region 5", fromRegion);

		Region result = Gir.getPath(fromRegion, m_currentState, 0);
	}

	@Test
	public void testMultipleFindFlag() throws Exception
	{
		exception.expect(FindPathException.class);

		setupDefaultMap();
		m_currentState.updateMap("update_map 5 player1 2 8 player2 2".split(" "));
		Region fromRegion = m_currentState.getVisibleMap().getRegion(5); // Ontario
		Assert.assertNotNull("Could not find region 5", fromRegion);

		Region result = Gir
			.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_ANY | Gir.SEARCH_FLAG_FIND_REGION_ID);
	}

	@Test
	public void testFindOpponentWhenNeighbor() throws Exception
	{
		setupDefaultMap();
		m_currentState.updateMap("update_map 5 player1 2 8 player2 2".split(" "));
		Region fromRegion = m_currentState.getVisibleMap().getRegion(5); // Ontario
		Assert.assertNotNull("Could not find region 5", fromRegion);

		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_OPPONENT);
		Assert.assertNotNull("Should have found 8 but found nothing", result);
		Assert.assertTrue("Should have found 8 but found region "+result.getId(), result.getId() == 8);
	}

	@Test
	public void testFindOpponentWhenNotNeighbor() throws Exception
	{
		setupDefaultMap();
		m_currentState.updateMap("update_map 5 player1 2 9 player2 2".split(" "));
		Region fromRegion = m_currentState.getVisibleMap().getRegion(5); // Ontario
		Assert.assertNotNull("Could not find region 5", fromRegion);

		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_OPPONENT);
		Assert.assertNotNull("Should have found Region 7 or 8 but found nothing", result);
		Assert.assertTrue("Should have found Region 7 or 8 but found region "+result.getId(), result.getId() == 7 || result.getId() == 8);
	}

	@Test
	public void testFindAnyWhenNeighbor() throws Exception
	{
		setupDefaultMap();
		m_currentState.updateMap("update_map 1 player1 2 2 player1 2 3 player1 2 4 player1 2 5 player1 2 6 player1 2 7 player1 2 9 player2 2".split(" "));
		Region fromRegion = m_currentState.getVisibleMap().getRegion(5); // Ontario
		Assert.assertNotNull("Could not find region 5", fromRegion);

		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_ANY);
		Assert.assertNotNull("Should have found 8 but found nothing", result);
		Assert.assertTrue("Should have found 8 but found region "+result.getId(), result.getId() == 8);

		m_currentState.updateMap("update_map 1 player1 2 2 player1 2 3 player1 2 4 player1 2 5 player1 2 6 player1 2 7 player1 2 9 player2 2 8 player2 2".split(" "));

		result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_ANY);
		Assert.assertNotNull("Should have found 8 but found nothing", result);
		Assert.assertTrue("Should have found 8 but found region "+result.getId(), result.getId() == 8);
	}

	@Test
	public void testFindAnyWhenNotNeighbor() throws Exception
	{
		setupDefaultMap();
		m_currentState.updateMap("update_map 1 player1 2 2 player1 2 3 player1 2 4 player1 2 5 player1 2 6 player1 2 7 player1 2 8 player1 2 14 player1 2".split(" "));
		Region fromRegion = m_currentState.getVisibleMap().getRegion(5); // Ontario
		Assert.assertNotNull("Could not find region 5", fromRegion);

		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_ANY);
		Assert.assertNotNull("Should have found Region 7 or 8 (target = neutral Region 9) but found nothing", result);
		Assert.assertTrue("Should have found Region 7 or 8 (target = neutral Region 9) but found region "+result.getId(), result.getId() == 7 || result.getId() == 8);

		m_currentState.updateMap("update_map 1 player1 2 2 player1 2 3 player1 2 4 player1 2 5 player1 2 6 player1 2 7 player1 2 8 player1 2 14 player1 2 9 player2 2".split(" "));
		result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_ANY);
		Assert.assertNotNull("Should have found Region 7 or 8 (target = opponent Region 9) but found nothing", result);
		Assert.assertTrue("Should have found Region 7 or 8 (target = opponent Region 9) but found region "+result.getId(), result.getId() == 7 || result.getId() == 8);
	}

	@Test
	public void testFindIdWhenNeighbor() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(5); // Ontario
		Assert.assertNotNull("Could not find region 5", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_REGION_ID, 8); // EasternUnitedStates
		Assert.assertNotNull("Should have found 8 but found nothing", result);
		Assert.assertTrue("Should have found 8 but found region "+result.getId(), result.getId() == 8);
	}

	@Test
	public void testFindIdWhenNotNeighbor() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(8); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 8", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_REGION_ID, 22); // Egypt
		Assert.assertNotNull("Should have found 9 but found nothing", result);
		Assert.assertTrue("Should have found 9 but found region "+result.getId(), result.getId() == 9);
	}

	@Test
	public void testFindRegionIdWithoutId() throws Exception
	{
		exception.expect(FindPathException.class);

		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(8); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 8", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_REGION_ID);
	}

	@Test
	public void testFindSameRegionId() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(8); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 8", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_REGION_ID, 8);
		Assert.assertNull("Should have found nothing", result);
	}

	@Test
	public void testFindUnknownRegionId() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(8); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 8", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_REGION_ID, 999); // Non-existing region id
		Assert.assertNull("Should have found nothing", result);
	}

	@Test
	public void testFindRegionIdWhenNeighbor() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(5); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 5", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_REGION_ID, 7); // Non-existing region id
		Assert.assertNotNull("Should have found 7 but found nothing", result);
		Assert.assertTrue("Should have found 7 but found region " + result.getId(), result.getId() == 7);
	}

	@Test
	public void testFindRegionIdWhenNotNeighbor() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(5); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 5", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_REGION_ID, 1); // Non-existing region id
		Assert.assertNotNull("Should have found Region 2 or 4 (target = opponent Region 9) but found nothing", result);
		Assert.assertTrue(
			"Should have found Region 2 or 4 (target = opponent Region 9) but found region " + result.getId(),
			result.getId() == 2 || result.getId() == 4);
	}

	@Test
	public void testFindSuperRegionIdWithoutId() throws Exception
	{
		exception.expect(FindPathException.class);

		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(8); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 8", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_SUPER_REGION_ID);
	}

	@Test
	public void testFindSuperRegionId() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(8); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 8", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_SUPER_REGION_ID, 999); // Non-existing super region id
		Assert.assertNull("Should have found nothing", result);
	}

	@Test
	public void testFindSuperRegionWhenNeighbor() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(9); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 9", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_SUPER_REGION_ID, 2);
		Assert.assertNotNull("Should have found 10 but found nothing", result);
		Assert.assertTrue("Should have found 10 but found region "+result.getId(), result.getId()== 10);
	}

	@Test
	public void testFindSuperRegionWhenNotNeighbor() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(8); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 8", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_SUPER_REGION_ID, 2);
		Assert.assertNotNull("Should have found 9 but found nothing", result);
		Assert.assertTrue("Should have found 9 but found region "+result.getId(), result.getId()== 9);
	}

	@Test
	public void testFindSuperRegionWhenNotSuperNeighbor() throws Exception
	{
		setupDefaultMap();
		Region fromRegion = m_currentState.getFullMap().getRegion(8); // EasternUnitedStates
		Assert.assertNotNull("Could not find region 8", fromRegion);
		Region result = Gir.getPath(fromRegion, m_currentState, Gir.SEARCH_FLAG_FIND_SUPER_REGION_ID, 4);
		Assert.assertNotNull("Should have found 9 but found nothing", result);
		Assert.assertTrue("Should have found 9 but found region "+result.getId(), result.getId()== 9);
	}
}