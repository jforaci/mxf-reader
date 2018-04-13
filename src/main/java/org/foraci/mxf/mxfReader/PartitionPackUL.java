package org.foraci.mxf.mxfReader;

import java.math.BigInteger;

/**
 * Date: Oct 1, 2009 5:24:46 PM
 *
 * @author jforaci
 */
public class PartitionPackUL extends PartialUL {
    public static byte[] PARTITION_KEY_PREFIX = { 0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x01, 0x0d, 0x01, 0x02 };

    public PartitionPackUL() {
        super("Partition Pack", PARTITION_KEY_PREFIX);
    }

    Key createKey(UL ul, BigInteger length) {
        return new PartitionPackKey(ul, length);
    }
}
