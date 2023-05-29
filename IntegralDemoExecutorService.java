// IntegralDemoExecutorService.java

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

class IntegralThread implements Callable <Double> {
  int taskNumber;
  double lo;
  double hi;
  double epsilon;

  IntegralThread( int num, double l, double h, double eps) {
    taskNumber = num;
    lo = l;
    hi = h;
    epsilon = eps;
  }

  @Override
  public Double call() {
    System.out.println(" IntegralIteration: " + taskNumber + ": lo= " + lo + ", hi= " + hi);
    return IntegralIteration( lo, hi, epsilon);
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
    System.out.println(" " + taskNumber + " s= " + s);
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

public class IntegralDemoExecutorService {

  IntegralDemoExecutorService( double lo, double hi, double epsilon, int taskNumber) {
    double step = (hi - lo) / taskNumber;
    double l = lo;
    ExecutorService executor = Executors.newFixedThreadPool( 4);
    Future future[] = new Future[taskNumber];

    long startTime = System.nanoTime();
    for (int i = 0; i < taskNumber; ++i) {
      double h = l + step;
      future[i] = executor.submit( new IntegralThread( i, l, h, epsilon / taskNumber));
      l = h;
      }

    double s = 0.0;
    for (int i = 0; i < taskNumber; ++i) {
      try {
        s += (double)future[i].get();
        }
      catch (Exception e) {
        e.printStackTrace();
        }
      }
    long endTime = System.nanoTime();
    System.out.println("F= " + s);
    executor.shutdown();
    System.out.println("Elapsed Time: " + (endTime - startTime) + " ns");
    }

  public static void main(String args[]) {
//    int i = 5;
    for (int i = 1; i < 15; ++i) {
      IntegralDemoExecutorService demo = new IntegralDemoExecutorService( 1.0, 4.0, 0.0001, i);
      }
    }
  }

