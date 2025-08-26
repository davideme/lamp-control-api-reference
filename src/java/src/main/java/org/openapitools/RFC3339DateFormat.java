package org.openapitools;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RFC3339DateFormat extends DateFormat {
  private static final long serialVersionUID = 1L;
  private static final TimeZone TIMEZONE_Z = TimeZone.getTimeZone("UTC");

  private StdDateFormat fmt =
      new StdDateFormat().withTimeZone(TIMEZONE_Z).withColonInTimeZone(true);

  public RFC3339DateFormat() {
    super();
    this.calendar = new GregorianCalendar();
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
    final RFC3339DateFormat copy = (RFC3339DateFormat) super.clone();
    // Reinitialize fmt on the cloned instance so we do not inherit
    // potentially inconsistent internal state from StdDateFormat.
    copy.fmt = new StdDateFormat().withTimeZone(TIMEZONE_Z).withColonInTimeZone(true);
    // copy the calendar state to preserve timezone/locale information
    if (this.calendar != null) {
      copy.calendar = (Calendar) this.calendar.clone();
    }
    return copy;
  }
}
