package com.tuapp.calculadora

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.SoundEffectConstants
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RawRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.tuapp.calculadora.ui.theme.CalculadoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Reproductor de video de fondo responsivo con soporte para silenciar/activar volumen dinámicamente.
 */
@Composable
fun BackgroundVideoPlayer(
    @RawRes videoResId: Int,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
    defaultVolume: Float = 0.15f
) {
    val isPreview = LocalInspectionMode.current
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    val currentVolume = if (isMuted) 0f else defaultVolume

    // Actualiza el volumen del video en tiempo real cuando se presiona el botón de silenciar
    LaunchedEffect(isMuted) {
        mediaPlayerRef?.setVolume(currentVolume, currentVolume)
    }

    if (isPreview) {
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3A0D56),
                        Color(0xFF13092D),
                        Color(0xFF080214)
                    )
                )
            )
        )
    } else {
        AndroidView(
            factory = { ctx ->
                object : VideoView(ctx) {
                    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                        val width = getDefaultSize(0, widthMeasureSpec)
                        val height = getDefaultSize(0, heightMeasureSpec)
                        setMeasuredDimension(width, height)
                    }
                }.apply {
                    val videoUri = Uri.parse("android.resource://${ctx.packageName}/$videoResId")
                    setVideoURI(videoUri)
                    setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        mp.isLooping = true
                        mp.setVolume(currentVolume, currentVolume)
                        start()
                    }
                }
            },
            update = {
                mediaPlayerRef?.setVolume(currentVolume, currentVolume)
            },
            modifier = modifier
        )
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val calculator = remember { CalculatorLogic() }
    var displayText by remember { mutableStateOf("0") }
    var expressionText by remember { mutableStateOf("") }

    // Estados para silenciar el video de fondo o los efectos de sonido de botones
    var isBgMuted by remember { mutableStateOf(false) }
    var isSfxMuted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val view = LocalView.current

    // Reproduce sonido al tocar un botón si no está silenciado
    fun playSound(symbol: String) {
        if (isSfxMuted) return

        view.playSoundEffect(SoundEffectConstants.CLICK)

        val soundRes = when (symbol) {
            "=" -> R.raw.yamade_kudasai
            "C" -> R.raw.cat
            "DEL" -> R.raw.mikudayo_made_with_voicemod
            "%" -> R.raw.miracle333_ara_ara_sound_effect_127279
            "-", "÷" -> R.raw.onin_chan
            "×" -> R.raw.uwu
            "+" -> R.raw.baka
            else -> R.raw.mambo
        }

        try {
            val mediaPlayer = MediaPlayer.create(context, soundRes)
            mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = maxHeight

        // Cálculos dinámicos de responsividad según el alto de la pantalla del teléfono
        val buttonHeight: Dp = (screenHeight * 0.082f).coerceIn(48.dp, 76.dp)
        val buttonFontSize: TextUnit = if (screenHeight < 600.dp) 18.sp else 22.sp
        val displayFontSize: TextUnit = if (screenHeight < 600.dp) 32.sp else 44.sp
        val rowPadding: Dp = if (screenHeight < 600.dp) 3.dp else 5.dp

        // 1. Video de fondo responsivo con volumen dinámico
        BackgroundVideoPlayer(
            videoResId = R.raw.f,
            modifier = Modifier.fillMaxSize(),
            isMuted = isBgMuted,
            defaultVolume = 0.15f
        )

        // 2. Capa de tinte oscuro para mejor contraste
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )

        // 3. Contenido de la Calculadora
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Barra superior con botones de control para silenciar audio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para silenciar/activar música/video de fondo
                IconButton(
                    onClick = { isBgMuted = !isBgMuted },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (isBgMuted) Color(0x66FF5252) else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_musica_fondo),
                        contentDescription = "Música de fondo",
                        tint = if (isBgMuted) Color(0xFFFF8A80) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón para silenciar/activar efectos de sonido de botones
                IconButton(
                    onClick = { isSfxMuted = !isSfxMuted },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (isSfxMuted) Color(0x66FF5252) else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_tono_svg),
                        contentDescription = "Tono de botones",
                        tint = if (isSfxMuted) Color(0xFFFF8A80) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Pantalla de la calculadora con mayor transparencia (Efecto Cristal)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(
                        color = Color(0x3312121A), // Mayor transparencia (20% de opacidad)
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.30f)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    // Historial / Expresión en curso
                    Text(
                        text = expressionText,
                        fontSize = (displayFontSize.value * 0.48f).sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.70f),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Número actual / Resultado
                    Text(
                        text = displayText,
                        fontSize = displayFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }

            // Cuadrícula de Botones Responsivos
            val buttons = listOf(
                listOf("C", "DEL", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            for (row in buttons) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = rowPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (symbol in row) {
                        val weight = if (symbol == "=") 2f else 1f

                        CalculatorButton(
                            symbol = symbol,
                            buttonHeight = buttonHeight,
                            fontSize = buttonFontSize,
                            modifier = Modifier.weight(weight)
                        ) {
                            playSound(symbol)

                            displayText = when (symbol) {
                                "C" -> calculator.onClearClick()
                                "DEL" -> calculator.onDeleteClick()
                                "+", "-", "×", "÷", "%" -> calculator.onOperatorClick(symbol)
                                "=" -> calculator.onEqualsClick()
                                else -> calculator.onNumberClick(symbol)
                            }
                            expressionText = calculator.expression
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    buttonHeight: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isEquals = symbol == "="
    val isOperator = symbol in listOf("÷", "×", "-", "+")
    val isControl = symbol in listOf("C", "DEL", "%")

    val backgroundColor = when {
        isEquals -> Color(0x40FFFFFF)
        isControl || isOperator -> Color(0x33FFFFFF)
        else -> Color(0x22FFFFFF)
    }

    val borderColor = when {
        isEquals -> Color.White.copy(alpha = 0.85f)
        isOperator -> Color.White.copy(alpha = 0.45f)
        else -> Color.White.copy(alpha = 0.30f)
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(buttonHeight),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, borderColor),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        )
    ) {
        Text(
            text = symbol,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    CalculadoraTheme {
        CalculatorScreen()
    }
}