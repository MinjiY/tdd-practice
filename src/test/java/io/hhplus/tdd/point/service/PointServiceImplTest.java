package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
        UserPoint expectedUserPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        // when
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(expectedUserPoint);

        UserPoint actualUserPoint = pointService.charge(userId, amount);

        // then
        verify(userPointTable).insertOrUpdate(userId, amount);
        assertThat(actualUserPoint.getId(), is(expectedUserPoint.getId()));
        assertThat(actualUserPoint.getPoint(), is(expectedUserPoint.getPoint()));
    }

    @Test
    @DisplayName("유저 포인트 충전시 PointHistory가 기록되어야한다.")
    public void testInsertChargeHistory() {
        // given
        long pointHistoryId = 1L;

        // when
        when(pointHistoryTable.insert(anyLong(), anyLong(), eq(TransactionType.CHARGE), anyLong())).thenAnswer(invocation -> {
            long userIdArg = invocation.getArgument(0);
            long amountArg = invocation.getArgument(1);
            TransactionType typeArg = invocation.getArgument(2);
            long updateMillisArg = invocation.getArgument(3);
            return new PointHistory(pointHistoryId, userIdArg, amountArg, typeArg, updateMillisArg);
        });

        pointService.charge(1L, 1000L);

        // then
        verify(pointHistoryTable).insert(anyLong(), anyLong(), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("charge 호출 시 pointHistoryTable.insert에 전달된 값이 올바른지 검증한다.")
    void testChargePointHistoryInsertArguments() {
        // given
        long userId = 1L;
        long amount = 1000L;
        TransactionType type = TransactionType.CHARGE;
        // ArgumentCaptor로 전달된 값 캡처
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> amountCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TransactionType> typeCaptor = ArgumentCaptor.forClass(TransactionType.class);
        ArgumentCaptor<Long> updateMillisCaptor = ArgumentCaptor.forClass(Long.class);


        UserPoint expectedUserPoint = new UserPoint(userId, amount, System.currentTimeMillis());
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(expectedUserPoint);

        // when
        pointService.charge(userId, amount);

        // then
        verify(pointHistoryTable).insert(
                userIdCaptor.capture(),
                amountCaptor.capture(),
                typeCaptor.capture(),
                updateMillisCaptor.capture()
        );

        assertThat(userIdCaptor.getValue(), is(userId));
        assertThat(amountCaptor.getValue(), is(amount));
        assertThat(typeCaptor.getValue(), is(type));
    }


    @Test
    @DisplayName("충전 기록보다 포인트 충전 여부가 먼저 결정되어야 한다.")
    void testChargeMethodCallOrder() {
        // given
        long userId = 1L;
        long amount = 1000L;
        UserPoint expectedUserPoint = new UserPoint(userId, amount, System.currentTimeMillis());
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(expectedUserPoint);

        // when
        pointService.charge(userId, amount);

        // then
        InOrder inOrder = inOrder(userPointTable, pointHistoryTable);
        inOrder.verify(userPointTable).insertOrUpdate(userId, amount);
        inOrder.verify(pointHistoryTable).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 서비스는 정상적인 값을 반환해야한다.")
    public void testUseUserPoint() {
        // given
        long userId = 1L;
        long amount = 500L;

        // when
        UserPoint userPoint = pointService.use(userId, amount);

        // then
        assertThat(userPoint.getId(), is(userId));
        assertThat(userPoint.getPoint(), is(amount));
    }

    @Test
    @DisplayName("포인트 사용 서비스는 내부적으로 userPoint.insertOrUpdate 메서드를 호출하고 정상적인 값을 반환해야한다.")
    public void testUseUserPointUpdate() {
        // given
        long userId = 1L;
        long amount = 500L;

        UserPoint expectedUserPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        // when
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(expectedUserPoint);
        UserPoint actualUserPoint = pointService.use(userId, amount);

        // then
        verify(userPointTable).insertOrUpdate(userId, amount);
        assertThat(actualUserPoint.getId(), is(expectedUserPoint.getId()));
        assertThat(actualUserPoint.getPoint(), is(expectedUserPoint.getPoint()));
    }

    @Test
    @DisplayName("포인트 사용 서비스는 갖고있는 포인트에서 차감해야한다.")
    public void testUsePoint() {
        // given
        long userId = 1L;
        long initialPoint = 1000L;
        long useAmount = 500L;

        UserPoint beforeUserPoint = new UserPoint(userId, initialPoint, System.currentTimeMillis());
        UserPoint afterUserPoint = new UserPoint(userId, initialPoint - useAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(beforeUserPoint);
        when(userPointTable.insertOrUpdate(userId, useAmount)).thenReturn(afterUserPoint);
        // when

        UserPoint actualUserPoint = pointService.use(userId, useAmount);

        // then
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, useAmount);
        assertThat(actualUserPoint.getId(), is(afterUserPoint.getId()));
        assertThat(actualUserPoint.getPoint(), is(afterUserPoint.getPoint()));
    }
}