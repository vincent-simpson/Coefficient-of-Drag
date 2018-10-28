package com.company.vince.physicswhiledriving;


import java.util.ArrayList;

//Calculation of vehicle drag coefficient and the coefficient of rolling resistance
public class CalculationOfVDCCRR
{
    //Data structures to hold velocity data trials. Could be implemented in MainActivity with a
    //switch statement
    /*
    switch(trial)
    case 1: v1.add(speedValue)...ect
     */
    static double[][] v1;

    static ArrayList<Double> vAverage = new ArrayList<>();

    private static final double ONE_KPH = 0.2777778; // m/s
    private static final double ONE_MPH = 0.4444444; // m/s
    private static final double GRAV_CONST = 9.81; // m/s^2
    private double densityOfAir = 1.22; // kg/m^3
    private double frontalAreaOfVehicle; // m^2
    private double massOfVehiclePlusOccupants; //kg

    public void printAverageVelocities() {
        for(double d: vAverage){
            System.out.println(d);
        }
    }

    public CalculationOfVDCCRR(double[][] a)
    {
        v1 = a.clone();
    }

    public double getDensityOfAir()
    {
        return densityOfAir;
    }

    public void setDensityOfAir(double densityOfAir)
    {
        this.densityOfAir = densityOfAir;
    }

    public void calculateAverageVelocity()
    {
        double sum;
        for(int i=0; i < 6; i++)
        {
            sum=0;
            for(int j=0; j < 6; j++) {
                sum += v1[i][j];
            }
            vAverage.add(sum / 6);
        }
    }

    public void printArrayContents(double[] a)
    {
        for(double d: a) {
            System.out.println(d);
        }
    }



}
