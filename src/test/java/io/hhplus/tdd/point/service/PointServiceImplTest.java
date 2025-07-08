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
import static org.mockito.Mockito.*;

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
        verify(pointHistoryTable).selectAllByUserId(userId);
        assertThat(actualHistories.size(), is(1));
        assertThat(actualHistories.get(0).userId(), is(expectedHistories.get(0).userId()));
        assertThat(actualHistories.get(0).amount(), is(expectedHistories.get(0).amount()));
        assertThat(actualHistories.get(0).type(), is(expectedHistories.get(0).type()));
        assertThat(actualHistories.get(0).updateMillis(), is(expectedHistories.get(0).updateMillis()));
    }

    @Test
    @DisplayName("유저 포인트/충전 이용 내역의 시간 값은 현재 시간보다 이전이어야 한다.")
    public void testHistoriesTime() {
        // given
        List<PointHistory> expectedHistories = List.of(
                new PointHistory(1L, 1L, 1000L, TransactionType.CHARGE, System.currentTimeMillis() - 1000L)
        );

        // when
        when(pointHistoryTable.selectAllByUserId(1L)).thenReturn(expectedHistories);

        List<PointHistory> actualHistories = pointService.getHistories(1L);

        // then
        assertTrue(actualHistories.get(0).updateMillis() < System.currentTimeMillis());
    }


    @Test
    @DisplayName("유저 포인트 충전 기능 반환값은 정상적으로 반환되어야 한다.")
    public void testChargeUserPoint() {
        // given
        long userId = 1L;
        long amount = 1000L;

        // when
        UserPoint userPoint = pointService.charge(userId, amount);

        // then
        assertThat(userPoint.getId(), is(userId));
        assertThat(userPoint.getPoint(), is(amount));
    }

    @Test
    @DisplayName("유저 포인트 충전 기능의 insert 로직은 정상적으로 호출되어야한다.")
    public void testInsertUserPoint() {
        // given
        long userId = 1L;
        long amount = 1000L;
        UserPoint expectedUserPoint = pointService.charge(userId, amount);

        // when
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(expectedUserPoint);

        UserPoint actualUserPoint = pointService.charge(userId, amount);

        // then
        verify(userPointTable).insertOrUpdate(userId, amount);
        assertThat(actualUserPoint.getId(), is(expectedUserPoint.getId()));
        assertThat(actualUserPoint.getPoint(), is(expectedUserPoint.getPoint()));
    }
}