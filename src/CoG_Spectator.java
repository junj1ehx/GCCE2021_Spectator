import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import dataloader.BalDataLoader;
import dataloader.BalFitnessDataLoader;
import dataloader.FzReader;
import enumerate.Action;
import enumerate.State;
import mcts.MCTS;
import mcts.Node;
import mcts.Prediction;
import parameter.FixParameter;
import simulator.Simulator;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.Key;
import struct.MotionData;
import ice_agent.TTSBridge;
import ice_agent.UKIBridge;

public class CoG_Spectator implements AIInterface {

	private Simulator simulator;
	private Key key;
	private CommandCenter commandCenter;
	private boolean playerNumber;

	/** 大本のFrameData */
	private FrameData frameData;

	/** 大本よりFRAME_AHEAD分遅れたFrameData */
	private FrameData simulatorAheadFrameData;

	/** 自分が行える行動全て */
	private LinkedList<Action> myActions;

	/** 相手が行える行動全て */
	private LinkedList<Action> oppActions;

	/** 自分の情報 */
	private CharacterData myCharacter;

	/** 相手の情報 */
	private CharacterData oppCharacter;

	private Action[] actionAir;

	private Action[] actionGround;

	/** STAND_D_DF_FCの回避行動用フラグ */
	private boolean isFcFirst = true;

	/** 敵がSTAND_D_DF_FCを使ってくるかどうか */
	private boolean canFC = true;

	/** STAND_D_DF_FCの回避行動時間を計測する */
	private long firstFcTime;

	private ArrayList<MotionData>  myMotion;

	private ArrayList<MotionData>  oppMotion;

	private Action spSkill;

	private Node rootNode;

	private MCTS mcts;
	
	Logger logger;
	
	//private double beta;

	//private int[] resultHpDiff;

	//private int target = 0;

	//MiG Version
	float pdaProbability;
	
	private static String[] HarmlessActionName = {"FORWARD_WALK", "DASH"};
	
	private static Action[] harmlessActionGround = new Action[] {Action.FORWARD_WALK, Action.DASH
	};
	
	// FDG Version
//	private BalDataLoader balDataLoader;
	private BalFitnessDataLoader balFitnessDataLoader;
	private BalDataLoader balDataLoader;
	private TTSBridge tts;
	private UKIBridge uki;
	

	
	//TTS text generator	
	int gameState; // initialize game state, 0 start, 1 early game, 2 mid game, 3 near end game, 4 end game, 5 specific mode
	int AIState; // 0 highlight, 1 mcts, 2 harmless
	String opponentCurrentMove;	
	String opponentPreviousMove;
	int opponentCurrentMoveDamage;
	int opponentCurrentMoveDamageMax;
	private LinkedList<String> tempOpponentActionList;	
	boolean isSpeaking;//check if it's speaking
	float balancednessFitness;
	
	TTSSkillMap ttsSkillMap;
	
	String opponentActionPath;
	String opponentCurrentAction;
	String opponentPreviousAction;
	//health action
//	long startTime;
//	long currentTime;
//	long countStartTime;
//	long previousHealthActionTime;
//	String niceSkill;
	
	//UKI Map
	private Map<String, String> ukiSkillMap;
	private Map<String, Integer> realToUkiMap;	
	
	float pdaEvalMin;
	float pdaEvalMax;
	float distanceMin;	
	float distanceMax;
	float current;
	float actionValue;
	
	private void setTTSParameters() {
		tts.rate = 1.0f;//fixed
	
		if (gameState == 0) {
			tts.gain = 6.0f;
			tts.pitch = 4.0f;
			gameState = 1;
			actionValue = 0.0f;
			distanceMax = 100.0f; 
			distanceMin = 100.0f;
		} else if (gameState == 1) {
			if (FixParameter.PDA_TTS_FLAG) {
//				tts.gain = pdaProbability * 12.0f; // -6 50% 0 100% 6 200%[-6,6] https://cloud.google.com/text-to-speech/docs/reference/rest/v1/text/synthesize
//				tts.pitch = (pdaProbability - 0.5f) * 8.0f; // [-4,4]
				
			} else {


				//temp must change later TODO
				float distance = Math.abs((float)frameData.getCharacter(true).getCenterX() - (float)frameData.getCharacter(false).getCenterX());

				if (distanceMax < distance) {
					distanceMax = distance;
				}
				if (distanceMin > distance) {
					distanceMin = distance;
				}
				
				float scoreValue = 1.0f - 
						( (((float)myCharacter.getHp() + (float)oppCharacter.getHp())/ 400.0f) * (1.0f - (float)(this.frameData.getRemainingTimeMilliseconds() / 60000)));
				
				float distanceValue = 1.0f - distance / distanceMax;
				
				current = (float) ( scoreValue
						+ distanceValue + actionValue
						) / 3.0f;
				
				//System.out.println("current=" + current + " score=" + scoreValue + " distance=" + distanceValue + " action=" + actionValue);				
				this.frameData.getCharacter(!playerNumber).getAction().name();
				
				tts.gain = 0.0f;
				tts.pitch = 0.0f;				
				//tts.gain = (current - 0.5f) * 12.0f;
				//tts.gain = (-1.0f) * (current - 0.5f) * 12.0f;				
				//tts.pitch = (current - 0.5f) * 20.0f + 4.0f;
				//tts.pitch = (-1.0f) * ((current - 0.5f) * 20.0f - 4.0f);
				//CoG adjustment
			}
//				if (canProcessing() && rootNode != null) {
//				
//					current = ((float)(mcts.getScore(frameData)) - pdaEvalMin )/ (pdaEvalMax - pdaEvalMin);
//					tts.gain = 20.0f * (current - 0.5f);
//					tts.pitch = 8.0f * (current - 0.5f);
//				} 

//			} 
//			else if (FixParameter.PDA_FULL) {
//				//MiG PDA
//					tts.gain = 1.0f + (pdaProbability - 0.5f) * 5.0f;
//					tts.pitch = 1.0f + (pdaProbability - 0.5f) * 3.0f;
//			}

				//normalized the maximum 
//				current = (float)(Math.abs(myCharacter.getHp() - oppCharacter.getHp())) / (pdaEvalMax - pdaEvalMin);
//				tts.gain = 20.0f * (current - (pdaEvalMax + pdaEvalMin) /2.0f);
//				tts.pitch = 8.0f * (current - (pdaEvalMax + pdaEvalMin) /2.0f);
//				if (pdaEvalMax < current) {
//					pdaEvalMax = current;
//				}
//				if (pdaEvalMin > current) {
//					pdaEvalMin = current;
//				}
				

//			if (opponentCurrentMove.contains("Default")) {				
//				tts.pitch = -4.0f;				
//			} else {
//				tts.pitch = 0.0f;		
//			}

		} else if (gameState == 2) {
			tts.gain = 0.0f;
			tts.pitch = 0.0f;			
		} else if (gameState == 3) {
			tts.gain = 0.0f;
			tts.pitch = 0.0f;			
		} else if (gameState == 4) {
			tts.gain = 0.0f;
			tts.pitch = 0.0f;				
		} else if (gameState == 5) {
			tts.gain = 0.0f;
			tts.pitch = 0.0f;			
		} else {
			
		}
	}
	private void getOpponentCurrentMoveInformation() {
		//TODO
//		if (opponentPreviousMove != ttsSkillMap.getActionRealName(this.frameData.getCharacter(!playerNumber).getAction().name())) {
//			opponentPreviousMove = opponentCurrentMove;
			opponentCurrentMove = ttsSkillMap.getActionRealName(this.frameData.getCharacter(!playerNumber).getAction().name());
			
			if (opponentCurrentMove == "Ultimate Hadouken"){
				actionValue = 1.0f;
			} else if (opponentCurrentMove == "Super Uppercut"){
				actionValue = 0.5f;
			} else if (opponentCurrentMove == "Slide Kick"){
				actionValue = 0.25f;
			} else if (opponentCurrentMove == "Super Hadouken"){
				actionValue = 0.125f;		
			} else {
				actionValue = 0.0f;
			}

			if (isSpeaking) {
				if (!opponentCurrentMove.contains("Default")){		
					tempOpponentActionList.add(opponentCurrentMove);
					opponentCurrentMoveDamage = myMotion.get(this.frameData.getCharacter(!playerNumber).getAction().ordinal()).getAttackHitDamage();
					if (opponentCurrentMoveDamage > opponentCurrentMoveDamageMax) {
						opponentCurrentMoveDamageMax = opponentCurrentMoveDamage;
						
					}
				}
			} else {
				if (tempOpponentActionList.size() != 0) {
					opponentCurrentMove = tempOpponentActionList.get(ttsSkillMap.getRandomNumber(tempOpponentActionList.size()));
					
//					current = (float) opponentCurrentMoveDamageMax / (float) myMotion.get(Action.STAND_D_DF_FC.ordinal()).getAttackHitDamage();
//					//System.out.println("Max=" + (float) opponentCurrentMoveDamageMax + "current" + (float) myMotion.get(Action.STAND_D_DF_FC.ordinal()).getAttackHitDamage());
					opponentCurrentMoveDamage = myMotion.get(Action.STAND_D_DF_FC.ordinal()).getAttackHitDamage() / myMotion.get(Action.STAND_D_DF_FC.ordinal()).getAttackHitDamage();
					tempOpponentActionList.clear();
					opponentCurrentMoveDamageMax = 0;
				}
			}			
		}


//	}
	@Override
	public void getInformation(FrameData frameData, boolean arg1) {
		this.frameData = frameData;
		this.commandCenter.setFrameData(this.frameData, playerNumber);
		this.myCharacter = this.frameData.getCharacter(playerNumber);
		this.oppCharacter = this.frameData.getCharacter(!playerNumber);
		
	}
	
	private static FileWriter csvWriter = null;
	
	@Override
	public int initialize(GameData gameData, boolean playerNumber) {
		this.playerNumber = playerNumber;

		this.key = new Key();
		this.frameData = new FrameData();
		this.commandCenter = new CommandCenter();

		this.myActions = new LinkedList<Action>();
		this.oppActions = new LinkedList<Action>();

		this.simulator = gameData.getSimulator();
		this.myMotion = gameData.getMotionData(playerNumber);
		this.oppMotion = gameData.getMotionData(!playerNumber);
		
//		logger = new Logger(playerNumber);
		
		this.opponentPreviousMove = "Default";
//		this.balDataLoader = new BalDataLoader("uki/bal.txt");
//		this.balFitnessDataLoader = new BalFitnessDataLoader("uki/fitness.txt");
		this.tempOpponentActionList = new LinkedList<String>();
		
	/*	this.beta = 0;
		this.resultHpDiff = new int[3];
		Arrays.fill(resultHpDiff, 0);*/
		
		
		// Init
		setPerformAction();
		gameState = 0;
		AIState = 0;
		isSpeaking = false;
		ttsSkillMap = new TTSSkillMap();
		
		tts = new TTSBridge();
		setTTSParameters();
		isSpeaking = tts.speak(ttsSkillMap.generateBeginCommentary());
//		startTime = System.currentTimeMillis();
//		previousHealthActionTime = startTime;
		uki = new UKIBridge();
		ukiSkillMap = new HashMap<String, String>();
		realToUkiMap = new HashMap<String, Integer>();		
		initUkiSkillMap();
		initRealUkiSkillMap();
		current = 0.0f;

		
//		//PDA init
//		opponentActionPath = "C:\\FTG\\CMD1.txt";
//		opponentCurrentAction = "STAND";
//		opponentPreviousAction = "STAND";
//		pdaProbability = 1.0f;
//		balancednessFitness = 1.0f;
//		
//		opponentCurrentMoveDamage = 0;
		
		
		return 0;
	}

	private void initUkiSkillMap() {
//		ukiSkillMap.put("STAND_D_DB_BA", "Flying crop");
//		ukiSkillMap.put("BACK_STEP", "Back step");
//		ukiSkillMap.put("FORWARD_WALK", "Step forward");
//		ukiSkillMap.put("DASH", "Lean forward");
//		ukiSkillMap.put("STAND_GUARD", "Guard");
//		ukiSkillMap.put("CROUCH_GUARD", "Guard");
		ukiSkillMap.put("Throw", "Two Hand Punch");//THROW_A
		ukiSkillMap.put("Step Down + Two Hand Punch", "Great Throw");//THROW_B
		ukiSkillMap.put("Left Punch", "Punch");//STAND_A
		ukiSkillMap.put("Left Knee Strike", "Kick");//STAND_B
		ukiSkillMap.put("Crouch + Left Punch", "Low Punch");//CROUCH_A
		ukiSkillMap.put("Left kick", "Low Kick");//CROUCH_B
		ukiSkillMap.put("Right Punch", "Heavy Punch");//STAND_FA
		ukiSkillMap.put("Right Knee Strike", "Heavy Kick");//STAND_FB
		ukiSkillMap.put("Crouch + Right Punch", "Low Heavy Punch");//CROUCH_FA
		ukiSkillMap.put("Right Kick", "Low Heavy Kick");//CROUCH_FB
		ukiSkillMap.put("STAND_D_DF_FA", "Hadouken");//STAND_D_DF_FA
//		ukiSkillMap.put("STAND_D_DF_FB", "Super Hadouken");	
		ukiSkillMap.put("Left uppercut", "Uppercut");//STAND_F_D_DFA
		ukiSkillMap.put("Knifehand Strike", "Super Uppercut");//STAND_F_D_DFB
		ukiSkillMap.put("Right swing", "Slide Kick");//STAND_D_DB_BB
//		ukiSkillMap.put("Hadouken", "Ultimate Hadouken");//STAND_D_DF_FC
	}
	
	private void initRealUkiSkillMap() {
		realToUkiMap.put("STAND_A", 7);
		realToUkiMap.put("STAND_FA", 8);
		realToUkiMap.put("STAND_B",9);
		realToUkiMap.put("STAND_FB",10);
		realToUkiMap.put("CROUCH",11);
		realToUkiMap.put("CROUCH_A", 13);
		realToUkiMap.put("CROUCH_FA", 14);
		realToUkiMap.put("CROUCH_B",15);
		realToUkiMap.put("CROUCH_FB",16);
		realToUkiMap.put("THROW_A",17);
		realToUkiMap.put("THROW_B",18);
		realToUkiMap.put("STAND_D_DB_BB",19);
		realToUkiMap.put("STAND_F_D_DFA",20);
		realToUkiMap.put("STAND_F_D_DFB", 21);
		realToUkiMap.put("STAND_D_DF_FA",22);
		
	}
	
	
	
	public String ukiNameToReal(String skillCode) {
		return ukiSkillMap.getOrDefault(skillCode, "Default");
	}
	
	public float getBalancednessFitness(String currentSkill) {
		//System.out.println(currentSkill);
		int skillUkiName = realToUkiMap.getOrDefault(currentSkill, 0);
		//System.out.println(skillUkiName);
		if (skillUkiName == 0) {
			return -1.0f;
		} else {
			return balFitnessDataLoader.getBalFitnessById(skillUkiName);			
		}

	}
	
	public void setTTSParamaterForSpectator() {
		
	}
	@Override
	public void processing() {

		if (canProcessing()) {

			getOpponentCurrentMoveInformation();
			//Commentary Generation
			setTTSParameters();
			//currentTime = System.currentTimeMillis();		
			//tts.speak(ttsSkillMap.generateNormalCommentary(opponentCurrentMove,balFitnessDataLoader.getMaxVarActionId()));
			isSpeaking = tts.speak(ttsSkillMap.generateNormalCommentary(opponentCurrentMove));
			
			// フラグによって予測をするか選択
			if (FixParameter.PREDICT_FLAG) {
				if (oppMotion.get(oppCharacter.getAction().ordinal()).getFrameNumber() == oppCharacter
						.getRemainingFrame()) {
					Prediction.getInstance().countOppAction(this.frameData,oppCharacter, commandCenter);
				}
			}

			if (commandCenter.getSkillFlag()) {
				key = commandCenter.getSkillKey();
			} else {
				key.empty();
				commandCenter.skillCancel();

				aheadFrame(); // 遅れフレーム分進める

				// フラグによって回避行動をするかどうか選択
				if (FixParameter.AVOID_FLAG) {
					String enemyAction = this.frameData.getCharacter(!playerNumber).getAction().name();
					int enemyEnergy = this.frameData.getCharacter(!playerNumber).getEnergy();

					if (enemyAction.equals("STAND_D_DF_FC")) {
						canFC = true;
						isFcFirst = true;
					}

					if (enemyEnergy >= 150 && canFC) {
						if (isFcFirst) {
							firstFcTime = frameData.getRemainingTime();
							isFcFirst = false;
						}
						if (firstFcTime - frameData.getRemainingTime() >= FixParameter.AVOIDANCE_TIME) {
							canFC = false;
							isFcFirst = true;
						} else {
							commandCenter.commandCall("STAND_D_DB_BA");
							rootNode = null;
							return;
						}
					}
				}

				if (FixParameter.PREDICT_FLAG) {
					Prediction.getInstance().getInfomation(); // 回数順でソート
				}
				
				// MCTSによる行動決定
				Action bestAction = Action.STAND_D_DB_BA;
								
				
				if(rootNode == null){
					mctsPrepare(); // MCTSの下準備を行う
				}
				//DDA or PDA
				//if (AIState == 0) {
					bestAction = mcts.runMcts();
				//} else if (AIState == 1) {
				//	bestAction = harmlessActionGround[rand.nextInt() % (harmlessActionGround.length - 1)];
					
				//} else {
					
				//}
				
				if(ableAction(bestAction)){
					commandCenter.commandCall(bestAction.name()); // MCTSで選択された行動を実行する
					
//					logger.updateLog(rootNode.games);
					if (FixParameter.DEBUG_MODE) {
						mcts.printNode(rootNode);
					}
					rootNode = null;
				}
			}
		} else {
			canFC = true;
			isFcFirst = true;
		}
	}

	public boolean ableAction(Action action) {
		if (action == null)
			return false;
		if (myCharacter.isControl()) {
			return true;
		} else {
			return myCharacter.isHitConfirm() && checkFrame() && checkAction(action);
		}
	}
	
	public boolean checkFrame(){
		return (myMotion.get(myCharacter.getAction().ordinal()).getCancelAbleFrame() <= myMotion.get(myCharacter.getAction().ordinal()).getFrameNumber() - myCharacter.getRemainingFrame());
	}

	public boolean checkAction(Action act){
		return (myMotion.get(myCharacter.getAction().ordinal()).getCancelAbleMotionLevel() >= myMotion.get(act.ordinal()).getMotionLevel());
	}
	
	@Override
	public void roundEnd(int x, int y, int frame) {
		
	}

	/**
	 * MCTSの下準備 <br>
	 * 遅れフレーム分進ませたFrameDataの取得などを行う
	 */
	public void mctsPrepare() {
		setMyAction();
		setOppAction();

		rootNode = new Node(null);
		mcts = new MCTS(rootNode, simulatorAheadFrameData, simulator, myCharacter.getHp(), oppCharacter.getHp(),
				myActions, oppActions, playerNumber, myMotion);

		mcts.createNode(rootNode);

	}

	/** 自身の可能な行動をセットする */
	public void setMyAction() {
		myActions.clear();

		int energy = myCharacter.getEnergy();

		if (myCharacter.getState() == State.AIR) {
			for (int i = 0; i < actionAir.length; i++) {
				if (Math.abs(myMotion.get(Action.valueOf(actionAir[i].name()).ordinal())
						.getAttackStartAddEnergy()) <= energy) {
					myActions.add(actionAir[i]);
				}
			}
		} else {
			if (Math.abs(
					myMotion.get(Action.valueOf(spSkill.name()).ordinal()).getAttackStartAddEnergy()) <= energy) {
				myActions.add(spSkill);
			}

			for (int i = 0; i < actionGround.length; i++) {
				if (Math.abs(myMotion.get(Action.valueOf(actionGround[i].name()).ordinal())
						.getAttackStartAddEnergy()) <= energy) {
					myActions.add(actionGround[i]);
				}
			}
		}

	}

	/** 相手の可能な行動をセットする */
	public void setOppAction() {
		oppActions.clear();

		int energy = oppCharacter.getEnergy();

		if (oppCharacter.getState() == State.AIR) {
			for (int i = 0; i < actionAir.length; i++) {
				if (Math.abs(oppMotion.get(Action.valueOf(actionAir[i].name()).ordinal())
						.getAttackStartAddEnergy()) <= energy) {
					oppActions.add(actionAir[i]);
				}
			}
		} else {
			if (Math.abs(oppMotion.get(Action.valueOf(spSkill.name()).ordinal())
					.getAttackStartAddEnergy()) <= energy) {
				oppActions.add(spSkill);
			}

			for (int i = 0; i < actionGround.length; i++) {
				if (Math.abs(oppMotion.get(Action.valueOf(actionGround[i].name()).ordinal())
						.getAttackStartAddEnergy()) <= energy) {
					oppActions.add(actionGround[i]);
				}
			}
		}
	}

	/** 遅れフレーム分進める */
	private void aheadFrame() {
		
		simulatorAheadFrameData = simulator.simulate(this.frameData, playerNumber, null, null,1);
		myCharacter = simulatorAheadFrameData.getCharacter(playerNumber);
		oppCharacter = simulatorAheadFrameData.getCharacter(!playerNumber);
	}

	/** アクションの配列の初期化 */
	private void setPerformAction() {
		actionAir = new Action[] { Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
				Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA, Action.AIR_D_DF_FB,
				Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA, Action.AIR_D_DB_BB };
		actionGround = new Action[] { Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
				Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD, Action.CROUCH_GUARD, Action.THROW_A,
				Action.THROW_B, Action.STAND_A, Action.STAND_B, Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA,
				Action.STAND_FB, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB,
				Action.STAND_F_D_DFA, Action.STAND_F_D_DFB, Action.STAND_D_DB_BB };
		spSkill = Action.STAND_D_DF_FC;
	}

	/**
	 * AIが行動できるかどうかを判別する
	 *
	 * @return AIが行動できるかどうか
	 */
	public boolean canProcessing() {
		return !frameData.getEmptyFlag() && frameData.getRemainingTime() > 0;
	}

	@Override
	public Key input() {
		// TODO 自動生成されたメソッド・スタブ
		return key;
	}
	
	@Override
	public void close() {
		// TODO 自動生成されたメソッド・スタブ
		tts.speak(ttsSkillMap.generateEndCommentary(),true);
	//	logger.outputLog();
	}

}
