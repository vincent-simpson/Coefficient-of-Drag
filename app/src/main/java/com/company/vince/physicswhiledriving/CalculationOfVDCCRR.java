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

    private static ArrayList<Double> vAverage = new ArrayList<>();
    private static ArrayList<Double> vActual = new ArrayList<>();
    private static ArrayList<Double> force = new ArrayList<>();
    private static ArrayList<Double> acceleration = new ArrayList<>();
    private static ArrayList<Double> vModel = new ArrayList<>();
    public static ArrayList<Double> errorSquared = new ArrayList<>();

    private static final double ONE_KPH = 0.2777778; // m/s
    private static final double ONE_MPH = 0.4444444; // m/s
    private static final double GRAV_CONST = 9.81; // m/s^2
    private static final double DRAG_COEFFICIENT = .2346;
    private static final double CRR = 0.016732;
    private static double SUM_OF_ERROR_SQUARED=0;

    private double densityOfAir = 1.22; // kg/m^3
    private double frontalAreaOfVehicle = 2.3; // m^2
    private double massOfVehiclePlusOccupants = 1000; //kg

    private BigDecimal bdGravConstant = new BigDecimal(GRAV_CONST);
    private BigDecimal bdDragCoefficient = new BigDecimal(DRAG_COEFFICIENT);
    private BigDecimal bdCRR = new BigDecimal(CRR);
    private BigDecimal bdDensityOfAir = new BigDecimal(densityOfAir);
    private BigDecimal bdMass = new BigDecimal(massOfVehiclePlusOccupants);
    private BigDecimal bdFrontalArea = new BigDecimal(frontalAreaOfVehicle);

    public CalculationOfVDCCRR(double[][] a)

    {
        v1 = a.clone();
    }

    public void calculateAverageVelocity()
    {
        double sum;
        int count;

        for(int i=0; i < 8; i++)
        {
            sum=0;
            count=0;
            for(int j=0; j < 6; j++) {
                if(v1[i][j] != 0.0){
                    sum += v1[i][j];
                    count++;
                }

            }
            if(count != 0){
                vAverage.add(Double.parseDouble(new DecimalFormat("##.##").format(sum / count)));
            }
        }
    }

    public void calculateActualVelocity()
    {
        for(double d : vAverage)
        {
            vActual.add(new BigDecimal(d).multiply(new BigDecimal(ONE_MPH)).doubleValue());
        }
        //printArrayListContents(vActual);
        vModel.add(vActual.get(0));
    }

    public void calculateModelVelocity(int row)
    {

         try
         {
             BigDecimal bd = (new BigDecimal(acceleration.get(row)).multiply(new BigDecimal(5.0)));
             vModel.add((new BigDecimal(vModel.get(row)).subtract(bd)).doubleValue());
             //System.out.println("Model velocity at row : " + row + " is " + vModel.get(row));
         } catch(IndexOutOfBoundsException e) {
             System.out.println("Index = " + row);
             System.out.println("Model velocity size = " + vModel.size());
             System.out.println("Acceleration size = " + acceleration.size());
             System.out.println("Force size = " + force.size());
             e.printStackTrace();
             System.exit(-1);
         }

    }

    public void calculateForce(int row)
    {

        //System.out.println(vModel.get(i));

        BigDecimal forcePart1 = bdDragCoefficient.multiply(bdFrontalArea).multiply(new BigDecimal(0.5))
                .multiply(bdDensityOfAir).multiply(new BigDecimal(vModel.get(row)).pow(2));
        BigDecimal forcePart2 = bdCRR.multiply(bdMass).multiply(bdGravConstant);


        force.add((forcePart1.add(forcePart2)).doubleValue());
        //System.out.println(force.get(row));

        //System.out.println("Force size = " + force.size());
    }

    public void calculateAcceleration(int row)
    {

        acceleration.add(
                (new BigDecimal(force.get(row)).divide(bdMass)
                        .doubleValue()
                ));
        //System.out.println("Acceleration at row : " + row + " is " + acceleration.get(row));

    }

    public void calculateErrorSquared(int vActualRow, int vModelRow)
    {
        errorSquared.add((new BigDecimal(vActual.get(vActualRow))
                .subtract(new BigDecimal(vModel.get(vModelRow)))).pow(2).doubleValue());



    }

    public void calculateSumOfError()
    {
        for(double d : errorSquared)
        {
            SUM_OF_ERROR_SQUARED += d;
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

    public static void printArrayListContents(ArrayList<Double> arrayList)
    {
        for(int i =0;i < arrayList.size(); i++) {
            System.out.println(arrayList.get(i));
        }
    }



}
