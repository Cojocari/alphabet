package com.cojocari.alphabet.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.bouncyClick

/**
 * [n] things to count, five per row. Tapping one gives it the next number and says it
 * out loud ("one", "two"…), which is how children learn to count objects one by one.
 */
@Composable
fun CountingField(thing: String, n: Int) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val t = LocalLanguage.current.strings
    val counted = remember { mutableStateMapOf<Int, Int>() } // index -> its count number

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        (0 until n).chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { i ->
                    val number = counted[i]
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(if (number != null) Color(0xFFFFF1C1) else Color.White)
                            .bouncyClick {
                                if (number == null) {
                                    val next = counted.size + 1
                                    counted[i] = next
                                    sfx.tap()
                                    speaker.say(t.num(next))
                                } else {
                                    speaker.say(t.num(number))
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(thing, fontSize = 50.sp)
                        if (number != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF77F00)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("$number", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(0.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
