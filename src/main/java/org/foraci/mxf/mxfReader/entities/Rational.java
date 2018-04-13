package org.foraci.mxf.mxfReader.entities;

/**
 * A rational number, i.e. a number expressable as a ratio of two integers
 * @author jforaci
 */
public class Rational {
    private final int numerator;
    private final int denominator;

    public Rational(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public int getNumerator() {
        return numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    public String toString() {
        return numerator + "/" + denominator;
    }
}
