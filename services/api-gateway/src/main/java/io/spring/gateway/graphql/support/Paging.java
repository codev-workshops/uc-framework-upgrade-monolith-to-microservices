package io.spring.gateway.graphql.support;

/** Translates Relay first/after (or last/before) arguments into REST offset/limit paging. */
public final class Paging {
  public final int offset;
  public final int limit;

  private Paging(int offset, int limit) {
    this.offset = offset;
    this.limit = limit;
  }

  public static Paging from(Integer first, String after, Integer last, String before) {
    if (first == null && last == null) {
      throw new IllegalArgumentException("first and last must not both be null");
    }
    if (first != null) {
      int off = after == null ? 0 : Integer.parseInt(after) + 1;
      return new Paging(off, first);
    }
    // backward pagination (best-effort over offset-based lists)
    int end = before == null ? 0 : Integer.parseInt(before);
    int off = Math.max(0, end - last);
    return new Paging(off, last);
  }
}
