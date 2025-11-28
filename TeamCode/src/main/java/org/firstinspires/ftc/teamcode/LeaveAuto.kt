package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.Constants.Companion.TIMEOUT_MS
import org.firstinspires.ftc.teamcode.Constants.Companion.WHEELS_INCHES_TO_TICKS
import kotlin.math.abs

@Autonomous
class LeaveAuto : LinearOpMode() {
    private var frontLeft: DcMotor? = null
    private var frontRight: DcMotor? = null
    private var backLeft: DcMotor? = null
    private var backRight: DcMotor? = null
    private val leftDistanceInch = 10
    private val rightDistanceInch = 10
    private val speed = 0.5
    private val timeout = ElapsedTime()
    override fun runOpMode() {
        frontLeft = hardwareMap.get(DcMotor::class.java, "FL")
        frontRight = hardwareMap.get(DcMotor::class.java, "FR")
        backLeft = hardwareMap.get(DcMotor::class.java, "BL")
        backRight = hardwareMap.get(DcMotor::class.java, "BR")
        frontLeft!!.mode = DcMotor.RunMode.RUN_TO_POSITION
        frontLeft!!.direction = DcMotorSimple.Direction.FORWARD
        frontRight!!.mode = DcMotor.RunMode.RUN_TO_POSITION
        frontRight!!.direction = DcMotorSimple.Direction.REVERSE
        backLeft!!.mode = DcMotor.RunMode.RUN_TO_POSITION
        backLeft!!.direction = DcMotorSimple.Direction.FORWARD
        backRight!!.mode = DcMotor.RunMode.RUN_TO_POSITION
        backRight!!.direction = DcMotorSimple.Direction.REVERSE
        frontLeft!!.targetPosition =
            (frontLeft!!.currentPosition + leftDistanceInch * WHEELS_INCHES_TO_TICKS).toInt()
        frontRight!!.targetPosition =
            (frontRight!!.currentPosition + rightDistanceInch * WHEELS_INCHES_TO_TICKS).toInt()
        backLeft!!.targetPosition =
            (backLeft!!.currentPosition + leftDistanceInch * WHEELS_INCHES_TO_TICKS).toInt()
        backRight!!.targetPosition =
            (backRight!!.currentPosition + rightDistanceInch * WHEELS_INCHES_TO_TICKS).toInt()

        waitForStart()
        if (opModeIsActive()) {
            timeout.reset()


            frontLeft!!.power = speed
            frontRight!!.power = speed
            backLeft!!.power = speed
            backRight!!.power = speed
            while (opModeIsActive() && (frontLeft!!.isBusy || frontRight!!.isBusy || backLeft!!.isBusy || backRight!!.isBusy) && timeout.milliseconds() < TIMEOUT_MS) {
                idle()
            }
            frontLeft!!.power = 0.0
            frontRight!!.power = 0.0
            backLeft!!.power = 0.0
            backRight!!.power = 0.0
            frontLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            frontRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            backLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            backRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }
}