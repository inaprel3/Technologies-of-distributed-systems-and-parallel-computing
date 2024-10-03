import java.util.concurrent.*;

class DataClass {
  int value[][];
  int currentRow;

  DataClass( int r, int c) {
    value = new int[r][c];
    currentRow = 0;
    }

  public void addVector( int v[]) {
    for (int i = 0; i < value[currentRow].length; ++i) {
      value[currentRow][i] = v[i];
      }
    ++currentRow;
    }

  public int[][] getValue() { 
    return( value); 
    }
  }

class TVector implements Runnable {
  int pid;
  int vect[];
  Exchanger<DataClass> exchanger12;
  Exchanger<DataClass> exchanger23;

  TVector( int id, int vlen, Exchanger<DataClass> ex12, Exchanger<DataClass> ex23) {
    pid = id;
    exchanger12 = ex12;
    exchanger23 = ex23;
    vect = new int[vlen];
    for (int i = 0; i < vect.length; ++i) {
      vect[i] = i + 1;
      }
    }

  public void run() {
    try {
      DataClass vData = new DataClass( 1, vect.length);
      vData.addVector( vect);
      DataClass mdData = exchanger23.exchange( vData);

      int md[][] = mdData.getValue();

      DataClass data = exchanger12.exchange( vData);
      data = new DataClass( md.length / 2, md[0].length);
      for (int i = 0; i < md.length / 2; ++i) {
        data.addVector( md[i]);
        }
      data = exchanger12.exchange( data);

      int result[] = new int[md.length];
      for (int i = md.length / 2; i < md.length; ++i) {
        result[i] = 0;
        for (int j = 0; j < md[i].length; ++j) {
          result[i] += vect[j] * md[i][j] ;
          }
        System.out.println( "TVector(" + pid + "):run:result[" + i + "] = " + result[i]);
        }

      int max[] = new int[3];
      for (int i = 0; i < max.length; ++i) {
        max[i] = 0;
        }

      data = exchanger12.exchange( data);
      int v1[][] = data.getValue();
      for (int i = 0; i < v1[0].length; ++i) {
        System.out.println( "TVector(" + pid + "):run:t1:result[" + i + "] = " + v1[0][i]);
        if (max[0] < v1[0][i]) {
          max[0] = v1[0][i];
          }
        }
      System.out.println( "TVector(" + pid + "):Max from T1 = " + max[0]);

      for (int i = md.length / 2; i < md.length; ++i) {
        System.out.println( "TVector(" + pid + "):run:result[" + i + "] = " + result[i]);
        if (max[1] < result[i]) {
          max[1] = result[i];
          }
        }
      System.out.println( "TVector(" + pid + "):Max TVector = " + max[1]);

      data = exchanger23.exchange( data);
      int v2[][] = data.getValue();
      for (int i = 0; i < v2[0].length; ++i) {
        System.out.println( "TVector(" + pid + "):run:t3:result[" + i + "] = " + v2[0][i]);
        if (max[2] < v2[0][i]) {
          max[2] = v2[0][i];
          }
        }
      System.out.println( "TVector(" + pid + "):Max from T3 & T4 = " + max[2]);

      for (int i = 0; i < max.length; ++i) {
        if (max[0] < max[i]) {
          max[0] = max[i];
          }
        }
      System.out.println( "TVector(" + pid + "):MaxResult = " + max[0]);
      }
    catch (InterruptedException e) {
      System.err.println( e.toString());
      }
    }
  }

class TMatrix implements Runnable {
  int pid;
  int matrix[][];
  Exchanger<DataClass> exchanger23;
  Exchanger<DataClass> exchanger34;

  TMatrix( int id, int rows, int cols, Exchanger<DataClass> ex23, Exchanger<DataClass> ex34) {
    pid = id;
    exchanger23 = ex23;
    exchanger34 = ex34;
    matrix = new int[rows][cols];
    int k = cols + 1;
    for (int i = 0; i < matrix.length; ++i) {
      for (int j = 0; j < matrix[i].length; ++j) {
        matrix[i][j] = k++;
        }
      }
    }

  public void run() {
    try {
      DataClass data = new DataClass( matrix.length / 2, matrix[0].length);
      for (int i = 0; i < matrix.length / 2; ++i) {
        data.addVector( matrix[i]);
        }
      data = exchanger23.exchange( data);
      int v[][] = data.getValue();

      data = exchanger34.exchange( data);
      data = new DataClass( matrix.length / 4, matrix[0].length);
      for (int i = matrix.length * 3 / 4; i < matrix.length; ++i) {
        data.addVector( matrix[i]);
        }
      data = exchanger34.exchange( data);

      int result[] = new int[matrix.length / 2];
      int k = 0;
      for (int i = matrix.length / 2; i < matrix.length * 3 / 4; ++i) {
        int m = 0;
        for (int j = 0; j < matrix[i].length; ++j) {
          m += v[0][j] * matrix[i][j] ;
          }
        result[k++] = m;
        System.out.println( "TMatrix(" + pid + "):run:result[" + i + "] = " + m);
        }

      data = exchanger34.exchange( data);
      int v4[][] = data.getValue();
      for (int i = 0; i < v4[0].length; ++i) {
        result[k++] = v4[0][i];
        }

      data = new DataClass( 1, result.length);
      data.addVector( result);
      data = exchanger23.exchange( data);
      }
    catch (InterruptedException e) {
      System.err.println( e.toString());
      }
    }
  }

class TCommon implements Runnable {
  int pid;
  Exchanger<DataClass> exchanger;

  TCommon( int id, Exchanger<DataClass> ex) {
    pid = id;
    exchanger = ex;
    }

  public void run() {
    try {
      DataClass vData = new DataClass( 1, 10);
      vData = exchanger.exchange( vData);
      int v[][] = vData.getValue();
      
      DataClass mData = new DataClass( 3, 3);
      mData = exchanger.exchange( mData);
      int m[][] = mData.getValue();

      int result[] = new int[m.length];
      for (int i = 0; i < m.length; ++i) {
        result[i] = 0;
        for (int j = 0; j < m[i].length; ++j) {
          result[i] += v[0][j] * m[i][j] ;
          }
        System.out.println( "TCommon(" + pid + "):run:result[" + i + "] = " + result[i]);
        }

      DataClass rData = new DataClass( 1, result.length);
      rData.addVector( result);
      rData = exchanger.exchange( rData);
      }
    catch (InterruptedException e) {
      System.err.println( e.toString());
      }
    }
  }

public class VectorMatrixMult {
  public static void main( String args[]) {
    System.out.println( "Main process started");
    int cols = 5;
    int rows = 16;
    Exchanger<DataClass> exchanger12 = new Exchanger<DataClass>();
    Exchanger<DataClass> exchanger23 = new Exchanger<DataClass>();
    Exchanger<DataClass> exchanger34 = new Exchanger<DataClass>();
    (new Thread( new TCommon( 1, exchanger12))).start();
    (new Thread( new TVector( 2, cols, exchanger12, exchanger23))).start();
    (new Thread( new TMatrix( 3, rows, cols, exchanger23, exchanger34))).start();
    (new Thread( new TCommon( 4, exchanger34))).start();
    System.out.println( "Main process ended");
    }
  } 
