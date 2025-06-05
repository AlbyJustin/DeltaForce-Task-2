package com.example.arcadecrawler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.tooling.preview.Preview
import com.example.arcadecrawler.ui.theme.ArcadeCrawlerTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.DrawScope


data class Gun(var x: Float, var y: Float, val width: Float, val height: Float)

data class Bullet(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val speed: Float
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArcadeCrawlerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    GameScreen(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    var gunPosition by remember { mutableStateOf(Offset(0f, 0f)) }

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val density = LocalDensity.current

    val gunWidthPx = with(density) { 50.dp.toPx() }
    val gunHeightPx = with(density) { 30.dp.toPx() }

    var gunMinY by remember { mutableStateOf(0f) }
    var gunMaxY by remember { mutableStateOf(0f) }

    val gunAreaBottomPaddingDp = 50.dp
    val gunAreaHeightDp = 80.dp

    val bullets = remember { mutableStateListOf<Bullet>() }

    val bulletWidthPx = with(density) { 5.dp.toPx() }
    val bulletHeightPx = with(density) { 15.dp.toPx() }
    val bulletSpeed = with(density) { 20.dp.toPx() }

    LaunchedEffect(Unit) {
        while (true) {
            val bulletsToRemove = mutableListOf<Bullet>()
            bullets.forEach { bullet ->
                bullet.y -= bullet.speed
                if (bullet.y < -bullet.height) {
                    bulletsToRemove.add(bullet)
                }
            }
            bullets.removeAll(bulletsToRemove)

            delay(16L)
        }
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = {

                    if (canvasSize != Size.Zero) {
                        val newBullet = Bullet(
                            x = gunPosition.x + (gunWidthPx / 2f) - (bulletWidthPx / 2f),
                            y = gunPosition.y,
                            width = bulletWidthPx,
                            height = bulletHeightPx,
                            speed = bulletSpeed
                        )
                        bullets.add(newBullet)
                    }
                }) {
                    Text("Shoot")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (canvasSize == Size.Zero) {
                                    canvasSize = this.size.toSize()
                                    val gunAreaBottomPaddingPx = with(density) { gunAreaBottomPaddingDp.toPx() }
                                    val gunAreaHeightPx = with(density) { gunAreaHeightDp.toPx() }

                                    gunMaxY = canvasSize.height - gunHeightPx - gunAreaBottomPaddingPx
                                    gunMinY = gunMaxY - gunAreaHeightPx

                                    gunPosition = Offset(
                                        x = (canvasSize.width / 2f) - (gunWidthPx / 2f),
                                        y = gunMaxY
                                    )
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()

                                val newX = (gunPosition.x + dragAmount.x)
                                    .coerceIn(0f, canvasSize.width - gunWidthPx)

                                val newY = (gunPosition.y + dragAmount.y)
                                    .coerceIn(gunMinY, gunMaxY)

                                gunPosition = Offset(x = newX, y = newY)
                            }
                        )
                    }
            ) {
                if (canvasSize == Size.Zero) {
                    canvasSize = size
                    val gunAreaBottomPaddingPx = with(density) { gunAreaBottomPaddingDp.toPx() }
                    val gunAreaHeightPx = with(density) { gunAreaHeightDp.toPx() }

                    gunMaxY = canvasSize.height - gunHeightPx - gunAreaBottomPaddingPx
                    gunMinY = gunMaxY - gunAreaHeightPx

                    gunPosition = Offset(
                        x = (canvasSize.width / 2f) - (gunWidthPx / 2f),
                        y = gunMaxY
                    )
                }

                drawRect(
                    color = Color.Blue,
                    topLeft = gunPosition,
                    size = Size(gunWidthPx, gunHeightPx)
                )

                drawLine(
                    color = Color.Black,
                    start = Offset(0f, gunMaxY + gunHeightPx),
                    end = Offset(size.width, gunMaxY + gunHeightPx),
                    strokeWidth = 2.dp.toPx()
                )

                drawLine(
                    color = Color.Black,
                    start = Offset(0f, gunMinY),
                    end = Offset(size.width, gunMinY),
                    strokeWidth = 2.dp.toPx()
                )

                bullets.forEach { bullet ->
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(bullet.x, bullet.y),
                        size = Size(bullet.width, bullet.height)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ArcadeCrawlerTheme {
        GameScreen()
    }
}