package rFSS;

import General.Run;

public class Cell {
	
	private double[] centroid;
	private double F;
	private double fitness;

	public Cell(int[] adress){
		
		double[] centroidToUpdate = new double[Run.DIMENSIONS];
		
		for(int i=0; i<centroidToUpdate.length; i++){
			
			double sense;
			
			if(Double.compare(adress[i],0.0)<0){
				sense=-1;
			}else{
				sense=1;
			}
			
			centroidToUpdate[i]=sense*((Math.abs(adress[i])*Run.CELL_WIDTH)-Run.CELL_WIDTH/2);
	
		}
		
		centroid=centroidToUpdate;
		fitness=10000000000000000000000000000000000000000000000.0;
		F=-10;
	}

	public double[] getCentroid() {
		return centroid.clone();
	}

	public void setCentroid(double[] centroid) {
		this.centroid = centroid;
	}

	public double getF() {
		return F;
	}

	public void setF(double f) {
		F = f;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	public void printCell(){
		System.out.println("Cell = " + this);
		System.out.println("Centroid:");
		Run.printArray(centroid);
		System.out.println("fitness:" + fitness);
		System.out.println("F:" + F);
	}
	
}
