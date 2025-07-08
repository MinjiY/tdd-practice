package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @InjectMocks
    PointService pointService;

    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;

    @Test
    @DisplayName("유저 포인트 조회 테스트")
    public void testGetUserPoint() {
        // given
        long userId = 1L;

        // when
        UserPoint actualPoint = pointService.getUserPoint(userId);

        // then
        assertThat(actualPoint.getId(), is(userId));
    }

    @Test
    @DisplayName("유저 포인트 조회 테스트 - when을 통한 동작 검증")
    public void testUserPointTable() {
        // given
        long userId = 1L;
        long amount = 1000L;
        UserPoint expectedPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(userId)).thenReturn(expectedPoint);

        UserPoint actualPoint = pointService.getUserPoint(userId);

        // then
        assertThat(actualPoint.getId(), is(expectedPoint.getId()));
        assertThat(actualPoint.getPoint(), is(expectedPoint.getPoint()));
    }

//    @Test
//    @DisplayName("유저 포인트 조회 테스트 - 예외 상황")
//    public void testGetUserPointException() {
//        // given
//        long userId = 1L;
//
//        // when
//        when(userPointTable.selectById(userId)).thenThrow(new ResourceNotFoundException("404","user point not found"));
//
//        // then
//        assertThrows(ResourceNotFoundException.class, () -> {
//            pointService.getUserPoint(userId);
//        });
//    }


    @Test
    @DisplayName("유저 포인트 충전/이용 내역 조회 테스트 - ")
    public void testGetPointHistory() {
        // given
        long userId = 1L;

        // when
        List<PointHistory> histories = pointService.getHistories(userId);

        // then
        assertTrue(histories.isEmpty());
    }

    @Test
    @DisplayName("유저 포인트/충전 이용 내역을 정상적으로 반환한다.")
    public void testGetHistories() {
        // given
        long pointHistoryId = 1L;
        long userId = 1L;
        long amount = 1000L;
        TransactionType type = TransactionType.CHARGE;

        List<PointHistory> expectedHistories = List.of(
                new PointHistory(pointHistoryId, userId, amount, type, System.currentTimeMillis())
        );

        // when
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expectedHistories);

        List<PointHistory> actualHistories = pointService.getHistories(userId);

        // then
        assertThat(actualHistories.size(), is(1));
        assertThat(actualHistories.get(0).userId(), is(expectedHistories.get(0).userId()));
        assertThat(actualHistories.get(0).amount(), is(expectedHistories.get(0).amount()));
        assertThat(actualHistories.get(0).type(), is(expectedHistories.get(0).type()));
        assertThat(actualHistories.get(0).updateMillis(), is(expectedHistories.get(0).updateMillis()));
    }






}