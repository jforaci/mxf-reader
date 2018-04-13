package org.foraci.mxf.mxfReader.parsers.factory;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.parsers.Parser;

import java.math.BigInteger;

/**
 * An interface for factories that create <code>Parser</code>s
 * @see org.foraci.mxf.mxfReader.parsers.Parser
 */
public interface ParserFactory
{
    Parser createParser(BigInteger length, MxfInputStream in);
}
