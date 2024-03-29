package mcts;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import enumerate.Action;
import parameter.FixParameter;
import simulator.Simulator;
import struct.FrameData;
import struct.MotionData;


public class MCTS {

	/** ä¹±æ•°ã‚’åˆ©ç”¨ã�™ã‚‹ã�¨ã��ã�«ä½¿ã�† */
	private Random rnd;

	private Node rootNode;

	/** ãƒŽãƒ¼ãƒ‰ã�®æ·±ã�• */
	private int depth;

	/** ãƒŽãƒ¼ãƒ‰ã�®æœ€å¤§ã�®æ·±ã�• */
//	public int mDepth;

	/** ãƒŽãƒ¼ãƒ‰ã�ŒæŽ¢ç´¢ã�•ã‚Œã�Ÿå›žæ•° */
	private int totalGames;

	/** ã‚·ãƒŸãƒ¥ãƒ¬ãƒ¼ã‚·ãƒ§ãƒ³ã�™ã‚‹ã�¨ã��ã�«åˆ©ç”¨ã�™ã‚‹ */
	private Simulator simulator;

	/** ã‚·ãƒŸãƒ¥ãƒ¬ãƒ¼ã‚·ãƒ§ãƒ³ã�™ã‚‹å‰�ã�®è‡ªåˆ†ã�®HP */
	private int myOriginalHp;

	/** ã‚·ãƒŸãƒ¥ãƒ¬ãƒ¼ã‚·ãƒ§ãƒ³ã�™ã‚‹å‰�ã�®ç›¸æ‰‹ã�®HP */
	private int oppOriginalHp;

	private boolean playerNumber;

	private Deque<Action> mAction;
	private Deque<Action> oppAction;

	/** é�¸æŠžã�§ã��ã‚‹è‡ªåˆ†ã�®å…¨Action */
	private LinkedList<Action> availableMyActions;

	/** é�¸æŠžã�§ã��ã‚‹ç›¸æ‰‹ã�®å…¨Action */
	private LinkedList<Action> availableOppActions;

	/** ãƒ•ãƒ¬ãƒ¼ãƒ ãƒ‡ãƒ¼ã‚¿(ã‚­ãƒ£ãƒ©æƒ…å ±ç­‰) */
	private FrameData frameData;

	private ArrayList<MotionData> myMotion;

	private double alpha = 0.5;

	public MCTS(Node node, FrameData fd, Simulator sim, int myHp, int oppHp, LinkedList<Action> myActions,
			LinkedList<Action> oppActions, boolean p, ArrayList<MotionData> mData) {

		rootNode = node; // ãƒ«ãƒ¼ãƒˆãƒŽãƒ¼ãƒ‰ã�®æƒ…å ±ã‚’æ ¼ç´�
		frameData = fd;
		simulator = sim;
		playerNumber = p;
		myOriginalHp = myHp;
		oppOriginalHp = oppHp;
		availableMyActions = myActions;
		availableOppActions = oppActions;

		mAction = new LinkedList<Action>();
		oppAction = new LinkedList<Action>();

		myMotion = mData;

//		mDepth = 0;

		alpha = (Math.tanh((double) (myOriginalHp - oppOriginalHp) / FixParameter.TANH_SCALE ) + 1) / 2;
	}

	/**
	 * MCTSã‚’è¡Œã�†
	 *
	 * @return æœ€çµ‚çš„ã�ªãƒŽãƒ¼ãƒ‰ã�®æŽ¢ç´¢å›žæ•°ã�Œå¤šã�„Action
	 */
	public Action runMcts() {

		long start = System.nanoTime();
		for (; System.nanoTime() - start <= FixParameter.UCT_TIME;) {
			uct(this.rootNode);
			// counting++;
		}
		// System.out.println(counting);

//		return getBestVisitAction(this.rootNode);
		return getBestScoreAction(this.rootNode);
	}

	private int getDistanceX(FrameData fd) {
		return fd.getCharacter(true).getLeft() < fd.getCharacter(false).getLeft() ? Math.max(fd.getCharacter(false).getLeft() - fd.getCharacter(true).getRight(), 0)
				: Math.max(fd.getCharacter(true).getLeft() - fd.getCharacter(false).getRight(), 0);
	}

	/**
	 * ãƒ—ãƒ¬ã‚¤ã‚¢ã‚¦ãƒˆ(ã‚·ãƒŸãƒ¥ãƒ¬ãƒ¼ã‚·ãƒ§ãƒ³)ã‚’è¡Œã�†
	 *
	 * @return ãƒ—ãƒ¬ã‚¤ã‚¢ã‚¦ãƒˆçµ�æžœã�®è©•ä¾¡å€¤
	 */
	public double playout(Node selectedNode) {

		mAction.clear();
		oppAction.clear();

		int distance = getDistanceX(frameData);

		LinkedList<Action> selectedMyActions = selectedNode.selectedActionFromRoot();
		rnd = new SecureRandom();

		for (int i = 0; i < selectedMyActions.size(); i++) {
			mAction.add(selectedMyActions.get(i));
		}

		// äºˆæ¸¬ã‚’ä½¿ã�£ã�Ÿã�ªã‚‰ã��ã�®çµ�æžœã‚’ã‚·ãƒŸãƒ¥ãƒ¬ãƒ¼ã‚·ãƒ§ãƒ³ã�«æ¸¡ã�™
		if (FixParameter.PREDICT_FLAG) {
			int predictSize = 0;
			for (int i = 0; i < 5; i++) {
				oppAction.add(Prediction.getInstance().predict(distance, availableOppActions));
				predictSize++;
			}

			for (int i = 0; i < 5 - predictSize; i++) {
				oppAction.add(availableOppActions.get(rnd.nextInt(availableOppActions.size())));
			}
		} else {
			for (int i = 0; i < 5; i++) {
				oppAction.add(availableOppActions.get(rnd.nextInt(availableOppActions.size())));
			}
		}
		// ã‚·ãƒŸãƒ¥ãƒ¬ãƒ¼ã‚·ãƒ§ãƒ³ã‚’å®Ÿè¡Œ
		//FrameData nFrameData = simulator.simulate(frameData, playerNumber, mAction, oppAction, 60);

		//mAction.clear();
		//oppAction.clear();

		for (int i = 0; i < 5-selectedMyActions.size(); i++) {
			mAction.add(availableMyActions.get(rnd.nextInt(availableMyActions.size())));
		}
		if (FixParameter.PREDICT_FLAG) {
			int predictSize = 0;
			for (int i = 0; i < 5; i++) {
				oppAction.add(Prediction.getInstance().predict(distance, availableOppActions));
				predictSize++;
			}

			for (int i = 0; i < 5 - predictSize; i++) {
				oppAction.add(availableOppActions.get(rnd.nextInt(availableOppActions.size())));
			}
		} else {
			for (int i = 0; i < 5; i++) {
				oppAction.add(availableOppActions.get(rnd.nextInt(availableOppActions.size())));
			}
		}

		// ã‚·ãƒŸãƒ¥ãƒ¬ãƒ¼ã‚·ãƒ§ãƒ³ã‚’å®Ÿè¡Œ
		FrameData nFrameData = simulator.simulate(this.frameData, playerNumber, mAction, oppAction, FixParameter.SIMULATION_TIME);
		return getScore(nFrameData, selectedMyActions);
	}

	/**
	 * UCTã‚’è¡Œã�† <br>
	 *
	 * @return è©•ä¾¡å€¤
	 */
	public double uct(Node parent) {

		Node selectedNode = null;
		double bestUcb;
		rnd = new SecureRandom();

		bestUcb = -99999;

		double scoreMax = -999;
		double scoreMin = 999;

		parent.games++;

		if (FixParameter.NORMALIZED_FLAG) {
			for (Node child : parent.children) {
				double current = child.score / child.games;
				if (scoreMax < current) {
					scoreMax = current;
				}
				if (scoreMin > current) {
					scoreMin = current;
				}
			}

			if (scoreMax - scoreMin == 0) {
				scoreMax = 10;
				scoreMin = -10;
			}
		}

		for (Node child : parent.children) {
			if (child.games == 0) {
				child.ucb = 9999 + rnd.nextInt(50);
			} else {
				if (FixParameter.NORMALIZED_FLAG) {
					child.ucb = getUcb(normalizeScore(child.score / child.games, scoreMax, scoreMin), totalGames,
							child.games);
				} else {
					child.ucb = getUcb(child.score / child.games, totalGames, child.games);
				}
			}

			if (bestUcb < child.ucb) {
				selectedNode = child;
				bestUcb = child.ucb;
			}
		}

		double score = 0;
		if (selectedNode.games == 0) {
//			checkDepth(selectedNode);
			score = playout(selectedNode);
		} else {
			if (selectedNode.children == null) {
				if (selectedNode.depth < FixParameter.UCT_TREE_DEPTH) {
					if (FixParameter.UCT_CREATE_NODE_THRESHOULD <= selectedNode.games) {
						createNode(selectedNode);
						selectedNode.isCreateNode = true;
						score = uct(selectedNode);
					} else {
//						checkDepth(selectedNode);
						score = playout(selectedNode);
					}
				} else {
//					checkDepth(selectedNode);
					score = playout(selectedNode);
				}
			} else {
				if (selectedNode.depth < FixParameter.UCT_TREE_DEPTH) {
					score = uct(selectedNode);
				} else {
//					checkDepth(selectedNode);
					playout(selectedNode);
				}
			}
		}

		selectedNode.games++;
		selectedNode.score += score;

		if (depth == 0) {
			totalGames++;
		}

		return score;
	}

//	private void checkDepth(Node selectedNode){
//		if (mDepth < selectedNode.depth){
//			mDepth = selectedNode.depth;
//		}
//	}
	/**
	 * ãƒŽãƒ¼ãƒ‰ã‚’ç”Ÿæˆ�ã�™ã‚‹
	 */
	public void createNode(Node parent) {
		parent.children = new Node[availableMyActions.size()];

		for (int i = 0; i < parent.children.length; i++) {
			parent.children[i] = new Node(parent, availableMyActions.get(i));
		}
	}

	/**
	 * æœ€å¤šè¨ªå•�å›žæ•°ã�®ãƒŽãƒ¼ãƒ‰ã�®Actionã‚’è¿”ã�™
	 *
	 * @return æœ€å¤šè¨ªå•�å›žæ•°ã�®ãƒŽãƒ¼ãƒ‰ã�®Action
	 */
	public Action getBestVisitAction(Node rootNode) {

		int selected = -1;
		double bestGames = -9999;

		for (int i = 0; i < rootNode.children.length; i++) {

			if (FixParameter.DEBUG_MODE) {
				System.out.println("è©•ä¾¡å€¤:" + rootNode.children[i].score / rootNode.children[i].games + ",è©¦è¡Œå›žæ•°:"
						+ rootNode.children[i].games + ",ucb:" + rootNode.children[i].ucb + ",Action:"
						+ rootNode.children[i].getAction());
			}

			if (bestGames < rootNode.children[i].games) {
				bestGames = rootNode.children[i].games;
				selected = i;
			}
		}

		if (FixParameter.DEBUG_MODE) {
			System.out.println(rootNode.children[selected].getAction() + ",å…¨è©¦è¡Œå›žæ•°:" + totalGames);
			System.out.println("");
		}

		return rootNode.children[selected].getAction();// availableMyActions.get(selected);
	}

	/**
	 * æœ€å¤šã‚¹ã‚³ã‚¢ã�®ãƒŽãƒ¼ãƒ‰ã�®Actionã‚’è¿”ã�™
	 *
	 * @return æœ€å¤šã‚¹ã‚³ã‚¢ã�®ãƒŽãƒ¼ãƒ‰ã�®Action
	 */
	public Action getBestScoreAction(Node rootNode) {

		int selected = -1;
		double bestScore = -9999;

		for (int i = 0; i < rootNode.children.length; i++) {

//			System.out.println("è©•ä¾¡å€¤:" + rootNode.children[i].score / rootNode.children[i].games + ",è©¦è¡Œå›žæ•°:"
//					+ rootNode.children[i].games + ",ucb:" + rootNode.children[i].ucb + ",Action:"
//					+ rootNode.children[i].getAction());

			double meanScore = rootNode.children[i].score / rootNode.children[i].games;
			if (bestScore < meanScore) {
				bestScore = meanScore;
				selected = i;
			}
		}

//		System.out.println(rootNode.children[selected].getAction() + ",å…¨è©¦è¡Œå›žæ•°:" + totalGames);
//		System.out.println("");
		
		return rootNode.children[selected].getAction();
	}

	/**
	 * è©•ä¾¡å€¤ã‚’è¿”ã�™
	 *
	 * @param fd
	 *            ãƒ•ãƒ¬ãƒ¼ãƒ ãƒ‡ãƒ¼ã‚¿(ã�“ã‚Œã�«hpã�¨ã�‹ã�®æƒ…å ±ã�Œå…¥ã�£ã�¦ã�„ã‚‹)
	 * @return è©•ä¾¡å€¤
	 */
	public double getScore(FrameData fd, LinkedList<Action> sAct) {
		if (FixParameter.MIX_FLAG) {
			return (1 - alpha) * evalStrength(fd) + alpha * evalTanh(fd);
		} else if (FixParameter.STRONG_FLAG) {
			return evalDifferenceHP(fd);
		}
		else if (FixParameter.HIGHLIGHT_FLAG){
			return evalHighlight(fd, sAct);
		}
		else {
			return evalTanh(fd);
		}
	}

	/**
	 * è‡ªåˆ†ã�¨ç›¸æ‰‹ã�®HPã�®å¤‰åŒ–é‡�ã�®å·®ã‚’è©•ä¾¡å€¤ã�®åŸºæº–ã�¨ã�—ã�¦è¿”ã�™
	 *
	 * @param ãƒ•ãƒ¬ãƒ¼ãƒ ãƒ‡ãƒ¼ã‚¿
	 *
	 * @return è‡ªåˆ†ã�¨ç›¸æ‰‹ã�®HPã�®å¤‰åŒ–é‡�ã�®å·®
	 */
	private int evalDifferenceHP(FrameData frameData) {
		if (playerNumber) {
			return (frameData.getCharacter(true).getHp() - myOriginalHp) - (frameData.getCharacter(false).getHp() - oppOriginalHp);
		} else {
			return (frameData.getCharacter(false).getHp() - myOriginalHp) - (frameData.getCharacter(true).getHp() - oppOriginalHp);
		}
	}

	private double evalTanh(FrameData frameData) {
		double score = Math
				.abs(frameData.getCharacter(playerNumber).getHp() - frameData.getCharacter(!playerNumber).getHp());

		return (1 - Math.tanh(score / FixParameter.TANH_SCALE));
	}

	private double evalStrength(FrameData frameData) {
		return Math.tanh(
				(double) (oppOriginalHp - frameData.getCharacter(!playerNumber).getHp()) / FixParameter.TANH_SCALE);
	}

	private double evalHighlight(FrameData frameData, LinkedList<Action> sAct){
		double normalHp = 0.0;
		double strength = 0.0;
		double actRank = 0.0;
		double distance = 0.0;
		Action fAct = sAct.getFirst();
		int maxEng = Math.abs(myMotion.get(Action.STAND_D_DF_FC.ordinal()).getAttackStartAddEnergy());

		// P1
		if(playerNumber){
			normalHp = (double)(oppOriginalHp - frameData.getCharacter(false).getHp())/myMotion.get(Action.STAND_D_DF_FC.ordinal()).getAttackHitDamage();
			strength = (double)(frameData.getFramesNumber()/3600.0)*normalHp;

			int startEng = Math.abs(myMotion.get(Action.valueOf(fAct.name()).ordinal()).getAttackStartAddEnergy());
			int damage = myMotion.get(Action.valueOf(fAct.name()).ordinal()).getAttackHitDamage();

			if(frameData.getCharacter(true).getEnergy() < maxEng){
				if(startEng == 0){
					actRank = 1.0;
				}else{
					actRank = 0.0;
				}
			}else{
				if(startEng == 0 || damage <= 30){
					actRank = 0.0;
				}else{
					actRank = (double)damage/startEng;
				}
			}

			distance = 1.0 - ((double)Math.abs(480.0 - frameData.getCharacter(true).getCenterX())/480.0);

			// P2
		} else {
			normalHp = (double)(oppOriginalHp - frameData.getCharacter(true).getHp())/myMotion.get(Action.STAND_D_DF_FC.ordinal()).getAttackHitDamage();
			strength = (double)(frameData.getFramesNumber()/3600.0)*normalHp;

			int startEng = Math.abs(myMotion.get(Action.valueOf(fAct.name()).ordinal()).getAttackStartAddEnergy());
			int damage = myMotion.get(Action.valueOf(fAct.name()).ordinal()).getAttackHitDamage();
			if(frameData.getCharacter(false).getEnergy() < maxEng){
				if(startEng == 0){
					actRank = 1.0;
				}else{
					actRank = 0.0;
				}
			}else{
				if(startEng == 0 || damage <= 30){
					actRank = 0.0;
				}else{
					actRank = (double)damage/startEng;
				}
			}

			distance = 1.0 - ((double)Math.abs(480.0 - frameData.getCharacter(false).getCenterX())/480.0);

		}

		return (strength+actRank+distance)/3.0;

	}

	/**
	 * è©•ä¾¡å€¤ã�¨å…¨ãƒ—ãƒ¬ã‚¤ã‚¢ã‚¦ãƒˆè©¦è¡Œå›žæ•°ã�¨ã��ã�®Actionã�®ãƒ—ãƒ¬ã‚¤ã‚¢ã‚¦ãƒˆè©¦è¡Œå›žæ•°ã�‹ã‚‰UCB1å€¤ã‚’è¿”ã�™
	 *
	 * @param score
	 *            è©•ä¾¡å€¤
	 * @param n
	 *            å…¨ãƒ—ãƒ¬ã‚¤ã‚¢ã‚¦ãƒˆè©¦è¡Œå›žæ•°
	 * @param ni
	 *            ã��ã�®Actionã�®ãƒ—ãƒ¬ã‚¤ã‚¢ã‚¦ãƒˆè©¦è¡Œå›žæ•°
	 * @return UCB1å€¤
	 */
	public double getUcb(double score, int n, int ni) {
		return score + FixParameter.UCB_C * Math.sqrt((2 * Math.log(n)) / ni);
	}

	public void printNode(Node node) {
		System.out.println("å…¨è©¦è¡Œå›žæ•°:" + node.games);
		for (int i = 0; i < node.children.length; i++) {
			System.out.println(i + ",å›žæ•°:" + node.children[i].games + ",æ·±ã�•:" + node.children[i].depth + ",score:"
					+ node.children[i].score / node.children[i].games + ",ucb:" + node.children[i].ucb);
		}
		System.out.println("");
		for (int i = 0; i < node.children.length; i++) {
			if (node.children[i].isCreateNode) {
				printNode(node.children[i]);
			}
		}
	}

	/**
	 * ã‚¹ã‚³ã‚¢ã‚’æ­£è¦�åŒ–ã�™ã‚‹
	 *
	 * @return æ­£è¦�åŒ–ã�•ã‚Œã�Ÿã‚¹ã‚³ã‚¢
	 */
	public double normalizeScore(double score, double scoreMax, double scoreMin) {
		double tmpScore = 0;

		tmpScore = (score - scoreMin) / (double) (scoreMax - scoreMin);
		if (tmpScore > 1) {
			tmpScore = 1;
		} else if (tmpScore < 0) {
			tmpScore = 0;
		}

		return tmpScore;
	}
}
