package io.hhplus.tdd.point;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {

    public long getId() {
        return id;
    }
    public long getUserId() {
        return userId;
    }
    public long getAmount() {
        return amount;
    }
    public TransactionType getType() {
        return type;
    }
    public long getUpdateMillis() {
        return updateMillis;
    }
}
