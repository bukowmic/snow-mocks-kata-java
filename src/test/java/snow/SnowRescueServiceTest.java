package snow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import snow.dependencies.MunicipalServices;
import snow.dependencies.PressService;
import snow.dependencies.SnowplowMalfunctioningException;
import snow.dependencies.WeatherForecastService;

import static org.mockito.Mockito.*;

public class SnowRescueServiceTest {
    private WeatherForecastService weatherForecastService;
    private MunicipalServices municipalService;
    private PressService pressService;
    private SnowRescueService snowRescueService;

    @BeforeEach
    void setUp() {
        weatherForecastService = mock(WeatherForecastService.class);
        municipalService = mock(MunicipalServices.class);
        pressService = mock(PressService.class);
        snowRescueService = new SnowRescueService(weatherForecastService, municipalService, pressService);
    }

    @Test
    void send_sander_when_temperature_below_0() {
        when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(-1);

        snowRescueService.checkForecastAndRescue();

        verify(municipalService, times(1)).sendSander();
    }

    @Test
    void do_not_send_sander_when_temperature_equal_or_above_0() {
        when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(0);

        snowRescueService.checkForecastAndRescue();

        verify(municipalService, never()).sendSander();
    }

    @Test
    void send_snowplow_when_snowfall_is_greater_then_3mm() {
        when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(4);

        snowRescueService.checkForecastAndRescue();

        verify(municipalService, times(1)).sendSnowplow();
    }

    @Test
    void do_not_send_snowplow_when_snowfall_is_equal_or_lower_then_3mm() {
        when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(3);

        snowRescueService.checkForecastAndRescue();

        verify(municipalService, never()).sendSnowplow();
    }

    @Test
    void if_snowplow_crashes_send_another() {
        when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(4);
        doThrow(SnowplowMalfunctioningException.class).doNothing().when(municipalService).sendSnowplow();

        snowRescueService.checkForecastAndRescue();

        verify(municipalService, times(2)).sendSnowplow();
    }

    @Test
    void send_two_snowplow_when_snowfall_is_greater_then_5mm() {
        when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(6);

        snowRescueService.checkForecastAndRescue();

        verify(municipalService, times(2)).sendSnowplow();
    }

    @Test
    void when_temp_below_minus_10_and_snowfall_greater_10mm_send_3_snowplow_sander_and_call_press() {
        when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(-11);
        when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(11);

        snowRescueService.checkForecastAndRescue();

        verify(municipalService, times(3)).sendSnowplow();
        verify(municipalService, times(1)).sendSander();
        verify(pressService, times(1)).sendWeatherAlert();
    }
}
