package us.brainstormz.paddieMatrick

import org.junit.Assert
import org.junit.Test
import us.brainstormz.localizer.PhoHardware

class StackAimerTest {
    @Test
    fun `foo`(){
        val telemetry = PhoHardware.PhoTelemetry()
        val testSubject = StackAimer(telemetry = telemetry, stackDetector = null)

        data class TestCase(
            val distanceFromStack:Double,
            val detectionPixelValue:Double,
            val offsetInches:Double
        )

        val testCases = listOf(
            TestCase(21.0, 133.5, -2.0),
            TestCase(21.0, 147.5, -1.0),
            TestCase(23.0, 144.8, -1.0),
//            TestCase(23.0, 140.95, -1.5),
            TestCase(23.0, 137.1, -2.0),
            TestCase(25.0, 180.5, 2.0),
            TestCase(25.0, 161.6, 0.0),

//            TestCase(24.0, 157.6, 0.0),

            TestCase(23.0, 152.6, 0.0),
            TestCase(25.0, 170.5, 1.0),
        )

        // when
        val results = testCases.map{
            testSubject.getStackInchesFromCenter(
                distanceFromStack = it.distanceFromStack,
                detectionPixelValue = it.detectionPixelValue)
        }

        fun toString(test:TestCase, result:Double):String{
            return "${test.offsetInches} ${test.distanceFromStack} = $result"
        }

        val actual = results.zip(testCases).map{(result, testCase)->
            toString(testCase, result)
        }.joinToString("\n")

        val expected = testCases.map{
            toString(it, it.offsetInches)
        }.joinToString("\n")

        Assert.assertEquals(expected, actual)
    }
}