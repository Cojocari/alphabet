package com.cojocari.alphabet.ui.components

import androidx.compose.runtime.snapshotFlow
import com.cojocari.alphabet.audio.Speaker
import com.cojocari.alphabet.data.Progress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Before a game moves on (next round, end of a demo): waits at least [minMs] so the celebration
 * can be seen, then until the voice has finished (praise, or counting "one, two, … ten!"), then
 * until a new sticker's reward is over, then [pauseMs] more. A fixed delay cut long sentences off.
 */
suspend fun Speaker.waitUntilSaid(progress: Progress, minMs: Long = 2000, pauseMs: Long = 500) {
    delay(minMs)
    awaitQuiet(15_000)
    awaitSticker(progress)
    delay(pauseMs)
}

/** Waits while a new sticker is being shown (and said), so nothing talks over it. */
suspend fun Speaker.awaitSticker(progress: Progress) {
    if (progress.newSticker == null) return
    withTimeoutOrNull(20_000) { snapshotFlow { progress.newSticker }.first { it == null } }
    awaitQuiet(8_000)
}
