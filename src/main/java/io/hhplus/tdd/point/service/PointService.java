package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.ResourceNotFoundException;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     */
    public List<PointHistory> getHistories (long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    /**
     * 특정 유저의 포인트를 충전하는 기능
     */
    public UserPoint charge(long id, long amount) {
        UserPoint userPoint = userPointTable.insertOrUpdate(id, amount);
        pointHistoryTable.insert(userPoint.getId(), userPoint.getPoint(), TransactionType.CHARGE, System.currentTimeMillis());
        return userPoint;
    }

    /**
     * 특정 유저의 포인트를 사용하는 기능
     */
    public UserPoint use(long id, long amount) {
        throw new UnsupportedOperationException("포인트 사용 기능은 아직 구현되지 않았습니다.");
    }
}
