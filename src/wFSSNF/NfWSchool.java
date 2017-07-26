package wFSSNF;

import java.util.ArrayList;

import General.Run;

public class NfWSchool {

	public double[][][] history;
	private ArrayList<Fish> fishes;
	private double totalWeight;
	private double lastTotalWeight;
	private double stepInd;
	private double stepVol;
	private double bestFitness;
	private double[] bestPosition;
	private double fMin;
	private double fMax;
	private ArrayList<Double> bestFitnessConvergence;
	
	public NfWSchool(){
		
		this.fishes = new ArrayList<Fish>();
		this.bestFitnessConvergence = new ArrayList<Double>();
		this.history = new double[Run.NUMBER_OF_ITERATIONS + 1][Run.NUMBER_OF_FISH][Run.DIMENSIONS];
		
		for(int i=0;i<Run.NUMBER_OF_FISH;i++){
			fishes.add(new Fish());			
		}
		
		bestFitness=-1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		fMin = 1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		fMax = -100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		stepInd=Run.INITIAL_IND_STEP;
		stepVol=Run.INITIAL_VOL_STEP;
		totalWeight=Run.NUMBER_OF_FISH*Run.W_SCALE/2;
		lastTotalWeight=totalWeight;
		
	}
	
	public void runSearch(){
				
		for(int i=0;i<Run.NUMBER_OF_ITERATIONS;i++){
			
			updateFitnesses();
			updateBestFish();	
			this.bestFitnessConvergence.add(this.bestFitness);
			individualMovement();
			updateFBoundaries();
			feed();
			linkFormator();
			collectiveInstintiveMovement(i);
			collectiveVolitiveMovement();
			updateSteps(i);
			updateHistory(i);			
		}
		updateFitnesses();
		updateBestFish();	
		this.bestFitnessConvergence.add(this.bestFitness);
	}

private void linkFormator() {
		
		Fish testFish;
		double rand;
		
		for(Fish e:fishes){
			
			if(e.getLeader()!=null){
				do{
					rand=Math.floor(Run.generator.nextDouble()*(fishes.size()-1));
					testFish=fishes.get((int)rand);
				}
				while(isTheSame(testFish, e.getLeader()) || isTheSame(e, testFish));
			}else{
				do{
					rand=Math.floor(Run.generator.nextDouble()*(fishes.size()-1));
					testFish=fishes.get((int)rand);
				}
				while(isTheSame(e, testFish));
			}			
			
			e.updateLeader(testFish);			
			
		}
		
	}

	private boolean isTheSame(Fish a, Fish b) {
	
		double[] positionA = a.getPosition();
		double[] positionB = b.getPosition();
		double distance = 0;
		
		for(int i=0; i<positionA.length; i++){
			distance+=Math.pow(positionA[i]-positionB[i], 2);
		}
		
		if(Double.compare(distance,0.0)==0){
			return true;
		}else{
			return false;
		}
	
	}

	private void updateSteps(int currentIteration){
		stepInd=stepInd-(Run.INITIAL_IND_STEP-Run.FINAL_IND_STEP)/Run.NUMBER_OF_ITERATIONS;
		stepVol=stepVol-(Run.INITIAL_VOL_STEP-Run.FINAL_VOL_STEP)/Run.NUMBER_OF_ITERATIONS;
	}
	
	private void collectiveVolitiveMovement() {
		
		double deltaw=totalWeight-lastTotalWeight;
		
		updateBarycenter();
		
		if(Double.compare(deltaw,0.0)>0){
			for(Fish e:fishes){
				e.moveFishVolitive(stepVol, -1);
			}
		}else{
			for(Fish e:fishes){
				e.moveFishVolitive(stepVol, 1);
			}
		}		
		
	}

	private void collectiveInstintiveMovement(int iteration) {
		
		for(Fish e:fishes){
			e.calculateI(iteration);
		}
		
		for(Fish a:fishes){
			a.moveFishColInstinctive();
		}
		
	}

	private void feed() {
		
		double totalLastWeightToChange = 0;
		double totalWeightToChange = 0;
		
		for(Fish e:fishes){
			e.feedFish(fMin, fMax);
			totalLastWeightToChange+=e.getLastWeight();
			totalWeightToChange+=e.getWeight();
		}
		
		lastTotalWeight=totalLastWeightToChange;
		totalWeight=totalWeightToChange;
		
	}
	
	private void updateFBoundaries() {
		
		double maxFToUpdate = getMaxF();
		double minFToUpdate = getMinF();
		
		if(Double.compare(maxFToUpdate,fMax)>0){
			fMax=maxFToUpdate;
		}
		
		if(Double.compare(minFToUpdate,fMin)<0){
			fMin=minFToUpdate;
		}
		
	}
	
	private double getMaxF() {
		double maxF = -100000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		double fishFitness;
		
		for(Fish e:fishes){
			fishFitness=e.getFitness();
			if(Double.compare(fishFitness,maxF)>0){
				maxF=fishFitness;
			}
		}
		
		return maxF;
	}
	
	private double getMinF() {
		
		double minF = 1000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		double fishFitness;
		
		for(Fish e:fishes){
			fishFitness=e.getFitness();
			if(Double.compare(fishFitness,minF)<0){
				minF=fishFitness;
			}
		}
		
		return minF;
	}

	private void individualMovement() {
		
		for(Fish e:fishes){
			
			e.moveFishIndividual(stepInd);
						
		}
	}
	
	private void updateBestFish(){
		
		for(Fish e:fishes){
			
			if(Double.compare(e.getFitness(),bestFitness)>0){
				bestFitness=e.getFitness();
				bestPosition=e.getPosition();
			}
		}
		
	}

	private void updateFitnesses() {
		
		for(Fish e:fishes){
			e.calculateFitness();
		}
		
	}
	
	private void updateBarycenter(){
		
		for(Fish e:fishes){
			e.calculateBarycenter();
		}

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

	public ArrayList<Double> getBestFitnessConvergence(){
		return this.bestFitnessConvergence;
	}
	
}
