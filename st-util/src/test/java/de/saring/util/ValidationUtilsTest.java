package de.saring.util;

import de.saring.util.unitcalc.SpeedToStringConverter;
import de.saring.util.unitcalc.FormatUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class contains all unit tests for the ValidationUtilsTest class.
 *
 * @author Stefan Saring
 */
public class ValidationUtilsTest {

    private static Locale defaultLocale;

    /**
     * Set default locale to GERMAN, because the number validation tests are locale dependent.
     */
    @BeforeAll
    public static void initLocale() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
    }

    /**
     * Reset default locale to previous value.
     */
    @AfterAll
    public static void resetLocale() {
        Locale.setDefault(defaultLocale);
    }

    /**
     * Tests the method isValueIntegerBetween() for success conditions.
     */
    @Test
    public void testIsValueIntegerBetweenSuccess() {
        assertTrue(ValidationUtils.isValueIntegerBetween("0", 0, 23));
        assertTrue(ValidationUtils.isValueIntegerBetween("10", 0, 23));
        assertTrue(ValidationUtils.isValueIntegerBetween("23", 0, 23));
    }

    /**
     * Tests the method isValueIntegerBetween() for failure conditions.
     */
    @Test
    public void testIsValueIntegerBetweenFailed() {
        assertFalse(ValidationUtils.isValueIntegerBetween(null, 0, 23));
        assertFalse(ValidationUtils.isValueIntegerBetween("", 0, 23));
        assertFalse(ValidationUtils.isValueIntegerBetween("foo", 0, 23));
        assertFalse(ValidationUtils.isValueIntegerBetween("-1", 0, 23));
        assertFalse(ValidationUtils.isValueIntegerBetween("24", 0, 23));
    }

    /**
     * Tests the method isOptionalValueIntegerBetween() for success conditions.
     */
    @Test
    public void testIsOptionalValueIntegerBetweenSuccess() {
        assertTrue(ValidationUtils.isOptionalValueIntegerBetween(null, 0, 23));
        assertTrue(ValidationUtils.isOptionalValueIntegerBetween("  ", 0, 23));
        assertTrue(ValidationUtils.isOptionalValueIntegerBetween("0", 0, 23));
        assertTrue(ValidationUtils.isOptionalValueIntegerBetween("10", 0, 23));
        assertTrue(ValidationUtils.isOptionalValueIntegerBetween("23", 0, 23));
    }

    /**
     * Tests the method isOptionalValueIntegerBetween() for failure conditions.
     */
    @Test
    public void testIsOptionalValueIntegerBetweenFailed() {
        assertFalse(ValidationUtils.isOptionalValueIntegerBetween("foo", 0, 23));
        assertFalse(ValidationUtils.isOptionalValueIntegerBetween("-1", 0, 23));
        assertFalse(ValidationUtils.isOptionalValueIntegerBetween("24", 0, 23));
    }

    /**
     * Tests the method isValueDoubleBetween() for success conditions.
     */
    @Test
    public void testIsValueDoubleBetween() {
        assertTrue(ValidationUtils.isValueDoubleBetween("0", 0, 100));
        assertTrue(ValidationUtils.isValueDoubleBetween("100", 0, 100));
        assertTrue(ValidationUtils.isValueDoubleBetween("0,1", 0, 100));
        assertTrue(ValidationUtils.isValueDoubleBetween("99,9", 0, 100));
        assertTrue(ValidationUtils.isValueDoubleBetween("-1,0", -2, 0));
        assertTrue(ValidationUtils.isValueDoubleBetween("100,3", 0, 100.3));
    }

    /**
     * Tests the method isValueDoubleBetween() for failure conditions.
     */
    @Test
    public void testIsValueDoubleBetweenFailed() {
        assertFalse(ValidationUtils.isValueDoubleBetween(null, 0, 100));
        assertFalse(ValidationUtils.isValueDoubleBetween("", 0, 100));
        assertFalse(ValidationUtils.isValueDoubleBetween("foo", 0, 100));
        assertFalse(ValidationUtils.isValueDoubleBetween("-1", 0, 100));
        assertFalse(ValidationUtils.isValueDoubleBetween("101", 0, 100));
        assertFalse(ValidationUtils.isValueDoubleBetween("-0,1", 0, 100));
        assertFalse(ValidationUtils.isValueDoubleBetween("100,1", 0, 100));
        assertFalse(ValidationUtils.isValueDoubleBetween("100.0", 0, 100)); // 100.0 is evaluated to 1000
        assertFalse(ValidationUtils.isValueDoubleBetween("100,11", 0, 100.1));
    }

    /**
     * Tests the method isValueTimeInSecondsBetween() for success conditions.
     */
    @Test
    public void testIsValueTimeInSecondsBetweenSuccess() {
        assertTrue(ValidationUtils.isValueTimeInSecondsBetween("0", 0, 3600));
        assertTrue(ValidationUtils.isValueTimeInSecondsBetween("59:0", 0, 3600));
        assertTrue(ValidationUtils.isValueTimeInSecondsBetween("59:59", 0, 3600));
        assertTrue(ValidationUtils.isValueTimeInSecondsBetween("0000:59:59", 0, 3600));
        assertTrue(ValidationUtils.isValueTimeInSecondsBetween("1:0:0", 0, 3600));
    }

    /**
     * Tests the method isValueTimeInSecondsBetween() for failure conditions.
     */
    @Test
    public void testIsValueTimeInSecondsBetweenFailed() {
        assertFalse(ValidationUtils.isValueTimeInSecondsBetween(null, 0, 3600));
        assertFalse(ValidationUtils.isValueTimeInSecondsBetween("", 0, 3600));
        assertFalse(ValidationUtils.isValueTimeInSecondsBetween("foo", 0, 3600));
        assertFalse(ValidationUtils.isValueTimeInSecondsBetween("0:59", 60, 3600));
        assertFalse(ValidationUtils.isValueTimeInSecondsBetween("0:0:59", 60, 3600));
        assertFalse(ValidationUtils.isValueTimeInSecondsBetween("1:00:01", 60, 3600));

        assertFalse(ValidationUtils.isValueTimeInSecondsBetween("0:10:10a", 0, 3600));
        assertFalse(ValidationUtils.isValueTimeInSecondsBetween("0a:10:10", 0, 3600));
        assertFalse(ValidationUtils.isValueTimeInSecondsBetween("0:1a:10", 0, 3600));
        assertFalse(ValidationUtils.isValueTimeInSecondsBetween("0::10", 0, 3600));
    }

    /**
     * Tests the method isValueSpeed() for success conditions.
     */
    @Test
    public void testIsValueSpeedSuccess() {

        SpeedToStringConverter speedConverterSpeed = new SpeedToStringConverter(FormatUtils.SpeedMode.SPEED);
        assertTrue(ValidationUtils.isValueSpeed("0", speedConverterSpeed, false));
        assertTrue(ValidationUtils.isValueSpeed("12", speedConverterSpeed, true));
        assertTrue(ValidationUtils.isValueSpeed("12.234", speedConverterSpeed, true));

        SpeedToStringConverter speedConverterPace = new SpeedToStringConverter(FormatUtils.SpeedMode.PACE);
        assertTrue(ValidationUtils.isValueSpeed("00:00", speedConverterPace, false));
        assertTrue(ValidationUtils.isValueSpeed("00:01", speedConverterPace, true));
        assertTrue(ValidationUtils.isValueSpeed("05:30", speedConverterPace, true));
        assertTrue(ValidationUtils.isValueSpeed("05", speedConverterPace, true));
    }

    /**
     * Tests the method isValueSpeed() for failure conditions.
     */
    @Test
    public void testIsValueSpeedFailed() {

        SpeedToStringConverter speedConverterSpeed = new SpeedToStringConverter(FormatUtils.SpeedMode.SPEED);
        assertFalse(ValidationUtils.isValueSpeed("12", speedConverterSpeed, false));
        assertFalse(ValidationUtils.isValueSpeed("0", speedConverterSpeed, true));
        assertFalse(ValidationUtils.isValueSpeed(null, speedConverterSpeed, true));
        assertFalse(ValidationUtils.isValueSpeed("", speedConverterSpeed, true));
        assertFalse(ValidationUtils.isValueSpeed("-1", speedConverterSpeed, true));

        SpeedToStringConverter speedConverterPace = new SpeedToStringConverter(FormatUtils.SpeedMode.PACE);
        assertFalse(ValidationUtils.isValueSpeed("05:30", speedConverterPace, false));
        assertFalse(ValidationUtils.isValueSpeed("00:00", speedConverterPace, true));
        assertFalse(ValidationUtils.isValueSpeed("5", speedConverterPace, true));
        assertFalse(ValidationUtils.isValueSpeed("-05:30", speedConverterPace, true));
        assertFalse(ValidationUtils.isValueSpeed("05 30", speedConverterPace, true));
    }
}
