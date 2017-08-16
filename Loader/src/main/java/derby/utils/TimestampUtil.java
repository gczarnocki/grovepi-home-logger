package derby.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gczarnocki on 2017-07-08.
 */
public class TimestampUtil {
    private static final String DATE_FORMAT = "yyyy-MM-dd:HH:mm:ss";
    private static SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);

    public static Timestamp convert(String string) {
        try {
            Date date = format.parse(string);
            return new Timestamp(date.getTime());
        } catch(ParseException pe) {
            System.out.println("Wystąpił błąd z parsowaniem daty.");
        }

        return null;
    }
}
