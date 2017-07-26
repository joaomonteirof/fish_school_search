package rFSS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import General.Run;

public class RSchool {

	public double[][][] history;
	private ArrayList<Fish> fishes;
	private HashMap<Integer,Cell> grid;
	private double[] barycenter;
	private double totalWeight;
	private double lastTotalWeight;
	private double stepInd;
	private double stepVol;
	private double epsilon;
	private double epsilonZero;
	private double cp;
	private double TF;
	private double bestFitness;
	private double[] bestPosition;
	private double[] bestFeasibility;
	private ArrayList<Double> bestFitnessConvergence;
	private ArrayList<Double> bestFeasibilityConvergence;
	private boolean anyFeasible;
	
	public RSchool(){
		
		fishes = new ArrayList<Fish>();
		grid = new HashMap<Integer,Cell>();
		this.history = new double[Run.NUMBER_OF_ITERATIONS + 1][Run.NUMBER_OF_FISH][Run.DIMENSIONS];
		this.bestFitnessConvergence = new ArrayList<Double>();
		this.bestFeasibilityConvergence = new ArrayList<Double>();
		
		for(int i=0;i<Run.NUMBER_OF_FISH;i++){
			fishes.add(new Fish());			
		}
		
		bestFeasibility = new double[3];
		bestFeasibility[0] = 100000.0;
		bestFeasibility[1] = 100000000000000000000000000000000000000000000.0;
		bestFitness=1000000000000000000000000000000000000000000000000.0;
		stepInd=Run.INITIAL_IND_STEP;
		stepVol=Run.INITIAL_VOL_STEP;
		totalWeight=Run.NUMBER_OF_FISH*Run.W_SCALE/2;
		lastTotalWeight=totalWeight;
		updateFishesAddresses();
		updateCulture();
		calculateEpsilonZero();
		this.cp=Math.max((-5.0-Math.log(this.epsilonZero))/Math.log(0.05),Run.cpMin);
		this.epsilon=this.epsilonZero;
		setFishesEpsilon();
		TF=Math.round(Run.TC*0.95);
		updateAnyFeasible();
		
	}

	public void runSearch(){
		
		int iteration=0;
		
		for(int i=0;i<this.TF;i++){
			updateFitnesses();
			updateFishesAddresses();
			updateCulture();
			updateBestCells();
			this.bestFitnessConvergence.add(this.bestFitness);
			this.bestFeasibilityConvergence.add(this.bestFeasibility[1]);
			individualMovement();
			updateFishesAddresses();
			updateCulture();
			updateBestCells();
			feed();
			collectiveInstintiveMovement();
			updateBarycenter();
			collectiveVolitiveMovement();
			updateSteps(i);
			updateHistory(i);
			updateEpsilon(i);
			setFishesEpsilon();
			updateAnyFeasible();
			iteration=i;
		}
		
		cp=0.3*cp+0.7*Run.cpMin;
		System.out.println("Part 1 over: i= "+ iteration);
		
		for(int i = (int)TF;i<Run.TC;i++){
			updateFitnesses();
			updateFishesAddresses();
			updateCulture();
			updateBestCells();
			this.bestFitnessConvergence.add(this.bestFitness);
			this.bestFeasibilityConvergence.add(this.bestFeasibility[1]);
			individualMovement();
			updateFishesAddresses();
			updateCulture();
			updateBestCells();
			feed();
			collectiveInstintiveMovement();
			updateBarycenter();
			collectiveVolitiveMovement();
			updateSteps(i);
			updateHistory(i);
			updateEpsilon(i);
			setFishesEpsilon();
			updateAnyFeasible();
			iteration=i;
		}
		
		System.out.println("Part 2 over: i= "+ iteration);
		this.epsilon=0.0;
		setFishesEpsilon();
		
		for(int i=(int)Run.TC;i<Run.NUMBER_OF_ITERATIONS;i++){
			
			updateFitnesses();
			updateFishesAddresses();
			updateCulture();
			updateBestCells();
			this.bestFitnessConvergence.add(this.bestFitness);
			this.bestFeasibilityConvergence.add(this.bestFeasibility[1]);
			individualMovement();
			updateFishesAddresses();
			updateCulture();
			updateBestCells();
			feed();
			collectiveInstintiveMovement();
			updateBarycenter();
			collectiveVolitiveMovement();
			updateSteps(i);
			updateHistory(i);
			updateAnyFeasible();
			iteration=i;
		}
		
		System.out.println("Run over: i= "+ iteration);
		updateFitnesses();
		updateBestFish();	
		this.bestFitnessConvergence.add(this.bestFitness);
		this.bestFeasibilityConvergence.add(this.bestFeasibility[1]);
		//printGrid();
		//printKeys();
	}

	private void updateAnyFeasible() {
	
		this.anyFeasible=false;
		
		for(Fish e:fishes){
			if(Double.compare(e.getFeasibility()[0],0.0)==0){
				//System.out.println("Feasible!");
				this.anyFeasible=true;
				break;
			}
		}
		
	}
	
	private void updateEpsilon(int iteration) {
		
		this.epsilon=this.epsilonZero*Math.pow(1-iteration/Run.TC,this.cp);
		
	}

	private void setFishesEpsilon() {
		
		for(Fish e:fishes){
			e.setEpsilon(this.epsilon);
		}
		
	}

	private void calculateEpsilonZero() {
		
		double minFi = 1000000000000000000000000000000.0;
		double sumFi = 0.0;
		double[] currentFeasibility;
		
		for(Fish e:fishes){
			currentFeasibility=e.getFeasibility();
			sumFi+=currentFeasibility[1];
			if(Double.compare(currentFeasibility[1],minFi)<0){
				minFi=currentFeasibility[1];
			}
		}
		
		this.epsilonZero=(sumFi/Run.NUMBER_OF_FISH+minFi)/2;
		
	}
	
	private void updateBestCells() {
		
		updateBestFish();
		
		if(bestPosition!=null&&bestFeasibility[1]<epsilon){
			grid.get(Arrays.hashCode(getAdress(bestPosition))).setF(2);
		}
	}

	private void updateCulture() {
		
		for(Fish e:fishes){
			e.updateCellParameters();
		}
		
	}

	private void updateFishesAddresses() {
		
		int[] adress = new int[Run.DIMENSIONS];
		Integer adressKey;
		
		for(Fish e:fishes){
			
			 adress = e.getAdress();
			 adressKey = Arrays.hashCode(adress);
			 
			 e.setCellOcuppied(getCell(adressKey, adress));
			
		}
		
	}

	private Cell getCell(Integer adressKey, int[] adress) {
		
		Cell toUpdate;
		
		if(!grid.containsKey(adressKey)){
			toUpdate = new Cell(adress);
			grid.put(adressKey, toUpdate);
		}else{
			toUpdate = grid.get(adressKey);
		}		
		
		return toUpdate;
	}

	private void updateSteps(int currentIteration){
		stepInd=stepInd-(Run.INITIAL_IND_STEP-Run.FINAL_IND_STEP)/Run.NUMBER_OF_ITERATIONS;
		stepVol=stepVol-(Run.INITIAL_VOL_STEP-Run.FINAL_VOL_STEP)/Run.NUMBER_OF_ITERATIONS;
	}
	
	private void collectiveVolitiveMovement() {
		
		double deltaw=totalWeight-lastTotalWeight;
		
		if(Double.compare(deltaw,0.0)<0){
			for(Fish e:fishes){
				e.moveFishVolitive(barycenter, stepVol, -1);
			}
		}else{
			for(Fish e:fishes){
				e.moveFishVolitive(barycenter, stepVol, 1);
			}
		}		
		
	}

	private void collectiveInstintiveMovement() {
		
		double gridSumSelection;
		
		if(this.anyFeasible){
			for(Fish e:fishes){
				
				gridSumSelection = Run.generator.nextDouble();
				
				if(Double.compare(gridSumSelection,Run.gridInstinctiveMovement)<=0){
					e.calculateIFitness(grid, stepInd);
				}else{
					e.calculateIFitness(fishes);
				}				
			}
		}else{
			for(Fish e:fishes){
				
				gridSumSelection = Run.generator.nextDouble();
				
				if(Double.compare(gridSumSelection,Run.gridInstinctiveMovement)<=0){
					e.calculateIFeasibility(grid, stepInd);
				}else{
					e.calculateIFeasibility(fishes);
				}				
			}
		}
		
		for(Fish a:fishes){
			a.moveFishColInstinctive();
		}
		
	}

	private void feed() {
		
		double maxDeltaf = this.getMaxDeltaFitness();
		double totalWeightToChange = 0;
		
		for(Fish e:fishes){
			totalWeightToChange+=e.getWeight();
		}
		
		lastTotalWeight=totalWeightToChange;
		
		for(Fish e:fishes){
			e.feedFish(maxDeltaf);
		}
		
		totalWeightToChange = 0;
		
		for(Fish e:fishes){
			totalWeightToChange+=e.getWeight();
		}
		
		totalWeight=totalWeightToChange;
		
	}

	private void individualMovement() {
		
		for(Fish e:fishes){
			
			e.moveFishIndividual(stepInd);
						
		}
	}

	private double getMaxDeltaFitness(){
		
		double maxDelta = -10000000000000000000000000000000000000000000000000000.0;
		
		for(Fish e:fishes){
			if(Double.compare(Math.abs(e.getFitness() - e.getLastFitness()),maxDelta)>0){
				maxDelta=e.getFitness() - e.getLastFitness();
				maxDelta=Math.abs(maxDelta);
			}
		}
		
		return Math.max(maxDelta, 0.0001);
	}
	
	private void updateBestFish(){
		
		for(Fish e:fishes){
			
			if((Double.compare(e.getFeasibility()[1],this.epsilon)<0 && Double.compare(this.bestFeasibility[1],this.epsilon)<0)||(Double.compare(e.getFeasibility()[1],this.bestFeasibility[1])==0)){
				if(Double.compare(e.getFitness(),this.bestFitness)<0){
					bestFitness=e.getFitness();
					bestPosition=e.getPosition();
					bestFeasibility=e.getFeasibility();
				}
			}else if(Double.compare(e.getFeasibility()[1],this.bestFeasibility[1])<0){
					bestFitness=e.getFitness();
					bestPosition=e.getPosition();
					bestFeasibility=e.getFeasibility();
			}
			
		}
		
	}

	private void updateFitnesses() {
		
		for(Fish e:fishes){
			e.calculateFitness();
		}
		
	}
	
	private void updateBarycenter(){
		
		double[] actualPosition;
		double[] barycenterToUpdate = new double[Run.DIMENSIONS];
		double actualWeight;
		
		for(int j=0;j<barycenterToUpdate.length;j++){
			barycenterToUpdate[j]=0.0;
		}
		
		for(Fish e:fishes){
			actualPosition=e.getPosition();
			actualWeight=1/e.getWeight();
			for(int i=0; i<actualPosition.length; i++){
				barycenterToUpdate[i]+=actualPosition[i]*actualWeight;
			}
		}
		
		for(int k=0;k<barycenterToUpdate.length;k++){
			barycenterToUpdate[k]/=totalWeight;
		}
		
		barycenter=barycenterToUpdate;
	}

	public double getBestFitness() {
		// TODO Auto-generated method stub
		return bestFitness;
	}

	public double[] getBestPosition() {
		// TODO Auto-generated method stub
		
		return bestPosition.clone();
	}
	
	private void updateHistory(int currentIteration){
		
		int i = 0;
		
		for (Fish e:fishes){
			this.history[currentIteration][i] = e.getPosition();
			i++;
		}
	}
	
	public int[] getAdress(double[] position) {
		
		int[] adress = new int[Run.DIMENSIONS];
		
		for(int i=0; i< adress.length; i++){
			
			double adressComponent=position[i]/Run.CELL_WIDTH;
			adressComponent = Math.abs(adressComponent);
			adressComponent = Math.ceil(adressComponent);
			
			if(Double.compare(position[i],0.0)>0){
				adress[i]=(int)adressComponent;
			}else if(Double.compare(position[i],0.0)==0){
				adress[i]=1;
			}else{
				adress[i]=(int)(-1*adressComponent);
			}
			
		}
		
		return adress;
	}

	public void printGrid(){
		
		Set<Integer> keysSet = grid.keySet();
		Cell cellToPrint;
		
		System.out.println("Size: "+grid.size());
		
		for(Integer key : keysSet){
			
			cellToPrint=grid.get(key);
			cellToPrint.printCell();			
			
		}
		
	}
	
	public void printKeys(){
		
		Set<Integer> keysSet = grid.keySet();
		
		System.out.println("Size: "+grid.size());
		
		for(Integer key : keysSet){
			
			System.out.println("Key: "+key);			
			
		}
		
	}

	public ArrayList<Double> getBestFitnessConvergence() {
		// TODO Auto-generated method stub
		return this.bestFitnessConvergence;
	}
	
	public ArrayList<Double> getBestFeasibilityConvergence() {
		// TODO Auto-generated method stub
		return this.bestFeasibilityConvergence;
	}

	public double[] getBestFeasibility() {
		// TODO Auto-generated method stub
		return this.bestFeasibility;
	}
	
	public double getEpsilon(){
		return this.epsilon;
	}
	
	public double getEpsilonZero(){
		return this.epsilonZero;
	}
	
}
