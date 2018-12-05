package com.company.vince.physicswhiledriving;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

//Calculation of vehicle drag coefficient and the coefficient of rolling resistance
public class CalculationOfVDCCRR
{
    static double[][] v1;

    private static ArrayList<BigDecimal> vAverage = new ArrayList<>();
    private static ArrayList<BigDecimal> vActual = new ArrayList<>();
    private static ArrayList<BigDecimal> force = new ArrayList<>();
    private static ArrayList<BigDecimal> acceleration = new ArrayList<>();
    private static ArrayList<BigDecimal> vModel = new ArrayList<>();
    public static ArrayList<BigDecimal> errorSquared = new ArrayList<>();

    private static final String ONE_KPH = "0.2777778"; // m/s
    private static final String ONE_MPH = "0.4444444"; // m/s
    private static final String GRAV_CONST = "9.81"; // m/s^2
    private static final String DRAG_COEFFICIENT = ".2346";
    private static final String CRR = "0.016732";
    private static String SUM_OF_ERROR_SQUARED;

    private String densityOfAir = "1.22"; // kg/m^3
    private String frontalAreaOfVehicle = "2.3"; // m^2
    private String massOfVehiclePlusOccupants = "1000"; //kg

    private BigDecimal bdGravConstant = new BigDecimal(GRAV_CONST);
    private BigDecimal bdDragCoefficient = new BigDecimal(DRAG_COEFFICIENT);
    private BigDecimal bdCRR = new BigDecimal(CRR);
    private BigDecimal bdDensityOfAir = new BigDecimal(densityOfAir);
    private BigDecimal bdMass = new BigDecimal(massOfVehiclePlusOccupants);
    private BigDecimal bdFrontalArea = new BigDecimal(frontalAreaOfVehicle);
    private BigDecimal bdOneMph = new BigDecimal(ONE_MPH);

    public CalculationOfVDCCRR(double[][] a)
    {
        v1 = a.clone();
    }

    public void calculateAverageVelocity()
    {
        double sum;
        int count;

        for(int i=0; i < 8; i++) {
            sum=0;
            count=0;
            for(int j=0; j < 6; j++) {
                if(v1[i][j] != 0.0){
                    sum += v1[i][j];
                    count++;
                }
            }
            if(count != 0){
                vAverage.add(new BigDecimal("" + sum / count));
            }
        }
    }

    public void calculateActualVelocity()
    {
        for(BigDecimal d : vAverage)
        {
            vActual.add(d.multiply(bdOneMph));
        }
        vModel.add(vActual.get(0));
    }

    public void calculateModelVelocity(int row)
    {
        try
         {
             BigDecimal bd = (acceleration.get(row).multiply(new BigDecimal(5.0)));
             vModel.add(vModel.get(row).subtract(bd));

             //System.out.println("Model velocity at row : " + row + " is " + vModel.get(row).toPlainString());
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
                .multiply(bdDensityOfAir).multiply(vModel.get(row).pow(2));
        BigDecimal forcePart2 = bdCRR.multiply(bdMass).multiply(bdGravConstant);


        force.add((forcePart1.add(forcePart2)));
        //System.out.println(force.get(row));

        //System.out.println("Force size = " + force.size());
    }

    public void calculateAcceleration(int row)
    {
        acceleration.add(
                (force.get(row).divide(bdMass, RoundingMode.HALF_UP)
                ));
        //System.out.println("Acceleration at row : " + row + " is " + acceleration.get(row));
    }

    public void calculateErrorSquared(int vActualRow, int vModelRow)
    {
        errorSquared.add(vActual.get(vActualRow)
                .subtract(vModel.get(vModelRow)).pow(2));

    }

    public void calculateSumOfError()
    {
        BigDecimal bdErrorSum = new BigDecimal(SUM_OF_ERROR_SQUARED);

        for(BigDecimal d : errorSquared)
        {
            bdErrorSum = bdErrorSum.add(d);
        }
    }

    public String getDensityOfAir()
    {
        return densityOfAir;
    }

    public void setDensityOfAir(String densityOfAir)
    {
        this.densityOfAir = densityOfAir;
    }

    public void printArrayContents(double[] a)
    {
        for(double d: a) {
            System.out.println(d);
        }
    }

    public static void printArrayListContents(ArrayList<BigDecimal> arrayList)
    {
        for(int i =0;i < arrayList.size(); i++) {
            System.out.println(arrayList.get(i));
        }
    }



}
