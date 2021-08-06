package com.eseasky.codegen.generate.util;

import org.apache.commons.lang.RandomStringUtils;

import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class NonceUtils
{
  private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  private static final String[] b = { "0", "00", "0000", "00000000" };
  private static Date c;
  private static int d = 0;

  public static String randomAlphanumeric(int paramInt)
  {
    return RandomStringUtils.randomAlphanumeric(paramInt);
  }

  public static int randomAlphanumeric()
  {
    return new SecureRandom().nextInt();
  }

  public static String toHexString()
  {
    return Integer.toHexString(randomAlphanumeric());
  }

  public static long randomLong()
  {
    return new SecureRandom().nextLong();
  }

  public static String randomLongHex()
  {
    return Long.toHexString(randomLong());
  }

  public static String uuid()
  {
    return UUID.randomUUID().toString();
  }

  public static String dateFormat()
  {
    Date localDate = new Date();
    return format.format(localDate);
  }

  public static long currentTime()
  {
    return System.currentTimeMillis();
  }

  public static String currentTimeHex()
  {
    return Long.toHexString(currentTime());
  }

  public static synchronized String i()
  {
    Date localDate = new Date();

    if (localDate.equals(c)) {
      d += 1;
    } else {
      c = localDate;
      d = 0;
    }
    return Integer.toHexString(d);
  }

  public static String randomAlphanumeric(String paramString, int paramInt)
  {
    int i = paramInt - paramString.length();
    StringBuilder localStringBuilder = new StringBuilder();

    while (i >= 8) {
      localStringBuilder.append(b[3]);
      i -= 8;
    }

    for (int j = 2; j >= 0; j--) {
      if ((i & 1 << j) != 0) {
        localStringBuilder.append(b[j]);
      }
    }

    localStringBuilder.append(paramString);
    return localStringBuilder.toString();
  }
  public static void main(String[] args) throws IOException {
    System.out.println(randomLong() + currentTime());
  }
}