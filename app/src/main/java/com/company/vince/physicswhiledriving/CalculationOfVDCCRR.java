package com.company.vince.physicswhiledriving;


import java.math.BigDecimal;
import java.text.DecimalFormat;
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
    static ArrayList<Double> vActual = new ArrayList<>();
    static ArrayList<Double> force = new ArrayList<>();
    static ArrayList<Double> acceleration = new ArrayList<>();
    static ArrayList<Double> vModel = new ArrayList<>();
    static ArrayList<Double> errorSquared = new ArrayList<>();

    private static final double ONE_KPH = 0.2777778; // m/s
    private static final double ONE_MPH = 0.4444444; // m/s
    private static final double GRAV_CONST = 9.81; // m/s^2
    private static final double DRAG_COEFFICIENT = .234;
    private static final double CRR = 0.016;
    private static double SUM_OF_ERROR_SQUARED=0;

    private double densityOfAir = 1.22; // kg/m^3
    private double frontalAreaOfVehicle = 2.3; // m^2
    private double massOfVehiclePlusOccupants = 1000; //kg

    public CalculationOfVDCCRR(double[][] a)

    {
        v1 = a.clone();
    }

    public void calculateAverageVelocity()
    {
        double sum;
        for(int i=0; i < 8; i++)
        {
            sum=0;
            for(int j=0; j < 6; j++) {
                sum += v1[i][j];
            }
            vAverage.add(Double.parseDouble(new DecimalFormat("##.##").format(sum / 6)));
        }
    }

    public void calculateActualVelocity()
    {
        for(double d : vAverage)
        {
            vActual.add(d * ONE_MPH);
        }
        vModel.add(vActual.get(0));
    }

    public void calculateModelVelocity()
    {
        for(int i=0; i < vActual.size(); i++)
        {
         try
         {
             vModel.add(vModel.get(i) - acceleration.get(i) * 5);
         } catch(IndexOutOfBoundsException e) {
             System.out.println("Index = " + i);
             System.out.println("Model velocity size = " + vModel.size());
             System.out.println("Acceleration size = " + acceleration.size());
             System.exit(-1);
         }
        }
    }

    public void calculateForce()
    {
        for(double d : vModel) {
            force.add((DRAG_COEFFICIENT * frontalAreaOfVehicle * 0.5 * densityOfAir * Math.pow(d, 2))  +
                    (CRR * massOfVehiclePlusOccupants * GRAV_CONST));
        }
    }

    public void calculateAcceleration()
    {
        for(double d : force)
        {
            acceleration.add(d / massOfVehiclePlusOccupants);
        }
    }

    public void calculateErrorSquared()
    {
        for(int i=0; i < 7; i++)
        {
            errorSquared.add(Math.pow(vActual.get(i) - vModel.get(i), 2));
        }
    }

    public void calculateSumOfError()
    {
        for(double d : errorSquared)
        {
            SUM_OF_ERROR_SQUARED += d;
        }
    }

    public void printAverageVelocities() {
        for(double d: vAverage){
            System.out.println(d);
        }
    }

    public void printActualVelocities() {
        for(double d: vActual){
            System.out.println(d);
        }
    }

    public void printModelVelocities() {
        for(double d: vModel){
            System.out.println(d);
        }
    }



    public double getDensityOfAir()
    {
        return densityOfAir;
    }

    public void setDensityOfAir(double densityOfAir)
    {
        this.densityOfAir = densityOfAir;
    }

    public void printArrayContents(double[] a)
    {
        for(double d: a) {
            System.out.println(d);
        }
    }



}
