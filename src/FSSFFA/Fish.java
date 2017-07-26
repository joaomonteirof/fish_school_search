package FSSFFA;

import java.util.ArrayList;

import General.FitnessFunction;
import General.Run;

public class Fish {
	
	private double[] position;
	private double[] lastPosition;
	private double[] lastDeltaPosition;
	private double fitness;
	private double lastFitness;
	private double weight;
	private double lastWeight;
	private double[] I;
	private boolean hasMoved;
	
	public Fish(){
		
		position = new double[Run.DIMENSIONS];
		lastPosition = new double[Run.DIMENSIONS];
		lastDeltaPosition = new double[Run.DIMENSIONS];
		I = new double[Run.DIMENSIONS];
		
		position=generateRandomPosition();
		updateFitness();
		
		updatePosition(position);
		updateFitness(fitness);
		
		for(int i=0;i<lastDeltaPosition.length;i++){
			lastDeltaPosition[i] = 0.0;
		}

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
			randNum=Run.generator.nextDouble();
			generatedPosition[i]=randNum*(xMax-xMin)+xMin;
		}
		
		return generatedPosition;
	}
	
	public boolean checkFitnessImprove(double newFitness){
		
		if(Double.compare(newFitness,fitness)>0){
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
	
	public void calculateI(ArrayList<Fish> fishesList,double fakeDeltaX, double maxDeltaW){
		
		double[] IToUpdate = new double[Run.DIMENSIONS];
		double[] fakeDisplacement = new double[Run.DIMENSIONS];
		double fakeDeltaW;
		
		for(int i=0;i<IToUpdate.length;i++){
			IToUpdate[i]=0.0;
		}
		
		double sumWeightDifference = 0;
		
		for(Fish e : fishesList){
			
			fakeDeltaW = maxDeltaW*(e.getWeight()-1)/(Run.W_SCALE-1);
			
			if(e.getHasMoved()){
				for(int i=0;i<IToUpdate.length;i++){
					IToUpdate[i]+=(e.getPosition()[i]-e.getLastPosition()[i])*(e.getWeight()-e.getLastWeight());
				}
				sumWeightDifference+=(e.getWeight()-e.getLastWeight());
			}else{
				fakeDisplacement=e.getLastDeltaPosition();
				for(int i=0;i<IToUpdate.length;i++){
					IToUpdate[i]+=(fakeDisplacement[i]*fakeDeltaX)*(fakeDeltaW);
				}
				sumWeightDifference+=(fakeDeltaW);
			}			
		}
		
		if(Double.compare(sumWeightDifference,0.0) == 0){
			sumWeightDifference = 1;
		}
		
		for(int k = 0; k < IToUpdate.length; k++){
			IToUpdate[k] = IToUpdate[k] / sumWeightDifference; 
		}
		
		I=IToUpdate;
		
	}	

	private double[] getLastDeltaPosition() {
		return this.lastDeltaPosition.clone();
	}
	
	private void updateLastDeltaPosition(double stepInd) {
		
		double[] deltaX = new double[Run.DIMENSIONS];
		
		for(int i=0;i<deltaX.length;i++){
			deltaX[i] = (position[i]-lastPosition[i])/stepInd;
		}
		
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

	public void moveFishIndividual(double step) {
		
		double randNum;
		double newFitness;
		double[] displacement = new double[Run.DIMENSIONS];
		double[] candidatePosition = new double[Run.DIMENSIONS];
		
		for(int i=0;i<displacement.length;i++){
			randNum=Run.generator.nextDouble();
			displacement[i]=2*randNum*step-step;
		}
		
		candidatePosition=findNewPosition(displacement);
		newFitness=FitnessFunction.calculateFitness(candidatePosition);
		
		if(checkFitnessImprove(newFitness)){
			updateFitness(newFitness);
			updatePosition(candidatePosition);
			updateLastDeltaPosition(step);
			hasMoved=true;
		}else{
			hasMoved=false;
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

	public void feedFish(double maxF, double minF) {
		
		lastWeight = 1 + (Run.W_SCALE-1)*(lastFitness-minF)/(maxF-minF);
		weight = 1 + (Run.W_SCALE-1)*(fitness-minF)/(maxF-minF);
		
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
	
	public boolean getHasMoved() {
		
		return this.hasMoved;
	}

	public void moveFishVolitive(double[] barycenter, double stepVol, int sense) {
				
		double[] displacement = new double[Run.DIMENSIONS];
		double randNum;
		double distance=euclidianDistance(barycenter,position);
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

}