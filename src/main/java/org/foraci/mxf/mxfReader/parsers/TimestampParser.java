package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;

import java.math.BigInteger;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Calendar;

/**
 * Date: Sep 25, 2009 12:30:09 PM
 *
 * @author jforaci
 */
public class TimestampParser extends Parser {
    public TimestampParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public Date read() throws IOException {
        if (count.compareTo(length) >= 0) {
            return null;
        }
        if (length.subtract(count).compareTo(BigInteger.valueOf(8)) < 0) {
            throw new RuntimeException("date field not a multiple of 8 bytes");
        }
        short year = in.readShort();
        int month = in.readUnsignedByte();
        int day = in.readUnsignedByte();
        int hour = in.readUnsignedByte();
        int minute = in.readUnsignedByte();
        int second = in.readUnsignedByte();
        int milli = in.readUnsignedByte() * 4;
        count = count.add(BigInteger.valueOf(8));
        GregorianCalendar cal = new GregorianCalendar(year, month - 1, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, milli);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new Date(cal.getTimeInMillis());
    }
}
