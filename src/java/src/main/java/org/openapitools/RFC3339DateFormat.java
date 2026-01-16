package org.openapitools;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class RFC3339DateFormat extends DateFormat {
  private static final long serialVersionUID = 1L;
  private static final TimeZone TIMEZONE_Z = TimeZone.getTimeZone("UTC");

  private final StdDateFormat fmt =
      new StdDateFormat().withTimeZone(TIMEZONE_Z).withColonInTimeZone(true);

  public RFC3339DateFormat() {
    super();
    this.calendar = new GregorianCalendar(TIMEZONE_Z);
  }

  @Override
  public Date parse(final String source, final ParsePosition pos) {
    return fmt.parse(source, pos);
  }

  @Override
  public StringBuffer format(
      final Date date, final StringBuffer toAppendTo, final FieldPosition fieldPosition) {
    return fmt.format(date, toAppendTo, fieldPosition);
  }

  @Override
  @SuppressWarnings("PMD.ProperCloneImplementation")
  public RFC3339DateFormat clone() {
    // Return a fresh, fully-initialized instance instead of delegating
    // to super.clone(). The class is final, so returning a new
    // instance does not violate the clone contract for subclasses.
    final RFC3339DateFormat copy = new RFC3339DateFormat();
    if (this.calendar != null) {
      copy.calendar = (Calendar) this.calendar.clone();
    }
    return copy;
  }
}
