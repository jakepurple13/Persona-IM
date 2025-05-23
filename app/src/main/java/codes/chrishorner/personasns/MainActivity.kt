package codes.chrishorner.personasns

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.collections.immutable.toImmutableList

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val transcriptState = rememberTranscriptState()
            var season by remember { mutableStateOf(Season.NONE) }

            Scaffold(
                bottomBar = {
                    MessageInput(
                        onSendMessage = { transcriptState.advance(it) },
                        enabled = true,
                        modifier = Modifier
                            .background(PersonaRed.compositeOver(Color.Black))
                            .navigationBarsPadding()
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = PersonaRed)
                ) {
                    Crossfade(season) { target ->
                        BackgroundParticles(target)
                    }

                    Image(
                        painter = painterResource(R.drawable.bg_splatter_background),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.offset(y = (-16).dp)
                    )

                    val entries = transcriptState.entries
                    Transcript(entries)

                    Row(
                        modifier = Modifier.padding(padding)
                    ) {
                        SeasonMenu(
                            hostElement = {
                                Image(
                                    painter = painterResource(R.drawable.logo_im),
                                    contentDescription = null,
                                    modifier = Modifier.height(100.dp)
                                )
                            },
                            onSeasonChange = { season = it },
                            modifier = Modifier.offset(x = 8.dp, y = (-4).dp),
                        )

                        Portraits(
                            senders = Sender.entries.minus(Sender.Ren).toImmutableList(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NextButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.90f else 1f, label = "scale")

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Image(
            painter = painterResource(R.drawable.next),
            contentDescription = "Next button",
        )
    }
}

@Composable
private fun RootContainer(content: @Composable () -> Unit) {
    /*val view = LocalView.current
    val window = (view.context as Activity).window
    SideEffect {
        window.statusBarColor = Color.Black.copy(alpha = 0.3f).toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
    }*/
    content()
}

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    var userMessage by rememberSaveable { mutableStateOf("") }

    ElevatedCard(
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomEnd = 0.dp,
            bottomStart = 0.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = PersonaRed,
            contentColor = Color.White,
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = userMessage,
            label = {
                Text(
                    "Message",
                    fontFamily = OptimaNova
                )
            },
            textStyle = LocalTextStyle.current.copy(
                fontFamily = OptimaNova
            ),
            onValueChange = { userMessage = it },
            enabled = enabled,
            shape = with(LocalDensity.current) {
                GenericShape { size, _ ->
                    lineTo(size.width, 0f)
                    lineTo(size.width - 23.dp.toPx(), size.height)
                    lineTo(15.6.dp.toPx(), size.height - 8.dp.toPx())
                    close()
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSendMessage(userMessage)
                    userMessage = ""
                    keyboard?.hide()
                }
            ),
            trailingIcon = {
                NextButton(
                    onClick = {
                        onSendMessage(userMessage)
                        userMessage = ""
                        keyboard?.hide()
                    },
                    modifier = Modifier.padding(16.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}