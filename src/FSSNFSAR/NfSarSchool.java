package FSSNFSAR;

import java.util.ArrayList;
import General.Run;

public class NfSarSchool {

	public double[][][] history;
	private ArrayList<Fish> fishes;
	private double[] barycenter;
	private double totalWeight;
	private double lastTotalWeight;
	private double stepInd;
	private double stepVol;
	private double bestFitness;
	private double[] bestPosition;
	private double fMin;
	private double fMax;
	private ArrayList<Double> bestFitnessConvergence;
	
	public NfSarSchool(){
		
		this.fishes = new ArrayList<Fish>();
		this.history = new double[Run.NUMBER_OF_ITERATIONS + 1][Run.NUMBER_OF_FISH][Run.DIMENSIONS];
		this.bestFitnessConvergence = new ArrayList<Double>();
		fMin = 10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		fMax = -1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		
		for(int i=0;i<Run.NUMBER_OF_FISH;i++){
			fishes.add(new Fish());			
		}
		
		bestFitness=-100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		stepInd=Run.INITIAL_IND_STEP;
		stepVol=Run.INITIAL_VOL_STEP;
		
	}
	
	public void runSearch(){
		
		for(int i=0;i<Run.NUMBER_OF_ITERATIONS;i++){
			
			updateFitnesses();
			updateBestFish();	
			this.bestFitnessConvergence.add(this.bestFitness);
			individualMovement(i);
			updateFBoundaries();
			feed();
			collectiveInstintiveMovement();
			updateBarycenter();
			collectiveVolitiveMovement();
			updateSteps(i);
			updateHistory(i);
			
		}
		
		updateFitnesses();
		updateBestFish();	
		this.bestFitnessConvergence.add(this.bestFitness);
	}
	
	private void updateFBoundaries() {
		
		double fMaxToUpdate=getMaxF();
		double fMinToUpdate=getMinF();
		
		if(Double.compare(fMaxToUpdate,fMax)>0){
			fMax=fMaxToUpdate;
		}
		
		if(Double.compare(fMinToUpdate,fMin)<0){
			fMin=fMinToUpdate;
		}
		
	}

	private double getMinF() {
		
		double minF = 1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		double fishFitness;
		
		for(Fish e:fishes){
			fishFitness=e.getFitness();
			if(Double.compare(fishFitness,minF)<0){
				minF=fishFitness;
			}
		}
		
		return minF;
	}

	private double getMaxF() {
		double maxF = -10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		double fishFitness;
		
		for(Fish e:fishes){
			fishFitness=e.getFitness();
			if(Double.compare(fishFitness,maxF)>0){
				maxF=fishFitness;
			}
		}
		
		return maxF;
	}

	private void updateSteps(int currentIteration){
		stepInd=stepInd-(Run.INITIAL_IND_STEP-Run.FINAL_IND_STEP)/Run.NUMBER_OF_ITERATIONS;
		stepVol=stepVol-(Run.INITIAL_VOL_STEP-Run.FINAL_VOL_STEP)/Run.NUMBER_OF_ITERATIONS;
	}
	
	private void collectiveVolitiveMovement() {
		
		double deltaw=totalWeight-lastTotalWeight;
		
		if(Double.compare(deltaw,0.0)>0){
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
		
		for(Fish e:fishes){
			e.calculateI(fishes);
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

	private void individualMovement(int iteration) {
		
		for(Fish e:fishes){
			
			e.moveFishIndividual(stepInd, iteration);
						
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
		
		double[] actualPosition;
		double[] barycenterToUpdate = new double[Run.DIMENSIONS];
		double actualWeight;
		
		for(int j=0;j<barycenterToUpdate.length;j++){
			barycenterToUpdate[j]=0.0;
		}
		
		for(Fish e:fishes){
			actualPosition=e.getPosition();
			actualWeight=e.getWeight();
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

	public ArrayList<Double> getBestFitnessConvergence() {
		// TODO Auto-generated method stub
		return this.bestFitnessConvergence;
	}

	
}