package org.openapitools;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RFC3339DateFormat extends DateFormat {
  private static final long serialVersionUID = 1L;
  private static final TimeZone TIMEZONE_Z = TimeZone.getTimeZone("UTC");

  private final StdDateFormat fmt =
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
  public RFC3339DateFormat clone() {
    return (RFC3339DateFormat) super.clone();
  }
}
