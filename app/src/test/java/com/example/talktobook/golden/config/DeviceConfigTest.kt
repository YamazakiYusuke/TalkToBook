package com.example.talktobook.golden.config

import org.junit.Assert.*
import org.junit.Test

class DeviceConfigTest {

    @Test
    fun `DeviceConfig should have predefined configurations`() {
        // Then
        assertNotNull(DeviceConfig.PHONE_SMALL)
        assertNotNull(DeviceConfig.PHONE_NORMAL)
        assertNotNull(DeviceConfig.PHONE_LARGE)
        assertNotNull(DeviceConfig.TABLET)
    }

    @Test
    fun `PHONE_NORMAL should have correct configuration`() {
        // Given
        val phoneNormal = DeviceConfig.PHONE_NORMAL

        // Then
        assertEquals(ScreenSize.NORMAL_PHONE, phoneNormal.screenSize)
        assertEquals(Density.XHDPI, phoneNormal.density)
        assertEquals(Orientation.PORTRAIT, phoneNormal.orientation)
        assertEquals(SystemUiMode.NORMAL, phoneNormal.systemUiMode)
    }

    @Test
    fun `TABLET should have correct configuration`() {
        // Given
        val tablet = DeviceConfig.TABLET

        // Then
        assertEquals(ScreenSize.TABLET, tablet.screenSize)
        assertEquals(Density.XHDPI, tablet.density)
        assertEquals(Orientation.LANDSCAPE, tablet.orientation)
        assertEquals(SystemUiMode.NORMAL, tablet.systemUiMode)
    }

    @Test
    fun `custom DeviceConfig should be creatable`() {
        // Given
        val customConfig = DeviceConfig(
            screenSize = ScreenSize.LARGE_PHONE,
            density = Density.XXHDPI,
            orientation = Orientation.LANDSCAPE,
            systemUiMode = SystemUiMode.DARK
        )

        // Then
        assertEquals(ScreenSize.LARGE_PHONE, customConfig.screenSize)
        assertEquals(Density.XXHDPI, customConfig.density)
        assertEquals(Orientation.LANDSCAPE, customConfig.orientation)
        assertEquals(SystemUiMode.DARK, customConfig.systemUiMode)
    }
}