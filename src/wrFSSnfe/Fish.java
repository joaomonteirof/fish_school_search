package wrFSSnfe;

import java.util.ArrayList;

import General.FeasibilityAnalysis;
import General.FitnessFunction;
import General.Run;

public class Fish {
	
	private double[] position;
	private double[] lastPosition;
	private double fitness;
	private double lastFitness;
	private double weight;
	private double lastWeight;
	private double[] I;
	private Fish leader;
	private ArrayList<Fish> team;
	private double[] barycenter;
	private double[] feasibility;
	private double[] lastFeasibility;
	private double epsilon;
	
	public Fish(){
		
		position = new double[Run.DIMENSIONS];
		lastPosition = new double[Run.DIMENSIONS];
		I = new double[Run.DIMENSIONS];
		barycenter = new double[Run.DIMENSIONS];
		leader = null;
		team = new ArrayList<Fish>();
		feasibility = new double[3];
		lastFeasibility = new double[3];
		
		position=generateRandomPosition();
		updateFitness();
		
		updatePosition(position);
		updateFitness(fitness);
		this.feasibility=FeasibilityAnalysis.isFeasible(this.position);
		this.updateFeasibility(this.feasibility);
	}

	public void updateWeight(double j) {
		
		lastWeight=weight;
		weight=j;
		
	}

	public void updateFitness(double newFitness) {
		
		lastFitness=fitness;
		fitness=newFitness;
		
	}

	private void updatePosition(double[] newPosition) {
				
		lastPosition=position;
		position=newPosition;
		
	}

	public void updateFitness() {
		lastFitness = fitness;
		fitness=FitnessFunction.calculateFitness(position);
	}

	private double[] generateRandomPosition() {
		
		double xMin = Run.LOWEST_SPACE_BOUNDARY;
		double xMax = Run.HIGHEST_SPACE_BOUNDARY;
		double[] generatedPosition = new double[Run.DIMENSIONS];
		double randNum;
				
		for(int i=0;i<generatedPosition.length;i++){			
			
			if(Run.FITNESS_FUNCTION == 6 ||
					Run.FITNESS_FUNCTION == 7 ||
					Run.FITNESS_FUNCTION == 8){
			
				randNum=Run.generator.nextDouble();
			
				if(Run.NUMBER_OF_DISKS == 4){
					if(randNum>0.5){
						xMin = 75;
						xMax = 87.5;
					}else{
						xMin = -87.5;
						xMax = -75;
					}
				}
				else{
					if(randNum>0.5){
                        xMin = 87.5;
                        xMax = 93.75;
                  }else{
                        xMin = -93.75;
                        xMax = -87.5;
                  }
				}			
			}
			
			randNum=Run.generator.nextDouble();
			generatedPosition[i]=randNum*(xMax-xMin)+xMin;
		}
		
		return generatedPosition;
	}
	
	public boolean checkImprove(double newFitness, double newFi){
		
		if((Double.compare(this.feasibility[1],this.epsilon)<0 && Double.compare(newFi,this.epsilon)<0) || Double.compare(this.feasibility[1],newFi)==0){
			if(Double.compare(newFitness,fitness)<0){
				return true;
			}else{
				return false;
			}
		}else if(Double.compare(newFi,this.feasibility[1])<0){
			return true;
		}else{
			return false;
		}		
	}
	
	public void moveFish(double[] displacement){
		double[] actualPosition = position;
		double[] finalPosition = new double[Run.DIMENSIONS];
		
		for(int i=0;i<displacement.length;i++){
			finalPosition[i]=actualPosition[i]+displacement[i];
		}
		
		this.updatePosition(bounderingControl(finalPosition));
		
	}
	
	private double[] bounderingControl(double[] positionToCheck){
		
		double[] toCorrect = positionToCheck;
		double xMin = Run.LOWEST_SPACE_BOUNDARY;
		double xMax = Run.HIGHEST_SPACE_BOUNDARY;
		
		for(int i=0;i<toCorrect.length;i++){
			
			if(Double.compare(toCorrect[i],xMax)>0){
				toCorrect[i]=xMax;
			}else if(Double.compare(toCorrect[i],xMin)<0){
				toCorrect[i]=xMin;
			}			
		}
		
		return toCorrect;
	}
	
	public void calculateI(int iteration){
		
		double[] IToUpdate = new double[Run.DIMENSIONS];
		double[] fishPosition = this.getPosition();
		double[] leaderPosition;
		double[] fishLastPosition = this.getLastPosition();
		double[] leaderLastPosition;
		double fishWeight = fitness;
		double fishLastWeight = lastFitness;
		double leaderWeight;
		double leaderLastWeight;
		double alpha=iteration/Run.NUMBER_OF_ITERATIONS;
				
		for(int i=0;i<IToUpdate.length;i++){
			IToUpdate[i]=0.0;
		}
		
		double sumWeightDifference = 0;
		
		if(leader!=null){
			
			leaderPosition=leader.getPosition();
			leaderLastPosition=leader.getLastPosition();
			leaderWeight = leader.getWeight();
			leaderLastWeight = leader.getLastWeight();
			
			if(Double.compare(fishWeight,fishLastWeight)>0 && Double.compare(leaderWeight,leaderLastWeight)>0){
				for(int i=0;i<IToUpdate.length;i++){
					IToUpdate[i]=(fishPosition[i]-fishLastPosition[i])*(fishWeight-fishLastWeight)+
							(leaderPosition[i]-leaderLastPosition[i])*(leaderWeight-leaderLastWeight);
				}
				sumWeightDifference=(fishWeight-fishLastWeight)+(leaderWeight-leaderLastWeight);
			}
		}else if(Double.compare(fishWeight,fishLastWeight)>0){
			for(int i=0;i<IToUpdate.length;i++){
				IToUpdate[i]=(fishPosition[i]-fishLastPosition[i])*(fishWeight-fishLastWeight);
			}
			sumWeightDifference=(fishWeight-fishLastWeight);
		}
		
		if(Double.compare(sumWeightDifference,0.0) == 0){
			sumWeightDifference = 1;
		}
		
		for(int k = 0; k < IToUpdate.length; k++){
			IToUpdate[k] = alpha*IToUpdate[k] / sumWeightDifference; 
		}
		
		I=IToUpdate;
		
	}	

	public double[] getLastFeasibility() {
		return this.lastFeasibility;
	}

	public double getFitness() {
		return fitness;
	}

	public double getLastFitness() {
		return lastFitness;
	}

	public double getWeight() {
		return weight;
	}

	public double getLastWeight() {
		return lastWeight;
	}

	public void moveFishIndividual(double step, int iteration) {
		
		double randNum;
		double newFitness;
		double[] newFeasibility;
		double[] displacement = new double[Run.DIMENSIONS];
		double[] candidatePosition = new double[Run.DIMENSIONS];
		double alpha = 0.8*Math.exp(-0.007*iteration);
		double randSar = Run.generator.nextDouble();
		
		for(int i=0;i<displacement.length;i++){
			randNum=Run.generator.nextDouble();
			displacement[i]=(2*randNum*step-step);
		}
		
		candidatePosition=findNewPosition(displacement);
		newFitness=FitnessFunction.calculateFitness(candidatePosition);
		newFeasibility=FeasibilityAnalysis.isFeasible(candidatePosition);
		
		if(checkImprove(newFitness, newFeasibility[1])){
			this.updateFitness(newFitness);
			this.updatePosition(candidatePosition);
			this.updateFeasibility(newFeasibility);
		}else if(Double.compare(randSar,alpha)<0){
			updateFitness(newFitness);
			updatePosition(candidatePosition);
			this.updateFeasibility(newFeasibility);
		}
		
	}
	
	private double[] findNewPosition(double[] displacement){
		
		double[] actualPosition = position;
		double[] finalPosition = new double[Run.DIMENSIONS];
		
		for(int i=0;i<displacement.length;i++){
			finalPosition[i]=actualPosition[i]+displacement[i];
		}
		
		return (bounderingControl(finalPosition));
		
	}

	public void calculateFitness() {
		updateFitness(FitnessFunction.calculateFitness(position));
		
	}

	public void feedFishFitness(double maxF, double minF) {
		
		if(Double.compare(maxF,minF)==0){
			lastWeight = 1.0;
			weight = 1.0;
		}else{
			lastWeight = Run.W_SCALE + (1-Run.W_SCALE)*(lastFitness-minF)/(maxF-minF);
			weight = Run.W_SCALE + (1-Run.W_SCALE)*(fitness-minF)/(maxF-minF);
		}
		
	}
	
	public void feedFishFeasibility(double maxFi, double minFi) {
		
		if(Double.compare(maxFi,minFi)==0){
			lastWeight = 1.0;
			weight = 1.0;
		}else{
			lastWeight = Run.W_SCALE + (1-Run.W_SCALE)*(lastFeasibility[1]-minFi)/(maxFi-minFi);
			weight = Run.W_SCALE + (1-Run.W_SCALE)*(feasibility[1]-minFi)/(maxFi-minFi);
		}
		
	}

	public void moveFishColInstinctive() {

		moveFish(I);
		
	}

	public double[] getPosition() {
		// TODO Auto-generated method stub
		return position.clone();
	}
	
	public double[] getLastPosition() {
		// TODO Auto-generated method stub
		return lastPosition.clone();
	}

	public void moveFishVolitive(double stepVol, int sense) {
				
		double[] displacement = new double[Run.DIMENSIONS];
		double randNum;
		double distance=Math.max(euclidianDistance(barycenter,position),0.0001);
		double constant;
		
		if(Double.compare(sense,0.0)<0){
			
			for(int i=0;i<displacement.length;i++){
				randNum=Run.generator.nextDouble();
				constant=-stepVol*randNum/distance;
				displacement[i]=(position[i]-barycenter[i])*constant;
			}
			
			moveFish(displacement);			
		}else{
			
			for(int i=0;i<displacement.length;i++){
				randNum=Run.generator.nextDouble();
				constant=stepVol*randNum/distance;
				displacement[i]=(position[i]-barycenter[i])*constant;
			}
			
			moveFish(displacement);
			
		}
		
	}
	
	public double euclidianDistance(double[] a, double[] b){
		
		double total = 0;
		
		for(int i=0;i<a.length;i++){
			total+=Math.pow((a[i]-b[i]),2);
		}
		
		total=Math.sqrt(total);
		
		return Math.max(total, 0.001);
	}

	public void calculateBarycenter() {

		double[] barycenterToUpdate = new double[Run.DIMENSIONS];
		double[] fishPosition = this.getPosition();
		double[] leaderPosition;
		double fishWeight = weight;
		double leaderWeight;
		double totalWeight;
		
		for(int j=0;j<barycenterToUpdate.length;j++){
			barycenterToUpdate[j]=0.0;
		}
		
		if(leader!=null){
			
			leaderPosition=this.leader.getPosition();
			leaderWeight=this.leader.getWeight();
			
			for(int i=0; i<fishPosition.length; i++){
				barycenterToUpdate[i]=fishPosition[i]*fishWeight + leaderPosition[i]*leaderWeight;
			}
			
			totalWeight=fishWeight+leaderWeight;
			
		}else{
			
			for(int i=0; i<fishPosition.length; i++){
				barycenterToUpdate[i]+=fishPosition[i]*fishWeight;
			}
			
			totalWeight=fishWeight;
			
		}		

		for(int i=0; i<fishPosition.length; i++){
			barycenterToUpdate[i]=barycenterToUpdate[i]/totalWeight;
		}
		
		barycenter=barycenterToUpdate;
		
	}

	public Fish getLeader() {
		// TODO Auto-generated method stub
		return this.leader;
	}

	public void updateLeader(Fish testFish) {
		
		double totalWeight = 0;
		
		if(leader!=null && Double.compare(weight,leader.getWeight())>0){
			leader.getTeam().remove(this);
			leader=null;
		}
		
		if(leader==null){
			if(Double.compare(testFish.getWeight(),weight)>0){
				leader=testFish;
				testFish.getTeam().add(this);
			}
		}else{
			
			for(Fish e:this.getTeam()){
				totalWeight+=e.getWeight();
			}
			
			if(Double.compare(totalWeight,testFish.getWeight())>0){
				leader=testFish;
				testFish.getTeam().add(this);
			}
			
		}
		
	}

	private ArrayList<Fish> getTeam() {
		// TODO Auto-generated method stub
		return team;
	}

	public void evaluateFeasibility() {
		
		updateFeasibility(FeasibilityAnalysis.isFeasible(position.clone()));
		
	}

	private void updateFeasibility(double[] feasible) {
		
		this.lastFeasibility=this.feasibility;
		this.feasibility=feasible;
		
	}

	public double[] getFeasibility() {
		return this.feasibility;
	}
	
	public void setEpsilon(double epsilonToUpdate) {
		this.epsilon=epsilonToUpdate;
		
	}

}