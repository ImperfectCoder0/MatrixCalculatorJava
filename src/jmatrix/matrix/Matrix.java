package jmatrix.matrix;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.*;


@SuppressWarnings("all")
public class Matrix<T extends Number> {
    private T[][] matrix;
    public int rows;
    public int columns;
    private final Class<? extends T> type;
    private final MatrixTypes caseType;
    public String __version__ = "0.0.1 ll1";
    private boolean warn = true;

    public Matrix (Class<? extends T> type, int rows, int columns){
        this((T[][]) Array.newInstance(type, rows, columns), type);
        this.rows = rows;
        this.columns = columns;

    }

    public Matrix (T[][] matrix, Class<? extends T> type){
        this.type = type;
        this.matrix = matrix;
        if (type == Long.class){
            caseType = MatrixTypes.LongMatrix;
        } else if (type == Integer.class){
            caseType = MatrixTypes.IntMatrix;
        } else if (type == Short.class){
            caseType = MatrixTypes.ShortMatrix;
        } else if (type == Byte.class){
            caseType = MatrixTypes.ByteMatrix;
        } else if (type == Float.class){
            caseType = MatrixTypes.FloatMatrix;
        } else if (type == Double.class){
            caseType = MatrixTypes.DoubleMatrix;
        } else if (type == BigDecimal.class){
            warn();
            caseType = MatrixTypes.BigDecimalMatrix;
        } else if (type == BigInteger.class){
            warn();
            caseType = MatrixTypes.BigIntegerMatrix;
        } else {
            throw new ClassFormatError("Class is neither: \njava.lang.Long\njava.lang.Int\njava.lang.Short\n" +
                    "java.lang.Byte\njava.lang.Float\njava.lang.Double\njava.lang.BigInteger\njava.lang.BigDecimal");
        }
        this.rows = matrix.length;
        this.columns = matrix[0].length;
    }


    public Number get(int row, int column) {
        return matrix[row][column];
    }

    @SuppressWarnings("unchecked")
    public void put(int row, int column, Number value) throws IndexOutOfBoundsException {
        if (row > rows - 1 || column > columns - 1){
            throw new IndexOutOfBoundsException("Row or column is greater than maximum.");
        }
        matrix[row][column] = type.cast(value);
    }

    public Class<? extends T> getType(){
        return type;
    }


    @SuppressWarnings("unchecked")
    public Matrix<T> add(Matrix<T> matrix) throws SizeDifferenceException, NoSuchMethodException {
        if (this.rows != matrix.rows || this.columns != matrix.columns){
            throw new SizeDifferenceException(String.format("Sizes of matrices are different: %1$d by %2$d merging into " +
                    "%3$d by %4$d", this.rows, this.columns, matrix.rows, matrix.columns));
        }
        Matrix<T> return_matrix = new Matrix<>(type, rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                switch (caseType) {
                    case BigIntegerMatrix:
                        return_matrix.put(i, j, ((BigInteger) get(i, j)).add((BigInteger) matrix.get(i, j)));
                        break;
                    case LongMatrix:
                        return_matrix.put(i, j, get(i, j).longValue() + matrix.get(i, j).longValue());
                        break;
                    case IntMatrix:
                        return_matrix.put(i, j, get(i, j).intValue() + matrix.get(i, j).intValue());
                        break;
                    case ShortMatrix:
                        return_matrix.put(i, j, get(i, j).shortValue() + matrix.get(i, j).shortValue());
                        break;
                    case ByteMatrix:
                        return_matrix.put(i, j, get(i, j).byteValue() + matrix.get(i, j).byteValue());
                        break;
                    case BigDecimalMatrix:
                        return_matrix.put(i, j, ((BigDecimal) get(i, j)).add((BigDecimal) matrix.get(i, j)));
                        break;
                    case DoubleMatrix:
                        return_matrix.put(i, j, get(i, j).doubleValue() + matrix.get(i, j).doubleValue());
                        break;
                    case FloatMatrix:
                        return_matrix.put(i, j, get(i, j).floatValue() + matrix.get(i, j).floatValue());
                        break;

                    default:
                        throw new ClassFormatError("Class is neither: \njava.lang.Long\njava.lang.Int\njava.lang.Short\n" +
                                "java.lang.Byte\njava.lang.Float\njava.lang.Double\njava.lang.BigInteger\njava.lang.BigDecimal");
                }
            }
        }

        return return_matrix;
    }

    public Matrix<T> add(T value) throws SizeDifferenceException, NoSuchMethodException {
        Matrix<T> return_matrix = new Matrix<>(type, rows, columns);
        for (int i = 0; i < this.rows; i++){
            for (int j = 0; j < this.columns; j++){
                return_matrix.put(i, j, value);
            }
        }
        return this.add(return_matrix);
    }


    public Matrix<T> subtract(Matrix<T> matrix) throws SizeDifferenceException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Matrix<T> return_matrix = matrix.scale((T) type.getDeclaredConstructor(String.class).newInstance("-1"));
        return add(return_matrix);
    }

    public Matrix<T> subtract(T value) throws SizeDifferenceException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Matrix<T> return_matrix = new Matrix<>(type, rows, columns);
        for (int i = 0; i < this.rows; i++){
            for (int j = 0; j < this.columns; j++){
                return_matrix.put(i, j, value);
            }
        }
        return this.subtract(return_matrix);
    }


    public Matrix<T> multiply(Matrix<T> matrix) throws SizeDifferenceException {
        if (this.rows != matrix.columns || this.columns != matrix.rows){
            throw new SizeDifferenceException(String.format("One matrix's number of rows is unequal to the other matrix's columns \n" +
                    "Attempting to merge %1$d by %2$d to %3$d by %4$d", this.rows, this.columns, matrix.rows, matrix.columns));
        } else if (this.rows == matrix.columns){
            Matrix<T> return_matrix = new Matrix<>(type, this.columns, matrix.rows);
            for (int i = 0; i < this.rows; i++){
                for (int j = 0; j < matrix.columns; j++){
                    switch (caseType) {
                        case LongMatrix:
                            long longSum = 0;
                            for (int k = 0; k < this.columns; k++) {
                                longSum += this.matrix[i][k].longValue() * matrix.get(k, j).longValue();
                            }
                            return_matrix.put(i, j,  longSum);
                            break;
                        case IntMatrix:
                            int intSum = 0;
                            for (int k = 0; k < this.columns; k++) {
                                intSum += this.matrix[i][k].intValue() * matrix.get(k, j).intValue();
                            }
                            return_matrix.put(i, j,  intSum);
                            break;
                        case ShortMatrix:
                            short shortSum = 0;
                            for (int k = 0; k < this.columns; k++) {
                                shortSum += this.matrix[i][k].shortValue() * matrix.get(k, j).shortValue();
                            }
                            return_matrix.put(i, j,  shortSum);
                            break;
                        case ByteMatrix:
                            byte byteSum = 0;
                            for (int k = 0; k < this.columns; k++) {
                                byteSum += this.matrix[i][k].byteValue() * matrix.get(k, j).byteValue();
                            }
                            return_matrix.put(i, j,  byteSum);
                            break;
                        case FloatMatrix:
                            float floatSum = 0F;
                            for (int k = 0; k < this.columns; k++) {
                                floatSum += this.matrix[i][k].floatValue() * matrix.get(k, j).floatValue();
                            }
                            return_matrix.put(i, j,  floatSum);
                            break;
                        case DoubleMatrix:
                            double doubleSum = 0D;
                            for (int k = 0; k < this.columns; k++) {
                                doubleSum += this.matrix[i][k].doubleValue() * matrix.get(k, j).doubleValue();
                            }
                            return_matrix.put(i, j,  doubleSum);
                            break;
                            
                        case BigDecimalMatrix:
                            BigDecimal bigDecimalsum = new BigDecimal("0");
                            for (int k = 0; k < this.columns; k++) {
                                bigDecimalsum.add(new BigDecimal(this.get(i, k).toString()).multiply(new BigDecimal(matrix.get(k, j).toString())));
                            }
                            return_matrix.put(i, j,  bigDecimalsum);
                            break;
                        case BigIntegerMatrix:
                            BigInteger otherTypeSum = new BigInteger("0");
                            for (int k = 0; k < this.columns; k++) {
                                otherTypeSum.add(new BigInteger(this.get(i, k).toString()).multiply(new BigInteger(matrix.get(k, j).toString())));
                            }
                            return_matrix.put(i, j, (otherTypeSum));
                            break;
                    }
                }
            }
            return return_matrix;
        } else if (this.columns == matrix.rows) {
            return matrix.multiply(this).transpose();
        }
        return null;
    }




    public Matrix<T> transpose() {
        Matrix<T> return_matrix = new Matrix<>(type, columns, rows);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                return_matrix.put(i, j, this.matrix[j][i]);
            }
        }

        return return_matrix;
    }


    public Matrix<T> scale(T scale) throws NullPointerException{
        Matrix<T> return_matrix = (Matrix<T>) new Matrix<>(type, rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                switch (caseType) {
                    case LongMatrix:
                        return_matrix.put(i, j, get(i, j).longValue() * (long) scale);
                        break;
                    case IntMatrix:
                        return_matrix.put(i, j, get(i, j).intValue() * (int) scale);
                        break;
                    case ShortMatrix:
                        return_matrix.put(i, j, get(i, j).shortValue() * (short) scale);
                        break;
                    case ByteMatrix:
                        return_matrix.put(i, j, get(i, j).byteValue() * (byte) scale);
                        break;
                    case DoubleMatrix:
                        return_matrix.put(i, j, get(i, j).doubleValue() * scale.doubleValue());
                        break;
                    case FloatMatrix:
                        return_matrix.put(i, j, get(i, j).floatValue() * scale.floatValue());
                        break;
                    case BigIntegerMatrix:
                        return_matrix.put(i, j, ((BigInteger) get(i, j)).multiply(new BigInteger(scale.toString())));
                        break;
                    case BigDecimalMatrix:
                        return_matrix.put(i, j, ((BigDecimal) get(i, j)).multiply(new BigDecimal(scale.toString())));
                        break;
                }
            }
        }

        return return_matrix;
    }


    public String toString() {
        int max_length = 0;
        String[][] stringArray = new String[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                String currentString = new BigDecimal(matrix[i][j].toString()).toPlainString();
                int currentStringLength = currentString.length();
                System.out.println(currentStringLength);
                currentString = currentString.contains(".") ? currentString.replaceAll("0*$", "") : currentString + ".";
                System.out.println(currentString.length());
                try {
                    currentString = currentString + " ".repeat(currentStringLength - currentString.length());
                } catch (IllegalArgumentException err){
                    currentStringLength++;
                    currentString = currentString + " ".repeat(currentStringLength - currentString.length());
                }
                if (currentString.length() > max_length) {
                    max_length = currentString.length();
                }
                stringArray[i][j] = currentString;

            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                String currentString = String.valueOf(stringArray[i][j]);
                stringArray[i][j] = " ".repeat(max_length - currentString.length()) + currentString;

            }
        }

        return Arrays.deepToString(stringArray).replace("], ", "]\n")
                .replace("\n[", "\n [")
                .replace(",", "");
    }

    public T[][] getMatrix(){
        return matrix;
    }

    public void warn(){
        if (this.warn) {
            System.out.print("\033[1;31m");
            System.out.print("WARNING: \033[0;33m");
            System.out.println("Non-Primitive Classes are buggy and can cause errors!\033[0m");
            warn = false;
        }
    }

}

