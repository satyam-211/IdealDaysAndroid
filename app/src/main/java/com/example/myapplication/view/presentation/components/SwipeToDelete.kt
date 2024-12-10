package com.example.myapplication.view.presentation.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@SuppressLint("AutoboxingStateCreation")
@Composable
fun SwipeToDelete(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    enableSwipe: Boolean = true,
    swipeThreshold: Float = 0.4f,
    animationSpec: AnimationSpec<Float> = tween(300),
    content: @Composable () -> Unit,
) {
    var offsetX by remember {
        mutableStateOf(0f)
    }

    val coroutineScope = rememberCoroutineScope()

    var componentWidth by remember { mutableIntStateOf(0) }

    fun animateToTarget(targetValue: Float) {
        coroutineScope.launch {
            animate(
                initialValue = offsetX,
                targetValue = targetValue,
                animationSpec = animationSpec
            ) { value, _ ->
                offsetX = value
            }
        }
    }

    Box(modifier = modifier.onSizeChanged {
        componentWidth = it.width
    }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterEnd)
                .offset {
                    IntOffset(
                        x = (componentWidth + offsetX).roundToInt(),
                        y = 0
                    )
                }
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = modifier
                        .width((-offsetX).dp)
                        .fillMaxHeight()
                        .background(Color.Red)
                ) {
                    Icon(
                        imageVector = Lucide.Trash,
                        contentDescription = "Delete",
                        modifier = Modifier
                            .size(44.dp)
                            .padding(end = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = enableSwipe,
                    state = rememberDraggableState { delta ->
                        val newOffset = offsetX + delta

                        if (newOffset <= 0) {
                            offsetX = newOffset.coerceAtLeast(-componentWidth.toFloat())
                        }
                    },
                    onDragStarted = {},
                    onDragStopped = {
                        val threshold = componentWidth * swipeThreshold
                        if (offsetX.absoluteValue >= threshold) {
                            animateToTarget(-componentWidth.toFloat())
                            onDelete()
                        } else {
                            animateToTarget(0f)
                        }
                    }
                )
        ) {
            content()
        }
    }
}


