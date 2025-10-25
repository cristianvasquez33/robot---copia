/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cristian
 */
public class trapesoidal{
   
  private double a,b,c,d,gp;
  public trapesoidal (double a,double b,double c,double d)
  {
this.a=a;
this.b=b;
this.c=c;
this.d=d;

  }
public double pertenencia(double u)
{
    if(u<a) gp=0.0;
    else if (u<b) gp=(u-a)/(b-a);
    else if (u<c) gp=1.0;
    else if (u<d) gp=(d-u)/(d-c);
    else gp= 0.0;
    return gp;
}
    public double getgp()
 {
    return gp;
 }
   
} 

