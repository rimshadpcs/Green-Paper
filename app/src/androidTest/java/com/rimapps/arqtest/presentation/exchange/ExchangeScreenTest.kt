package com.rimapps.arqtest.presentation.exchange

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.rimapps.arqtest.core.designsystem.theme.ArqTestTheme
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class ExchangeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun exchangeScreenContent_showsTitleRateCardsAndSwapButton() {

        composeTestRule.setContent {
            ArqTestTheme {
                ExchangeScreenContent(
                    state = ExchangeUiState(
                        topAmount = "10",
                        bottomAmount = "180.00",
                        currentRate = BigDecimal("18.00"),
                        lastUpdated = "2026-05-23T00:00:00Z"
                    ),
                    onEvent = { event ->
                    }
                )

            }
        }
        composeTestRule.onNodeWithText("Exchange calculator").assertIsDisplayed()
        composeTestRule.onNodeWithText("USDc").assertIsDisplayed()
        composeTestRule.onNodeWithText("MXN").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("180.00").assertIsDisplayed()

    }

    @Test
    fun exchangeScreenContent_clickingSwapButtonEmitsSwapEvent() {
        var emittedEvent: ExchangeUiEvent? = null


        composeTestRule.setContent {
            ArqTestTheme {
                ExchangeScreenContent(
                    state = ExchangeUiState(
                        topAmount = "10",
                        bottomAmount = "180.00",
                        currentRate = BigDecimal("18.00"),
                        lastUpdated = "2026-05-23T00:00:00Z"

                    ),
                    onEvent = { event ->
                        emittedEvent = event

                    }
                )

            }
        }

        composeTestRule.onNodeWithContentDescription("Swap currencies").performClick()
        Thread.sleep(3000)

        assertEquals(ExchangeUiEvent.SwapClicked, emittedEvent)
    }
}