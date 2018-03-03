package net.funkyjava.gametheory.io;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProgramArguments {

  private ProgramArguments() {}

  public static String[] splitArguments(final String argStr) {
    final List<String> list = new ArrayList<>();
    final Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(argStr);
    while (m.find()) {
      String str = m.group(1);
      if ((str.startsWith("\"") && str.endsWith("\""))
          || (str.startsWith("'") && str.endsWith("'"))) {
        str = str.substring(1, str.length() - 1);
      }
      list.add(m.group(1));
    }
    return list.toArray(new String[list.size()]);
  }

  public static Optional<String> getArgument(String[] args, String prefix) {
    for (String arg : args) {
      if (arg.startsWith(prefix)) {
        return Optional.of(arg.substring(prefix.length(), arg.length()));
      }
    }
    log.warn("Argument {} not found", prefix);
    return Optional.absent();
  }

  public static Optional<Boolean> getBoolArgument(String[] args, String prefix) {
    final Optional<String> arg = getArgument(args, prefix);
    if (!arg.isPresent()) {
      return Optional.absent();
    }
    try {
      final Boolean res = Boolean.parseBoolean(arg.get());
      return Optional.of(res);
    } catch (NumberFormatException e) {
      log.warn("Unable to parse boolean arg {}", prefix);
      return Optional.absent();
    }
  }

  public static Optional<Integer> getIntArgument(String[] args, String prefix) {
    final Optional<String> strOpt = getArgument(args, prefix);
    if (!strOpt.isPresent()) {
      return Optional.absent();
    }
    try {
      final Integer res = Integer.parseInt(strOpt.get());
      return Optional.of(res);
    } catch (NumberFormatException e) {
      log.warn("Unable to parse integer arg {}", prefix);
      return Optional.absent();
    }
  }

  public static Optional<Integer> getStrictlyPositiveIntArgument(String[] args, String prefix) {
    final Optional<Integer> intOpt = getIntArgument(args, prefix);
    if (intOpt.isPresent()) {
      if (intOpt.get() <= 0) {
        log.warn("Argument {} expected to be strictly positive", prefix);
        return Optional.absent();
      }
    }
    return intOpt;
  }

  public static Optional<Integer> getPositiveIntArgument(String[] args, String prefix) {
    final Optional<Integer> intOpt = getIntArgument(args, prefix);
    if (intOpt.isPresent()) {
      if (intOpt.get() < 0) {
        log.warn("Argument {} expected to be positive", prefix);
        return Optional.absent();
      }
    }
    return intOpt;
  }

}
