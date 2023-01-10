package org.dcsa.endorsementchain.persistence.entity;

import java.util.Comparator;

public class TransactionByTimestampComparator implements Comparator<Transaction> {

  // We need this indirection as hibernate wants to reference a concrete class in @SortComparator.
  // Otherwise, this entire class could have been replaced by a public version of this field.
  private static final Comparator<Transaction> IMPL = Comparator.comparing(Transaction::getTimestamp).reversed();

  public static final TransactionByTimestampComparator INSTANCE = new TransactionByTimestampComparator();
  @Override
  public int compare(Transaction o1, Transaction o2) {
    return IMPL.compare(o1, o2);
  }
}
