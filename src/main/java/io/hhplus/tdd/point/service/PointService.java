package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.IllegalArgumentException;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import io.hhplus.tdd.exception.IllegalArgumentException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;


    /**
     * 특정 유저의 포인트를 조회하는 기능
     *
     * @param userId 유저 ID
     * @return UserPoint 객체
     * @throws IllegalArgumentException 유효하지 않은 userId인 경우
     */
    public UserPoint getUserPoint(long userId) {
        if(userId <= 0) {
            throw new IllegalArgumentException("400", "UserId는 0보다 큰 정수여야 합니다.");
        }
        return userPointTable.selectById(userId);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
     *
     * @param userId 유저 ID
     * @return List<PointHistory> 포인트 내역 리스트
     */
    public List<PointHistory> getUserPointHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 특정 유저의 포인트를 충전하는 기능
     *
     * @param userId 유저 ID
     * @param amount 충전할 포인트 금액
     * @return UserPoint 객체
     */
    public UserPoint chargeUserPoint(long userId, long amount) {
        return userPointTable.insertOrUpdate(userId, amount);
    }
}
