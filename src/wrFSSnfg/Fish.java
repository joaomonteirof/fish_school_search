package wrFSSnfg;

import java.util.ArrayList;
import java.util.HashMap;

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
	
	public boolean checkFitnessImprove(double newFitness){
		
		if(Double.compare(newFitness,this.fitness)<0){
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
	
	public void calculateIFitness(int iteration){
		
		double[] IToUpdate = new double[Run.DIMENSIONS];
		double[] fishPosition = this.getPosition();
		double[] leaderPosition;
		double[] fishLastPosition = this.getLastPosition();
		double[] leaderLastPosition;
		double fishFitness = fitness;
		double fishLastFitness = lastFitness;
		double leaderFitness;
		double leaderLastFitness;
		double alpha=iteration/Run.NUMBER_OF_ITERATIONS;		
		
		for(int i=0;i<IToUpdate.length;i++){
			IToUpdate[i]=0.0;
		}
		
		double sumFitnessDifference = 0;
		
		if(leader!=null){
			
			leaderPosition=leader.getPosition();
			leaderLastPosition=leader.getLastPosition();
			leaderFitness = leader.getFitness();
			leaderLastFitness = leader.getLastFitness();
			
			
			if(Double.compare(fishLastFitness,fishFitness)>0 && Double.compare(leaderLastFitness,leaderFitness)>0){
			
				for(int i=0;i<IToUpdate.length;i++){
					IToUpdate[i]=(fishPosition[i]-fishLastPosition[i])*(fishLastFitness-fishFitness)+
							(leaderPosition[i]-leaderLastPosition[i])*(leaderLastFitness-leaderFitness);
				}
				sumFitnessDifference=(fishLastFitness-fishFitness)+(leaderLastFitness-leaderFitness);				
			}
			
		}else if(Double.compare(fishLastFitness,fishFitness)>0){
			for(int i=0;i<IToUpdate.length;i++){
				IToUpdate[i]=(fishPosition[i]-fishLastPosition[i])*(fishLastFitness-fishFitness);
			}
			sumFitnessDifference=(fishLastFitness-fishFitness);
		}
		
		if(sumFitnessDifference == 0){
			sumFitnessDifference = 1;
		}
		
		for(int k = 0; k < IToUpdate.length; k++){
			IToUpdate[k] = alpha*IToUpdate[k] / sumFitnessDifference; 
		}
		
		I=IToUpdate;
		
	}
	
	public void calculateIFeasibility(int iteration){
		
		double[] IToUpdate = new double[Run.DIMENSIONS];
		double[] fishPosition = this.getPosition();
		double[] leaderPosition;
		double[] fishLastPosition = this.getLastPosition();
		double[] leaderLastPosition;
		double[] fishFeasibility = this.feasibility;
		double[] fishLastFeasibility = this.lastFeasibility;
		double[] leaderFeasibility;
		double[] leaderLastFeasibility;
		double alpha=iteration/Run.NUMBER_OF_ITERATIONS;
		
		
		for(int i=0;i<IToUpdate.length;i++){
			IToUpdate[i]=0.0;
		}
		
		double sumFiDifference = 0;
		
		if(leader!=null){
			
			leaderPosition=leader.getPosition();
			leaderLastPosition=leader.getLastPosition();
			leaderFeasibility = leader.getFeasibility();
			leaderLastFeasibility = leader.getLastFeasibility();
			
			if(Double.compare(fishLastFeasibility[1],fishFeasibility[1])>0 && Double.compare(leaderLastFeasibility[1],leaderFeasibility[1])>0){
				for(int i=0;i<IToUpdate.length;i++){
					IToUpdate[i]=(fishPosition[i]-fishLastPosition[i])*(fishLastFeasibility[1]-fishFeasibility[1])+
							(leaderPosition[i]-leaderLastPosition[i])*(leaderLastFeasibility[1]-leaderFeasibility[1]);
				}
				sumFiDifference=(fishLastFeasibility[1]-fishFeasibility[1])+(leaderLastFeasibility[1]-leaderFeasibility[1]);
			}			
		}else if(Double.compare(fishLastFeasibility[1],fishFeasibility[1])>0){
			for(int i=0;i<IToUpdate.length;i++){
				IToUpdate[i]=(fishPosition[i]-fishLastPosition[i])*(fishLastFeasibility[1]-fishFeasibility[1]);
			}
			sumFiDifference=(fishLastFeasibility[1]-fishFeasibility[1]);
		}
		
		if(sumFiDifference == 0){
			sumFiDifference = 1;
		}
		
		for(int k = 0; k < IToUpdate.length; k++){
			IToUpdate[k] = alpha*IToUpdate[k] / sumFiDifference; 
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

	public void moveFishIndividualFitness(double step, int iteration) {
		
		double randNum;
		double newFitness;
		double[] displacement = new double[Run.DIMENSIONS];
		double[] candidatePosition = new double[Run.DIMENSIONS];
		double alpha = 0.8*Math.exp(-0.007*iteration);
		double randSar = Run.generator.nextDouble();
		
		for(int i=0;i<displacement.length;i++){
			randNum=Run.generator.nextDouble();
			displacement[i]=2*randNum*step-step;
		}
		
		candidatePosition=findNewPosition(displacement);
		newFitness=FitnessFunction.calculateFitness(candidatePosition);
		
		if(checkFitnessImprove(newFitness)){
			updateFitness(newFitness);
			updatePosition(candidatePosition);
			updateFeasibility(FeasibilityAnalysis.isFeasible(candidatePosition));
		}else if(Double.compare(randSar,alpha)<0){
			updateFitness(newFitness);
			updatePosition(candidatePosition);
			updateFeasibility(FeasibilityAnalysis.isFeasible(candidatePosition));
		}
		
	}
	
	public void moveFishIndividualFeasibility(double step, int iteration) {
		
		double randNum;
		double[] newFeasibility;
		double[] displacement = new double[Run.DIMENSIONS];
		double[] candidatePosition = new double[Run.DIMENSIONS];
		double alpha = 0.8*Math.exp(-0.007*iteration);
		double randSar = Run.generator.nextDouble();
		
		for(int i=0;i<displacement.length;i++){
			randNum=Run.generator.nextDouble();
			displacement[i]=2*randNum*step-step;
		}
		
		candidatePosition=findNewPosition(displacement);
		newFeasibility=FeasibilityAnalysis.isFeasible(candidatePosition);
		
		if(checkFiImprove(newFeasibility)){
			updateFitness(FitnessFunction.calculateFitness(candidatePosition));
			updatePosition(candidatePosition);
			updateFeasibility(newFeasibility);
		}else if(Double.compare(randSar,alpha)<0){
			updateFitness(FitnessFunction.calculateFitness(candidatePosition));
			updatePosition(candidatePosition);
			updateFeasibility(newFeasibility);
		}
		
	}
	
	private boolean checkFiImprove(double[] newFeasibility) {

		if(Double.compare(newFeasibility[1],this.feasibility[1])<=0){
			return true;
		}else{
			return false;
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
	
	public double[] getGradient(){
		
		double[] positionToPertubate = new double[Run.DIMENSIONS];
		double[] currentPosition = new double[Run.DIMENSIONS];
		double[] gradient = new double[Run.DIMENSIONS];
		double[] testFeasibility = new double[3];
		double currentFi = this.feasibility[1];
		double pertubatedFi;

		for(int j=0;j<gradient.length;j++){
			gradient[j]=0.0;
		}
		
		currentPosition = this.position.clone();
		
		for(int i=0;i<positionToPertubate.length;i++){
			
			positionToPertubate[i] = currentPosition[i]+Run.pertubation;
			testFeasibility=FeasibilityAnalysis.isFeasible(positionToPertubate);
			pertubatedFi = (testFeasibility[1]-currentFi)/Run.pertubation;
			gradient[i] = pertubatedFi;
		}
		
		return gradient;
	}
	
	public double innerProduct(double[] array1, double[] array2){
		
		double product = 0.0;
		int minimumLength = array1.length;
		if(array2.length<minimumLength){
			minimumLength=array2.length;
		}
		
		for(int i=0; i<minimumLength; i++){
			product+=array1[i]*array2[i];
		}
		
		return product;
	}
	
	public double[] generateRandomDirection(){
		
		double[] direction = new double[Run.DIMENSIONS];
		double randNum;
		
		for(int i=0; i<direction.length;i++){
			randNum=Run.generator.nextDouble();
			direction[i] = Math.cos(randNum*Math.PI);
		}
		
		return direction;
		
	}

	public void moveFishIndividualGradientFitness(double stepInd, int iteration) {
		
		double[] candidateDirection = new double[Run.DIMENSIONS];
		double[] chosenDirection = new double[Run.DIMENSIONS];
		double directionalDerivative = 0.0;
		double minDirectionalDerivative = 1111111111111111111100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0;
		double[] gradient = getGradient();
		HashMap<Double, double[]> candidatesList = new HashMap<Double, double[]>();
		
		for(int i=0;i<Run.numberOfCandidateDirections;i++){
			candidateDirection=generateRandomDirection();
			directionalDerivative=Math.abs(innerProduct(candidateDirection, gradient));
			candidatesList.put(directionalDerivative, candidateDirection);
			
			if(Double.compare(directionalDerivative,minDirectionalDerivative)<0){
				minDirectionalDerivative=directionalDerivative;
			}
			
		}
		
		chosenDirection=candidatesList.get(minDirectionalDerivative);
		//Run.printArray(chosenDirection);
		
		double newFitness;
		double[] candidatePosition = new double[Run.DIMENSIONS];
		double alpha = 0.8*Math.exp(-0.007*iteration);
		double randSar = Run.generator.nextDouble();
		
		for(int j=0;j<candidatePosition.length;j++){
			candidatePosition[j]=stepInd*chosenDirection[j]+position[j];
		}
		
		newFitness=FitnessFunction.calculateFitness(candidatePosition);
		
		if(checkFitnessImprove(newFitness)){
			updateFitness(newFitness);
			updatePosition(candidatePosition);
			updateFeasibility(FeasibilityAnalysis.isFeasible(candidatePosition));
		}else if(Double.compare(randSar,alpha)<0){
			updateFitness(newFitness);
			updatePosition(candidatePosition);
			updateFeasibility(FeasibilityAnalysis.isFeasible(candidatePosition));
		}
		
	}

	public void moveFishIndividualGradientFeasibility(double stepInd, int iteration) {
		
		double[] candidateDirection = new double[Run.DIMENSIONS];
		double[] chosenDirection = new double[Run.DIMENSIONS];
		double directionalDerivative = 0.0;
		double minDirectionalDerivative = 111111111111111111110000000000000000000000000000000000000000000000000000.0;
		double[] gradient = getGradient();
		HashMap<Double, double[]> candidatesList = new HashMap<Double, double[]>();
		
		for(int i=0;i<Run.numberOfCandidateDirections;i++){
			candidateDirection=generateRandomDirection();
			directionalDerivative=innerProduct(candidateDirection, gradient);
			candidatesList.put(directionalDerivative, candidateDirection);
			
			if(Double.compare(directionalDerivative,minDirectionalDerivative)<0){
				minDirectionalDerivative=directionalDerivative;
			}
			
		}
		
		chosenDirection=candidatesList.get(minDirectionalDerivative);
		
		double[] newFeasibility = new double[3];
		double[] candidatePosition = new double[Run.DIMENSIONS];
		double alpha = 0.8*Math.exp(-0.007*iteration);
		double randSar = Run.generator.nextDouble();
		
		for(int i=0;i<candidatePosition.length;i++){
			candidatePosition[i]=stepInd*chosenDirection[i]+position[i];
		}
		
		newFeasibility=FeasibilityAnalysis.isFeasible(candidatePosition);
		
		if(checkFiImprove(newFeasibility)){
			updateFitness();
			updatePosition(candidatePosition);
			updateFeasibility(newFeasibility);
		}else if(Double.compare(randSar,alpha)<0){
			updateFitness();
			updatePosition(candidatePosition);
			updateFeasibility(newFeasibility);
		}
		
	}

}