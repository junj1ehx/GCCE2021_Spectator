package parameter;

public class FixParameter {
	//////////// MCTSÃ©â€“Â¢Ã©â‚¬Â£Ã£ï¿½Â®Ã£Æ’â€˜Ã£Æ’Â©Ã£Æ’Â¡Ã£Æ’Â¼Ã£â€šÂ¿///////////////

	/** UCTÃ£ï¿½Â®Ã¥Â®Å¸Ã¨Â¡Å’Ã¦â„¢â€šÃ©â€“â€œ */
	public static final long UCT_TIME = 165 * 100000;

	/** UCB1Ã£ï¿½Â®Ã¥Â®Å¡Ã¦â€¢Â°CÃ£ï¿½Â®Ã¥â‚¬Â¤ */
	public static final double UCB_C = 0.42;

	/** Ã¦Å½Â¢Ã§Â´Â¢Ã£ï¿½â„¢Ã£â€šâ€¹Ã¦Å“Â¨Ã£ï¿½Â®Ã¦Â·Â±Ã£ï¿½â€¢ */
	public static final int UCT_TREE_DEPTH = 3;

	/** Ã£Æ’Å½Ã£Æ’Â¼Ã£Æ’â€°Ã£â€šâ€™Ã§â€�Å¸Ã¦Ë†ï¿½Ã£ï¿½â„¢Ã£â€šâ€¹Ã©â€“Â¾Ã¥â‚¬Â¤ */
	public static final int UCT_CREATE_NODE_THRESHOULD = 7;

	/** Ã£â€šÂ·Ã£Æ’Å¸Ã£Æ’Â¥Ã£Æ’Â¬Ã£Æ’Â¼Ã£â€šÂ·Ã£Æ’Â§Ã£Æ’Â³Ã£â€šâ€™Ã¨Â¡Å’Ã£ï¿½â€ Ã¦â„¢â€šÃ©â€“â€œ */
	public static final int SIMULATION_TIME = 60;

	/** Ã£Æ’â€¢Ã£Æ’Â¬Ã£Æ’Â¼Ã£Æ’Â Ã£ï¿½Â®Ã¨ÂªÂ¿Ã¦â€¢Â´Ã§â€�Â¨Ã¦â„¢â€šÃ©â€“â€œ */
	public static final int FRAME_AHEAD = 14;

	/** TanhÃ¥Â¼ï¿½Ã£ï¿½Â®Ã£â€šÂ¹Ã£â€šÂ±Ã£Æ’Â¼Ã£Æ’Â« */
	public static final double TANH_SCALE = 10;

	/** Ã£Æ’â€¡Ã£Æ’ï¿½Ã£Æ’Æ’Ã£â€šÂ°Ã£Æ’Â¢Ã£Æ’Â¼Ã£Æ’â€°Ã£ï¿½Â§Ã£ï¿½â€šÃ£â€šâ€¹Ã£ï¿½â€¹Ã£ï¿½Â©Ã£ï¿½â€ Ã£ï¿½â€¹Ã£â‚¬â€štrueÃ£ï¿½Â®Ã¥Â Â´Ã¥ï¿½Ë†Ã£â‚¬ï¿½Ã¦Â§ËœÃ£â‚¬â€¦Ã£ï¿½ÂªÃ£Æ’Â­Ã£â€šÂ°Ã£ï¿½Å’Ã¥â€¡ÂºÃ¥Å â€ºÃ£ï¿½â€¢Ã£â€šÅ’Ã£â€šâ€¹ */
	public static final boolean DEBUG_MODE = false;

	/////////// Ã¥ï¿½â€žÃ¦ï¿½ï¿½Ã¦Â¡Ë†Ã¦â€°â€¹Ã¦Â³â€¢Ã£ï¿½Â«Ã©â€“Â¢Ã£ï¿½â„¢Ã£â€šâ€¹Ã£Æ’â€˜Ã£Æ’Â©Ã£Æ’Â¡Ã£Æ’Â¼Ã£â€šÂ¿//////////////

	/** Ã¥â€ºÅ¾Ã©ï¿½Â¿Ã¨Â¡Å’Ã¥â€¹â€¢Ã£â€šâ€™Ã§Â¶Å¡Ã£ï¿½â€˜Ã£â€šâ€¹Ã¦â„¢â€šÃ©â€“â€œ */
	public static final int AVOIDANCE_TIME = 10;

	/////////// Ã¥ï¿½â€žÃ¦ï¿½ï¿½Ã¦Â¡Ë†Ã¦â€°â€¹Ã¦Â³â€¢Ã£â€šâ€™Ã§Âµâ€žÃ£ï¿½Â¿Ã¨Â¾Â¼Ã£â€šâ‚¬Ã£ï¿½â€¹Ã£ï¿½Â©Ã£ï¿½â€ Ã£ï¿½â€¹Ã£â€šâ€™Ã§Â®Â¡Ã§ï¿½â€ Ã£ï¿½â„¢Ã£â€šâ€¹Ã£Æ’â€¢Ã£Æ’Â©Ã£â€šÂ°//////////////

	/** Ã©â‚¬Å¡Ã¥Â¸Â¸MCTSÃ£ï¿½â€¹Ã¦â€°â€¹Ã¥Å Â Ã¦Â¸â€ºMCTSÃ£ï¿½â€¹Ã£â€šâ€™Ã§Â®Â¡Ã§ï¿½â€ Ã£ï¿½â„¢Ã£â€šâ€¹Ã£Æ’â€¢Ã£Æ’Â©Ã£â€šÂ° */
	public static final boolean STRONG_FLAG = false;

	/** Ã¨â€¡ÂªÃ§â€žÂ¶Ã£ï¿½â€¢Ã£â€šâ€™Ã¨â‚¬Æ’Ã¦â€¦Â®Ã£ï¿½â„¢Ã£â€šâ€¹Ã£ï¿½â€¹Ã£ï¿½Â©Ã£ï¿½â€ Ã£ï¿½â€¹Ã£â€šâ€™Ã§Â®Â¡Ã§ï¿½â€ Ã£ï¿½â„¢Ã£â€šâ€¹Ã£Æ’â€¢Ã£Æ’Â©Ã£â€šÂ° */
	public static final boolean MIX_FLAG = false;

	/** Ã¨Â©â€¢Ã¤Â¾Â¡Ã¥â‚¬Â¤Ã£â€šâ€™Ã¦Â­Â£Ã¨Â¦ï¿½Ã¥Å’â€“Ã£ï¿½â„¢Ã£â€šâ€¹Ã£ï¿½â€¹Ã£ï¿½Â©Ã£ï¿½â€ Ã£ï¿½â€¹Ã£â€šâ€™Ã§Â®Â¡Ã§ï¿½â€ Ã£ï¿½â„¢Ã£â€šâ€¹Ã£Æ’â€¢Ã£Æ’Â©Ã£â€šÂ° */
	public static final boolean NORMALIZED_FLAG = true;

	/** Ã¤ÂºË†Ã¦Â¸Â¬Ã£â€šâ€™Ã£ï¿½â„¢Ã£â€šâ€¹Ã£ï¿½â€¹Ã£ï¿½Â©Ã£ï¿½â€ Ã£ï¿½â€¹Ã£â€šâ€™Ã§Â®Â¡Ã§ï¿½â€ Ã£ï¿½â„¢Ã£â€šâ€¹Ã£Æ’â€¢Ã£Æ’Â©Ã£â€šÂ° */
	public static final boolean PREDICT_FLAG = true;

	//pda
	public static final boolean PDA_FLAG = false;
	
	public static final boolean PDA_TTS_FLAG = false;
	
	public static final boolean PDA_FULL = false;
	
	public static final boolean HIGHLIGHT_FLAG = true;
	
	/** Ã¥â€ºÅ¾Ã©ï¿½Â¿Ã¨Â¡Å’Ã¥â€¹â€¢Ã£â€šâ€™Ã£ï¿½â„¢Ã£â€šâ€¹Ã£ï¿½â€¹Ã£ï¿½Â©Ã£ï¿½â€ Ã£ï¿½â€¹Ã£â€šâ€™Ã§Â®Â¡Ã§ï¿½â€ Ã£ï¿½â„¢Ã£â€šâ€¹Ã£Æ’â€¢Ã£Æ’Â©Ã£â€šÂ° */
	public static final boolean AVOID_FLAG = false;

	public static final boolean ACTION_LOG = true;
	
	public static final boolean PLAY_FEATURE_LOG = false;
	
	public static final boolean SEARCHNUM_LOG = false;
	
}
