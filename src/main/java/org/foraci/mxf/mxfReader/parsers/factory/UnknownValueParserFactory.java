package org.foraci.mxf.mxfReader.parsers.factory;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.parsers.Parser;

import java.math.BigInteger;

/**
 * A <code>ParserFactory</code> that only creates parsers that will skip the contents of the
 * value in a KLV (see SMPTE 336M)
 * @see ParserFactory
 */
public class UnknownValueParserFactory implements ParserFactory
{
    public Parser createParser(BigInteger length, MxfInputStream in)
    {
        return new Parser(length, in);
    }
}
