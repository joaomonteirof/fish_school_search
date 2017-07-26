package rFSS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
	private Cell cellOcuppied;
	private double[] feasibility;
	private double[] lastFeasibility;
	private double epsilon;
	
	public Fish(){
		
		position = new double[Run.DIMENSIONS];
		I = new double[Run.DIMENSIONS];
		feasibility = new double[3];
		lastFeasibility = new double[3];
		
		double[] positionToInit = new double[Run.DIMENSIONS];
		
		positionToInit=generateRandomPosition();
		position=positionToInit;
		weight=Run.W_SCALE/2;
		fitness=1000000000000000000000000000000000000000000000000000.0;
		updatePosition(positionToInit.clone());
		updateFitness(fitness);
		updateWeight(Run.W_SCALE/2);
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
		position=newPosition.clone();
		
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
	
	public boolean checkImprove(double newFitness, double newFi){
		
		if((Double.compare(this.feasibility[1],this.epsilon)<0 && Double.compare(newFi,this.epsilon)<0) || (Double.compare(this.feasibility[1],newFi)==0)){
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
	
	public Cell getCellOcuppied() {
		return cellOcuppied;
	}

	public void calculateIFeasibility(HashMap<Integer, Cell> grid, double step){
		
		double[] IToUpdate = new double[Run.DIMENSIONS];
		Set<Integer> keysSet = grid.keySet();
		Cell cellToCompare;
		double[] cellCentroid;
		
		for(int i=0;i<IToUpdate.length;i++){
			IToUpdate[i]=0.0;
			this.I[i]=0.0;
		}
		
		double sumFDifference = 0;
		
		for(Integer key : keysSet){
			
			cellToCompare=grid.get(key);
			cellCentroid=cellToCompare.getCentroid();
			double cellF = cellToCompare.getF();
			double selfF = this.cellOcuppied.getF();
			
			if(Double.compare(selfF,cellF)<0){
				for(int i=0;i<IToUpdate.length;i++){
					IToUpdate[i]+=(cellCentroid[i]-position[i])*(cellF-selfF);
				}
				sumFDifference+=(cellF-selfF);
			}
			
			//System.out.println("fitness: "+fitness);
			//System.out.println("cellFitness: "+cellFitness);
		}
		
		if(Double.compare(sumFDifference,0.0) != 0){
			
			for(int k = 0; k < IToUpdate.length; k++){
				IToUpdate[k] = IToUpdate[k] / sumFDifference; 
			}
			
			double iSize = modulo(IToUpdate);
			
			for(int k = 0; k < IToUpdate.length; k++){
				IToUpdate[k] = IToUpdate[k]*Math.max(step, Run.CELL_WIDTH)/iSize; 
			}
			
			this.I=IToUpdate;
			
		}		
		
		//System.out.println(modulo(IToUpdate));
		
	}
	
	public void calculateIFitness(HashMap<Integer, Cell> grid, double step){
		
		double[] IToUpdate = new double[Run.DIMENSIONS];
		Set<Integer> keysSet = grid.keySet();
		Cell cellToCompare;
		double[] cellCentroid;
		
		for(int i=0;i<IToUpdate.length;i++){
			IToUpdate[i]=0.0;
			this.I[i]=0.0;
		}
		
		double sumFitnessDifference = 0;
		
		for(Integer key : keysSet){
			
			cellToCompare=grid.get(key);
			cellCentroid=cellToCompare.getCentroid();
			double cellFitness = cellToCompare.getFitness();
			double selfFitness = this.fitness;
			
			if(Double.compare(cellFitness,selfFitness)<0){
				for(int i=0;i<IToUpdate.length;i++){
					IToUpdate[i]+=(cellCentroid[i]-position[i])*(selfFitness-cellFitness);
				}
				sumFitnessDifference+=(selfFitness-cellFitness);
			}
			
			//System.out.println("fitness: "+fitness);
			//System.out.println("cellFitness: "+cellFitness);
		}
		
		if(Double.compare(sumFitnessDifference,0.0) != 0){
			
			for(int k = 0; k < IToUpdate.length; k++){
				IToUpdate[k] = IToUpdate[k] / sumFitnessDifference; 
			}
			
			double iSize = modulo(IToUpdate);
			
			for(int k = 0; k < IToUpdate.length; k++){
				IToUpdate[k] = IToUpdate[k]*Math.max(step, Run.CELL_WIDTH)/iSize; 
			}
			
			this.I=IToUpdate;
			
		}		
		
		//System.out.println(modulo(IToUpdate));
		
	}
	
	public double[] getFeasibility() {
		return feasibility;
	}

	private double modulo(double[] toMeasure) {
		
		double modulo = 0.0;
		
		for(int i=0; i<toMeasure.length; i++){
			modulo+=toMeasure[i]*toMeasure[i];
		}
		
		return Math.sqrt(modulo);
	}

	public void calculateIFitness(ArrayList<Fish> fishesList){
		
		double[] IToUpdate = new double[Run.DIMENSIONS];
		
		for(int i=0;i<IToUpdate.length;i++){
			IToUpdate[i]=0.0;
			this.I[i]=0.0;
		}
		
		double sumFitnessDifference = 0;
		
		for(Fish e : fishesList){
			
			if(Double.compare(e.getLastFitness(),e.getFitness())>0){
				for(int i=0;i<IToUpdate.length;i++){
					IToUpdate[i]+=e.getCellOcuppied().getF()*(e.getPosition()[i]-e.getLastPosition()[i])*(e.getLastFitness()-e.getFitness());
				}
				sumFitnessDifference+=e.getCellOcuppied().getF()*(e.getLastFitness()-e.getFitness());
				//sumFitnessDifference+=(e.getLastFitness()-e.getFitness());
			}
		}
		
		if(Double.compare(sumFitnessDifference,0.0) != 0){
			for(int k = 0; k < IToUpdate.length; k++){
				IToUpdate[k] = IToUpdate[k] / sumFitnessDifference;
			}
			
			this.I=IToUpdate;
		}		
		//System.out.println(modulo(IToUpdate));
		
	}
	
	public void calculateIFeasibility(ArrayList<Fish> fishesList){
		
		double[] IFeasibilityToUpdate = new double[Run.DIMENSIONS];
		double[] IFitnessToUpdate = new double[Run.DIMENSIONS];
		double[] IToReturn = new double[Run.DIMENSIONS];
		
		for(int i=0;i<IFeasibilityToUpdate.length;i++){
			IFeasibilityToUpdate[i]=0.0;
			IFitnessToUpdate[i]=0.0;
			IToReturn[i]=0.0;
			this.I[i]=0.0;
		}

		double sumFiDifference = 0;
		double sumFitnessDifference = 0;
		
		for(Fish e : fishesList){
			
			if(Double.compare(e.getLastFeasibility()[1],e.getFeasibility()[1])>0){
				for(int i=0;i<IFeasibilityToUpdate.length;i++){
					IFeasibilityToUpdate[i]+=(e.getPosition()[i]-e.getLastPosition()[i])*(e.getLastFeasibility()[1]-e.getFeasibility()[1]);
				}
				sumFiDifference+=(e.getLastFeasibility()[1]-e.getFeasibility()[1]);
			}
			
			if(Double.compare(e.getLastFitness(),e.getFitness())>0){
				for(int i=0;i<IFeasibilityToUpdate.length;i++){
					IFeasibilityToUpdate[i]+=e.getCellOcuppied().getF()*(e.getPosition()[i]-e.getLastPosition()[i])*(e.getLastFitness()-e.getFitness());
				}
				sumFitnessDifference+=e.getCellOcuppied().getF()*(e.getLastFitness()-e.getFitness());
			}
			
			
		}
		
		if(Double.compare(sumFiDifference,0.0) != 0 && Double.compare(sumFitnessDifference,0.0) != 0){
			for(int k = 0; k < IFeasibilityToUpdate.length; k++){
				IFeasibilityToUpdate[k] = IFeasibilityToUpdate[k] / sumFiDifference;
				IFitnessToUpdate[k] = IFitnessToUpdate[k] / sumFitnessDifference;
				IToReturn[k]=IFeasibilityToUpdate[k]+IFitnessToUpdate[k];
			}
			
			this.I=IToReturn;
		}else if(Double.compare(sumFiDifference,0.0) == 0 && Double.compare(sumFitnessDifference,0.0) !=0){
			for(int k = 0; k < IFeasibilityToUpdate.length; k++){
				IFitnessToUpdate[k] = IFitnessToUpdate[k] / sumFitnessDifference;
			}
			
			this.I=IFitnessToUpdate;
		}else if(Double.compare(sumFiDifference,0.0) != 0 && Double.compare(sumFitnessDifference,0.0) == 0){
			for(int k = 0; k < IFeasibilityToUpdate.length; k++){
				IFeasibilityToUpdate[k] = IFeasibilityToUpdate[k] / sumFiDifference;
			}
			
			this.I=IFeasibilityToUpdate;
		}
		
		//System.out.println(modulo(IToUpdate));
		
	}

	private double[] getLastFeasibility() {
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

	public void moveFishIndividual(double step) {
		
		double randNum;
		double newFitness;
		double[] newFeasibility;
		double[] displacement = new double[Run.DIMENSIONS];
		double[] candidatePosition = new double[Run.DIMENSIONS];
		
		for(int i=0;i<displacement.length;i++){
			randNum=Run.generator.nextDouble();
			displacement[i]=(2*randNum*step-step)/cellOcuppied.getF();
		}
		
		candidatePosition=findNewPosition(displacement);
		newFitness=FitnessFunction.calculateFitness(candidatePosition);
		newFeasibility=FeasibilityAnalysis.isFeasible(candidatePosition);
		
		if(checkImprove(newFitness, newFeasibility[1])){
			this.updateFitness(newFitness);
			this.updatePosition(candidatePosition);
			//this.feasibility=newFeasibility; This is updated when checkFeasibility() runs
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

	public void feedFish(double maxDeltaf) {
		
		updateWeight(weight+(fitness-lastFitness)/maxDeltaf);
		
	}

	public void moveFishColInstinctive() {

		moveFish(this.I);
		
	}

	public double[] getPosition() {
		// TODO Auto-generated method stub
		return position.clone();
	}
	
	public double[] getLastPosition() {
		
		return lastPosition.clone();
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
		
		return Math.max(total, 0.0001);
	}

	public int[] getAdress() {
		
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
		
		//Run.printArray(position);
		//Run.printArray(adress);
		
		return adress;
	}

	public void updateCellParameters() {
		
		double F = this.cellOcuppied.getF();
		
		if(this.checkFeasibility()){
			if(F<0){
				cellOcuppied.setF(1.0);
			}else if(Double.compare(F,0.0)>0 && Double.compare(F,0.5)<0){
				cellOcuppied.setF(0.5);
			}else if(Double.compare(F,0.0)==0){
				
			}else if(Double.compare(F,1)==0){
				
			}else if(Double.compare(F,2)==0){
				cellOcuppied.setF(1.0);
			}
			
		}else{
			if(Double.compare(F,0.0)<0){
				cellOcuppied.setF(Math.pow(10, -Run.maxFPot*this.feasibility[0]/this.feasibility[2]));
			}else if(Double.compare(F,0.0)>0 && Double.compare(F,0.5)<0.5){
				
				if(Double.compare(F,(Math.pow(10, -Run.maxFPot*this.feasibility[0]/this.feasibility[2])))>0){
					cellOcuppied.setF(Math.pow(10, -Run.maxFPot*this.feasibility[0]/this.feasibility[2]));
				}				
				
			}else if(Double.compare(F,0.5)==0){
				
			}else if(Double.compare(F,1)==0){
				cellOcuppied.setF(0.5);
			}else if(Double.compare(F,2)==0){
				cellOcuppied.setF(0.5);
			}
		}
		
		if(Double.compare(this.fitness,cellOcuppied.getFitness())<0){
			 cellOcuppied.setFitness(fitness);
		}
		
		//printCell(cellOcuppied);
		
	}

	public boolean checkFeasibility() {

		updateFeasibility(FeasibilityAnalysis.isFeasible(position.clone()));
		
		if(Double.compare(this.feasibility[0],0.0)==0){
			return true;
		}else{
			return false;
		}
		
	}
	
	private void updateFeasibility(double[] feasible) {
		
		this.lastFeasibility=this.feasibility.clone();
		this.feasibility=feasible.clone();
		
	}

	public boolean isFeasible() {
		
		if(Double.compare(this.feasibility[1],this.epsilon)<=0){
			return true;
		}else{
			return false;
		}
	}

	public void setCellOcuppied(Cell cellOcuppied) {
		this.cellOcuppied = cellOcuppied;
	}
	
	public void printCell(){
		cellOcuppied.printCell();
	}

	public void setEpsilon(double epsilonToUpdate) {
		this.epsilon=epsilonToUpdate;
		
	}

}
