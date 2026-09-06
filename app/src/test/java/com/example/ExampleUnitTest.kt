package com.example

import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType
import com.example.insights.InsightEngine
import com.example.insights.InsightType
import com.example.sms.SmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testGtBankSmsParsing() {
    val sms = "Acct: 0123456789 Amt: NGN 3,500.00 DR Desc: POS/CHOWDECK LAGOS NG Date: 12-OCT-2024 14:30 Bal: NGN 45,000.00"
    val parsed = SmsParser.parseSms("GTBank", sms, 1728740000000L)

    assertNotNull(parsed)
    assertEquals(3500.0, parsed!!.amount, 0.01)
    assertEquals(TransactionType.DEBIT, parsed.type)
    assertEquals(TransactionCategory.FOOD, parsed.category)
    assertEquals("GTBank", parsed.bankName)
    assertNotNull(parsed.deduplicationHash)
  }

  @Test
  fun testAccessBankSmsParsing() {
    val sms = "Debit Alert: NGN 12,000.00 debited from acct ...4321 for UBER TRIP LEKKI on 15/10/2024. Bal: NGN 20,000"
    val parsed = SmsParser.parseSms("AccessBank", sms, 1729000000000L)

    assertNotNull(parsed)
    assertEquals(12000.0, parsed!!.amount, 0.01)
    assertEquals(TransactionType.DEBIT, parsed.type)
    assertEquals(TransactionCategory.TRANSPORT, parsed.category)
    assertEquals("Access Bank", parsed.bankName)
  }

  @Test
  fun testOPayCreditParsing() {
    val sms = "You have received NGN 50,000.00 from John Doe for Freelance web design. Your new wallet balance is NGN 65,000."
    val parsed = SmsParser.parseSms("OPay", sms, 1729000000000L)

    assertNotNull(parsed)
    assertEquals(50000.0, parsed!!.amount, 0.01)
    assertEquals(TransactionType.CREDIT, parsed.type)
    assertEquals(TransactionCategory.BUSINESS, parsed.category)
    assertEquals("OPay", parsed.bankName)
  }

  @Test
  fun testInsightEngineWeekOverWeekChange() {
    val now = System.currentTimeMillis()
    val oneDay = 24L * 60 * 60 * 1000
    val oneWeek = 7L * oneDay

    // Current week: ₦8,000 on Transport
    val tx1 = TransactionEntity(
      amount = 8000.0,
      type = TransactionType.DEBIT,
      category = TransactionCategory.TRANSPORT,
      narration = "Danfo & Uber",
      timestamp = now - (2 * oneDay)
    )

    // Prior week: ₦4,000 on Transport (Increase = ₦4,000 >= 500, +100% >= 20%)
    val tx2 = TransactionEntity(
      amount = 4000.0,
      type = TransactionType.DEBIT,
      category = TransactionCategory.TRANSPORT,
      narration = "Bus fare",
      timestamp = now - (9 * oneDay)
    )

    val insights = InsightEngine.generateInsights(listOf(tx1, tx2))
    val transportInsight = insights.find { it.type == InsightType.CATEGORY_WEEK_CHANGE && it.category == TransactionCategory.TRANSPORT }

    assertNotNull(transportInsight)
    assertTrue(transportInsight!!.message.contains("transport"))
    assertTrue(transportInsight.message.contains("₦4,000") || transportInsight.message.contains("4,000"))
    assertEquals("Was that a need or a want?", transportInsight.reflectiveQuestion)
  }

  @Test
  fun testInsightEngineSpikeDetection() {
    val now = System.currentTimeMillis()
    val oneDay = 24L * 60 * 60 * 1000

    val txHistory1 = TransactionEntity(amount = 2000.0, type = TransactionType.DEBIT, category = TransactionCategory.FOOD, narration = "Lunch", timestamp = now - (20 * oneDay))
    val txHistory2 = TransactionEntity(amount = 2500.0, type = TransactionType.DEBIT, category = TransactionCategory.FOOD, narration = "Dinner", timestamp = now - (15 * oneDay))
    // Historical avg = 2250. Spike threshold = 1.5 * 2250 = 3375.
    // Spike: ₦8,500 (>= 3,375 and >= 3,000)
    val txSpike = TransactionEntity(id = 99L, amount = 8500.0, type = TransactionType.DEBIT, category = TransactionCategory.FOOD, narration = "Fancy Restaurant", timestamp = now - (1 * oneDay))

    val insights = InsightEngine.generateInsights(listOf(txSpike, txHistory1, txHistory2))
    val spike = insights.find { it.type == InsightType.TRANSACTION_SPIKE }

    assertNotNull(spike)
    assertTrue(spike!!.message.contains("8,500"))
    assertTrue(spike.reflectiveQuestion.contains("need or a want"))
  }
}
