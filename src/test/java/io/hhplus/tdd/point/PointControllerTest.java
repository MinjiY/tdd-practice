package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("PointControllerTest 통합 테스트")
@AutoConfigureMockMvc
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Test
    @DisplayName("유저의 포인트 조회 API - 성공")
    void getPointApi_success() throws Exception {
        // given
        long userId = 1L;
        long amount = 1000L;

        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(amount));
    }

    @Test
    @DisplayName("유저의 포인트 충전 API")
    void chargePointApi_success() throws Exception {
        // given
        long userId = 1L;
        long initialAmount = 1000L;
        long chargeAmount = 500L;

        userPointTable.insertOrUpdate(userId, initialAmount);

        mockMvc.perform(MockMvcRequestBuilders.patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(initialAmount + chargeAmount));
    }

    @Test
    @DisplayName("유저의 포인트 사용 API")
    void usePointApi_success() throws Exception {
        // given
        long userId = 1L;
        long initialAmount = 1000L;
        long useAmount = 300L;

        userPointTable.insertOrUpdate(userId, initialAmount);

        mockMvc.perform(MockMvcRequestBuilders.patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(initialAmount - useAmount));
    }

    @Test
    @DisplayName("유저의 포인트 충전/사용 기록 조회 API")
    void getPointHistoryApi_success() throws Exception {
        // given
        long userId = 1L;
        long chargeAmount = 500L;
        long useAmount = 300L;

        mockMvc.perform(MockMvcRequestBuilders.patch("/point/{id}/charge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(chargeAmount)))
            .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.patch("/point/{id}/use", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(useAmount)))
            .andExpect(status().isOk());

        // when & then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].amount").value(chargeAmount))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].userId").value(userId))
                .andExpect(jsonPath("$[1].amount").value(useAmount))
                .andExpect(jsonPath("$[1].type").value("USE"));
    }
}