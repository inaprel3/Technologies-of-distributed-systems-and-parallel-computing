// IntegralDemoCyclicBarrier.java

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

class IntegralThread extends Thread {
  int procNumber;
  IntegralDemoCyclicBarrier manager;
  double lo;
  double hi;
  double epsilon;

  IntegralThread( int num, IntegralDemoCyclicBarrier mgr, double l, double h, double eps) {
    procNumber = num;
    manager = mgr;
    lo = l;
    hi = h;
    epsilon = eps;
  }

  @Override
  public void run() {
    System.out.println(" IntegralIteration: " + procNumber + ": lo= " + lo + ", hi= " + hi);
    manager.result[procNumber] = IntegralIteration( lo, hi, epsilon);
    try {
      manager.barrier.await();
      System.out.println( "IntegralThread(" + procNumber + "): end");
      }
    catch (InterruptedException e) {
      e.printStackTrace();
      }
    catch (BrokenBarrierException e) {
      e.printStackTrace();
      }
  }

  double userFunction(double a, double b, double x) {
    return(a * x + b);
  }

  double IntegralStep( double lo, double hi, double step) {
    double s = 0.0;

    for (double x = lo; x < hi; x += step) {
      double f = userFunction( 1, 0, x);
      s += f * step;
    }
    System.out.println(" " + procNumber + " s= " + s);
    return( s);
  }

  double IntegralIteration( double lo, double hi, double epsilon) {
    double step = (hi - lo) / 10;
    double s1 = 0.0, s2 = 0.0;
    do {
      s1 = s2;
      s2 = IntegralStep( lo, hi, step);
      step /= 2;
    }
    while (Math.abs(s1 - s2) > epsilon);
    return( s2);
  }
}

public class IntegralDemoCyclicBarrier {
  CyclicBarrier barrier;
  double result[];

  IntegralDemoCyclicBarrier( double lo, double hi, double epsilon, int procNumber) {
    result = new double[procNumber];
    double step = (hi - lo) / procNumber;
    double l = lo;
    barrier = new CyclicBarrier( procNumber, new Runnable() {
      public void run() {
          System.out.println( "CyclicBarrier: run");
          double s = 0.0;
          for (int i = 0; i < procNumber; ++i) {
            s += result[i];
          }
          System.out.println("F= " + s);
          }
        });


    for (int i = 0; i < procNumber; ++i) {
      double h = l + step;
      new IntegralThread( i, this, l, h, epsilon / procNumber).start();
      System.out.println( i + ") lo= " + l + ", hi= " + h);
      l = h;
    }
  }

  public static void main(String args[]) {
    IntegralDemoCyclicBarrier demo = new IntegralDemoCyclicBarrier( 1.0, 4.0, 0.01, 3);
  }
}

