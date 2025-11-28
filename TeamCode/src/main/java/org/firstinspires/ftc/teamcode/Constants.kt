package org.firstinspires.ftc.teamcode

class Constants {
    companion object {
        const val BANK_VELOCITY = 1300
        const val FAR_VELOCITY = 2000
        const val MAX_VELOCITY = 2200
        const val TELEOP = "TELEOP"
        const val AUTO_BLUE = "AUTO BLUE"
        const val AUTO_RED = " AUTO RED"
        const val WHEELS_INCHES_TO_TICKS = (28 * 30.21) / (3 * Math.PI)

        // Δd (in.) = (ticks/rev) * (total gear reduction) / (d (in.) * π)
        const val TIMEOUT_MS = 5000
    }
}