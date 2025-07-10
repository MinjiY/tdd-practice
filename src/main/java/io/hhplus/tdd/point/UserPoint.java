package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public long getId() {
        return this.id;
    }

    public long getPoint() {
        return this.point;
    }
    public long getUpdateMillis() {
        return this.updateMillis;
    }
}
