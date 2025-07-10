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


    public UserPoint getUserPoint(long userId) {
        if(userId <= 0) {
            throw new IllegalArgumentException("400", "UserId는 0보다 큰 정수여야 합니다.");
        }
        return userPointTable.selectById(userId);
    }

}
