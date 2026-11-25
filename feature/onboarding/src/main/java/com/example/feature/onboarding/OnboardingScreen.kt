package com.example.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.designsystem.components.AutoBoxButton
import com.example.core.designsystem.components.AutoBoxButtonVariant
import com.example.core.designsystem.preview.AutoBoxComponentPreviews
import com.example.core.designsystem.theme.AutoBoxSpacing
import com.example.core.designsystem.theme.AutoBoxTheme
import kotlinx.coroutines.launch

private data class OnboardingPage(
  @StringRes val titleRes: Int,
  @StringRes val descriptionRes: Int,
  @DrawableRes val imageRes: Int,
)

private val onboardingPages =
  listOf(
    OnboardingPage(
      titleRes = R.string.onboarding_title_1,
      descriptionRes = R.string.onboarding_desc_1,
      imageRes = R.drawable.img_onboarding_care,
    ),
    OnboardingPage(
      titleRes = R.string.onboarding_title_2,
      descriptionRes = R.string.onboarding_desc_2,
      imageRes = R.drawable.img_onboarding_pix,
    ),
    OnboardingPage(
      titleRes = R.string.onboarding_title_3,
      descriptionRes = R.string.onboarding_desc_3,
      imageRes = R.drawable.img_onboarding_rewards,
    ),
  )

@Composable
fun OnboardingRoute(
  onNavigateToHome: () -> Unit,
  viewModel: OnboardingViewModel = hiltViewModel(),
) {
  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        OnboardingEvent.NavigateToHome -> onNavigateToHome()
      }
    }
  }

  OnboardingScreen(
    onSkip = viewModel::onFinishOnboarding,
    onFinish = viewModel::onFinishOnboarding,
  )
}

@Composable
fun OnboardingScreen(
  onSkip: () -> Unit,
  onFinish: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
  val scope = rememberCoroutineScope()
  val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier =
          Modifier
            .widthIn(max = 600.dp)
            .fillMaxSize()
            .padding(horizontal = AutoBoxSpacing.lg, vertical = AutoBoxSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
        ) {
          AutoBoxButton(
            text = stringResource(R.string.onboarding_skip),
            onClick = onSkip,
            variant = AutoBoxButtonVariant.TEXT,
            testTag = "onboarding_skip_button",
          )
        }

        HorizontalPager(
          state = pagerState,
          modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { pageIndex ->
          val page = onboardingPages[pageIndex]
          Column(
            modifier =
              Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Image(
              painter = painterResource(id = page.imageRes),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier =
                Modifier
                  .fillMaxWidth()
                  .aspectRatio(4f / 3f)
                  .clip(MaterialTheme.shapes.large),
            )
            Spacer(modifier = Modifier.height(AutoBoxSpacing.lg))
            Text(
              text = stringResource(page.titleRes),
              style = MaterialTheme.typography.headlineSmall,
              textAlign = TextAlign.Center,
              modifier = Modifier.semantics { heading() },
            )
            Spacer(modifier = Modifier.height(AutoBoxSpacing.sm))
            Text(
              text = stringResource(page.descriptionRes),
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }

        Spacer(modifier = Modifier.height(AutoBoxSpacing.md))

        val indicatorDescription =
          stringResource(
            R.string.onboarding_page_indicator_cd,
            pagerState.currentPage + 1,
            onboardingPages.size,
          )
        Row(
          horizontalArrangement = Arrangement.spacedBy(AutoBoxSpacing.xs),
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier
              .padding(vertical = AutoBoxSpacing.sm)
              .semantics { contentDescription = indicatorDescription },
        ) {
          repeat(onboardingPages.size) { index ->
            val selected = index == pagerState.currentPage
            Box(
              modifier =
                Modifier
                  .size(if (selected) 10.dp else 8.dp)
                  .clip(CircleShape)
                  .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                  ),
            )
          }
        }

        Spacer(modifier = Modifier.height(AutoBoxSpacing.sm))

        AutoBoxButton(
          text =
            stringResource(
              if (isLastPage) R.string.onboarding_start else R.string.onboarding_next,
            ),
          onClick = {
            if (isLastPage) {
              onFinish()
            } else {
              scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
          testTag = "onboarding_primary_button",
        )
      }
    }
  }
}

@AutoBoxComponentPreviews
@Composable
private fun OnboardingScreenPreview() {
  AutoBoxTheme {
    OnboardingScreen(
      onSkip = {},
      onFinish = {},
    )
  }
}
