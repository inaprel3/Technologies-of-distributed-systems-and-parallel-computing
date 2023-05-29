// IntegralDemoCountDownLatch.java

import java.util.concurrent.CountDownLatch;

class IntegralThread extends Thread {
  int procNumber;
  IntegralDemoCountDownLatch manager;
  double lo;
  double hi;
  double epsilon;

  IntegralThread( int num, IntegralDemoCountDownLatch mgr, double l, double h, double eps) {
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
    manager.latch.countDown();
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

public class IntegralDemoCountDownLatch {
  CountDownLatch latch;
  double result[];

  IntegralDemoCountDownLatch( double lo, double hi, double epsilon, int procNumber) {
    result = new double[procNumber];
    latch = new CountDownLatch( procNumber);
    double step = (hi - lo) / procNumber;
    double l = lo;

    long startTime = System.nanoTime();
    for (int i = 0; i < procNumber; ++i) {
      double h = l + step;
      new IntegralThread( i, this, l, h, epsilon / procNumber).start();
      System.out.println( i + ") lo= " + l + ", hi= " + h);
      l = h;
    }
    try {
      latch.await();
    } catch (InterruptedException e) { }
    long endTime = System.nanoTime();

    double s = 0.0;
    for (int i = 0; i < procNumber; ++i) {
      s += result[i];
    }
    System.out.println("F= " + s);
    System.out.println("Elapsed Time: " + (endTime - startTime) + " ns");
  }

  public static void main(String args[]) {
    for(int i = 1; i < 11; ++i) {
      IntegralDemoCountDownLatch demo = new IntegralDemoCountDownLatch( 1.0, 4.0, 0.0001, i);
      }
  }
}

