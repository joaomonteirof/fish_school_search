package wrFSSnf;

import java.util.ArrayList;
import General.Run;


public class WRNFSchool {

	public double[][][] history;
	private ArrayList<Fish> fishes;
	private double totalWeight;
	private double lastTotalWeight;
	private double stepInd;
	private double stepVol;
	private double bestFitness;
	private double[] bestPosition;
	private double[] bestFeasibility;
	private boolean anyFeasible;
	private double fMin;
	private double fMax;
	private double fiMin;
	private double fiMax;
	private int fitnessEvaluations;
	private ArrayList<Double> bestFitnessConvergence;
	private ArrayList<Double> bestFeasibilityConvergence;
	
	public WRNFSchool(){
		
		this.fishes = new ArrayList<Fish>();
		this.bestFitnessConvergence = new ArrayList<Double>();
		this.bestFeasibilityConvergence = new ArrayList<Double>();
		this.history = new double[Run.NUMBER_OF_ITERATIONS + 1][Run.NUMBER_OF_FISH][Run.DIMENSIONS];
		
		for(int i=0;i<Run.NUMBER_OF_FISH;i++){
			fishes.add(new Fish());			
		}
		
		bestFeasibility = new double[3];
		bestFeasibility[0] = 100000.0;
		bestFeasibility[1] = 100000000000000000000000000000000000000000000000000000000000000000.0;		
		bestFitness=1000000000000000000000000000000000000000000000000000000000000000.0;
		fMin = 100000000000000000000000000000000000000000000000000000000000000000000.0;
		fMax = -10000000000000000000000000000000000000000000000000000000000000000000.0;
		fiMin = 10000000000000000000000000000000000000000000000000000000000000000000.0;
		fiMax = -1000000000000000000000000000000000000000000000000000000000000000000.0;
		stepInd=Run.INITIAL_IND_STEP;
		stepVol=Run.INITIAL_VOL_STEP;
		fitnessEvaluations = Run.NUMBER_OF_FISH;
		
	}
	
	public void runSearch(){
		
		for(int i=0; i<Run.NUMBER_OF_ITERATIONS;i++){
			updateAnyFeasible();
			//linkFormator();
			individualMovement(i);
			updateBoundaries();
			feed(i);
			linkFormator();
			collectiveInstintiveMovement(i);
			collectiveVolitiveMovement();
			updateFitnesses();
			updateFeasibility();
			updateBestFish();
			updateSteps(i);
			updateHistory(i);			
			this.bestFitnessConvergence.add(this.bestFitness);
			this.bestFeasibilityConvergence.add(this.bestFeasibility[1]);
		}
	}

	private void updateFeasibility() {
		
		for(Fish e:fishes){
			e.evaluateFeasibility();
		}
		
	}
	
	private void updateBoundaries() {
		
		double maxFToUpdate = getMaxF();
		double minFToUpdate = getMinF();
		double maxFiToUpdate = getMaxFi();
		double minFiToUpdate = getMinFi();
		
		if(Double.compare(maxFToUpdate,fMax)>0){
			fMax=maxFToUpdate;
		}
		
		if(Double.compare(minFToUpdate,fMin)<0){
			fMin=minFToUpdate;
		}
		
		if(Double.compare(maxFiToUpdate,fiMax)>0){
			fiMax=maxFiToUpdate;
		}
		
		if(Double.compare(minFiToUpdate,fiMin)<0){
			fiMin=minFiToUpdate;
		}
		
	}
	
	private double getMaxF() {
		double maxF = -10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
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
		
		double minF = 1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		double fishFitness;
		
		for(Fish e:fishes){
			fishFitness=e.getFitness();
			if(Double.compare(fishFitness,minF)<0){
				minF=fishFitness;
			}
		}
		
		return minF;
	}
	
	private double getMaxFi() {
		double maxFi = -10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		double fishFi;
		
		for(Fish e:fishes){
			fishFi=e.getFeasibility()[1];
			if(Double.compare(fishFi,maxFi)>0){
				maxFi=fishFi;
			}
		}
		
		return maxFi;
	}
	
	private double getMinFi() {
		
		double minFi = 100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		double fishFi;
		
		for(Fish e:fishes){
			fishFi=e.getFeasibility()[1];
			if(Double.compare(fishFi,minFi)<0){
				minFi=fishFi;
			}
		}
		
		return minFi;
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
	
	private void updateAnyFeasible() {
		
		boolean lastFeasible=this.anyFeasible;
		int feasibleCount = 0;
		
		for(Fish e:fishes){
			if(e.getFeasibility()[0]<1){
				//System.out.println("Feasible!");
				feasibleCount++;
			}
		}
		
		if(Double.compare((feasibleCount/Run.NUMBER_OF_FISH),Run.PP)>0){
			this.anyFeasible=true;
		}else{
			this.anyFeasible=false;
		}
		
		if(anyFeasible!=lastFeasible){
			stepInd=Math.min(stepInd*Run.stepIncrease,Run.INITIAL_IND_STEP);
			stepVol=Math.min(stepVol*Run.stepIncrease,Run.INITIAL_VOL_STEP);
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
		
		//System.out.println("StepInd >> " + stepInd);
		//System.out.println("StepVol >> " + stepVol);
		stepInd=stepInd-(stepInd-Run.FINAL_IND_STEP)/(Run.NUMBER_OF_ITERATIONS-currentIteration);
		stepVol=stepVol-(stepVol-Run.FINAL_VOL_STEP)/(Run.NUMBER_OF_ITERATIONS-currentIteration);
		
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
		
		if(this.anyFeasible){
			for(Fish e:fishes){
				e.calculateIFitness(iteration);
			}
		}else{
			for(Fish e:fishes){
				e.calculateIFeasibility(iteration);
			}
		}
		
		for(Fish a:fishes){
			a.moveFishColInstinctive();
		}
		
	}

	private void feed(int iteration) {
		
		double totalLastWeightToChange = 0;
		double totalWeightToChange = 0;
		
		if(this.anyFeasible){
			for(Fish e:fishes){
				e.feedFishFitness(fMin, fMax);
				totalLastWeightToChange+=e.getLastWeight();
				totalWeightToChange+=e.getWeight();
			}
			
			lastTotalWeight=totalLastWeightToChange;
			totalWeight=totalWeightToChange;
		}else{
			for(Fish e:fishes){
				e.feedFishFeasibility(fiMin, fiMax);
				totalLastWeightToChange+=e.getLastWeight();
				totalWeightToChange+=e.getWeight();
			}
			
			lastTotalWeight=totalLastWeightToChange;
			totalWeight=totalWeightToChange;
		}
		
	}

	private void individualMovement(int iteration) {
		
		if(this.anyFeasible){
			for(Fish e:fishes){				
				e.moveFishIndividualFitness(stepInd,iteration);						
			}
			
			fitnessEvaluations+=Run.NUMBER_OF_FISH;
			
		}else{
			for(Fish e:fishes){				
				e.moveFishIndividualFeasibility(stepInd,iteration);						
			}
		}
	}
	
	private void updateBestFish(){
		
		for(Fish e:fishes){
			
			if(e.getFeasibility()[0]>0 && this.bestFeasibility[0]>0){
				if(Double.compare(e.getFeasibility()[1],this.bestFeasibility[1])<0){
					bestFitness=e.getFitness();
					bestPosition=e.getPosition();
					bestFeasibility=e.getFeasibility();
				}
			}else if(e.getFeasibility()[0]<1&&this.bestFeasibility[0]>0){
					bestFitness=e.getFitness();
					bestPosition=e.getPosition();
					bestFeasibility=e.getFeasibility();
			}else if(e.getFeasibility()[0]<1&&this.bestFeasibility[0]<1){
				if(Double.compare(e.getFitness(),this.bestFitness)<0){
					bestFitness=e.getFitness();
					bestPosition=e.getPosition();
					bestFeasibility=e.getFeasibility();
				}
			}
		}		
	}

	private void updateFitnesses() {
		

		for(Fish e:fishes){
			e.calculateFitness();
		}
		
		fitnessEvaluations+=Run.NUMBER_OF_FISH;
		
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
	
	public ArrayList<Double> getBestFeasibilityConvergence() {
		// TODO Auto-generated method stub
		return this.bestFeasibilityConvergence;
	}

	public double[] getBestFeasibility() {

		return this.bestFeasibility;
	}
	
}