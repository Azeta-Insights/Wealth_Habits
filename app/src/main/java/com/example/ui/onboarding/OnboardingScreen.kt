package com.example.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EditorialBorder
import com.example.ui.theme.EditorialCardBg
import com.example.ui.theme.EditorialOlive
import com.example.ui.theme.EditorialOliveContainer
import com.example.ui.theme.EditorialRust
import com.example.ui.theme.EditorialRustContainer
import com.example.ui.theme.EditorialSlate

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val badge: String
)

@Composable
fun OnboardingScreen(
    onComplete: (requestSmsPermission: Boolean) -> Unit
) {
    val steps = remember {
        listOf(
            OnboardingStep(
                badge = "FINANCIAL LITERACY",
                title = "Calm, Plain-Language Insights",
                subtitle = "Turn everyday bank alerts into human understanding",
                description = "Instead of complicated charts, confusing graphs, or financial jargon, Wealth Habits turns your transactions into gentle, non-judgmental questions: 'You spent ₦3,200 more on transport this week. Was that a need or a want?'",
                icon = Icons.Default.Psychology
            ),
            OnboardingStep(
                badge = "ABSOLUTE SAFETY",
                title = "We Never Touch or Move Money",
                subtitle = "No BVN, PIN, or card details ever requested",
                description = "Wealth Habits is strictly a reflective awareness tool. We never ask for your bank logins, BVN, ATM card digits, or transaction PINs. We have zero ability to withdraw, move, or modify your money.",
                icon = Icons.Default.Shield
            ),
            OnboardingStep(
                badge = "TOTAL PRIVACY",
                title = "100% On-Device Processing",
                subtitle = "Your financial data never leaves your phone",
                description = "All bank alert reading, statement parsing, and habits insights run entirely in pure Kotlin on your device. Nothing is ever uploaded to the cloud or sent across the network. Your records stay completely yours.",
                icon = Icons.Default.Lock
            )
        )
    }

    var currentStep by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Identity Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wealth Habits",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EditorialOlive,
                        letterSpacing = 0.5.sp
                    )
                )

                if (currentStep < steps.size - 1) {
                    OutlinedButton(
                        onClick = { currentStep = steps.size - 1 },
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder),
                        modifier = Modifier.testTag("skip_onboarding_button")
                    ) {
                        Text("Skip", style = MaterialTheme.typography.labelMedium.copy(color = EditorialSlate, fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            // Step Content
            AnimatedContent(
                targetState = currentStep,
                label = "onboarding_step"
            ) { stepIndex ->
                val step = steps[stepIndex]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon Container
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                when (stepIndex) {
                                    0 -> EditorialOliveContainer
                                    1 -> EditorialRustContainer
                                    else -> EditorialCardBg
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = step.title,
                            modifier = Modifier.size(54.dp),
                            tint = when (stepIndex) {
                                0 -> EditorialOlive
                                1 -> EditorialRust
                                else -> EditorialOlive
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Pill Badge
                    Surface(
                        color = when (stepIndex) {
                            0 -> EditorialOliveContainer.copy(alpha = 0.7f)
                            1 -> EditorialRustContainer.copy(alpha = 0.7f)
                            else -> EditorialCardBg
                        },
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (stepIndex) {
                                0 -> EditorialOlive.copy(alpha = 0.2f)
                                1 -> EditorialRust.copy(alpha = 0.2f)
                                else -> EditorialBorder
                            }
                        ),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = step.badge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when (stepIndex) {
                                    0 -> EditorialOlive
                                    1 -> EditorialRust
                                    else -> EditorialOlive
                                },
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialOlive
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialRust
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Navigation Dots and Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(steps.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (index == currentStep) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep) {
                                        EditorialOlive
                                    } else {
                                        EditorialBorder
                                    }
                                )
                        )
                    }
                }

                if (currentStep < steps.size - 1) {
                    Button(
                        onClick = { currentStep++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_next_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EditorialOlive
                        )
                    ) {
                        Text("Continue", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next"
                        )
                    }
                } else {
                    // Final Step: Option to enable SMS or continue
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onComplete(true) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_enable_sms_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EditorialOlive
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Automatic SMS Alerts",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = { onComplete(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_manual_first_button"),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder)
                        ) {
                            Text(
                                text = "Start with Manual & Statement Upload",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = EditorialOlive)
                            )
                        }
                    }
                }
            }
        }
    }
}
